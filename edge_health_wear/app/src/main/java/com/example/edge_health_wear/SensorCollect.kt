package com.example.edge_health_wear

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class SensorCollect(

    @ColumnInfo(name = "heart_rate") var heartRate: Double = 0.0,
    @ColumnInfo(name = "gyroscope_x") var gyroscopeX: Double = 0.0,
    @ColumnInfo(name = "gyroscope_y") var gyroscopeY: Double = 0.0,
    @ColumnInfo(name = "gyroscope_z") var gyroscopeZ: Double = 0.0,
    @ColumnInfo(name = "accelerometer_x") var accelerometerX: Double = 0.0,
    @ColumnInfo(name = "accelerometer_y") var accelerometerY: Double = 0.0,
    @ColumnInfo(name = "accelerometer_z") var accelerometerZ: Double = 0.0,
    @ColumnInfo(name = "light") var light: Double = 0.0,
    @ColumnInfo(name = "stepCounter") var stepCounter: Int = 0,
    @ColumnInfo(name = "userState") var userState: String = "none",
    @PrimaryKey var timestamp: Long = System.currentTimeMillis() / 1000 // Unix epoch time in seconds
){
    override fun toString () : String {
        return """
            {
                "userState": "$userState",
                "heartRate": $heartRate,
                "gyroscope": [$gyroscopeX, $gyroscopeY, $gyroscopeZ],
                "accelerometer": [$accelerometerX, $accelerometerY, $accelerometerZ],
                "light": $light,
                "timestamp": $timestamp,
                "stepCounter": $stepCounter
            }
        """.trimIndent() // No indentation, in JSON indentation is not necessary, -> save memory
    }
    private fun floatEquals(a: Float, b: Float): Boolean {
        // Compare two floats up to 3 decimal places
        return Math.abs(a - b) < 0.001
    }
    fun equals(other: SensorCollect): Boolean {
        // Compare up to 3 decimal places
        return floatEquals(heartRate.toFloat(), other.heartRate.toFloat()) &&
                floatEquals(gyroscopeX.toFloat(), other.gyroscopeX.toFloat()) &&
                floatEquals(gyroscopeY.toFloat(), other.gyroscopeY.toFloat()) &&
                floatEquals(gyroscopeZ.toFloat(), other.gyroscopeZ.toFloat()) &&
                floatEquals(accelerometerX.toFloat(), other.accelerometerX.toFloat()) &&
                floatEquals(accelerometerY.toFloat(), other.accelerometerY.toFloat()) &&
                floatEquals(accelerometerZ.toFloat(), other.accelerometerZ.toFloat()) &&
                floatEquals(light.toFloat(), other.light.toFloat()) &&
                userState.equals(other.userState) &&
                stepCounter == other.stepCounter
    }

    fun deepCopy(): SensorCollect {
        return SensorCollect(
            heartRate,
            gyroscopeX,
            gyroscopeY,
            gyroscopeZ,
            accelerometerX,
            accelerometerY,
            accelerometerZ,
            light,
            stepCounter,
            userState,
            timestamp
        )
    }

}
