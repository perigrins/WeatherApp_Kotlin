package com.example.weatherapp.ViewModel

import androidx.lifecycle.ViewModel
import com.example.weatherapp.Repository.WeatherRepository
import com.example.weatherapp.Server.ApiClient
import com.example.weatherapp.Server.ApiServices

class WeatherViewModel(val repository : WeatherRepository): ViewModel() {
    constructor():this(WeatherRepository(ApiClient().getClient().create(ApiServices::class.java)))

    fun loadCurrentWeather(city: String, unit: String) =
        repository.getCurrentWeather(city, unit)

    fun loadForecast(city: String, unit: String) =
        repository.getForecast(city, unit)

}