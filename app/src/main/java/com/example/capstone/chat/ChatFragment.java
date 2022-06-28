package com.example.capstone.chat;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.capstone.BaseFragment;
import com.example.capstone.R;
import com.example.capstone.chat.adapter.ChatAdapter;
import com.example.capstone.data.model.ChatMessage;
import com.example.capstone.data.model.ChatRoom;
import com.example.capstone.data.model.Posting;
import com.example.capstone.databinding.FragmentChatBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import timber.log.Timber;


public class ChatFragment extends BaseFragment implements EventListener<QuerySnapshot>, Observer<QuerySnapshot> {

    private final static String ARGUMENT_CHAT_ROOM = "ARGUMENT_CHAT_ROOM";
    public final static String TAG = "ChatFragment";


    /**
     * 채팅하기 버튼을 눌렀을 때 사용됨
     */
    public static ChatFragment newInstance(Posting posting, FirebaseUser user) {
        ChatRoom room = new ChatRoom(posting, user);
        return newInstance(room);
    }

    /**
     * 채팅 목록에서 접근할 때 사용됨
     */
    public static ChatFragment newInstance(ChatRoom room) {
        Bundle arguments = new Bundle();
        arguments.putParcelable(ARGUMENT_CHAT_ROOM, room);

        ChatFragment fragment = new ChatFragment();
        fragment.setArguments(arguments);

        return fragment;
    }


    private final String PREFS_NAME_FIRST_CHAT_STATUS = "PREFS_NAME_FIRST_CHAT_STATUS";

    private ChatRoom room;

    private FragmentChatBinding binding;
    private SharedPreferences preferences;
    private ChatViewModel viewModel;

    private ChatAdapter adapter;
    private TextWatcher textWatcher;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            room = getArguments().getParcelable(ARGUMENT_CHAT_ROOM);
        }

        if (savedInstanceState != null) {
            room = savedInstanceState.getParcelable(ARGUMENT_CHAT_ROOM);
        }

        assert room != null;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putParcelable(ARGUMENT_CHAT_ROOM, room);
        super.onSaveInstanceState(outState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentChatBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        preferences = requireContext().getSharedPreferences(PREFS_NAME_FIRST_CHAT_STATUS, Context.MODE_PRIVATE);
        viewModel = new ViewModelProvider(this, new ChatViewModelFactory(room)).get(ChatViewModel.class);

        initUi();

        FirebaseUser user = getCurrentUser();
        assert user != null;

        viewModel.getRoomCreationTask(room, user).addOnCompleteListener(task -> {
            prepareChatRoom();
        });
    }

    private void initUi() {
        binding.toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_leave) {
                leave();
            }

            return true;
        });

        adapter = new ChatAdapter();
        binding.recyclerView.setAdapter(adapter);

        textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                binding.sendButton.setEnabled(!binding.messageEditText.getText().toString().trim().isEmpty());
            }
        };

        binding.messageEditText.addTextChangedListener(textWatcher);

        binding.sendButton.setEnabled(false);
        binding.sendButton.setOnClickListener(v -> sendMessage());
    }

    private void prepareChatRoom() {
        final Observer<DocumentSnapshot> postingObserver = snapshot -> {
            if (snapshot.exists()) {
                Posting posting = snapshot.toObject(Posting.class);
                assert posting != null;

                FirebaseUser user = getCurrentUser();
                assert user != null;

                if (posting.getMatchingTarget() != null) {
                    SpannableStringBuilder builder = new SpannableStringBuilder();

                    // 매칭 상대가 자신이라면, 포스팅 작성자의 닉네임을 보여줌
                    if (posting.getMatchingTarget().equals(user.getUid())) {
                        String ownerNickName = room.getNames().get(room.getPostingOwner());
                        builder.append(ownerNickName);
                        builder.setSpan(new UnderlineSpan(), 0, ownerNickName.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                    } else {
                        String nickName = room.getNames().get(posting.getMatchingTarget());
                        builder.append(nickName);
                        builder.setSpan(new UnderlineSpan(), 0, nickName.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }

                    builder.append(" 님과\n매칭 완료!");

                    binding.matchingTargetTextView.setText(builder);
                    binding.matchingTargetView.setVisibility(View.VISIBLE);

                    binding.messageEditText.removeTextChangedListener(textWatcher);
                    binding.sendButton.setEnabled(false);

                    binding.matchingButton.setEnabled(false);
                }
            }

            if (!viewModel.messageLiveData.hasActiveObservers()) {
                viewModel.messageLiveData.observe(getViewLifecycleOwner(), ChatFragment.this);
            }
        };

        final Observer<DocumentSnapshot> roomObserver = snapshot -> {
            if (!snapshot.exists() && !snapshot.getMetadata().isFromCache()) {
                showToastMessage("대화가 종료되었습니다.");
                onBackPressed();

            } else {
                room = snapshot.toObject(ChatRoom.class);

                if (!viewModel.postingLiveData.hasActiveObservers()) {
                    viewModel.postingLiveData.observe(getViewLifecycleOwner(), postingObserver);
                }

                binding.nameTextView.setText(room.getRestaurantName());
                binding.contentsTextView.setText(room.getContents());

                binding.minOrderAmountTextView.setText(
                        "최소 주문 금액 : " + NumberFormat.getInstance(Locale.US).format(room.getMinimumOrderAmount()) + "원");
                binding.deliveryFeeTextView.setText(
                        "배달요금 : " + NumberFormat.getInstance(Locale.US).format(room.getDeliveryFee()) + "원");

                if (room.getPostingOwner().equals(getCurrentUser().getUid())) {
                    binding.nameDistanceTextView.setText(null);

                    binding.matchingButton.setVisibility(View.VISIBLE);
                    binding.matchingButton.setOnClickListener(v -> {
                        matching();
                    });
                } else {
                    SpannableStringBuilder builder = new SpannableStringBuilder();
                    String ownerNickName = room.getNames().get(room.getPostingOwner());
                    builder.append(ownerNickName);
                    builder.setSpan(new UnderlineSpan(), 0, ownerNickName.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                    String distance = "";
                    float[] results = new float[2];

                    if (location != null) {
                        Location.distanceBetween(location.getLatitude(), location.getLongitude(),
                                room.getLatitude(), room.getLongitude(), results);
                        distance = "" + ((int) results[0]) + "m";
                    }

                    builder.append(" (");
                    builder.append(distance);
                    builder.append(")");
                    binding.nameDistanceTextView.setText(builder);

                    binding.matchingButton.setVisibility(View.GONE);
                    binding.matchingButton.setOnClickListener(null);
                }

                binding.storeInformationView.setVisibility(View.VISIBLE);
            }
        };

        viewModel.roomLiveData.observe(getViewLifecycleOwner(), roomObserver);
    }

    @Override
    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
        if (error != null) {
            error.printStackTrace();
            return;
        }

        binding.progressBar.setVisibility(View.GONE);

        ArrayList<ChatMessage> messages = new ArrayList<>();

        Date now = new Date();

        for (DocumentSnapshot snapshot : value.getDocuments()) {
            ChatMessage message = snapshot.toObject(ChatMessage.class);

            if (message.getTimestamp() == null) {
                message.setTimestamp(now);
            }

            messages.add(message);
        }

        Timber.d("Message count : " + messages.size());

        if (preferences.getBoolean(room.getId(), true)) {
            int totalItemCount = messages.size() - 1;
            int lastVisibleItemPosition = ((LinearLayoutManager) binding.recyclerView.getLayoutManager()).findLastVisibleItemPosition();

            if (Math.abs(totalItemCount - lastVisibleItemPosition) <= 5) {
                try {
                    adapter.submitList(messages, () -> binding.recyclerView.smoothScrollToPosition(messages.size() - 1));
                } catch (Exception ignore) {
                }
                return;
            }
        }

        adapter.submitList(messages);
    }

    private void sendMessage() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        assert user != null;

        String message = binding.messageEditText.getText().toString().trim();
        if (message.isEmpty()) return;

        binding.messageEditText.setText("");
        hideKeyboard(binding.messageEditText);

        ChatMessage chatMessage = new ChatMessage(user);
        chatMessage.setMessage(message);

        viewModel.sendMessage(chatMessage);
    }

    private void matching() {
        viewModel.matching(room);
    }

    private void leave() {
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setMessage("채팅방에서 나가시겠습니까?")
                .setPositiveButton("확인", (dialogInterface, i) -> {
                    viewModel.leave();
                })
                .setNegativeButton("취소", null)
                .create();

        dialog.show();
    }

    @Override
    public void onDestroy() {
        if (preferences.getBoolean(room.getId(), false)) {
            preferences.edit()
                    .putBoolean(room.getId(), true)
                    .apply();
        }

        super.onDestroy();
    }

    @Override
    public void onChanged(QuerySnapshot values) {
        binding.progressBar.setVisibility(View.GONE);

        ArrayList<ChatMessage> messages = new ArrayList<>();

        Date now = new Date();

        for (DocumentSnapshot snapshot : values.getDocuments()) {
            ChatMessage message = snapshot.toObject(ChatMessage.class);

            if (message.getTimestamp() == null) {
                message.setTimestamp(now);
            }

            messages.add(message);
        }

        if (preferences.getBoolean(room.getId(), true)) {
            int totalItemCount = messages.size() - 1;
            int lastVisibleItemPosition = ((LinearLayoutManager) binding.recyclerView.getLayoutManager()).findLastVisibleItemPosition();

            if (Math.abs(totalItemCount - lastVisibleItemPosition) <= 5) {
                try {
                    adapter.submitList(messages, () -> binding.recyclerView.smoothScrollToPosition(messages.size() - 1));
                } catch (Exception ignore) {
                }
                return;
            }
        }

        adapter.submitList(messages);
    }
}
