package com.example.weatherapp.Repository

import com.example.weatherapp.Model.CurrentResponseApi
import com.example.weatherapp.Model.ForecastResponseApi
import com.example.weatherapp.Server.ApiServices
import retrofit2.Call

class WeatherRepository(private val api : ApiServices) {
    fun getCurrentWeather(city: String, unit: String): Call<CurrentResponseApi> {
        return api.getCurrentWeather(city, unit, "a36c4942dc1564603b3dfbe5ccd2205d")
    }

    fun getForecast(city: String, unit: String): Call<ForecastResponseApi> {
        return api.getForecast(city, unit, "a36c4942dc1564603b3dfbe5ccd2205d")
    }
}