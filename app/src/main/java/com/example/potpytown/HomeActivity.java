package com.example.potpytown;

import android.Manifest;
import android.annotation.SuppressLint;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.example.potpytown.component.Common;
import com.example.potpytown.data.ITEM;
import com.example.potpytown.data.WEATHER;
import com.example.potpytown.network.WeatherObject;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends BaseActivity { // BaseActivity 상속

    private TextView locationWeatherText;
    private com.airbnb.lottie.LottieAnimationView weatherIcon;

    private String baseDate, baseTime;
    private int nx, ny; // 격자 좌표
    private Handler handler = new Handler();
    private Runnable weatherUpdateTask;
    private static final long REQUEST_INTERVAL = 15 * 60 * 1000; // 15분 (밀리초 단위)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // 뷰 초기화
        locationWeatherText = findViewById(R.id.location);
        weatherIcon = findViewById(R.id.imgWeather);

        // 권한 요청
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        }, 1);

        // 첫 데이터 요청
        fetchCurrentLocationAndWeather();

        // 주기적으로 날씨 데이터를 업데이트
        startWeatherUpdates();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Handler 작업 중지
        stopWeatherUpdates();
    }

    // BaseActivity에서 사용할 레이아웃 리소스 ID를 제공
    @Override
    protected int getLayoutResId() {
        return R.layout.activity_home;
    }

    @SuppressLint("MissingPermission")
    private void fetchCurrentLocationAndWeather() {
        com.google.android.gms.location.FusedLocationProviderClient locationClient = LocationServices.getFusedLocationProviderClient(this);

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
                        android.graphics.Point point = common.dfsXyConv(location.getLatitude(), location.getLongitude());
                        nx = point.x;
                        ny = point.y;

                        Log.d("Converted Point", "nx: " + nx + ", ny: " + ny);
                        updateLocationText(location.getLatitude(), location.getLongitude());

                        // 날씨 데이터 가져오기
                        fetchWeatherData();
                    } else {
                        Log.e("LocationError", "Location is null");
                        nx = 60; // 기본 좌표 (서울)
                        ny = 127; // 기본 좌표 (서울)
                        fetchWeatherData(); // 기본 값으로 요청
                    }
                }
            }
        }, Looper.getMainLooper());
    }

    private void startWeatherUpdates() {
        weatherUpdateTask = new Runnable() {
            @Override
            public void run() {
                fetchWeatherData(); // 날씨 데이터 요청
                handler.postDelayed(this, REQUEST_INTERVAL); // 일정 시간 후 다시 실행
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
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);

                // 동 단위를 우선으로 표시, 없으면 상위 단위 표시
                String district = address.getSubLocality(); // 동 단위
                if (district == null || district.isEmpty()) {
                    district = address.getLocality(); // 구 단위
                }

                // 로그 추가: 확인용
                Log.d("LocationDebug", "Address: " + address.toString());
                Log.d("LocationDebug", "District: " + (district != null ? district : "Unknown"));

                locationWeatherText.setText(district != null ? district : "위치 정보를 가져올 수 없습니다.");
            } else {
                Log.e("LocationDebug", "Geocoder returned no results.");
                locationWeatherText.setText("위치 정보를 가져올 수 없습니다.");
            }
        } catch (IOException e) {
            Log.e("GeocoderError", "Failed to fetch location", e);
            locationWeatherText.setText("위치 정보 오류");
        } catch (IllegalArgumentException e) {
            Log.e("GeocoderError", "Invalid latitude or longitude provided", e);
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
                    parseWeatherData(response.body());
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
        // Null 체크
        if (weatherData == null ||
                weatherData.getBody() == null ||
                weatherData.getBody().getItems() == null ||
                weatherData.getBody().getItems().getItem() == null) {
            Log.e("WeatherDataError", "Response, Body, or Items is null");
            Toast.makeText(this, "날씨 데이터를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        String temp = null, sky = null, rainType = null;

        // 아이템 리스트 가져오기
        List<ITEM> items = weatherData.getBody().getItems().getItem();
        for (ITEM item : items) {
            if (item.getCategory() == null || item.getFcstValue() == null) {
                Log.w("WeatherDataWarning", "Category or FcstValue is null. Skipping...");
                continue;
            }

            switch (item.getCategory()) {
                case "T1H": // 기온
                    temp = item.getFcstValue() + "°C";
                    Log.d("WeatherData", "Temperature: " + temp);
                    break;
                case "SKY": // 하늘 상태
                    sky = item.getFcstValue();
                    Log.d("WeatherData", "Sky: " + sky);
                    break;
                case "PTY": // 강수 형태
                    rainType = item.getFcstValue();
                    Log.d("WeatherData", "Rain Type: " + rainType);
                    break;
            }
        }

        // UI 업데이트
        if (temp != null && sky != null) {
            locationWeatherText.append(" " + temp);
            updateWeatherIcon(rainType, sky);
        } else {
            Log.e("WeatherDataError", "Temperature or Sky data is missing");
            Toast.makeText(this, "날씨 정보를 표시할 수 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("ResourceType")
    private void updateWeatherIcon(String rainType, String sky) {
        boolean isDaytime = isDaytime();
        int iconResId = R.raw.weather_unknown;

        if ("0".equals(rainType)) { // 강수 없음
            if ("1".equals(sky)) {
                iconResId = isDaytime ? R.raw.clear_day : R.raw.clear_night;
            } else if ("3".equals(sky)) {
                iconResId = isDaytime ? R.raw.cloudy_day : R.raw.cloudy_night;
            } else if ("4".equals(sky)) {
                iconResId = R.raw.foggy;
            }
        } else if ("1".equals(rainType) || "4".equals(rainType)) {
            iconResId = isDaytime ? R.raw.rain_day : R.raw.rain_night;
        } else if ("3".equals(rainType)) {
            iconResId = R.raw.snow;
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