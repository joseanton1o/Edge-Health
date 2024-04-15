/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter and
 * https://github.com/android/wear-os-samples/tree/main/ComposeAdvanced to find the most up to date
 * changes to the libraries and their usages.
 */

package com.example.edge_health.presentation

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.wear.compose.material.TimeText
import com.example.edge_health.R
import com.example.edge_health.presentation.theme.Edge_healthTheme

class SensorsActivity : ComponentActivity() {
    private lateinit var sensorManager: SensorManager
    private var mSensor: Sensor? = null
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setTheme(android.R.style.Theme_DeviceDefault)

        setContentView(R.layout.act2)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val sensorList: List<Sensor> = sensorManager.getSensorList(Sensor.TYPE_ALL)
        val TAG = "Sensors"
        // get the gyroscope sensor
        mSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        if (mSensor == null) {
            Log.d(TAG, "No gyroscope sensor found")
        }
        else {
            Log.d(TAG, "Gyroscope sensor found")
            Log.d(TAG, "Sensor name: ${mSensor?.name}")
            // Log data from the gyroscope sensor
            Log.d(TAG, "Sensor resolution: ${mSensor?.resolution}")
            Log.d(TAG, "Sensor vendor: ${mSensor?.vendor}")
            Log.d(TAG, "Sensor version: ${mSensor?.version}")
            Log.d(TAG, "Sensor power: ${mSensor?.power}")
            Log.d(TAG, "Sensor type: ${mSensor?.type}")
            Log.d(TAG, "Sensor max range: ${mSensor?.maximumRange}")
            Log.d(TAG, "Sensor min delay: ${mSensor?.minDelay}")

            // Register the gyroscope sensor
            sensorManager.registerListener(object : SensorEventListener {
                override fun onSensorChanged(event: android.hardware.SensorEvent) {
                    // Do something with this sensor data.
                    //Log.d(TAG, "Sensor data: ${event.values[0]}") )
                    Log.d(TAG, "Sensor data gyroscope X: ${event.values[0]}")
                    Log.d(TAG, "Sensor data gyroscope Y: ${event.values[1]}")
                    Log.d(TAG, "Sensor data gyroscope Z: ${event.values[2]}")

                    // Change the text view to display the sensor data
                    var textView = findViewById<TextView>(R.id.GyroscopeX)
                    textView.text = "Gyroscope X: ${event.values[0]}"
                    textView = findViewById<TextView>(R.id.GyroscopeY)
                    textView.text = "Gyroscope Y: ${event.values[1]}"
                    textView = findViewById<TextView>(R.id.GyroscopeZ)
                    textView.text = "Gyroscope Z: ${event.values[2]}"

                    processSensorData(event, Sensor.TYPE_GYROSCOPE)

                }

                override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
                    Log.d(TAG, "Accuracy changed")
                }
            }, mSensor, SensorManager.SENSOR_DELAY_NORMAL)
        }

    }

    private fun processSensorData(event: android.hardware.SensorEvent, sensorType: Int ) {
        // Here we could map the type to our expected nums with for example a dictionary,
        // given a sensorType translate the values to the expected ones starting from 0 to the length of the sensors we want to use

        // Check event accuracy

        // Log a dummy message to know that this is being called
        Log.d("SensorData", "Processing sensor data")
    }
}
