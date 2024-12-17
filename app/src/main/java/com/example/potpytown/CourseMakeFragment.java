package com.example.potpytown;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class CourseMakeFragment extends Fragment implements OnMapReadyCallback {

    private TextView startPoint;
    private MapView mapView;
    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_coursemake, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // UI 요소 초기화
        startPoint = view.findViewById(R.id.text_start_point);
        TextView viaPoint = view.findViewById(R.id.text_via_point);
        TextView endPoint = view.findViewById(R.id.text_end_point);
        mapView = view.findViewById(R.id.map_view);
        ImageView backButton = view.findViewById(R.id.btn_back);

        // MapView 설정
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        // FusedLocationProviderClient 초기화
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext());

        // 출발지 자동 채우기
        fetchCurrentLocation();

        // 출발지, 경유지, 도착지 클릭 이벤트 설정
        startPoint.setOnClickListener(v -> navigateToCourseEdit("start"));
        viaPoint.setOnClickListener(v -> navigateToCourseEdit("via"));
        endPoint.setOnClickListener(v -> navigateToCourseEdit("end"));

        // 뒤로가기 버튼 클릭 이벤트
        backButton.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());
    }

    /**
     * CourseEditFragment로 이동
     * @param pointType 클릭한 위치 타입 ("start", "via", "end")
     */
    private void navigateToCourseEdit(String pointType) {
        Bundle bundle = new Bundle();
        bundle.putString("point_type", pointType); // 클릭한 지점 정보 전달

        CourseEditFragment courseEditFragment = new CourseEditFragment();
        courseEditFragment.setArguments(bundle);

        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, courseEditFragment) // 프래그먼트 전환
                .addToBackStack(null) // 백 스택에 추가
                .commit();
    }


    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
    }

    @SuppressLint("MissingPermission")
    private void fetchCurrentLocation() {
        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(1000);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                Location location = locationResult.getLastLocation();
                if (location != null) {
                    LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());

                    // Geocoder를 사용하여 위치의 주소 또는 이름 가져오기
                    Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
                    try {
                        List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                        if (addresses != null && !addresses.isEmpty()) {
                            Address address = addresses.get(0);

                            // 전체 주소 라인 가져오기
                            String fullAddress = address.getAddressLine(0);

                            // "대한민국" 제거
                            if (fullAddress != null && fullAddress.contains("대한민국")) {
                                fullAddress = fullAddress.replace("대한민국", "").trim();
                            }

                            // 도로명 주소나 구 단위 주소
                            String thoroughfare = address.getThoroughfare(); // 도로명
                            String subLocality = address.getSubLocality();   // 동
                            String locality = address.getLocality();         // 구
                            String adminArea = address.getAdminArea();       // 시/도

                            String displayText;

                            // 도로명 주소를 우선으로 표시
                            if (thoroughfare != null && !thoroughfare.isEmpty()) {
                                displayText = String.format("%s %s %s",
                                        adminArea != null ? adminArea : "",
                                        locality != null ? locality : "",
                                        thoroughfare);
                            } else {
                                // 도로명 주소가 없으면 국가명 제외한 전체 주소 사용
                                displayText = fullAddress.isEmpty() ? "위치 정보 없음" : fullAddress;
                            }

                            // 화면에 표시
                            startPoint.setText("내위치 : " + displayText);
                        } else {
                            startPoint.setText("내위치 : 위치 정보를 가져올 수 없습니다.");
                        }
                    } catch (IOException e) {
                        startPoint.setText("내위치 : 위치 정보 오류");
                    }

                    // 지도에 현재 위치 표시 (커스텀 핀 적용)
                    if (googleMap != null) {
                        googleMap.clear();
                        googleMap.addMarker(new MarkerOptions()
                                .position(currentLocation)
                                .title("출발지")
                                .icon(resizeBitmap(R.drawable.pin_origin, 156, 124))); // 핀 이미지 설정
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
                    }

                    // 위치 업데이트 중지
                    fusedLocationProviderClient.removeLocationUpdates(this);
                }
            }
        };

        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, requireActivity().getMainLooper());
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mapView != null) mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mapView != null) mapView.onPause();
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mapView != null) mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mapView != null) mapView.onLowMemory();
    }

    private BitmapDescriptor resizeBitmap(int drawableRes, int width, int height) {
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(), drawableRes);
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, width, height, false);
        return BitmapDescriptorFactory.fromBitmap(resizedBitmap);
    }

}
