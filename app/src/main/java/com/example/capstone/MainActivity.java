package com.example.capstone;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Looper;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.capstone.main.MainFragment;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    // 권한 요청 Launcher
    private final ActivityResultLauncher<String[]> locationPermissionRequest = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            result -> {
                Boolean fineLocationGranted = result.get(Manifest.permission.ACCESS_FINE_LOCATION);
                Boolean coarseLocationGranted = result.get(Manifest.permission.ACCESS_COARSE_LOCATION);

                if (fineLocationGranted != null && fineLocationGranted) {
                    startLocationUpdates();

                } else if (coarseLocationGranted != null && coarseLocationGranted) {
                    startLocationUpdates();

                } else {
                    Toast.makeText(this,
                            "위치 권한이 거부되어 앱을 실행할 수 없습니다.",
                            Toast.LENGTH_SHORT
                    ).show();
                    finish();
                }
            }
    );

    private LocationViewModel locationViewModel;

    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    private FirebaseAuth auth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 뷰 모델 생성 (MainActivity 에서 가져온 현재 위치를 MainActivity 에 속한 모든 Fragment 가 접근하기 위함)
        // https://developer.android.com/topic/libraries/architecture/viewmodel?hl=ko 참조
        locationViewModel = new ViewModelProvider(this).get(LocationViewModel.class);

        // 위치 서비스 클라이언트 생성
        // https://developer.android.com/training/location/retrieve-current?hl=ko 참조
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        // 익명 계정으로 로그인
        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            auth.signInAnonymously();
        }

        // UI 초기화
        initUi();
    }

    /**
     * UI 초기화 함수
     */
    private void initUi() {
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, new MainFragment())
                .commit();
    }

    /**
     * 위치 권한이 부여되었는지 확인하는 함수.
     * 위치 권한이 없다면 권한 요청, 권한이 있는 경우 사용자의 위치를 실시간으로 받기 위해 startLocationUpdates 호출
     */
    private boolean checkLocationPermission() {
        boolean coarseLocationGranted = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED;

        boolean fineLocationGranted = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED;

        if (!coarseLocationGranted && !fineLocationGranted) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * 위치 업데이트 요청 함수
     * locationCallback 이 null 이 아닌 경우는 이미 업데이트 요청을 한 상태이다.
     */
    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        if (locationCallback != null) return;

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                locationViewModel.setLocation(locationResult.getLastLocation());
            }
        };

        fusedLocationClient.requestLocationUpdates(locationRequest,
                locationCallback,
                Looper.getMainLooper());
    }

    /**
     * 위치 업데이트 중지 함수
     * locationCallback 이 null 인 경우는 이미 업데이트를 중지했거나, 요청을 하지 않은 상태이다.
     */
    private void stopLocationUpdates() {
        if (locationCallback == null) return;

        fusedLocationClient.removeLocationUpdates(locationCallback);
        locationCallback = null;
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (checkLocationPermission()) {
            startLocationUpdates();

        } else {
            locationPermissionRequest.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }

    @Override
    protected void onPause() {
        stopLocationUpdates();

        super.onPause();
    }
}