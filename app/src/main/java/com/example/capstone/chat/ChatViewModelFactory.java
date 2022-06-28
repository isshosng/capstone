package com.example.capstone.chat;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.capstone.data.model.ChatRoom;

public class ChatViewModelFactory implements ViewModelProvider.Factory {

    private final ChatRoom room;


    public ChatViewModelFactory(ChatRoom room) {
        this.room = room;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new ChatViewModel(room.getId(), room.getPostingId());
    }
}
