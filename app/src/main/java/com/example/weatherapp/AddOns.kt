package com.example.weatherapp

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch

class AddOns : AppCompatActivity(), SensorEventListener {


    private lateinit var textViewGyroscope: TextView
    private lateinit var textViewMagnetometer: TextView

    private lateinit var sensorManager: SensorManager
    private var magnetometer: Sensor? = null
    private var gyroscope: Sensor? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.addons_activity)

        // hiding the status bar
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        actionBar?.hide()


        textViewGyroscope = findViewById(R.id.textViewG)
        textViewMagnetometer = findViewById(R.id.textViewM)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

    }

    override fun onResume() {
        super.onResume()
        magnetometer?.let { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
        gyroscope?.let { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // not used
    }

   override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_MAGNETIC_FIELD) {
            val magneticField = event.values
            val magneticX = magneticField[0]
            val magneticY = magneticField[1]
            val magneticZ = magneticField[2]

            textViewMagnetometer.text = "Magnetic Field (μT):\nX: $magneticX\nY: $magneticY\nZ: $magneticZ"
        }
       if (event?.sensor?.type == Sensor.TYPE_GYROSCOPE) {
           val gyroscopeValues = event.values
           val gyroX = gyroscopeValues[0]
           val gyroY = gyroscopeValues[1]
           val gyroZ = gyroscopeValues[2]
           
           textViewGyroscope.text = "Gyroscope Values (rad/s):\nX: $gyroX\nY: $gyroY\nZ: $gyroZ"
       }
    }
}