package com.example.potpytown.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.example.potpytown.BuildConfig

object WeatherObject {
    private fun getRetrofit(): Retrofit{
        return Retrofit.Builder()
            .baseUrl(BuildConfig.URL_WEATHER)
            .addConverterFactory(GsonConverterFactory.create()) // Json데이터를 사용자가 정의한 Java 객채로 변환해주는 라이브러리
            .build()
    }
    @JvmStatic
    fun getRetrofitService(): WeatherInterface{
        return  getRetrofit().create(WeatherInterface::class.java) //retrofit객체 만듦
    }


}