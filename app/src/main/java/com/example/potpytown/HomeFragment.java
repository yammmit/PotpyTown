package com.example.potpytown;

import android.Manifest;
import android.annotation.SuppressLint;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.potpytown.component.Common;
import com.example.potpytown.data.ITEM;
import com.example.potpytown.data.WEATHER;
import com.example.potpytown.network.WeatherObject;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.CircularBounds;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.api.net.SearchNearbyRequest;

import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private TextView locationWeatherText;
    private com.airbnb.lottie.LottieAnimationView weatherIcon;
    public View placeDetail_view;
    private String baseDate, baseTime;
    private int nx, ny; // 격자 좌표
    private int lastNx = -1, lastNy = -1; // 이전 좌표
    private Handler handler = new Handler();
    private Runnable weatherUpdateTask;
    boolean toastDisplayed = false;
    private static final long REQUEST_INTERVAL = 15 * 60 * 1000; // 15분 (밀리초 단위)
    private PlacesClient placesClient;
    final List<Place.Field> placeFields = Arrays.asList(
            Place.Field.ID,
            Place.Field.DISPLAY_NAME,
            Place.Field.ADDRESS,
            Place.Field.PHONE_NUMBER,
            Place.Field.RATING,
            Place.Field.ALLOWS_DOGS,
            Place.Field.PHOTO_METADATAS
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // fragment_home.xml 레이아웃 사용
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Google Places API Key 가져오기
        String apiKey = BuildConfig.PLACES_API_KEY;

        // API 키가 비어 있거나 기본 키가 설정된 경우 로그를 남기고 종료
        if (TextUtils.isEmpty(apiKey) || apiKey.equals("DEFAULT_API_KEY")) {
            Log.e("Places test", "No api key");
            return; // 종료
        }

        // Places SDK 초기화
        Places.initializeWithNewPlacesApiEnabled(getContext(), apiKey);

        // PlacesClient 생성
        placesClient = Places.createClient(requireContext());

        placeDetail_view = view.findViewById(R.id.placeDetail_view);

        // 뷰 초기화
        locationWeatherText = view.findViewById(R.id.location);
        weatherIcon = view.findViewById(R.id.imgWeather);

        // 권한 요청
        ActivityCompat.requestPermissions(requireActivity(), new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        }, 1);

        // 첫 데이터 요청
        fetchCurrentLocationAndWeather();

        // 주기적으로 날씨 데이터를 업데이트
        startWeatherUpdates();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Handler 작업 중지
        stopWeatherUpdates();
    }

    @SuppressLint("MissingPermission")
    private void fetchCurrentLocationAndWeather() {
        com.google.android.gms.location.FusedLocationProviderClient locationClient =
                LocationServices.getFusedLocationProviderClient(requireActivity());

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(1000);

        locationClient.requestLocationUpdates(locationRequest, new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult != null && !locationResult.getLocations().isEmpty()) {
                    Location location = locationResult.getLastLocation();

                    if (location != null) {
                        Common common = new Common();
                        android.graphics.Point point = common.dfsXyConv(
                                location.getLatitude(),
                                location.getLongitude()
                        );
                        if (point.x != lastNx || point.y != lastNy) { // 좌표 변경 확인
                            lastNx = point.x;
                            lastNy = point.y;
                            nx = point.x;
                            ny = point.y;

                            Log.d("Converted Point", "nx: " + nx + ", ny: " + ny);
                            updateLocationText(location.getLatitude(), location.getLongitude());
                            fetchWeatherData(); // 좌표가 변경된 경우에만 요청
                            searchNearbyPlaces(37.62938, 127.0815);
                        }
                    }
                } else {
                    Log.e("LocationError", "Location is null or empty");
                }
            }
        }, Looper.getMainLooper());
    }

    private void searchNearbyPlaces(double latitude, double longitude) {
        // 검색 영역을 1000m 반경의 원으로 설정
        LatLng center = new LatLng(latitude, longitude);
        CircularBounds circle = CircularBounds.newInstance(center, 5000);

        // 검색할 장소 유형 설정
        //final List<String> includedTypes = Arrays.asList("restaurant", "cafe", "dog_park", "garden", "park");
        //final List<String> excludedTypes = Arrays.asList("pizza_restaurant", "american_restaurant");

        // SearchNearbyRequest 객체 생성
        final SearchNearbyRequest searchNearbyRequest = SearchNearbyRequest.builder(circle, placeFields)
                //.setIncludedTypes(includedTypes)
                //.setExcludedTypes(excludedTypes)
                .setMaxResultCount(10)
                .build();

        // Nearby 장소 검색
        placesClient.searchNearby(searchNearbyRequest)
                .addOnSuccessListener(response -> {
                    Log.d("Place request 전송", "Place request 전송");
                    List<Place> places = response.getPlaces();
                    // 검색된 장소 리스트 처리
                    Log.d("place response", "" + response.getPlaces().size());
                    for (Place place : places) {
                        Log.d("Place", "Found place: " + place.getDisplayName());
                    }
                    setRecommendedPlaceItem(places);
                })
                .addOnFailureListener(exception -> {
                    Log.e("PlaceSearchError", "Place search failed: " + exception.getMessage());
                });
    }

    private void setRecommendedPlaceItem(List<Place> places) {
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();

        for (Place place : places) {
            card_placeitem card = new card_placeitem();

            Bundle bundle = new Bundle();
            bundle.putString("name", place.getDisplayName());
            card.setArguments(bundle);

            // 카드 프래그먼트를 레이아웃에 추가
            transaction.add(R.id.content_recommended_place, card);
        }

        transaction.commit();
    }

    private void startWeatherUpdates() {
        weatherUpdateTask = new Runnable() {
            @Override
            public void run() {
                if (isAdded()) {
                    fetchWeatherData(); // 날씨 데이터 요청
                    handler.postDelayed(this, REQUEST_INTERVAL); // 일정 시간 후 다시 실행
                }
            }
        };

        handler.post(weatherUpdateTask); // 초기 실행
    }

    private void stopWeatherUpdates() {
        if (weatherUpdateTask != null) {
            handler.removeCallbacks(weatherUpdateTask); // 작업 중지
        }
    }

    private void updateLocationText(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);

                String district = address.getSubLocality();
                if (district == null || district.isEmpty()) {
                    district = address.getLocality();
                }

                locationWeatherText.setText(district != null ? district : "위치 정보를 가져올 수 없습니다.");
            } else {
                locationWeatherText.setText("위치 정보를 가져올 수 없습니다.");
            }
        } catch (IOException e) {
            locationWeatherText.setText("위치 정보 오류");
        }
    }

    private void fetchWeatherData() {
        Call<WEATHER> call = WeatherObject.getRetrofitService().getWeather(
                BuildConfig.API_KEY,
                10,
                1,
                "XML",
                baseDate,
                baseTime,
                nx,
                ny
        );

        call.enqueue(new Callback<WEATHER>() {
            @Override
            public void onResponse(Call<WEATHER> call, Response<WEATHER> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (isAdded()) {
                        parseWeatherData(response.body());
                    }
                } else {
                    Log.e("WeatherResponse", "Response failed: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<WEATHER> call, Throwable t) {
                Log.e("WeatherAPIError", "Request failed: " + t.getMessage());
            }
        });
    }

    private void parseWeatherData(WEATHER weatherData) {
        if (weatherData == null || weatherData.getBody() == null || weatherData.getBody().getItems() == null) {
            if (isAdded()) {
                Toast.makeText(requireContext(), "날씨 데이터를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        String temp = null, sky = null, rainType = null;

        List<ITEM> items = weatherData.getBody().getItems().getItem();
        for (ITEM item : items) {
            if ("T1H".equals(item.getCategory())) temp = item.getFcstValue() + "°C";
            else if ("SKY".equals(item.getCategory())) sky = item.getFcstValue();
            else if ("PTY".equals(item.getCategory())) rainType = item.getFcstValue();
        }

        if (temp != null && sky != null) {
            locationWeatherText.append(" " + temp);
            updateWeatherIcon(rainType, sky);
        }
    }

    private void updateWeatherIcon(String rainType, String sky) {
        boolean isDaytime = isDaytime();
        int iconResId = R.raw.weather_unknown;

        if ("0".equals(rainType)) {
            if ("1".equals(sky)) iconResId = isDaytime ? R.raw.clear_day : R.raw.clear_night;
            else if ("3".equals(sky)) iconResId = isDaytime ? R.raw.cloudy_day : R.raw.cloudy_night;
            else if ("4".equals(sky)) iconResId = R.raw.foggy;
        }

        if (weatherIcon instanceof com.airbnb.lottie.LottieAnimationView) {
            ((com.airbnb.lottie.LottieAnimationView) weatherIcon).setAnimation(iconResId);
            ((com.airbnb.lottie.LottieAnimationView) weatherIcon).playAnimation();
        }
    }

    private boolean isDaytime() {
        int currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        return currentHour >= 6 && currentHour <= 18;
    }
}