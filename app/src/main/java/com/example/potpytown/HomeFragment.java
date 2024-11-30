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
import android.widget.ImageView;
import android.widget.LinearLayout;
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

    // 이전 위치를 저장하기 위한 변수
    private double lastLatitude = Double.MIN_VALUE; // 이전 위도
    private double lastLongitude = Double.MIN_VALUE; // 이전 경도
    private static final float LOCATION_THRESHOLD = 0.005f; // 위치 변화 허용 오차 (500m 정도)

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
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();

                        // 위치가 변경되었는지 확인
                        if (isLocationChanged(latitude, longitude)) {
                            lastLatitude = latitude;
                            lastLongitude = longitude;

                            // Google Places API 요청
                            searchNearbyPlaces(latitude, longitude);

                            // 기상청 좌표 변환 및 날씨 API 호출
                            Common common = new Common();
                            android.graphics.Point point = common.dfsXyConv(latitude, longitude);

                            if (point.x != lastNx || point.y != lastNy) { // 기상청 좌표 변경 확인
                                lastNx = point.x;
                                lastNy = point.y;
                                nx = point.x;
                                ny = point.y;

                                Log.d("Converted Point", "nx: " + nx + ", ny: " + ny);
                                updateLocationText(latitude, longitude);
                                fetchWeatherData(); // 좌표가 변경된 경우에만 요청
                            }
                        }
                    }
                } else {
                    Log.e("LocationError", "Location is null or empty");
                }
            }
        }, Looper.getMainLooper());
    }

    private boolean isLocationChanged(double latitude, double longitude) {
        float[] results = new float[1];
        Location.distanceBetween(lastLatitude, lastLongitude, latitude, longitude, results);
        return results[0] > LOCATION_THRESHOLD; // 위치 변화가 설정한 허용 오차를 넘었는지 확인
    }

    private void searchNearbyPlaces(double latitude, double longitude) { // 구글맵 주변 검색
        LatLng center = new LatLng(latitude, longitude);
        CircularBounds circle = CircularBounds.newInstance(center, 5000);

        // 검색할 장소 유형 설정
        final List<String> includedTypes = Arrays.asList("restaurant", "cafe", "dog_park", "garden", "park");
        //final List<String> excludedTypes = Arrays.asList("pizza_restaurant", "american_restaurant");

        // SearchNearbyRequest 객체 생성
        final SearchNearbyRequest searchNearbyRequest = SearchNearbyRequest.builder(circle, placeFields)
                .setIncludedTypes(includedTypes)
                //.setExcludedTypes(excludedTypes)
                .setMaxResultCount(20)
                .build();

        // Nearby 장소 검색
        placesClient.searchNearby(searchNearbyRequest)
                .addOnSuccessListener(response -> {
                    Log.d("Place request 전송", "Place request 전송");
                    List<Place> places = response.getPlaces();
                    // 검색된 장소 리스트 처리
                    Log.d("place response", "" + places.size());
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
        LinearLayout container = getView().findViewById(R.id.content_recommended_place);

        // 기존에 추가된 뷰 제거
        container.removeAllViews();

        // 장소 카드 동적 생성 (반려견 동반 가능 장소만 추가)
        for (Place place : places) {
            Place.BooleanPlaceAttributeValue allowsDogs = place.getAllowsDogs();

            // 반려견 동반 가능한 장소만 처리
            if (allowsDogs != null && allowsDogs == Place.BooleanPlaceAttributeValue.TRUE) {
                View card = LayoutInflater.from(getContext()).inflate(R.layout.card_placeitem, container, false);

                // 카드 내용 설정
                TextView placeName = card.findViewById(R.id.txt_placename);
                ImageView backgroundImage = card.findViewById(R.id.img_place);

                placeName.setText(place.getDisplayName());

                // 클릭 이벤트 처리
                card.setOnClickListener(v -> {
                    // 클릭한 장소 데이터를 DetailFragment로 전달
                    FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
                    DetailPlaceFragment detailFragment = DetailPlaceFragment.newInstance(place);
                    transaction.replace(R.id.fragment_container, detailFragment);
                    transaction.addToBackStack(null); // 백스택 추가
                    transaction.commit();
                });

                // LinearLayout에 카드 추가
                container.addView(card);
            }
        }
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