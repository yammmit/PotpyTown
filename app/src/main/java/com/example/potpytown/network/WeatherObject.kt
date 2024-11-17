package com.example.potpytown.network

import com.example.potpytown.BuildConfig
import retrofit2.Retrofit
import retrofit2.converter.simplexml.SimpleXmlConverterFactory

object WeatherObject {
    private fun getRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.URL_WEATHER)
            .addConverterFactory(SimpleXmlConverterFactory.createNonStrict()) // XML 파싱을 위한 SimpleXmlConverterFactory 추가
            .build()
    }

    @JvmStatic
    fun getRetrofitService(): WeatherInterface {
        return getRetrofit().create(WeatherInterface::class.java)
    }
}