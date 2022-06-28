package com.example.capstone.main;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.capstone.data.repository.PostingRepository;
import com.google.firebase.firestore.QuerySnapshot;

public class MainViewModel extends ViewModel {

    public LiveData<QuerySnapshot> postingSnapshotLiveData =
            PostingRepository.getInstance().getPostingListSnapshotLiveData();
}

