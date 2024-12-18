package com.example.potpytown.network;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface PlacesApiService {
    @GET("maps/api/place/autocomplete/json")
    Call<AutocompleteResponse> getAutocomplete(
            @Query("input") String input,
            @Query("key") String apiKey
    );
}
