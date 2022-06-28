package com.example.capstone.posting;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.capstone.data.repository.PostingRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

public class PostingListViewModel extends ViewModel {

    private final PostingRepository repository = PostingRepository.getInstance();

    public LiveData<QuerySnapshot> postingListLiveData =
            repository.getPostingListSnapshotLiveData(FirebaseAuth.getInstance().getCurrentUser());

    public void removePostingList(List<String> postingIdList) {
        repository.removePostingList(postingIdList);
    }
}
