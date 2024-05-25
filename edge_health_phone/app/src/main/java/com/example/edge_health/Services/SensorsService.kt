package com.example.edge_health.Services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.room.Room
import com.example.edge_health.Databases.SensorDao
import com.example.edge_health.Databases.SensorDatabase
import com.example.edge_health.SensorsCollect
import com.google.android.gms.wearable.MessageClient.OnMessageReceivedListener
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import org.json.JSONObject
import java.util.Timer
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService

public class SensorsService : Service() , OnMessageReceivedListener{
    // Declare a ScheduledExecutorService field
    private lateinit var executor: ScheduledExecutorService

    private var wifiConnected: Boolean = false
    private lateinit var wifiManager: WifiManager

    val TAG = "SensorsService"
    private lateinit var intent : Intent
    private lateinit var db: SensorDatabase
    private lateinit var sensorDao: SensorDao

    private lateinit var watchSensors: SensorsCollect
    private var prevWatchSensors: SensorsCollect? = null
    override fun onBind(intent: Intent): IBinder? {

        return null
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

        Log.d("Service", "Service stopped")
    }

    private val CHANNEL_ID = "ForegroundService Kotlin"
    val timer = Timer()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        this.intent = intent!!
        db = Room.databaseBuilder(this, SensorDatabase::class.java, "sensor-final-database").build()
        sensorDao = db.sensorDao()
        Log.d("Service", db.toString())
        Log.d("Service", "Service started")
        //onCreateSync.start();
        Wearable.getMessageClient(this).addListener(this)


        return START_STICKY
    }
    override fun onMessageReceived(messageEvent: MessageEvent) {
        if (messageEvent.path == "/sensores") {
            try {
                val respuesta = JSONObject(String(messageEvent.data))
                Log.d("Message", respuesta.toString())
                watchSensors = SensorsCollect(String(messageEvent.data))

                // Insert the data into the database
                val finalwatchSensors: SensorsCollect = watchSensors
                val t = Thread { sensorDao.insert(finalwatchSensors) }

                t.start()

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun setAppAsMessageClient() {
        Wearable.getMessageClient(this).addListener(this)
    }

    private fun removeAppAsMessageClient() {
        Wearable.getMessageClient(this).removeListener(this)
    }
    override fun stopService(name: Intent?): Boolean {
        return super.stopService(name)
    }
}