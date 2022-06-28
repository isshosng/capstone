package com.example.capstone.data.repository;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;

import timber.log.Timber;


public class DocumentSnapshotLiveData extends LiveData<DocumentSnapshot> implements EventListener<DocumentSnapshot> {

    private final DocumentReference reference;
    private ListenerRegistration registration = null;

    public DocumentSnapshotLiveData(DocumentReference reference) {
        this.reference = reference;
    }

    @Override
    protected void onActive() {
        super.onActive();

        Timber.d("onActive");

        if (registration == null) {
            registration = reference.addSnapshotListener(this);
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
    public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
        if (error != null) {
            error.printStackTrace();

        } else {
            setValue(value);
        }
    }
}
