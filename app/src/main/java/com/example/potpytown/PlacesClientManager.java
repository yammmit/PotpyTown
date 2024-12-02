package com.example.potpytown;

import static com.example.potpytown.BuildConfig.PLACES_API_KEY;

import android.content.Context;

import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;

public class PlacesClientManager {

    private static PlacesClientManager instance;
    private PlacesClient placesClient;

    private PlacesClientManager(Context context) {
        // Places API 초기화
        if (!Places.isInitialized()) {
            Places.initialize(context.getApplicationContext(), PLACES_API_KEY);
        }
        placesClient = Places.createClient(context);
    }

    public static synchronized PlacesClientManager getInstance(Context context) {
        if (instance == null) {
            instance = new PlacesClientManager(context);
        }
        return instance;
    }

    public PlacesClient getPlacesClient() {
        return placesClient;
    }
}
