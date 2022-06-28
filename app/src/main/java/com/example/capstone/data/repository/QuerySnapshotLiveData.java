package com.example.capstone.data.repository;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import timber.log.Timber;


public class QuerySnapshotLiveData extends LiveData<QuerySnapshot> implements EventListener<QuerySnapshot> {

    private final Query query;
    private ListenerRegistration registration = null;

    public QuerySnapshotLiveData(Query query) {
        this.query = query;
    }

    @Override
    protected void onActive() {
        super.onActive();

        Timber.d("onActive");

        if (registration == null) {
            registration = query.addSnapshotListener(this);
        }
    }

    @Override
    protected void onInactive() {
        super.onInactive();

        Timber.d("onInactive");

        if (registration != null) {
            registration.remove();
            registration = null;
        }
    }

    @Override
    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
        if (error != null) {
            error.printStackTrace();

        } else {
            setValue(value);
        }
    }
}
