package com.example.capstone.main;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.capstone.BaseFragment;
import com.example.capstone.R;
import com.example.capstone.auth.NickNameBottomSheet;
import com.example.capstone.chat.ChatFragment;
import com.example.capstone.chat.ChatListFragment;
import com.example.capstone.data.model.Posting;
import com.example.capstone.databinding.FragmentMainBinding;
import com.example.capstone.find_center.FindCenterFragment;
import com.example.capstone.posting.PostingFragment;
import com.example.capstone.posting.PostingListFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.skt.Tmap.TMapMarkerItem;
import com.skt.Tmap.TMapPoint;
import com.skt.Tmap.TMapView;
import com.skt.Tmap.poi_item.TMapPOIItem;

import org.ocpsoft.prettytime.PrettyTime;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import timber.log.Timber;

public class MainFragment extends BaseFragment {

    private FragmentMainBinding binding;

    private MainViewModel viewModel;
    private TMapView tMapView;

    private final HashMap<String, Posting> markerData = new HashMap<>();
    private TMapMarkerItem selectedMarkerItem = null;


    public MainFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentMainBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        tMapView = new TMapView(view.getContext());
        tMapView.setSKTMapApiKey(getString(R.string.tmap_api_key));
        tMapView.setIconVisibility(true);
        binding.mapContainer.addView(tMapView);

        tMapView.setOnClickListenerCallBack(new TMapView.OnClickListenerCallback() {
            @Override
            public boolean onPressEvent(ArrayList<TMapMarkerItem> arrayList, ArrayList<TMapPOIItem> arrayList1, TMapPoint tMapPoint, PointF pointF) {
                if (!arrayList.isEmpty()) {
                    TMapMarkerItem markerItem = arrayList.get(0);
                    showStoreInformation(markerItem);
                }

                return false;
            }

            @Override
            public boolean onPressUpEvent(ArrayList<TMapMarkerItem> arrayList, ArrayList<TMapPOIItem> arrayList1, TMapPoint tMapPoint, PointF pointF) {
                return false;
            }
        });

        binding.storeInformationView.setOnClickListener(v -> hideStoreInformation());

        binding.addButton.setOnClickListener(v -> {
            if (!hasNickName()) {
                requireNickName();
                return;
            }

            startFragment(new PostingFragment(), PostingFragment.TAG, null);
        });

        binding.moreButton.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(v.getContext(), v);
            MenuInflater inflater = popupMenu.getMenuInflater();
            inflater.inflate(R.menu.popup, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.action_search_address) {
                    startFragment(new FindCenterFragment(), FindCenterFragment.TAG, null);

                } else if (item.getItemId() == R.id.action_my_posting) {
                    startFragment(new PostingListFragment(), PostingListFragment.TAG, null);

                } else if (item.getItemId() == R.id.action_chat_list) {
                    startFragment(new ChatListFragment(), ChatListFragment.TAG, null);
                }

                return true;
            });

            popupMenu.show();
        });

        binding.myLocationButton.setOnClickListener(v -> {
            if (location == null) return;

            tMapView.setCenterPoint(location.getLongitude(), location.getLatitude());
        });

        locationViewModel.getLocation().observe(getViewLifecycleOwner(), new Observer<Location>() {
            private boolean first = true;

            @Override
            public void onChanged(Location location) {
                if (location == null) return;

                if (first) {
                    tMapView.setCenterPoint(location.getLongitude(), location.getLatitude());
                    first = false;
                }

                tMapView.setLocationPoint(location.getLongitude(), location.getLatitude());
            }
        });

        viewModel.postingSnapshotLiveData.observe(getViewLifecycleOwner(), snapshot -> {
            for (DocumentChange change : snapshot.getDocumentChanges()) {
                if (change.getType() == DocumentChange.Type.ADDED) {
                    addMarker(change.getDocument().toObject(Posting.class));

                } else if (change.getType() == DocumentChange.Type.REMOVED) {
                    removeMarker(change.getDocument().getId());

                } else if (change.getType() == DocumentChange.Type.MODIFIED) {
                    modifyMarker(change.getDocument().toObject(Posting.class));
                }
            }
        });

        requireActivity().getSupportFragmentManager().setFragmentResultListener(
                PostingListFragment.REQUEST_SHOW_MARKER, this, (requestKey, result) -> {
                    Posting posting = result.getParcelable(PostingListFragment.ARG_POSTING);
                    if (posting == null) return;

                    TMapMarkerItem item = tMapView.getMarkerItemFromID(posting.getId());
                    if (item == null) return;

                    showStoreInformation(item);
                });

//        tMapView.setOnDisableScrollWithZoomLevelListener((v, tMapPoint) -> {
//            float[] distances = new float[2];
//            Location.distanceBetween(tMapView.getLeftTopPoint().getLatitude(), tMapView.getLeftTopPoint().getLongitude(),
//                    tMapPoint.getLatitude(), tMapPoint.getLongitude(), distances);
//
//            Timber.d("Scrolled");
//        });
    }

    private void addMarker(Posting posting) {
        Timber.d("addMarker: " + posting.getId());

        Bitmap icon = getMarkerBitmap(posting.getFoodClassification().substring(0, 2), null, null);

        TMapMarkerItem markerItem = new TMapMarkerItem();
        markerItem.setTMapPoint(new TMapPoint(posting.getLatitude(), posting.getLongitude()));
        markerItem.setName(posting.getRestaurantName());
        markerItem.setIcon(icon);
        tMapView.addMarkerItem(posting.getId(), markerItem);

        markerData.put(posting.getId(), posting);
    }

    private void removeMarker(String id) {
        Timber.d("removeMarker: " + id);

        tMapView.removeMarkerItem(id);
        markerData.remove(id);
    }

    private void modifyMarker(Posting posting) {
        Timber.d("modifyMarker: " + posting.getId());

        markerData.put(posting.getId(), posting);
    }

    private void showStoreInformation(TMapMarkerItem item) {
        Posting posting = markerData.get(item.getID());
        if (posting == null) return;

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        assert user != null;

        tMapView.post(() -> {
            selectedMarkerItem = item;
            item.setIcon(getMarkerBitmap(posting.getFoodClassification().substring(0, 2), 0xFFFF9900, null));

            tMapView.setCenterPoint(posting.getLongitude(), posting.getLatitude());
        });

        binding.nameTextView.setText(posting.getRestaurantName());
        binding.contentsTextView.setText(posting.getContents());

        String distance = "";
        float[] results = new float[2];

        if (location != null) {
            Location.distanceBetween(location.getLatitude(), location.getLongitude(),
                    item.latitude, item.longitude, results);
            distance = "" + ((int) results[0]) + "m";
        }

        if (posting.getTimestamp() != null) {
            binding.timestampNameDistanceTextView.setText(new PrettyTime().format(posting.getTimestamp()));
            binding.timestampNameDistanceTextView.append(", " + distance);

        } else {
            binding.timestampNameDistanceTextView.setText(distance);
        }

        binding.minOrderAmountTextView.setText(
                "최소 주문 금액 : " + NumberFormat.getInstance(Locale.US).format(posting.getMinimumOrderAmount()) + "원");
        binding.deliveryFeeTextView.setText(
                "배달요금 : " + NumberFormat.getInstance(Locale.US).format(posting.getDeliveryFee()) + "원");

        if (posting.getUid().equals(user.getUid())) {
            binding.chatButton.setVisibility(View.GONE);
            binding.chatButton.setOnClickListener(null);

        } else {
            binding.chatButton.setVisibility(View.VISIBLE);
            binding.chatButton.setOnClickListener(v -> {
                if (!hasNickName()) {
                    requireNickName();
                    return;
                }

                startFragment(ChatFragment.newInstance(posting, user), ChatFragment.TAG, null);
                hideStoreInformation();
            });
        }

        binding.storeInformationView.setVisibility(View.VISIBLE);
    }

    private void hideStoreInformation() {
        if (selectedMarkerItem != null) {
            Posting posting = markerData.get(selectedMarkerItem.getID());

            if (posting != null) {
                selectedMarkerItem.setIcon(getMarkerBitmap(posting.getFoodClassification().substring(0, 2), null, null));
                tMapView.postInvalidate();
            }

            selectedMarkerItem = null;
        }

        binding.chatButton.setOnClickListener(null);
        binding.storeInformationView.setVisibility(View.GONE);
    }

    private boolean hasNickName() {
        String nickName = getCurrentUser().getDisplayName();

        return nickName != null && !nickName.trim().isEmpty();
    }

    private void requireNickName() {
        if (getChildFragmentManager().findFragmentByTag(NickNameBottomSheet.TAG) == null) {
            new NickNameBottomSheet().show(getChildFragmentManager(), NickNameBottomSheet.TAG);
        }
    }

    /**
     * 마커 Bitmap 을 리턴하는 함수
     *
     * @param text        마커에 표시할 글씨 (최대 2글자)
     * @param markerColor 마커 색상 (기본 primary color)
     * @param textColor   글씨 색상 (기본 검정)
     * @return 마커 Bitmap
     */
    public Bitmap getMarkerBitmap(String text, @Nullable Integer markerColor, @Nullable Integer textColor) {
        Resources resources = getResources();
        float scale = resources.getDisplayMetrics().density;
        Bitmap bitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_marker_48);

        Bitmap.Config bitmapConfig = bitmap.getConfig();
        // set default bitmap config if none
        if (bitmapConfig == null) {
            bitmapConfig = Bitmap.Config.ARGB_8888;
        }
        // resource bitmaps are imutable,
        // so we need to convert it to mutable one
        bitmap = bitmap.copy(bitmapConfig, true);

        Bitmap result = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), bitmapConfig);

        Canvas canvas = new Canvas(result);

        // new antialiased Paint
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        // set marker color
        if (markerColor != null) {
            paint.setColorFilter(new PorterDuffColorFilter(markerColor, PorterDuff.Mode.SRC_ATOP));
        }

        // draw marker
        canvas.drawBitmap(bitmap, 0, 0, paint);

        // clear color
        paint.setColorFilter(null);

        // text color
        if (textColor != null) {
            paint.setColor(textColor);

        } else {
            paint.setColor(Color.BLACK);
        }
        // text size in pixels
        paint.setTextSize((int) (14 * scale));
        // text shadow
        // paint.setShadowLayer(1f, 0f, 1f, Color.WHITE);

        // draw text to the Canvas center
        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);
        int x = (bitmap.getWidth() - bounds.width()) / 2;
        int y = (bitmap.getHeight() + bounds.height()) / 5 * 2;

        canvas.drawText(text, x, y, paint);

        return result;
    }
}