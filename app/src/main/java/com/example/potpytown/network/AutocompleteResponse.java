package com.example.potpytown.network;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class AutocompleteResponse {
    @SerializedName("predictions")
    public List<Prediction> predictions;

    public static class Prediction {
        @SerializedName("description")
        public String description;  // 예측된 장소 설명
    }
}