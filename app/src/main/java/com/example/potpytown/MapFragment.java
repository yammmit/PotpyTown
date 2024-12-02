package com.example.potpytown;

import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapFragment extends Fragment implements OnMapReadyCallback {
    private String placeName;

    public static MapFragment newInstance(String placeName) {
        MapFragment fragment = new MapFragment();
        Bundle args = new Bundle();
        args.putString("placeName", placeName);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            placeName = getArguments().getString("placeName");
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        if (placeName != null && !placeName.isEmpty()) {
            // Geocoding 비동기 작업으로 실행
            new GeocodeTask(googleMap).execute(placeName);
        } else {
            Log.e("MapFragment", "유효한 장소 이름이 없습니다.");
        }
    }

    // Geocoding을 비동기 작업으로 처리하는 AsyncTask 클래스
    private class GeocodeTask extends AsyncTask<String, Void, LatLng> {
        private GoogleMap googleMap;

        public GeocodeTask(GoogleMap googleMap) {
            this.googleMap = googleMap;
        }

        @Override
        protected LatLng doInBackground(String... params) {
            String locationName = params[0];
            Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
            try {
                List<Address> addressList = geocoder.getFromLocationName(locationName, 1);
                if (addressList != null && !addressList.isEmpty()) {
                    Address address = addressList.get(0);
                    return new LatLng(address.getLatitude(), address.getLongitude());
                }
            } catch (IOException e) {
                Log.e("MapFragment", "Geocoding 실패: ", e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(LatLng latLng) {
            if (latLng != null) {
                // 마커 추가 및 카메라 이동
                googleMap.addMarker(new MarkerOptions().position(latLng).title(placeName));
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
            } else {
                Log.e("MapFragment", "해당 이름으로 장소를 찾을 수 없습니다.");
            }
        }
    }
}
