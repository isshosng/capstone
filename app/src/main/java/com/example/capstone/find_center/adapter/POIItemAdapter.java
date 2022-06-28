package com.example.capstone.find_center.adapter;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Consumer;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.capstone.data.model.ParcelableTMapPOIItem;
import com.example.capstone.databinding.ItemAddressBinding;

import java.util.ArrayList;
import java.util.List;


public class POIItemAdapter extends ListAdapter<ParcelableTMapPOIItem, POIItemAdapter.POIItemViewHolder> {

    private Consumer<ParcelableTMapPOIItem> onClickListener;
    private Consumer<ParcelableTMapPOIItem> onLongClickListener;

    public POIItemAdapter() {
        super(new DiffUtil.ItemCallback<ParcelableTMapPOIItem>() {
            @Override
            public boolean areItemsTheSame(@NonNull ParcelableTMapPOIItem oldItem, @NonNull ParcelableTMapPOIItem newItem) {
                return oldItem.getPOIID().equals(newItem.getPOIID());
            }

            @Override
            public boolean areContentsTheSame(@NonNull ParcelableTMapPOIItem oldItem, @NonNull ParcelableTMapPOIItem newItem) {
                return oldItem.getPOIAddress().equals(newItem.getPOIAddress()) &&
                        oldItem.getPOIName().equals(newItem.getPOIName());
            }
        });
    }

    public void setOnClickListener(Consumer<ParcelableTMapPOIItem> onClickListener) {
        this.onClickListener = onClickListener;
    }

    public void setOnLongClickListener(Consumer<ParcelableTMapPOIItem> onLongClickListener) {
        this.onLongClickListener = onLongClickListener;
    }

    @Override
    public void submitList(@Nullable List<ParcelableTMapPOIItem> list) {
        if (list != null) {
            super.submitList(new ArrayList<>(list));

        } else {
            super.submitList(null);
        }
    }

    @Override
    public void submitList(@Nullable List<ParcelableTMapPOIItem> list, @Nullable Runnable commitCallback) {
        if (list != null) {
            super.submitList(new ArrayList<>(list), commitCallback);

        } else {
            super.submitList(null, commitCallback);
        }
    }

    @NonNull
    @Override
    public POIItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemAddressBinding binding = ItemAddressBinding.inflate(inflater, parent, false);
        return new POIItemViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull POIItemViewHolder holder, int position) {
        ParcelableTMapPOIItem item = getItem(position);

        String name = item.getPOIName();
        String address = item.getPOIAddress();

        if (name.isEmpty()) {
            name = address;
            address = null;
        }

        holder.binding.poiNameTextView.setText(name);
        holder.binding.addressTextView.setText(address);
        holder.binding.addressTextView.setVisibility(address == null ? View.GONE : View.VISIBLE);

        holder.binding.getRoot().setOnClickListener(v -> {
            if (onClickListener != null) {
                onClickListener.accept(item);
            }
        });

        holder.binding.getRoot().setOnLongClickListener(v -> {
            if (onLongClickListener != null) {
                onLongClickListener.accept(item);
            }

            return true;
        });
    }

    static class POIItemViewHolder extends RecyclerView.ViewHolder {
        private final ItemAddressBinding binding;

        public POIItemViewHolder(ItemAddressBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
