package com.example.capstone.chat;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.capstone.data.repository.ChatRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.QuerySnapshot;

public class ChatListViewModel extends ViewModel {

    private final ChatRepository chatRepository = ChatRepository.getInstance();

    public LiveData<QuerySnapshot> chatListLiveData =
            chatRepository.getChatListSnapshotLiveData(FirebaseAuth.getInstance().getCurrentUser());
}
