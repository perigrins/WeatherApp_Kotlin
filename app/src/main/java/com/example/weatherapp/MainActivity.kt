package com.example.weatherapp

import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationRequest
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.weatherapp.Model.CurrentResponseApi
import com.example.weatherapp.ViewModel.WeatherViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationServices
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var city: EditText
    private lateinit var btn: Button
    private lateinit var resultData: TextView
    private lateinit var checkHum: RadioButton
    private lateinit var checkPr: RadioButton
    private lateinit var checkTmin: RadioButton
    private lateinit var checkTmax: RadioButton
    private lateinit var currentDesc : TextView
    private lateinit var temp : TextView
    private lateinit var feelslike : TextView
    private lateinit var buttonNewAct : Button
    private lateinit var imgbtn : ImageButton

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val LOCATION_PERMISSION_REQUEST_CODE = 1

    private val weatherViewModel : WeatherViewModel by viewModels()

    var HUM : Boolean = false
    var PR : Boolean = false
    var TMAX : Boolean = false
    var TMIN : Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        /*ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }*/

        // hiding the status bar
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        actionBar?.hide()

        // location
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        checkLocationPermission()

        city = findViewById(R.id.editTextCity)
        btn = findViewById(R.id.buttonGetWeather)
        resultData = findViewById(R.id.textViewData)
        checkHum = findViewById((R.id.radioButtonHumidity))
        checkPr = findViewById(R.id.radioButtonPressure)
        checkTmin = findViewById((R.id.radioButtonTempMin))
        checkTmax = findViewById((R.id.radioButtonTempMax))
        currentDesc = findViewById(R.id.textViewDesc)
        temp = findViewById(R.id.textViewTemp)
        buttonNewAct = findViewById(R.id.buttonLoc)
        feelslike = findViewById(R.id.textViewFL)
        imgbtn = findViewById(R.id.imageButtonLocation)

        btn.setOnClickListener{
            val location = city.text.toString()
            weatherFun(location)
        }

        buttonNewAct.setOnClickListener {
            val intent = Intent(this, LocationActivity::class.java)
            startActivity(intent)
        }

        imgbtn.setOnClickListener {
            getLastLocation()
        }

        checkHum.setOnClickListener {
            if(HUM){
                checkHum.isChecked = false
                HUM = false
            }
            else{
                checkHum.isChecked =true
                HUM = true
            }
        }

        checkPr.setOnClickListener {
            if(PR){
                checkPr.isChecked = false
                PR = false
            }
            else{
                checkPr.isChecked =true
                PR = true
            }
        }

        checkTmin.setOnClickListener {
            if(TMIN){
                checkTmin.isChecked = false
                TMIN = false
            }
            else{
                checkTmin.isChecked =true
                TMIN = true
            }
        }

        checkTmax.setOnClickListener {
            if(TMAX){
                checkTmax.isChecked = false
                TMAX = false
            }
            else{
                checkTmax.isChecked =true
                TMAX = true
            }
        }

        city.onFocusChangeListener = View.OnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                city.text.clear()
            }
        }
    }

    // location request
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
                    weatherFun(cityName)
                    city.setText("$cityName")
                } else {
                    Toast.makeText(this, "City name not found", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error getting city name", Toast.LENGTH_SHORT).show()
        }
    }

    private fun weatherFun(location: String){
        weatherViewModel.loadCurrentWeather(location, "metric").enqueue(object:
            Callback<CurrentResponseApi>{
            override fun onResponse(
                call: Call<CurrentResponseApi>,
                response: Response<CurrentResponseApi>
            ) {
                if (response.isSuccessful) {
                    val data = response.body()
                    if (data != null) {
                        Log.d("WeatherApp", "Weather data: $data")

                        val tempText = data.main?.temp?.let {
                            "${Math.round(it)} °C"
                        } ?: "N/A"
                        temp.text = tempText

                        val descText = data.weather?.firstOrNull()?.description.let{
                            "$it"
                        }
                        currentDesc.text = descText

                        val feelsLikeText = data.main?.feelsLike?.let{
                            "Feels like: ${Math.round(it)} °C"
                        } ?: "N/A"
                        feelslike.text = feelsLikeText

                        val humidityText = data.main?.humidity?.let {
                            "$it %"
                        } ?: "N/A"

                        val pressureText = data.main?.pressure?.let {
                            "$it hPa"
                        } ?: "N/A"

                        val tempMinText = data.main?.tempMin?.let {
                            "${Math.round(it)} °C"
                        } ?: "N/A"

                        val tempMaxText = data.main?.tempMax?.let {
                            "${Math.round(it)} °C"
                        } ?: "N/A"

                        resultData.text = ""
                        if (checkHum.isChecked) {
                            HUM = true
                            resultData.append("Humidity: $humidityText\n")
                        }
                        if (checkPr.isChecked) {
                            PR = true
                            resultData.append("Pressure: $pressureText\n")
                        }
                        if (checkTmin.isChecked) {
                            TMIN = true
                            resultData.append("Temp Min: $tempMinText\n")
                        }
                        if (checkTmax.isChecked) {
                            TMAX = true
                            resultData.append("Temp Max: $tempMaxText\n")
                        }
                    } else {
                        Toast.makeText(this@MainActivity, "no data available", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e("WeatherApp", "response not successful: ${response.errorBody()?.string()}")
                    Toast.makeText(this@MainActivity, "response not successful", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<CurrentResponseApi>, t: Throwable) {
                Toast.makeText(this@MainActivity, t.toString(), Toast.LENGTH_SHORT).show()
            }
        })
    }
}
