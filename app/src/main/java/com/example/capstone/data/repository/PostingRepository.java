package com.example.capstone.data.repository;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.example.capstone.data.model.Posting;
import com.firebase.geofire.GeoFireUtils;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQueryBounds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class PostingRepository {
    private static PostingRepository _instance;

    public static synchronized PostingRepository getInstance() {
        if (_instance == null) {
            _instance = new PostingRepository();
        }

        return _instance;
    }


    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private PostingRepository() {
    }

    public Task<Void> posting(Posting posting) {
        return db.collection("posting").document().set(posting);
    }

    public List<Task<QuerySnapshot>> getPostingList(double latitude, double longitude, double radius) {
        // Find cities within 50km of London
        final GeoLocation center = new GeoLocation(latitude, longitude);
        final double radiusInM = radius;

        // Each item in 'bounds' represents a startAt/endAt pair. We have to issue
        // a separate query for each pair. There can be up to 9 pairs of bounds
        // depending on overlap, but in most cases there are 4.
        List<GeoQueryBounds> bounds = GeoFireUtils.getGeoHashQueryBounds(center, radiusInM);
        final List<Task<QuerySnapshot>> tasks = new ArrayList<>();
        for (GeoQueryBounds b : bounds) {
            Query q = db.collection("posting")
                    .orderBy("geoHash")
                    .startAt(b.startHash)
                    .endAt(b.endHash);

            tasks.add(q.get());
        }

        // Collect all the query results together into a single list
        Tasks.whenAllComplete(tasks)
                .addOnCompleteListener(new OnCompleteListener<List<Task<?>>>() {
                    @Override
                    public void onComplete(@NonNull Task<List<Task<?>>> t) {
                        List<DocumentSnapshot> matchingDocs = new ArrayList<>();

                        for (Task<QuerySnapshot> task : tasks) {
                            QuerySnapshot snap = task.getResult();
                            for (DocumentSnapshot doc : snap.getDocuments()) {
                                double lat = doc.getDouble("latitude");
                                double lng = doc.getDouble("longitude");

                                // We have to filter out a few false positives due to GeoHash
                                // accuracy, but most will match
                                GeoLocation docLocation = new GeoLocation(lat, lng);
                                double distanceInM = GeoFireUtils.getDistanceBetween(docLocation, center);
                                if (distanceInM <= radiusInM) {
                                    matchingDocs.add(doc);
                                }
                            }
                        }
                    }
                });

        return tasks;
    }

    public LiveData<QuerySnapshot> getPostingListSnapshotLiveData() {
        Query query = db.collection("posting")
                .whereEqualTo("matchingTarget", null);

        return new QuerySnapshotLiveData(query);
    }

    public LiveData<DocumentSnapshot> getPostingSnapshotLiveData(String postingId) {
        DocumentReference reference = db.collection("posting").document(postingId);

        return new DocumentSnapshotLiveData(reference);
    }

    public LiveData<QuerySnapshot> getPostingListSnapshotLiveData(FirebaseUser user) {
        Query query = db.collection("posting")
                .whereEqualTo("uid", user.getUid())
                .orderBy("timestamp", Query.Direction.DESCENDING);

        return new QuerySnapshotLiveData(query);
    }

    public void removePostingList(List<String> postingIdList) {
        db.runBatch(batch -> {
            for (String id : postingIdList) {
                batch.delete(db.collection("posting").document(id));
            }
        });
    }
}
