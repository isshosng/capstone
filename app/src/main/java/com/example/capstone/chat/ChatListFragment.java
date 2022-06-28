package com.example.capstone.chat;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Consumer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.capstone.BaseFragment;
import com.example.capstone.data.model.ChatRoom;
import com.example.capstone.databinding.FragmentChatListBinding;
import com.example.capstone.databinding.ItemChatListBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;

import org.ocpsoft.prettytime.PrettyTime;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map;


public class ChatListFragment extends BaseFragment {

    public static final String TAG = "ChatListFragment";

    private FragmentChatListBinding binding;
    private ChatListViewModel viewModel;
    private final ChatListAdapter adapter = new ChatListAdapter();


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentChatListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(ChatListViewModel.class);

        adapter.setOnClickListener(room ->
                startFragment(ChatFragment.newInstance(room), ChatFragment.TAG, null));

        binding.recyclerView.setAdapter(adapter);
        binding.recyclerView.addItemDecoration(
                new DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL));

        viewModel.chatListLiveData.observe(getViewLifecycleOwner(), snapshot -> {
            binding.progressBar.setVisibility(View.GONE);

            ArrayList<ChatRoom> dataSet = new ArrayList<>();

            for (DocumentSnapshot doc : snapshot.getDocuments()) {
                dataSet.add(doc.toObject(ChatRoom.class));
            }

            adapter.submitList(dataSet);
            binding.emptyView.setVisibility(dataSet.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }


    private static class ChatListAdapter extends ListAdapter<ChatRoom, ChatListAdapter.ChatListItemViewHolder> {

        private final FirebaseUser user;
        private Consumer<ChatRoom> onClickListener;

        protected ChatListAdapter() {
            super(new DiffUtil.ItemCallback<ChatRoom>() {
                @Override
                public boolean areItemsTheSame(@NonNull ChatRoom oldItem, @NonNull ChatRoom newItem) {
                    return oldItem.getId().equals(newItem.getId());
                }

                @Override
                public boolean areContentsTheSame(@NonNull ChatRoom oldItem, @NonNull ChatRoom newItem) {
                    Date oldItemTimestamp = oldItem.getTimestamp();
                    Date newItemTimestamp = newItem.getTimestamp();

                    if (oldItemTimestamp == null && newItemTimestamp != null) {
                        return false;
                    }

                    if (oldItemTimestamp != null && newItemTimestamp == null) {
                        return false;
                    }

                    if (oldItemTimestamp != null && newItemTimestamp != null) {
                        if (oldItemTimestamp.getTime() != newItemTimestamp.getTime()) {
                            return false;
                        }
                    }

                    return oldItem.getLatestMessage().equals(newItem.getLatestMessage());
                }
            });

            user = FirebaseAuth.getInstance().getCurrentUser();
            assert user != null;
        }

        @NonNull
        @Override
        public ChatListItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            return new ChatListItemViewHolder(ItemChatListBinding.inflate(inflater, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ChatListItemViewHolder holder, int position) {
            ChatRoom room = getItem(position);

            for (Map.Entry<String, String> entry : room.getNames().entrySet()) {
                if (!entry.getKey().equals(user.getUid())) {
                    holder.binding.nickNameTextView.setText(entry.getValue());
                    break;
                }
            }

            holder.binding.timestampTextView.setText(new PrettyTime().format(room.getTimestamp()));
            holder.binding.foodClassificationNameTextView.setText(
                    room.getFoodClassification() + " (" + room.getRestaurantName() + ")");

            holder.binding.getRoot().setOnClickListener(v -> {
                if (onClickListener != null) {
                    onClickListener.accept(room);
                }
            });
        }

        public void setOnClickListener(Consumer<ChatRoom> onClickListener) {
            this.onClickListener = onClickListener;
        }

        static class ChatListItemViewHolder extends RecyclerView.ViewHolder {

            final ItemChatListBinding binding;

            public ChatListItemViewHolder(ItemChatListBinding binding) {
                super(binding.getRoot());

                this.binding = binding;
            }
        }
    }
}
