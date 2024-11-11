package com.example.potpytown.network

import com.example.potpytown.data.WEATHER
import com.example.potpytown.BuildConfig

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

// 날씨 데이터를 가져오는 인터페이스
interface WeatherInterface {
    // 초단기 예보 조회
    @GET("VilageFcstInfoService_2.0/getUltraSrtFcst") // API 엔드포인트
    fun getWeather(
        @Query("serviceKey") serviceKey: String = BuildConfig.API_KEY, // API 키
        @Query("numOfRows") num_of_rows: Int,   // 한 페이지 경과 수
        @Query("pageNo") page_no: Int,          // 페이지 번호
        @Query("dataType") data_type: String,   // 응답 자료 형식 (JSON/XML)
        @Query("base_date") base_date: String,  // 발표 일자
        @Query("base_time") base_time: String,  // 발표 시각
        @Query("nx") nx: Int,                   // 예보지점 X 좌표
        @Query("ny") ny: Int                    // 예보지점 Y 좌표
    ): Call<WEATHER>
}
