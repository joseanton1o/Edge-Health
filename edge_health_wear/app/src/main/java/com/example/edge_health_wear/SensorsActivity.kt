/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter and
 * https://github.com/android/wear-os-samples/tree/main/ComposeAdvanced to find the most up to date
 * changes to the libraries and their usages.
 */

package com.example.edge_health_wear

import android.Manifest
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import com.example.edge_health.R


// https://stackoverflow.com/questions/22816089/sensormanager-one-sensoreventlistener-vs-multiple-listeners
class SensorsActivity : ComponentActivity() {
    private lateinit var sensorManager: SensorManager
    private var mSensor: Sensor? = null


    // Create a new, generic sensor event listener, type of sensor will be retrieved from the event itself
    private val sensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: android.hardware.SensorEvent) {
            // Do something with this sensor data.
            // With a switch statement we could check the type of sensor and process the data accordingly
            when (event.sensor.type) {

                Sensor.TYPE_HEART_RATE -> {
                    // lets see what is stored here
                    Log.d("Sensors", "Sensor data heart rate: ${event.toString()}")
                    // get textview of giroscope x
                    var textView = findViewById<TextView>(R.id.GyroscopeX)
                    // set text to the value of the sensor
                    textView.text = "Heart rate: ${event.values[0]}"

                }

                Sensor.TYPE_GYROSCOPE -> {
                    // lets see what is stored here
                    Log.d("Sensors", "Sensor data gyroscope X: ${event.values[0]}")
                    Log.d("Sensors", "Sensor data gyroscope Y: ${event.values[1]}")
                    Log.d("Sensors", "Sensor data gyroscope Z: ${event.values[2]}")
                    // get textview of giroscope x
                }
            }
        }

        override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
            Log.d("Sensors", "Accuracy changed")
        }
    }



    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setTheme(android.R.style.Theme_DeviceDefault)

        setContentView(R.layout.act2)
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        val sensorList: List<Sensor> = sensorManager.getSensorList(Sensor.TYPE_ALL)
        val TAG = "Sensors"
        // get the gyroscope sensor

        for (sensor in sensorList) {
            Log.d(TAG, sensor.name)
        }

        // If permission is not granted, request it
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BODY_SENSORS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.BODY_SENSORS), 1)
        }
        else {
            Log.d(TAG, "PERMISSION GRANTED")
        }

        sensorManager.registerListener(sensorEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE), SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager.registerListener(sensorEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_NORMAL)

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
            /*
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
            }, mSensor, SensorManager.SENSOR_DELAY_NORMAL)*/
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
