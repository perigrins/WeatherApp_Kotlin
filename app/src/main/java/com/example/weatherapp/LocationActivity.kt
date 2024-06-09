package com.example.weatherapp

import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.media.Image
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.weatherapp.Model.CurrentResponseApi
import com.example.weatherapp.Model.ForecastResponseApi
import com.example.weatherapp.ViewModel.WeatherViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class LocationActivity : AppCompatActivity(){

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val LOCATION_PERMISSION_REQUEST_CODE = 1
    private lateinit var editCity : EditText
    private val forecastViewModel : WeatherViewModel by viewModels()

    private lateinit var forecast1 : TextView
    private lateinit var forecast2 : TextView
    private lateinit var forecast3 : TextView
    private lateinit var forecast4 : TextView
    private lateinit var forecast5 : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.loc_activity)

        // hiding the status bar
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        actionBar?.hide()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        checkLocationPermission()

        val btn3 = findViewById<Button>(R.id.buttonAddons)
        editCity = findViewById(R.id.editTextLoc)
        val btnF = findViewById<Button>(R.id.buttonForecast)
        val imgBtnLoc = findViewById<ImageButton>(R.id.imageButtonLocationForecast)

        forecast1 = findViewById((R.id.textViewForecast1))
        forecast2 = findViewById((R.id.textViewForecast2))
        forecast3 = findViewById((R.id.textViewForecast3))
        forecast4 = findViewById((R.id.textViewForecast4))
        forecast5 = findViewById((R.id.textViewForecast5))

        btn3.setOnClickListener {
            val intent3 = Intent(this, AddOns::class.java)
            startActivity(intent3)
        }

        btnF.setOnClickListener {
            val loc = editCity.text.toString()
            forecastFun(loc)
        }

        editCity.onFocusChangeListener = View.OnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                editCity.text.clear()
            }
        }

        imgBtnLoc.setOnClickListener {
            getLastLocation()
        }
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED) {ActivityCompat.requestPermissions(this,
            arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        } else {
            getLastLocation()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                getLastLocation()
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getLastLocation() {
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location : Location? ->
                if (location != null) {
                    val lat = location.latitude
                    val lon = location.longitude
                    getCityName(lat, lon)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to get location", Toast.LENGTH_SHORT).show()
            }
    }

    private fun getCityName(latitude: Double, longitude: Double) {
        val geocoder = Geocoder(this, Locale.getDefault())
        try {
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                val cityName = addresses[0].locality
                if (cityName != null) {
                    editCity.setText("$cityName")
                    forecastFun(cityName)
                } else {
                    Toast.makeText(this, "City name not found", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error getting city name", Toast.LENGTH_SHORT).show()
        }
    }

    fun displayMaps(view: View) {
        val loc = findViewById<EditText>(R.id.editTextLoc)
        val location = loc.text.toString()
        val geocoder = Geocoder(this)
        CoroutineScope(Dispatchers.Main).launch{
            val addressList = geocoder.getFromLocationName(location, 1)
            if (!addressList.isNullOrEmpty()) {
                val address = addressList[0]
                val lat = address.latitude
                val lon = address.longitude
                val u: Uri = Uri.parse("geo:$lat,$lon?q=$lat,$lon")
                val intent = Intent(Intent.ACTION_VIEW, u)
                if (intent.resolveActivity(packageManager) != null) {
                    startActivity(intent)
                }
            }
        }
    }

    private fun forecastFun(location: String){
        forecastViewModel.loadForecast(location, "metric").enqueue(object:
            Callback<ForecastResponseApi> {

            override fun onResponse(
                call: Call<ForecastResponseApi>,
                response: Response<ForecastResponseApi>
            ) {
                if (response.isSuccessful) {
                    val data = response.body()
                    if (data != null) {
                        Log.d("WeatherApp", "Forecast data: $data")

                        val forecastList = data.list
                        if (!forecastList.isNullOrEmpty()) {
                            // forecast every 12h
                            val f1 = forecastList[1]
                            val f2 = forecastList[5]
                            val f3 = forecastList[9]
                            val f4 = forecastList[13]
                            val f5 = forecastList[17]

                            forecast1.text=""
                            forecast2.text=""
                            forecast3.text=""
                            forecast4.text=""
                            forecast5.text=""

                            // date
                            val formattedDate1 = formatDateTime(f1.dt ?: 0L)
                            val formattedDate2 = formatDateTime(f2.dt ?: 0L)
                            val formattedDate3 = formatDateTime(f3.dt ?: 0L)
                            val formattedDate4 = formatDateTime(f4.dt ?: 0L)
                            val formattedDate5 = formatDateTime(f5.dt ?: 0L)
                            forecast1.append(formattedDate1+"\n")
                            forecast2.append(formattedDate2+"\n")
                            forecast3.append(formattedDate3+"\n")
                            forecast4.append(formattedDate4+"\n")
                            forecast5.append(formattedDate5+"\n")

                            // description
                            val desc1 = f1.weather?.firstOrNull()?.description.let {
                                "$it"
                            }
                            forecast1.append(desc1+"\n")
                            val desc2 = f2.weather?.firstOrNull()?.description.let {
                                "$it"
                            }
                            forecast2.append(desc2+"\n")
                            val desc3 = f3.weather?.firstOrNull()?.description.let {
                                "$it"
                            }
                            forecast3.append(desc3+"\n")
                            val desc4 = f4.weather?.firstOrNull()?.description.let {
                                "$it"
                            }
                            forecast4.append(desc4+"\n")
                            val desc5 = f5.weather?.firstOrNull()?.description.let {
                                "$it"
                            }
                            forecast5.append(desc5+"\n")

                            // temperature
                            val temp1 = f1.main?.temp?.let {
                                "${Math.round(it)} °C"
                            } ?: "N/A"
                            forecast1.append(temp1+", ")
                            val temp2 = f2.main?.temp?.let {
                                "${Math.round(it)} °C"
                            } ?: "N/A"
                            forecast2.append(temp2+", ")
                            val temp3 = f3.main?.temp?.let {
                                "${Math.round(it)} °C"
                            } ?: "N/A"
                            forecast3.append(temp3+", ")
                            val temp4 = f4.main?.temp?.let {
                                "${Math.round(it)} °C"
                            } ?: "N/A"
                            forecast4.append(temp4+", ")
                            val temp5 = f5.main?.temp?.let {
                                "${Math.round(it)} °C"
                            } ?: "N/A"
                            forecast5.append(temp5+", ")

                            // feels like
                            val feelsLike1 = f1.main?.feelsLike?.let {
                                "Feels like: ${Math.round(it)} °C"
                            } ?: "N/A"
                            forecast1.append(feelsLike1)
                            val feelsLike2 = f2.main?.feelsLike?.let {
                                "Feels like: ${Math.round(it)} °C"
                            } ?: "N/A"
                            forecast2.append(feelsLike2)
                            val feelsLike3 = f3.main?.feelsLike?.let {
                                "Feels like: ${Math.round(it)} °C"
                            } ?: "N/A"
                            forecast3.append(feelsLike3)
                            val feelsLike4 = f4.main?.feelsLike?.let {
                                "Feels like: ${Math.round(it)} °C"
                            } ?: "N/A"
                            forecast4.append(feelsLike4)
                            val feelsLike5 = f5.main?.feelsLike?.let {
                                "Feels like: ${Math.round(it)} °C"
                            } ?: "N/A"
                            forecast5.append(feelsLike5)
                        }
                    } else {
                        Toast.makeText(this@LocationActivity, "no data available", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e("WeatherApp", "response not successful: ${response.errorBody()?.string()}")
                    Toast.makeText(this@LocationActivity, "response not successful", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ForecastResponseApi>, t: Throwable) {
                Toast.makeText(this@LocationActivity, t.toString(), Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun formatDateTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp * 1000
        return sdf.format(calendar.time)
    }

}