package com.example.capstone.find_center;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.capstone.BaseFragment;
import com.example.capstone.data.model.ParcelableTMapPOIItem;
import com.example.capstone.databinding.FragmentSearchAddressBinding;
import com.example.capstone.find_center.adapter.POIItemAdapter;
import com.skt.Tmap.TMapData;
import com.skt.Tmap.poi_item.TMapPOIItem;

import java.util.ArrayList;

public class SearchAddressFragment extends BaseFragment {

    public static final String TAG = "SearchAddressFragment";

    private FragmentSearchAddressBinding binding;
    private final TMapData tmapData = new TMapData();
    private final Handler uiThread = new Handler(Looper.getMainLooper());
    private final POIItemAdapter adapter = new POIItemAdapter();


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSearchAddressBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

//        binding.searchView.setOnSearchClickListener(v -> {
//            requireActivity().onBackPressed();
//            binding.searchView.setIconified(false);
//        });

        binding.backButton.setOnClickListener(v -> {
            requireActivity().onBackPressed();
        });

        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchAddress(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return true;
            }
        });

        adapter.setOnClickListener((item) -> {
            startFragment(POIMapFragment.newInstance(item), POIMapFragment.TAG, null);
        });

        binding.recyclerView.addItemDecoration(new DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL));
        binding.recyclerView.setAdapter(adapter);
    }

    private void searchAddress(String query) {
        if (binding.progressView.getVisibility() == View.VISIBLE) return;

        binding.progressView.setVisibility(View.VISIBLE);

        tmapData.findAllPOI(query, poiItem -> {
            ArrayList<ParcelableTMapPOIItem> items = new ArrayList<>();

            for (TMapPOIItem item : poiItem) {
                items.add(new ParcelableTMapPOIItem(item));
            }

            uiThread.post(() -> {
                uiThread.post(() -> binding.progressView.setVisibility(View.INVISIBLE));
                binding.emptyView.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
                adapter.submitList(items);
            });
        });
    }
}

