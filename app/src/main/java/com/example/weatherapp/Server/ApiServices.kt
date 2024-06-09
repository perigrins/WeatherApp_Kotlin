package com.example.weatherapp.Server

import com.example.weatherapp.Model.CurrentResponseApi
import com.example.weatherapp.Model.ForecastResponseApi
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiServices {
    @GET("data/2.5/weather")
    fun getCurrentWeather(
        @Query("q") cityName:String,
        @Query("units") units:String,
        @Query("APPID") ApiKey:String
    ): Call<CurrentResponseApi>

    @GET("data/2.5/forecast")
    fun getForecast(
        @Query("q") cityName:String,
        @Query("units") units:String,
        @Query("APPID") ApiKey:String
    ): Call<ForecastResponseApi>
}