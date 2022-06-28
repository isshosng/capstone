package com.example.capstone.posting;

import androidx.lifecycle.ViewModel;

import com.example.capstone.data.model.Posting;
import com.example.capstone.data.repository.PostingRepository;

public class PostingViewModel extends ViewModel {

    private final PostingRepository repository = PostingRepository.getInstance();


    public void posting(Posting posting) {
        repository.posting(posting);
    }
}
