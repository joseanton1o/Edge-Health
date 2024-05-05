package com.example.edge_health_wear.theme

import java.sql.Timestamp

data class SensorsMessage(
    var heartRate: Double = 0.0,
    var gyroscope: Array<Double> = arrayOf(0.0, 0.0, 0.0),
    var accelerometer: Array<Double> = arrayOf(0.0, 0.0, 0.0),
    var temperature: Double = 0.0,
    var humidity: Double = 0.0,
    var light: Double = 0.0,
    var timestamp: Long = Timestamp(System.currentTimeMillis()).time / 1000 // Unix epoch time in seconds
) {

    fun toJSON () : String {
        return """
            {
                "heartRate": $heartRate,
                "gyroscope": [${gyroscope[0]}, ${gyroscope[1]}, ${gyroscope[2]}],
                "accelerometer": [${accelerometer[0]}, ${accelerometer[1]}, ${accelerometer[2]}],
                "temperature": $temperature,
                "humidity": $humidity,
                "light": $light,
                "timestamp": $timestamp
            }
        """.trimIndent() // No indentation, in JSON indentation is not necessary, -> save memory
    }

    fun saveToRoomDatabase() {
        // Save this object to the Room database
    }

}
