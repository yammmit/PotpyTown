package com.example.potpytown;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class WebViewMapFragment extends Fragment {
    private String placeName;

    public static WebViewMapFragment newInstance(String placeName) {
        WebViewMapFragment fragment = new WebViewMapFragment();
        Bundle args = new Bundle();
        args.putString("placeName", placeName);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_webview_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            placeName = getArguments().getString("placeName");
        }

        WebView webView = view.findViewById(R.id.webview_map);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true); // JavaScript 사용 허용

        webView.setWebViewClient(new WebViewClient()); // WebView 내에서 링크 클릭 시 새 창이 아닌 WebView 내에서 열리도록 설정

        // 장소 이름을 활용하여 지도를 렌더링
        String html = "<html>" +
                "<head>" +
                "<script src=\"https://maps.googleapis.com/maps/api/js?key=AIzaSyCxap1ebQfrcukTLvWr3OmYxIliHjHI3aU\"></script>" +
                "<script>" +
                "function initialize() {" +
                "  var geocoder = new google.maps.Geocoder();" +
                "  var mapOptions = {" +
                "    zoom: 15" +
                "  };" +
                "  var map = new google.maps.Map(document.getElementById('map'), mapOptions);" +
                "  geocoder.geocode({'address': '" + placeName + "'}, function(results, status) {" +
                "    if (status === 'OK') {" +
                "      map.setCenter(results[0].geometry.location);" +
                "      var marker = new google.maps.Marker({" +
                "        map: map," +
                "        position: results[0].geometry.location" +
                "      });" +
                "    } else {" +
                "      alert('Geocode was not successful for the following reason: ' + status);" +
                "    }" +
                "  });" +
                "}" +
                "</script>" +
                "</head>" +
                "<body onload='initialize()'>" +
                "<div id='map' style='width:100%; height:100%;'></div>" +
                "</body>" +
                "</html>";

        webView.loadData(html, "text/html", "UTF-8");
    }
}