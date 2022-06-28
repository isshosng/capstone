package com.example.capstone.chat;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.capstone.data.model.ChatMessage;
import com.example.capstone.data.model.ChatRoom;
import com.example.capstone.data.repository.ChatRepository;
import com.example.capstone.data.repository.PostingRepository;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class ChatViewModel extends ViewModel {

    private final ChatRepository chatRepository = ChatRepository.getInstance();
    private final PostingRepository postingRepository = PostingRepository.getInstance();

    private final String chatRoomId;
    public final LiveData<DocumentSnapshot> roomLiveData;
    public final LiveData<QuerySnapshot> messageLiveData;
    public final LiveData<DocumentSnapshot> postingLiveData;

    public ChatViewModel(String chatRoomId, String postingId) {
        this.chatRoomId = chatRoomId;

        roomLiveData = chatRepository.getRoomLiveData(chatRoomId);
        messageLiveData = chatRepository.getMessageLiveData(chatRoomId);
        postingLiveData = postingRepository.getPostingSnapshotLiveData(postingId);
    }

    public void sendMessage(ChatMessage message) {
        chatRepository.sendMessage(chatRoomId, message);
    }

    public Task<Void> getRoomCreationTask(ChatRoom room, FirebaseUser user) {
        return chatRepository.getRoomCreationTask(room, user);
    }

    public void matching(ChatRoom room) {
        chatRepository.matching(room);
    }

    public void leave() {
        chatRepository.leave(chatRoomId);
    }
}
