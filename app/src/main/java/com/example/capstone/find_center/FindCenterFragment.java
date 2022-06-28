package com.example.capstone.find_center;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.capstone.BaseFragment;
import com.example.capstone.data.model.ParcelableTMapPOIItem;
import com.example.capstone.databinding.FragmentFindCenterBinding;
import com.example.capstone.find_center.adapter.POIItemAdapter;

import java.util.ArrayList;

// 이 액티비티의 역할: 주소로 검색 버튼을 눌렀을 때 주소를 검색하는 창이 나오고 검색한 문자열을 통해 실제 주소지를 검색하고 다음 액티비티로 넘겨줌

public class FindCenterFragment extends BaseFragment {

    public static final String TAG = "FindCenterFragment";

    private FragmentFindCenterBinding binding;
    private final POIItemAdapter adapter = new POIItemAdapter();

    public final ArrayList<ParcelableTMapPOIItem> poiList = new ArrayList<>();


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentFindCenterBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        requireActivity().getSupportFragmentManager().setFragmentResultListener(
                POIMapFragment.REQUEST_ADD_ADDRESS, this, (requestKey, result) -> {
                    ArrayList<ParcelableTMapPOIItem> items = result.getParcelableArrayList(POIMapFragment.ARG_POI_LIST);
                    if (items == null || items.isEmpty()) return;

                    ParcelableTMapPOIItem newPoi = items.get(0);
                    for (ParcelableTMapPOIItem poi : poiList) {
                        if (poi.getPOIID().equals(newPoi.getPOIID())) return;
                    }

                    poiList.add(newPoi);
                    adapter.submitList(poiList);
                });

        binding.searchBtn.setOnClickListener(v ->
                startFragment(new SearchAddressFragment(), SearchAddressFragment.TAG, TAG));

        binding.currentMyLocation.setOnClickListener(v ->
                startFragment(POIMapFragment.newInstance(), POIMapFragment.TAG, TAG));

        binding.findCenterBtn.setOnClickListener(v ->
                startFragment(POIMapFragment.newInstance(poiList), POIMapFragment.TAG, TAG));

        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            private void setVisibility() {
                binding.emptyView.setVisibility(adapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
                binding.findCenterBtn.setVisibility(adapter.getItemCount() >= 2 ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onChanged() {
                setVisibility();
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                setVisibility();
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                setVisibility();
            }
        });

        adapter.setOnLongClickListener(poi -> {
            new AlertDialog.Builder(requireContext())
                    .setMessage("삭제하시겠습니까?")
                    .setPositiveButton("확인", (dialogInterface, i) -> {
                        poiList.remove(poi);
                        adapter.submitList(poiList);
                    })
                    .setNegativeButton("취소", null)
                    .create()
                    .show();
        });

        binding.recyclerView.addItemDecoration(new DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL));
        binding.recyclerView.setAdapter(adapter);
    }
}