package com.example.capstone.find_center;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;

import com.example.capstone.BaseFragment;
import com.example.capstone.R;
import com.example.capstone.data.model.ParcelableTMapPOIItem;
import com.example.capstone.databinding.FragmentPoiMapBinding;
import com.skt.Tmap.TMapData;
import com.skt.Tmap.TMapMarkerItem;
import com.skt.Tmap.TMapPoint;
import com.skt.Tmap.TMapPolyLine;
import com.skt.Tmap.TMapView;

import java.util.ArrayList;
import java.util.Collections;

public class POIMapFragment extends BaseFragment {

    public static final String TAG = "POIMapFragment";
    public static final String REQUEST_ADD_ADDRESS = "REQUEST_ADD_ADDRESS";

    private static final String ARG_FROM = "ARG_FROM";
    public static final String ARG_POI_LIST = "ARG_POI_LIST";

    enum From {
        CENTER, ADDRESS, MY_LOCATION
    }

    /**
     * 주소 리스트에서 클릭할 때 사용하는 함수
     */
    public static POIMapFragment newInstance(ParcelableTMapPOIItem item) {
        Bundle arguments = new Bundle();
        arguments.putString(ARG_FROM, From.ADDRESS.name());
        arguments.putParcelableArrayList(ARG_POI_LIST, new ArrayList(Collections.singleton(item)));

        POIMapFragment fragment = new POIMapFragment();
        fragment.setArguments(arguments);

        return fragment;
    }

    /**
     * 중앙 지점을 클릭할 때 사용하는 함수
     */
    public static POIMapFragment newInstance(ArrayList<ParcelableTMapPOIItem> items) {
        Bundle arguments = new Bundle();
        arguments.putString(ARG_FROM, From.CENTER.name());
        arguments.putParcelableArrayList(ARG_POI_LIST, items);

        POIMapFragment fragment = new POIMapFragment();
        fragment.setArguments(arguments);

        return fragment;
    }

    /**
     * 현재 위치를 클릭할 때 사용하는 함수
     */
    public static POIMapFragment newInstance() {
        Bundle arguments = new Bundle();
        arguments.putString(ARG_FROM, From.MY_LOCATION.name());

        POIMapFragment fragment = new POIMapFragment();
        fragment.setArguments(arguments);

        return fragment;
    }


    private ArrayList<ParcelableTMapPOIItem> poiList;
    private From from = From.ADDRESS;

    private FragmentPoiMapBinding binding;
    private TMapView tMapView;
    private final Handler uiThread = new Handler(Looper.getMainLooper());
    private Bitmap markerIcon;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            from = From.valueOf(getArguments().getString(ARG_FROM, From.ADDRESS.name()));
            poiList = getArguments().getParcelableArrayList(ARG_POI_LIST);
        }

        if (savedInstanceState != null) {
            from = From.valueOf(savedInstanceState.getString(ARG_FROM, From.ADDRESS.name()));
            poiList = savedInstanceState.getParcelableArrayList(ARG_POI_LIST);
        }

        if (from == From.ADDRESS || from == From.CENTER) {
            assert (poiList != null);
            assert (!poiList.isEmpty());
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString(ARG_FROM, from.name());
        outState.putParcelableArrayList(ARG_POI_LIST, poiList);
        super.onSaveInstanceState(outState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentPoiMapBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tMapView = new TMapView(requireContext());
        tMapView.setSKTMapApiKey(getString(R.string.tmap_api_key)); // api key 설정
        binding.mapContainer.addView(tMapView);

        markerIcon = BitmapFactory.decodeResource(this.getResources(), R.drawable.ic_assistant_photo_black_24dp);

        if (from == From.MY_LOCATION && poiList == null) {
            locationViewModel.getLocation().observe(getViewLifecycleOwner(), new Observer<Location>() {
                private boolean first = true;

                @Override
                public void onChanged(Location location) {
                    if (location == null) return;

                    if (first) {
                        first = false;
                        poiList = new ArrayList<>();

                        TMapData tMapData = new TMapData();
                        tMapData.convertGpsToAddress(location.getLatitude(), location.getLongitude(), address -> {
                            poiList.add(new ParcelableTMapPOIItem(location, address));

                            uiThread.post(POIMapFragment.this::initUi);
                        });
                    }
                }
            });
        } else {
            initUi();
        }
    }

    private void initUi() {
        addMarkers();

        if (from == From.CENTER && poiList.size() > 1) {
            double totalLatitude = 0.0;
            double totalLongitude = 0.0;

            for (ParcelableTMapPOIItem item : poiList) {
                totalLatitude += item.getLatitude();
                totalLongitude += item.getLongitude();
            } // ArrayList인 poiList에 있는 걸 꺼내서 item에 넣어줌
            // poilist에는 주소 검색과 GPS를 통해 수신 받은 주소들이 들어있음
            // 해당 주소들은 get 함수를 통해 경위도 값을 받아서 totalLatitude와 totalLongitude에 저장한다.

            double centerLatitude = totalLatitude / poiList.size();
            double centerLongitude = totalLongitude / poiList.size();
            // ArrayList의 Size는 정보의 수와 같다. 들어있는 수만큼 나눠주어 중간 지점을 구할 수 있다.

            TMapMarkerItem center = new TMapMarkerItem();

            center.setIcon(markerIcon);         // 마커 아이콘 지정
            center.setCanShowCallout(true);     // 풍선뷰의 사용여부를 설정한다.
            center.setCalloutSubTitle("중간지점 입니다.");   // 풍선뷰 보조메시지 설정
            center.setCalloutTitle("중간지점 ");            // 풍선뷰 제목 설정
            // center.setCalloutRightButtonImage(temp);       // 풍선뷰 오른쪽 이미지 설정

            center.setAutoCalloutVisible(true); // 풍선뷰 자동 활성화
            // 마커의 좌표 지정
            center.setTMapPoint(new TMapPoint(centerLatitude, centerLongitude));    // 마커 위,경도 설정
            center.setVisible(TMapMarkerItem.VISIBLE);      // 아이콘 보이게

            //지도에 마커 추가
            tMapView.addMarkerItem("markerItem", center);
            tMapView.setCenterPoint(centerLongitude, centerLatitude);

            // 중간지점에 자동차경로 추가
            drawPath(center.getTMapPoint());
        }

        if (from != From.CENTER) {
            setTitle();

            binding.addButton.setVisibility(View.VISIBLE);
            binding.addButton.setOnClickListener(v -> {
                Bundle bundle = new Bundle();
                bundle.putParcelableArrayList(ARG_POI_LIST, poiList);

                requireActivity().getSupportFragmentManager().setFragmentResult(REQUEST_ADD_ADDRESS, bundle);
                requireActivity().getSupportFragmentManager().popBackStack(FindCenterFragment.TAG,
                        FragmentManager.POP_BACK_STACK_INCLUSIVE);
            });
        } else {
            binding.addButton.setVisibility(View.GONE);
        }
    }

    private void addMarkers() {
        int coordinateNumber = 0;
        if (from != From.CENTER) {
            Fragment fragment = requireActivity().getSupportFragmentManager().findFragmentByTag(FindCenterFragment.TAG);

            if (fragment instanceof FindCenterFragment) {
                coordinateNumber = ((FindCenterFragment) fragment).poiList.size();
            }
        }

        for (int i = 0; i < poiList.size(); i++) {
            ParcelableTMapPOIItem item = poiList.get(i);

            TMapMarkerItem markerItem = new TMapMarkerItem();
            // 마커 아이콘 지정
            markerItem.setIcon(markerIcon);
            markerItem.setCanShowCallout(true);
            markerItem.setCalloutSubTitle(item.getPOIName());
            markerItem.setCalloutTitle("좌표 " + (coordinateNumber + 1) + "번");
            markerItem.setAutoCalloutVisible(true);

            // 마커의 좌표 지정
            markerItem.setTMapPoint(item.getPOIPoint());

            //지도에 마커 추가
            tMapView.addMarkerItem("markerItem" + i, markerItem);
            tMapView.setCenterPoint(item.getLongitude(), item.getLatitude());
        }
    }

    /**
     * 자동차 경로 호출시 외부에서 Thread를 통해서 호출해줘야 정상적으로 실행
     */
    private void drawPath(TMapPoint centerPoint) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    for (int i = 0; i < poiList.size(); i++) {
                        TMapPolyLine tMapPolyLine = new TMapData().findPathData(poiList.get(i).getPOIPoint(), centerPoint);
                        tMapPolyLine.setLineColor(Color.BLUE);
                        tMapPolyLine.setLineWidth(2);
                        tMapView.addTMapPolyLine("Line1" + i, tMapPolyLine);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void setTitle() {
        String name = poiList.get(0).getPOIName();
        String address = poiList.get(0).getPOIAddress();

        if (name.isEmpty()) {
            name = address;
            address = null;
        }

        binding.toolbar.setTitle(name);
        binding.toolbar.setSubtitle(address);
    }
}