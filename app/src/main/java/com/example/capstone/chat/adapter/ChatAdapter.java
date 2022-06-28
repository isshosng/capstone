package com.example.capstone.chat.adapter;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.capstone.R;
import com.example.capstone.data.model.ChatMessage;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Calendar;
import java.util.Date;
import java.util.List;


public class ChatAdapter extends ListAdapter<ChatMessage, ChatAdapter.ChatItemViewHolder> {

    public static int SHOW_NICK_NAME = 0x1000;
    public static int SHOW_DAY = 0x0100;
    public static int SHOW_TIME = 0x0010;
    public static int IS_MINE = 0x0001;

    public static DiffUtil.ItemCallback<ChatMessage> DIFF_CALLBACK = new DiffUtil.ItemCallback<ChatMessage>() {
        @Override
        public boolean areItemsTheSame(@NonNull ChatMessage oldItem, @NonNull ChatMessage newItem) {
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull ChatMessage oldItem, @NonNull ChatMessage newItem) {
            return oldItem.getMessage().equals(newItem.getMessage()) &&
                    oldItem.getTimestamp().equals(newItem.getTimestamp());
        }
    };


    private final FirebaseUser user;

    public ChatAdapter() {
        super(DIFF_CALLBACK);

        user = FirebaseAuth.getInstance().getCurrentUser();
        assert user != null;
    }

    @Override
    public int getItemViewType(int position) {
        return getCurrentList().get(position).getViewType();
    }

    @NonNull
    @Override
    public ChatItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View view;
        if ((viewType & IS_MINE) > 0) {
            view = inflater.inflate(R.layout.item_chat_my_message, parent, false);

        } else {
            view = inflater.inflate(R.layout.item_chat_other_person_message, parent, false);
        }

        ChatItemViewHolder holder = new ChatItemViewHolder(view);

        if ((viewType & SHOW_DAY) == 0) {
            if (holder.dateContainer != null) {
                LinearLayout linearLayout = (LinearLayout) holder.dateContainer.getParent();
                linearLayout.removeView(holder.dateContainer);

                holder.dateContainer = null;
            }
        }

        if ((viewType & SHOW_TIME) == 0) {
            if (holder.timeTextView != null) {
                ConstraintLayout constraintLayout = (ConstraintLayout) holder.timeTextView.getParent();
                constraintLayout.removeView(holder.timeTextView);
                holder.timeTextView = null;

                ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) holder.messageContainer.getLayoutParams();

                if ((viewType & IS_MINE) > 0) {
                    params.leftToRight = ConstraintLayout.LayoutParams.UNSET;
                    params.leftToLeft = ConstraintLayout.LayoutParams.PARENT_ID;
                    params.horizontalBias = 1.0f;

                } else {
                    params.rightToLeft = ConstraintLayout.LayoutParams.UNSET;
                    params.rightToRight = ConstraintLayout.LayoutParams.PARENT_ID;
                    params.horizontalBias = 0.0f;
                }

                holder.messageContainer.setLayoutParams(params);
            }
        }

        if ((viewType & SHOW_NICK_NAME) == 0 && holder.nickNameTextView != null) {
            ConstraintLayout constraintLayout = (ConstraintLayout) holder.nickNameTextView.getParent();
            constraintLayout.removeView(holder.nickNameTextView);
            holder.nickNameTextView = null;


            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) holder.messageContainer.getLayoutParams();
            params.topMargin = 0;
            params.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
            holder.messageContainer.setLayoutParams(params);
        }

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ChatItemViewHolder holder, int position) {
        Context context = holder.itemView.getContext();

        ChatMessage message = getCurrentList().get(position);

        if (holder.dateTextView != null) {
            holder.dateTextView.setText(
                    DateUtils.formatDateTime(context,
                            message.getTimestamp().getTime(),
                            DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_WEEKDAY));
        }

        if (holder.messageTextView != null) {
            holder.messageTextView.setText(message.getMessage());
        }

        if (holder.timeTextView != null) {
            holder.timeTextView.setText(
                    DateUtils.formatDateTime(context,
                            message.getTimestamp().getTime(),
                            DateUtils.FORMAT_SHOW_TIME)
            );
        }

        if (holder.nickNameTextView != null) {
            holder.nickNameTextView.setText(message.getNickName());
        }
    }

    @Override
    public void submitList(@Nullable List<ChatMessage> list) {
        if (list != null) {
            setViewTypes(list);
        }

        super.submitList(list);
    }

    @Override
    public void submitList(@Nullable List<ChatMessage> list, @Nullable Runnable commitCallback) {
        if (list != null) {
            setViewTypes(list);
        }

        super.submitList(list, commitCallback);
    }

    private boolean dateEquals(Date lhs, Date rhs) {
        Calendar lhsC = Calendar.getInstance();
        lhsC.setTime(lhs);

        Calendar rhsC = Calendar.getInstance();
        rhsC.setTime(rhs);

        return lhsC.get(Calendar.YEAR) == rhsC.get(Calendar.YEAR) &&
                lhsC.get(Calendar.DAY_OF_YEAR) == rhsC.get(Calendar.DAY_OF_YEAR);
    }

    private boolean timeEquals(Date lhs, Date rhs) {
        Calendar lhsC = Calendar.getInstance();
        lhsC.setTime(lhs);

        Calendar rhsC = Calendar.getInstance();
        rhsC.setTime(rhs);

        return dateEquals(lhs, rhs) &&
                lhsC.get(Calendar.HOUR_OF_DAY) == rhsC.get(Calendar.HOUR_OF_DAY) &&
                lhsC.get(Calendar.MINUTE) == rhsC.get(Calendar.MINUTE);
    }

    private void setViewTypes(List<ChatMessage> messages) {
        Date now = new Date();

        for (int i = 0; i < messages.size(); i++) {
            int flag = 0;
            ChatMessage message = messages.get(i);
            ChatMessage prevMessage = null;
            ChatMessage nextMessage = null;

            Date timestamp = message.getTimestamp();
            Date prevTimestamp = null;
            Date nextTimestamp = null;

            if (i - 1 >= 0) {
                prevMessage = messages.get(i - 1);
                prevTimestamp = prevMessage.getTimestamp();

                if (prevTimestamp == null) {
                    prevTimestamp = now;
                }
            }

            if (i + 1 < messages.size()) {
                nextMessage = messages.get(i + 1);
                nextTimestamp = nextMessage.getTimestamp();

                if (nextTimestamp == null) {
                    nextTimestamp = now;
                }
            }

            if (prevMessage == null || !dateEquals(prevTimestamp, timestamp)) {
                flag |= ChatAdapter.SHOW_DAY;
            }

            if (message.getUid().equals(user.getUid())) {
                flag |= ChatAdapter.IS_MINE;
            }

            if (nextMessage == null ||
                    (!nextMessage.getUid().equals(message.getUid())) ||
                    ((nextMessage.getUid().equals(message.getUid())) && (!timeEquals(timestamp, nextTimestamp)))
            ) {
                flag |= ChatAdapter.SHOW_TIME;
            }

            if (prevMessage == null ||
                    (!prevMessage.getUid().equals(message.getUid())) ||
                    ((prevMessage.getUid().equals(message.getUid())) && (!timeEquals(prevTimestamp, timestamp)))
            ) {
                flag |= ChatAdapter.SHOW_NICK_NAME;
            }

            message.setViewType(flag);
        }
    }


    protected static class ChatItemViewHolder extends RecyclerView.ViewHolder {

        View dateContainer;
        TextView dateTextView;

        TextView nickNameTextView;
        View messageContainer;
        TextView messageTextView;
        TextView timeTextView;


        public ChatItemViewHolder(@NonNull View itemView) {
            super(itemView);

            dateContainer = itemView.findViewById(R.id.date_container);
            dateTextView = itemView.findViewById(R.id.date_text_view);

            messageTextView = itemView.findViewById(R.id.nick_name_text_view);
            messageContainer = itemView.findViewById(R.id.message_view_container);
            messageTextView = itemView.findViewById(R.id.message_text_view);
            timeTextView = itemView.findViewById(R.id.time_text_view);
        }
    }
}
