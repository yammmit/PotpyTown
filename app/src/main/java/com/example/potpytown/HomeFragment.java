package com.example.potpytown;

import android.Manifest;
import android.annotation.SuppressLint;
import android.graphics.Bitmap;
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
import android.widget.ImageButton;
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
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.api.net.SearchNearbyRequest;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    // 이전 위치를 저장하기 위한 변수
    private double lastLatitude = Double.MIN_VALUE; // 이전 위도
    private double lastLongitude = Double.MIN_VALUE; // 이전 경도
    private static final float LOCATION_THRESHOLD = 0.005f; // 위치 변화 허용 오차 (500m 정도)

    private FirebaseAuth mAuth;
    private String userId;
    private ImageButton favoriteButton;
    private FirebaseFirestore db;
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
            Place.Field.PHOTO_METADATAS,
            Place.Field.WEBSITE_URI,
            Place.Field.TYPES
    );

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // FirebaseAuth 인스턴스 초기화
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // 현재 인증된 사용자가 있다면 userId 가져오기
        if (mAuth.getCurrentUser() != null) {
            userId = mAuth.getCurrentUser().getUid();
        } else {
            // 사용자가 인증되지 않은 경우의 처리
            Log.e("HomeFragment", "User is not logged in.");
            // 로그인 화면으로 전환하거나 에러 처리를 추가할 수 있습니다.
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // fragment_home.xml 레이아웃 사용
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // 마지막 위도와 경도 저장
        outState.putDouble("lastLatitude", lastLatitude);
        outState.putDouble("lastLongitude", lastLongitude);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 상태 복원
        if (savedInstanceState != null) {
            lastLatitude = savedInstanceState.getDouble("lastLatitude", Double.MIN_VALUE);
            lastLongitude = savedInstanceState.getDouble("lastLongitude", Double.MIN_VALUE);
        }

        if (userId != null) {
            // Firestore 접근 시 userId를 사용
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("favorite_places")
                    .whereEqualTo("user_id", userId)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        // 성공적으로 즐겨찾기 데이터를 가져왔을 경우 처리
                    })
                    .addOnFailureListener(e -> {
                        Log.e("FirestoreError", "Error getting favorite places: ", e);
                    });
        }

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

        // 뷰 초기화
        locationWeatherText = view.findViewById(R.id.location);
        weatherIcon = view.findViewById(R.id.imgWeather);
        placeDetail_view = view.findViewById(R.id.placeDetail_view);

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
                ImageButton favoriteButton = card.findViewById(R.id.favorite_button);

                placeName.setText(place.getDisplayName());

                // 장소 사진 설정
                if (place.getPhotoMetadatas() != null && !place.getPhotoMetadatas().isEmpty()) {
                    PhotoMetadata photoMetadata = place.getPhotoMetadatas().get(0);
                    FetchPhotoRequest photoRequest = FetchPhotoRequest.builder(photoMetadata)
                            .build();

                    PlacesClient placesClient = PlacesClientManager.getInstance(getContext()).getPlacesClient();
                    placesClient.fetchPhoto(photoRequest).addOnSuccessListener(fetchPhotoResponse -> {
                        Bitmap bitmap = fetchPhotoResponse.getBitmap();
                        backgroundImage.setImageBitmap(bitmap);
                    }).addOnFailureListener(exception -> {
                        Log.e("PlaceImage", "사진을 불러올 수 없습니다.", exception);
                    });
                } else {
                    // 사진이 없을 경우 기본 이미지 설정
                    backgroundImage.setImageResource(R.drawable.img_place_default);
                }

                // Firestore에서 즐겨찾기 상태 확인
                db.collection("favorite_places")
                        .whereEqualTo("user_id", userId)
                        .whereEqualTo("place_id", place.getId())
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            if (!queryDocumentSnapshots.isEmpty()) {
                                favoriteButton.setImageResource(R.drawable.icon_favorite_active); // 채워진 별
                                favoriteButton.setTag("active");
                            } else {
                                favoriteButton.setImageResource(R.drawable.icon_favorite_inactive); // 빈 별
                                favoriteButton.setTag("inactive");
                            }
                        })
                        .addOnFailureListener(e -> {
                            Log.e("Firestore", "Error checking favorite status: " + e.getMessage());
                        });

                // 즐겨찾기 버튼 클릭 리스너 추가
                favoriteButton.setOnClickListener(v -> {
                    String status = (String) favoriteButton.getTag();
                    if ("active".equals(status)) {
                        // 즐겨찾기 삭제 로직
                        db.collection("favorite_places")
                                .whereEqualTo("user_id", userId)
                                .whereEqualTo("place_id", place.getId())
                                .get()
                                .addOnSuccessListener(queryDocumentSnapshots -> {
                                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                                        db.collection("favorite_places").document(document.getId()).delete()
                                                .addOnSuccessListener(aVoid -> {
                                                    favoriteButton.setImageResource(R.drawable.icon_favorite_inactive);
                                                    favoriteButton.setTag("inactive");
                                                    Log.d("Firestore", "Favorite removed");
                                                });
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("Firestore", "Error removing favorite: " + e.getMessage());
                                });
                    } else {
                        // 즐겨찾기 추가 로직
                        Map<String, Object> favoritePlace = new HashMap<>();
                        favoritePlace.put("user_id", userId);
                        favoritePlace.put("place_id", place.getId());
                        favoritePlace.put("place_name", place.getDisplayName());

                        db.collection("favorite_places").add(favoritePlace)
                                .addOnSuccessListener(documentReference -> {
                                    favoriteButton.setImageResource(R.drawable.icon_favorite_active);
                                    favoriteButton.setTag("active");
                                    Log.d("Firestore", "Favorite added with ID: " + documentReference.getId());
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("Firestore", "Error adding favorite: " + e.getMessage());
                                });
                    }
                });

                // 카드 클릭 이벤트 처리
                card.setOnClickListener(v -> {
                    // 클릭한 장소 데이터를 DetailFragment로 전달
                    FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();

                    // 화면 전환 애니메이션 추가
                    transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);

                    // HomeFragment 위에 DetailPlaceFragment 추가
                    DetailPlaceFragment detailFragment = DetailPlaceFragment.newInstance(place);
                    transaction.add(R.id.fragment_container, detailFragment);
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