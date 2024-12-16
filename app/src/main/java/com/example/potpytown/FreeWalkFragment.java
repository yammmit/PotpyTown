package com.example.potpytown;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.potpytown.network.TimeUtils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import android.os.Handler;

public class FreeWalkFragment extends Fragment implements OnMapReadyCallback {

    private MapView mapView;
    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    private long startTime;
    private double totalDistance = 0.0;
    private LatLng lastLocation;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private boolean isWalking = false;
    private Button btnStartStopWalk;
    private TextView walkTime;
    private TextView walkDistance;
    private Handler timerHandler = new Handler();
    private Runnable timerRunnable;
    private enum WalkState { IDLE, WALKING, PAUSED }
    private WalkState walkState = WalkState.IDLE;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_freewalk, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Log.d("FreeWalkFragment", "onViewCreated: Initializing components.");

        // Firebase 초기화
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // MapView 초기화
        mapView = view.findViewById(R.id.map_view);
        walkTime = view.findViewById(R.id.walk_time);
        walkDistance = view.findViewById(R.id.walk_distance);
        ProgressBar progressBar = view.findViewById(R.id.progress_bar);
        TextView progressText = view.findViewById(R.id.progress_text);
        btnStartStopWalk = view.findViewById(R.id.btn_start_stop_walk);
        View darkOverlay = view.findViewById(R.id.dark_overlay);
        View endWalkCard = view.findViewById(R.id.end_walk_card);
        MaterialButton continueWalkButton = view.findViewById(R.id.continue_walk_button);
        MaterialButton finishWalkButton = view.findViewById(R.id.finish_walk_button);

        // FusedLocationProviderClient 초기화
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext());


        if (mapView == null) {
            throw new NullPointerException("MapView is null. Check your XML file for the correct ID.");
        }

        // MapView 설정
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        // FusedLocationProviderClient 초기화
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext());

        // 위치 요청 초기화
        locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
                .setMinUpdateIntervalMillis(2000)
                .build();

        // 위치 콜백 설정
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult != null) {
                    double latitude = locationResult.getLastLocation().getLatitude();
                    double longitude = locationResult.getLastLocation().getLongitude();
                    updateLocationOnMap(latitude, longitude);

                    // 거리 계산 (산책 중인 경우에만)
                    if (walkState == WalkState.WALKING && lastLocation != null) {
                        double distance = calculateDistance(lastLocation, new LatLng(latitude, longitude));
                        totalDistance += distance;
                        walkDistance.setText(String.format(Locale.getDefault(), "%.2f km", totalDistance / 1000.0));
                    }
                    lastLocation = new LatLng(latitude, longitude);
                }
            }
        };

        // 위치 업데이트 시작
        startInitialLocationUpdates();

        // MapView 설정
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        // 버튼 클릭 이벤트
        btnStartStopWalk.setOnClickListener(v -> {
            if (walkState == WalkState.WALKING) {
                stopWalkTracking();
                walkState = WalkState.PAUSED;
                btnStartStopWalk.setText("산책 시작하기");

                // 카드와 어두운 배경을 보이게 하고 최상단으로 이동
                darkOverlay.setVisibility(View.VISIBLE);
                endWalkCard.setVisibility(View.VISIBLE);
                endWalkCard.bringToFront(); // 카드가 화면 최상단으로 올라옴
                darkOverlay.bringToFront(); // 어두운 배경도 함께 올라옴
            } else {
                startWalkTracking();
                walkState = WalkState.WALKING;
                btnStartStopWalk.setText("산책 그만하기");

                // 카드와 어두운 배경 숨김
                darkOverlay.setVisibility(View.GONE);
                endWalkCard.setVisibility(View.GONE);
            }
        });

        continueWalkButton.setOnClickListener(v -> {
            darkOverlay.setVisibility(View.GONE);
            endWalkCard.setVisibility(View.GONE);
            startWalkTracking();
            walkState = WalkState.WALKING;
        });


        finishWalkButton.setOnClickListener(v -> {
            darkOverlay.setVisibility(View.GONE);
            endWalkCard.setVisibility(View.GONE);
            stopWalkAndSaveData();
            navigateToFragment(new WalkCompleteFragment());
            isWalking = false;
        });
    }

    // 초기 위치 업데이트 함수
    @SuppressLint("MissingPermission")
    private void startInitialLocationUpdates() {
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.googleMap = googleMap;
        LatLng initialLocation = new LatLng(37.5665, 126.9780);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initialLocation, 15));
    }

    @SuppressLint("MissingPermission")
    private void startWalkTracking() {
        if (walkState != WalkState.PAUSED) {
            startTime = System.currentTimeMillis();
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                long elapsedTime = System.currentTimeMillis() - startTime;
                int seconds = (int) (elapsedTime / 1000) % 60;
                int minutes = (int) (elapsedTime / (1000 * 60)) % 60;
                int hours = (int) (elapsedTime / (1000 * 60 * 60));
                walkTime.setText(String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds));
                timerHandler.postDelayed(this, 1000);
            }
        };
        timerHandler.post(timerRunnable);
    }


    private void stopWalkTracking() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        timerHandler.removeCallbacks(timerRunnable);
    }

    private void updateLocationOnMap(double latitude, double longitude) {
        LatLng currentLocation = new LatLng(latitude, longitude);
        googleMap.clear();
        googleMap.addMarker(new MarkerOptions()
                .position(currentLocation)
                .title("현재 위치")
                .icon(BitmapDescriptorFactory.fromBitmap(resizeMarkerIcon(R.drawable.custom_marker, 130, 176))));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 19));
    }

    private double calculateDistance(LatLng start, LatLng end) {
        double earthRadius = 6371000;
        double dLat = Math.toRadians(end.latitude - start.latitude);
        double dLng = Math.toRadians(end.longitude - start.longitude);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(start.latitude)) * Math.cos(Math.toRadians(end.latitude))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadius * c;
    }

    private Bitmap resizeMarkerIcon(int resourceId, int width, int height) {
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(), resourceId);
        return Bitmap.createScaledBitmap(imageBitmap, width, height, false);
    }

    private String formatTimeToHHMMSS(long seconds) {
        int hours = (int) (seconds / 3600);
        int minutes = (int) ((seconds % 3600) / 60);
        int secs = (int) (seconds % 60);
        return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, secs);
    }

    private void calculateMonthlyTotalTime() {
        String currentMonth = new SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(new Date());

        db.collection("walkRecord")
                .whereEqualTo("month", currentMonth)
                .whereEqualTo("user_id", mAuth.getCurrentUser().getUid()) // 현재 사용자만
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    long totalSeconds = 0;

                    for (QueryDocumentSnapshot document : querySnapshot) {
                        Long time = document.getLong("time");
                        if (time != null) totalSeconds += time;
                    }

                    double totalHours = totalSeconds / 3600.0; // 시간 단위로 변환
                    String totalHoursFormatted = String.format(Locale.getDefault(), "%.1f시간", totalHours);
                    walkDistance.setText("이번달 총 산책 시간: " + totalHoursFormatted);
                })
                .addOnFailureListener(e -> Log.e("Firebase", "Error calculating monthly time", e));
    }


    private void stopWalkAndSaveData() {
        stopWalkTracking();

        long elapsedTimeSeconds = (System.currentTimeMillis() - startTime) / 1000; // 초 단위 시간
        String formattedTime = TimeUtils.formatTimeToHHMMSS(elapsedTimeSeconds);
        String currentMonth = new SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(new Date());

        Map<String, Object> walkData = new HashMap<>();
        walkData.put("date", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));
        walkData.put("distance", totalDistance);
        walkData.put("time", elapsedTimeSeconds); // 초 단위로 저장
        walkData.put("user_id", mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : "unknown");
        walkData.put("type", "freewalk");
        walkData.put("month", currentMonth);

        db.collection("walkRecord")
                .add(walkData)
                .addOnSuccessListener(documentReference -> {
                    Log.d("Firebase", "Walk record saved with ID: " + documentReference.getId());

                    // 산책 완료 화면으로 이동 및 데이터 전달
                    Bundle bundle = new Bundle();
                    bundle.putString("elapsedTime", formattedTime);
                    bundle.putString("distance", String.format(Locale.getDefault(), "%.2f km", totalDistance / 1000.0));

                    WalkCompleteFragment walkCompleteFragment = new WalkCompleteFragment();
                    walkCompleteFragment.setArguments(bundle);

                    navigateToFragment(walkCompleteFragment);
                })
                .addOnFailureListener(e -> Log.e("Firebase", "Error saving walk record", e));
    }



    private void navigateToFragment(Fragment fragment) {
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mapView != null) mapView.onResume();
        if (isWalking) startWalkTracking();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mapView != null) mapView.onPause();
        if (isWalking) stopWalkTracking();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mapView != null) mapView.onDestroy();
        stopWalkTracking();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mapView != null) mapView.onLowMemory();
    }
}