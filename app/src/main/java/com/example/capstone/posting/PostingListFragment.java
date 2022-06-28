package com.example.capstone.posting;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Consumer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.capstone.BaseFragment;
import com.example.capstone.data.model.Posting;
import com.example.capstone.databinding.FragmentPostingListBinding;
import com.example.capstone.databinding.ItemPostingListBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;


public class PostingListFragment extends BaseFragment implements CompoundButton.OnCheckedChangeListener {

    public static final String TAG = "PostingListFragment";
    public static final String REQUEST_SHOW_MARKER = "REQUEST_SHOW_MARKER";
    public static final String ARG_POSTING = "ARG_POSTING";

    private FragmentPostingListBinding binding;
    private PostingListViewModel viewModel;
    private final PostingListAdapter adapter = new PostingListAdapter();


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentPostingListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(PostingListViewModel.class);

        adapter.setOnCheckedChangeListener(stringBooleanHashMap -> {
            int count = 0;
            for (boolean checked : stringBooleanHashMap.values()) {
                if (checked) {
                    count += 1;
                }
            }

            binding.checkbox.setOnCheckedChangeListener(null);
            binding.checkbox.setChecked(count == adapter.getItemCount());
            binding.checkbox.setOnCheckedChangeListener(this);
            binding.removeButton.setEnabled(count > 0);
        });

        adapter.setOnItemClickedListener(posting -> {
            if (posting.getMatchingTarget() != null) {
                Toast.makeText(requireContext(), "이미 매칭된 글입니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            Bundle bundle = new Bundle();
            bundle.putParcelable(ARG_POSTING, posting);

            requireActivity().getSupportFragmentManager().setFragmentResult(REQUEST_SHOW_MARKER, bundle);
            onBackPressed();
        });

        binding.recyclerView.setAdapter(adapter);

        viewModel.postingListLiveData.observe(getViewLifecycleOwner(), snapshot -> {
            binding.progressBar.setVisibility(View.GONE);

            ArrayList<Posting> dataSet = new ArrayList<>();

            for (int i = 0; i < snapshot.size(); i++) {
                Posting posting = snapshot.getDocuments().get(i).toObject(Posting.class);
                assert posting != null;

                posting.setNumber(snapshot.size() - i);

                dataSet.add(posting);
            }

            adapter.submitList(dataSet);

            binding.emptyView.setText("게시한 글이 없습니다.");
            binding.emptyView.setVisibility(dataSet.isEmpty() ? View.VISIBLE : View.GONE);
        });

        binding.checkbox.setOnCheckedChangeListener(this);

        binding.removeButton.setOnClickListener(v -> {
            viewModel.removePostingList(new ArrayList<>(adapter.checkStatus.keySet()));
        });
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if (b) {
            adapter.allCheck();
            binding.removeButton.setEnabled(true);

        } else {
            adapter.clearCheck();
            binding.removeButton.setEnabled(false);
        }
    }


    private static class PostingListAdapter extends ListAdapter<Posting, PostingListAdapter.PostingListItemViewHolder> {

        private final FirebaseUser user;
        final HashMap<String, Boolean> checkStatus = new HashMap<>();
        private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd", Locale.US);
        private Consumer<HashMap<String, Boolean>> onCheckedChangeListener;
        private Consumer<Posting> onItemClickedListener;


        protected PostingListAdapter() {
            super(new DiffUtil.ItemCallback<Posting>() {
                @Override
                public boolean areItemsTheSame(@NonNull Posting oldItem, @NonNull Posting newItem) {
                    return oldItem.getId().equals(newItem.getId());
                }

                @Override
                public boolean areContentsTheSame(@NonNull Posting oldItem, @NonNull Posting newItem) {
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

                    return oldItem.getRestaurantName().equals(newItem.getRestaurantName()) &&
                            oldItem.getNumber() == newItem.getNumber();
                }
            });

            user = FirebaseAuth.getInstance().getCurrentUser();
            assert user != null;
        }

        @NonNull
        @Override
        public PostingListItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            return new PostingListItemViewHolder(ItemPostingListBinding.inflate(inflater, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull PostingListItemViewHolder holder, int position) {
            Posting posting = getItem(position);

            Boolean checked = checkStatus.get(posting.getId());
            if (checked == null) checked = false;

            holder.binding.checkbox.setChecked(checked);
            holder.binding.checkbox.setText("" + posting.getNumber());
            holder.binding.checkbox.setOnCheckedChangeListener((compoundButton, b) -> {
                checkStatus.put(posting.getId(), b);

                if (onCheckedChangeListener != null) {
                    onCheckedChangeListener.accept(checkStatus);
                }
            });

            holder.binding.nameTextView.setText(posting.getRestaurantName());

            if (posting.getTimestamp() != null) {
                holder.binding.timestampTextView.setText(dateFormat.format(posting.getTimestamp()));

            } else {
                holder.binding.timestampTextView.setText("");
            }

            holder.binding.getRoot().setOnClickListener(v -> {
                if (onItemClickedListener != null) {
                    onItemClickedListener.accept(posting);
                }
            });
        }

        public void setOnCheckedChangeListener(Consumer<HashMap<String, Boolean>> onCheckedChangeListener) {
            this.onCheckedChangeListener = onCheckedChangeListener;
        }

        public void setOnItemClickedListener(Consumer<Posting> onItemClickedListener) {
            this.onItemClickedListener = onItemClickedListener;
        }

        public void allCheck() {
            for (int i = 0; i < getItemCount(); i++) {
                checkStatus.put(getItem(i).getId(), true);
            }

            notifyDataSetChanged();
        }

        public void clearCheck() {
            checkStatus.clear();
            notifyDataSetChanged();
        }

        static class PostingListItemViewHolder extends RecyclerView.ViewHolder {

            final ItemPostingListBinding binding;

            public PostingListItemViewHolder(ItemPostingListBinding binding) {
                super(binding.getRoot());

                this.binding = binding;
            }
        }
    }
}
