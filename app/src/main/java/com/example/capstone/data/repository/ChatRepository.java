package com.example.capstone.data.repository;

import androidx.lifecycle.LiveData;

import com.example.capstone.data.model.ChatMessage;
import com.example.capstone.data.model.ChatRoom;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

public class ChatRepository {
    private static ChatRepository _instance;

    public static synchronized ChatRepository getInstance() {
        if (_instance == null) {
            _instance = new ChatRepository();
        }

        return _instance;
    }


    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private ChatRepository() {
    }

    public Task<Void> getRoomCreationTask(ChatRoom room, FirebaseUser user) {
        if (room.getPostingOwner().equals(user.getUid())) {
            return Tasks.forResult(null);
        }

        return db.runTransaction(transaction -> {
            DocumentReference chatRoomReference = db.collection("chat")
                    .document(room.getId());

            DocumentSnapshot snapshot = transaction.get(chatRoomReference);

            if (!snapshot.exists()) {
                chatRoomReference.set(room);
            }

            return null;
        });
    }

    public LiveData<DocumentSnapshot> getRoomLiveData(String chatRoomId) {
        DocumentReference reference = db.collection("chat")
                .document(chatRoomId);

        return new DocumentSnapshotLiveData(reference);
    }

    public LiveData<QuerySnapshot> getMessageLiveData(String chatRoomId) {
        Query query = db.collection("chat")
                .document(chatRoomId)
                .collection("message")
                .orderBy("timestamp");

        return new QuerySnapshotLiveData(query);
    }

    public void sendMessage(final String chatRoomId, final ChatMessage message) {
        db.runBatch(batch -> {
            batch.set(db.collection("chat")
                            .document(chatRoomId)
                            .collection("message")
                            .document(),
                    message);

            batch.update(db.collection("chat")
                            .document(chatRoomId),
                    "latestMessage", message.getMessage(),
                    "latestMessageTimestamp", FieldValue.serverTimestamp());
        });
    }

    public void matching(final ChatRoom chatRoom) {
        String target = null;
        for (String participant : chatRoom.getParticipants()) {
            if (!participant.equals(chatRoom.getPostingOwner())) {
                target = participant;
                break;
            }
        }

        assert target != null;

        db.collection("posting").document(chatRoom.getPostingId())
                .update("matchingTarget", target);
    }

    public void leave(final String chatRoomId) {
        DocumentReference reference = db.collection("chat").document(chatRoomId);

        reference.collection("message").get()
                .addOnCompleteListener(task -> {
                    QuerySnapshot snapshot = task.getResult();
                    if (snapshot != null) {
                        db.runBatch(batch -> {
                            for (DocumentSnapshot documentSnapshot : snapshot.getDocuments()) {
                                batch.delete(documentSnapshot.getReference());
                            }
                        });
                    }
                });

        reference.delete();
    }

    public LiveData<QuerySnapshot> getChatListSnapshotLiveData(FirebaseUser user) {
        Query query = db.collection("chat")
                .whereArrayContains("participants", user.getUid())
                .orderBy("timestamp", Query.Direction.DESCENDING);

        return new QuerySnapshotLiveData(query);
    }
}
