package com.example.potpytown;

import android.Manifest;
import android.annotation.SuppressLint;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity {
    private TextView locationWeatherText;
    private ImageView weatherIcon;

    private String baseDate, baseTime;
    private int nx, ny; // 격자 좌표

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        locationWeatherText = findViewById(R.id.location);
        weatherIcon = findViewById(R.id.imgWeather);

        // 권한 요청
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        }, 1);

        // GPS와 날씨 API 호출
        fetchCurrentLocationAndWeather();
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
                    Log.d("GPS", "Latitude: " + location.getLatitude() + ", Longitude: " + location.getLongitude()); // GPS 데이터 로그

                    // 위경도를 격자 좌표로 변환
                    Common common = new Common();
                    android.graphics.Point point = common.dfsXyConv(location.getLatitude(), location.getLongitude());
                    nx = point.x;
                    ny = point.y;
                    Log.d("Converted Point", "nx: " + nx + ", ny: " + ny); // 변환된 격자 좌표 로그

                    // 위치 정보 표시
                    updateLocationText(location.getLatitude(), location.getLongitude());

                    // 날씨 데이터 가져오기
                    fetchWeatherData(); // 데이터 가져온 후 UI 업데이트
                }
            }
        }, Looper.getMainLooper());
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

                locationWeatherText.setText(district != null ? district : "위치 정보를 가져올 수 없습니다.");
            } else {
                locationWeatherText.setText("위치 정보를 가져올 수 없습니다.");
            }
        } catch (IOException e) {
            e.printStackTrace();
            locationWeatherText.setText("위치 정보 오류");
        }
    }


    private void fetchWeatherData() {
        Calendar cal = Calendar.getInstance();
        baseDate = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(cal.getTime());
        String hour = new SimpleDateFormat("HH", Locale.getDefault()).format(cal.getTime());
        String minute = new SimpleDateFormat("mm", Locale.getDefault()).format(cal.getTime());
        Common common = new Common();
        baseTime = common.getBaseTime(hour, minute);

        Log.d("WeatherRequest", "baseDate: " + baseDate + ", baseTime: " + baseTime); // API 요청 정보 로그

        // Retrofit API 호출
        Call<WEATHER> call = WeatherObject.getRetrofitService().getWeather(BuildConfig.API_KEY,60, 1, "JSON", baseDate, baseTime, nx, ny);
        call.enqueue(new Callback<WEATHER>() {
            @Override
            public void onResponse(Call<WEATHER> call, Response<WEATHER> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("WeatherResponse", "Response received successfully"); // 응답 성공 로그
                    parseWeatherData(response.body());
                } else {
                    Toast.makeText(HomeActivity.this, "날씨 데이터를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show();
                    Log.e("WeatherResponse", "Response failed: " + response.message()); // 응답 실패 로그
                }
            }

            @Override
            public void onFailure(Call<WEATHER> call, Throwable t) {
                Toast.makeText(HomeActivity.this, "API 요청 실패: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("WeatherAPIError", t.getMessage()); // API 요청 실패 로그
            }
        });
    }

    private void parseWeatherData(WEATHER weatherData) {
        String temp = null, sky = null, rainType = null;

        for (ITEM item : weatherData.getResponse().getBody().getItems().getItem()) {
            switch (item.getCategory()) {
                case "T1H": // 기온
                    temp = item.getFcstValue() + "°C";
                    Log.d("WeatherData", "Temperature: " + temp); // 기온 데이터 로그
                    break;
                case "SKY": // 하늘 상태
                    sky = item.getFcstValue();
                    Log.d("WeatherData", "Sky: " + sky); // 하늘 상태 로그
                    break;
                case "PTY": // 강수 형태
                    rainType = item.getFcstValue();
                    Log.d("WeatherData", "Rain Type: " + rainType); // 강수 형태 로그
                    break;
            }
        }

        if (temp != null && sky != null) {
            locationWeatherText.append(" " + temp); // 위치와 기온 표시
            updateWeatherIcon(rainType, sky);
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