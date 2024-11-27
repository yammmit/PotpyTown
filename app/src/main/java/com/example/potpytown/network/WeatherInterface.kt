package com.example.potpytown.network

import com.example.potpytown.data.WEATHER
import com.example.potpytown.BuildConfig

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

// 날씨 데이터를 가져오는 인터페이스
interface WeatherInterface {
    @GET("getVilageFcst")
    fun getWeather(
        @Query("serviceKey") serviceKey: String, // 인증키
        @Query("numOfRows") numOfRows: Int,     // 한 페이지 결과 수
        @Query("pageNo") pageNo: Int,          // 페이지 번호
        @Query("dataType") dataType: String,   // 응답 자료 형식
        @Query("base_date") baseDate: String,  // 발표 일자
        @Query("base_time") baseTime: String,  // 발표 시각
        @Query("nx") nx: Int,                  // 예보 지점 X 좌표
        @Query("ny") ny: Int                   // 예보 지점 Y 좌표
    ): Call<WEATHER>
}