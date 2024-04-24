/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter and
 * https://github.com/android/wear-os-samples/tree/main/ComposeAdvanced to find the most up to date
 * changes to the libraries and their usages.
 */

//https://cursos.innovadomotics.com/courses/android-studio-y-arduino-comunicacion-bluetooth-kotlin/leccion/desarrollo-codigo/

package com.example.edge_health_wear


import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.wifi.ScanResult
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import com.android.volley.VolleyError
import com.example.edge_health.R
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.CapabilityInfo
import com.google.android.gms.wearable.Wearable
import org.json.JSONObject
import java.util.Date
import java.util.concurrent.ExecutionException

const val REQUEST_ENABLE_BT = 1

class MainActivity : ComponentActivity() {
    private lateinit var sensorManager: SensorManager
    private var mSensor: Sensor? = null

    var activityLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == REQUEST_ENABLE_BT) {
            Log.i("MainActivity", "ACTIVIDAD REGISTRADA")
        }
    }

    private val SSID = "jose-Legion-5"
    val TAG = "MainActivity"
    private var nodeId: String? = null
    private var sData: SensorsData = SensorsData.getInstance();
    private var wifiConnected: Boolean = false
    private lateinit var wifiManager: WifiManager
    // Create a new, generic sensor event listener, type of sensor will be retrieved from the event itself
    private val sensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: android.hardware.SensorEvent) {
            // Do something with this sensor data.
            // With a switch statement we could check the type of sensor and process the data accordingly
            when (event.sensor.type) {

                Sensor.TYPE_HEART_RATE -> {

                    val nowDate = Date()
                    // Add the data to the sensors data object
                    sData.addHeartRateData(event.values[0].toInt(), nowDate)

                }

                Sensor.TYPE_GYROSCOPE -> {
                    // Empty arraylist
                    val data = ArrayList<Double>()

                    // Add the values to the arraylist
                    for (i in 0..2) {
                        data.add(event.values[i].toDouble())
                    }

                    // Get unix timestamp
                    val timestamp = System.currentTimeMillis() / 1000L

                    // Add the data to the sensors data object
                    sData.addGyroscopeData(data, timestamp)
                }

                Sensor.TYPE_ACCELEROMETER -> {
                    // Empty arraylist
                    val data = ArrayList<Double>()

                    // Add the values to the arraylist
                    for (i in 0..2) {
                        data.add(event.values[i].toDouble())
                    }

                    // Get unix timestamp
                    val timestamp = System.currentTimeMillis() / 1000L

                    // Add the data to the sensors data object
                    sData.addAccelerometerData(data, timestamp)
                }

                Sensor.TYPE_STEP_COUNTER -> {
                    // Add the data to the sensors data object
                    sData.addStepsData(event.values[0].toInt())
                }

                Sensor.TYPE_LIGHT -> {
                    // Add the data to the sensors data object
                    var nowDate = Date()

                    sData.addLightData(event.values[0].toDouble(), nowDate)
                }
            }
        }

        override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
            Log.d("Sensors", "Accuracy changed")
        }
    }



    // Bluetooth connection class
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_layout)
        Log.d(TAG, "onCreate")

        // ASK FOR ALL PERMISSIONS

        // Get a list of currently connected Bluetooth devices
        if (ActivityCompat.checkSelfPermission(this,Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "PERMISSION NOT GRANTED")
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.BLUETOOTH), 1)
        }
        else {
            Log.d(TAG, "PERMISSION GRANTED2")
        }
        if (ActivityCompat.checkSelfPermission(this,Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "PERMISSION NOT GRANTED")
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.BLUETOOTH_ADMIN), 1)
        }
        else {
            Log.d(TAG, "PERMISSION GRANTED3")
        }

        // Scan WiFi
        wifiManager = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager

        // Connect to the node
        connectToNode()


        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        // Register gyroscope sensor
        sensorManager.registerListener(sensorEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_NORMAL)
        // Register heart rate sensor
        sensorManager.registerListener(sensorEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE), SensorManager.SENSOR_DELAY_NORMAL)
        // Register accelerometer sensor
        sensorManager.registerListener(sensorEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL)
        // Register step counter sensor
        sensorManager.registerListener(sensorEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER), SensorManager.SENSOR_DELAY_NORMAL)
        // Register light sensor
        sensorManager.registerListener(sensorEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT), SensorManager.SENSOR_DELAY_NORMAL)



        val myButton = findViewById<Button>(R.id.btButton)
        myButton.setOnClickListener {
            // Go to Sensors activity
            val intent = android.content.Intent(this, SensorsActivity::class.java)
            activityLauncher.launch(intent)
        }


        var resp = JSONObject()
        val callback = object : VolleyCallback {
            override fun onSuccess(response: JSONObject) {
                // Handle the JSON response here
                Log.d("Respooooonse", response.toString())
                resp = response;
                print(resp)
                val textView = findViewById<TextView>(R.id.textView)

                textView.setText(resp.getJSONArray("users").getJSONObject(0).getString("name"))
            }

            override fun onError(error: VolleyError) {
                // Handle errors here
                throw RuntimeException("Error: $error")
            }
        }
        val APIActioner = findViewById<Button>(R.id.API)
        APIActioner.setOnClickListener{
            /*
            val req = VolleyRequest(this)
            resp = req.sendRequest(callback, "/api/users/all", Request.Method.GET, null)
            */
            // Thread to await the data from the wearable device

            // Thread to await the data from the wearable device
            // Send a simple message to the wearable
            sendWearableMessage()
        }
        Log.d("Thread", "Starting thread")


    }

    private fun connectToNode(){
        Thread {
            Log.d("Thread", "Thread running")
            var capabilityInfo: CapabilityInfo? = null
            try {
                Log.d("Thread", "Awaiting capabilities")
                capabilityInfo = Tasks.await<CapabilityInfo>(
                    Wearable.getCapabilityClient(this).getCapability(
                        "sensor_reception", CapabilityClient.FILTER_REACHABLE
                    )
                )
            } catch (e: ExecutionException) {
                e.printStackTrace()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
            Log.d("Thread", "Capabilities received")

            // capabilityInfo has the reachable nodes with the transcription capability
            updateCapabilities(capabilityInfo!!)
        }.start()

        if (nodeId == null) {
            // Try to connect to a nearby node with wifi
            // TODO: Check whether the watch is connected to the node or not first
            // Maybe just disconnect from the current network and connect to the node
            // TODO: Encapsulate the connection to the node in either a method or a class
            val info = wifiManager.connectionInfo
            val ssid = info.ssid

            if (ssid == SSID) {
                Log.d(TAG, "Already connected to the node")
                wifiConnected = true
            }
            else {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
                }
                else {
                    Log.d(TAG, "PERMISSION GRANTE1D")
                }
                val wifiScanList: List<ScanResult> = wifiManager.scanResults
                var foundNode = false
                for (result in wifiScanList) {// local counter inside the if statement to check if we have to skip the node, this for later
                    Log.d(TAG, result.SSID)
                    if (result.SSID == SSID) {
                        Log.d(TAG, "FOUND NODE")
                        foundNode = true
                        break
                    }
                }
                Log.d(TAG, "Found node: $foundNode")
                if (!foundNode) {
                    Log.d(TAG, "No node found")
                } else {
                    val networkPass = "12345678"
                    val conf = WifiConfiguration()
                    conf.SSID = "\"" + SSID + "\""
                    conf.preSharedKey = "\"" + networkPass + "\""
                    val netId = wifiManager.addNetwork(conf)
                    wifiManager.disconnect()
                    wifiManager.enableNetwork(netId, true)
                    wifiManager.reconnect()
                    wifiConnected = true
                }
            }
        }
    }

    private fun updateCapabilities(capabilityInfo: CapabilityInfo) {
        val connectedNodes = capabilityInfo.nodes
        var bestNodeId: String? = null
        Log.d("Connected nodes", connectedNodes.toString())
        // Find a nearby node or pick one arbitrarily
        for (node in connectedNodes) {
            if (node.isNearby) {
                nodeId = node.id
                Log.d("Nearby node", nodeId.toString())
            }
            bestNodeId = node.id
            Log.d("All node", nodeId.toString())
        }
        nodeId = bestNodeId
    }

    private fun sendWearableMessage() {
        // Send a message to the wearable
        // This is a simple message, but you can also send data
        // or other types of messages
        // The message will be received by the WearableListenerService
        // on the wearable device
        Log.d("Sending message", "Sending message to wearable ${nodeId}")
        val data: JSONObject = this.sData.getSensorsData()
        Log.d("Data", data.toString())
        Log.d("Sending message", "Sending message to wearable")

        if (nodeId != null) {
            Log.d("Sending message", "Sending message to wearable")
            Wearable.getMessageClient(this).sendMessage(nodeId!!, "/sensores", data.toString().toByteArray())

        }
    }

    // Destroy the activity -- when the app is closed
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")

        // Save any data that needs to be persisted such as sensor data
    }
}