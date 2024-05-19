package com.example.edge_health_wear

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.wifi.ScanResult
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.os.IBinder
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.room.Room
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.CapabilityInfo
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import java.util.Date
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class SensorsService : Service() {
    // Declare a ScheduledExecutorService field
    private lateinit var executor: ScheduledExecutorService
    private var currentData: SensorCollect = SensorCollect()
    private var prevSent: SensorCollect? = null
    private lateinit var sensorManager: SensorManager
    private var nodeId: String? = null
    private var wifiConnected: Boolean = false
    private lateinit var wifiManager: WifiManager
    private val SSID = "jose-Legion-5"
    val TAG = "SensorsService"
    private var data_sent = 0

    private val sensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: android.hardware.SensorEvent) {
            // Do something with this sensor data.
            // With a switch statement we could check the type of sensor and process the data accordingly
            when (event.sensor.type) {

                Sensor.TYPE_HEART_RATE -> {

                    currentData.heartRate = event.values[0].toDouble()
                }

                Sensor.TYPE_GYROSCOPE -> {
                    currentData.gyroscopeX = event.values[0].toDouble()
                    currentData.gyroscopeY = event.values[1].toDouble()
                    currentData.gyroscopeZ = event.values[2].toDouble()
                }

                Sensor.TYPE_ACCELEROMETER -> {
                    currentData.accelerometerX = event.values[0].toDouble()
                    currentData.accelerometerY = event.values[1].toDouble()
                    currentData.accelerometerZ = event.values[2].toDouble()
                }

                Sensor.TYPE_STEP_COUNTER -> {
                    currentData.stepCounter = event.values[0].toInt()
                }

                Sensor.TYPE_LIGHT -> {
                    currentData.light = event.values[0].toDouble()
                }
            }
        }

        override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
            Log.d("Sensors", "Accuracy changed")
        }
    }
    private lateinit var db: SensorDatabase
    private lateinit var dao: SensorDao
    override fun onBind(intent: Intent): IBinder? {

        return null
    }
    inner class enviar : TimerTask() {
        override fun run() {
            val data: JSONObject = JSONObject(currentData.toString())

            if (prevSent == null) {
                prevSent = currentData.deepCopy()
                Log.d("Thread", "Sending message")
                sendWearableMessage()
            }
            else if (prevSent!!.equals(currentData)) {
                val prevData: JSONObject = JSONObject(prevSent.toString())
                Log.d("Thread", "Data is the same")
                Log.d("Thread", data.toString())
                Log.d("Thread", prevData.toString())
            }
            else {
                prevSent = currentData.deepCopy()
                Log.d("Thread", "Sending message")
                sendWearableMessage()
            }


        }
    }
    private fun createNotificationChannel() {
            val name = "Your Channel Name"
            val descriptionText = "Your channel description"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            // Register the channel with the system
            val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)

    }
    private lateinit var notification: Notification
    override fun onCreate() {
        super.onCreate()
        executor = Executors.newSingleThreadScheduledExecutor()
        // Create the notification channel (if targeting Oreo or higher)
        createNotificationChannel()

        notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Foreground Service")
            .setContentText("Service is running in the foreground")
            .build()

        startForeground(1, notification)
        // Initialize the ScheduledExecutorService with a single thread

        Log.d("Service", "Service created")
    }
    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(sensorEventListener)
        Log.d("Service", "Service stopped")
    }

    private val CHANNEL_ID = "ForegroundService Kotlin"
    val timer = Timer()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        db = Room.databaseBuilder(this, SensorDatabase::class.java, "sensor_database").build()
        dao = db.sensorDao
        Log.d("Service", "Service started")
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

        executor.scheduleAtFixedRate(enviar(), 0, 500, TimeUnit.MILLISECONDS)

        return START_STICKY
    }
    @SuppressLint("MissingPermission")
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
        }
    }

    private fun sendWearableMessage(): Boolean{
        // Send a message to the wearable
        // This is a simple message, but you can also send data
        // or other types of messages
        // The message will be received by the WearableListenerService
        // on the wearable device
        Log.d("Sending message", "Sending message to node ${nodeId}")
        currentData.timestamp = Date().time / 1000
        val data: JSONObject = JSONObject(this.currentData.toString())
        Log.d("Data", data.toString())

        if (prevSent != null) {
            // The current data steps will be the difference between the current steps and the previous steps
            currentData.stepCounter -= prevSent!!.stepCounter
        }
        else {
            currentData.stepCounter = 0
        }

        if (nodeId != null) {
            Wearable.getMessageClient(this).sendMessage(nodeId!!, "/sensores", data.toString().toByteArray())

            data_sent += 1
            return true
        }
        else{
            // Save the data to the room database if the node is not connected
            // This data will be sent to the node when it is connected
            Log.d("Sending message", "Node not connected, saving data to database")

            runBlocking {
                dao.insertSensorData(currentData)
            }
            data_sent += 1
        }
        if (data_sent == 10) {
            connectToNode()
            data_sent = 0
        }

        return false
    }

    override fun stopService(name: Intent?): Boolean {
        return super.stopService(name)
    }
}