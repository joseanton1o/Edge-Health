package com.example.edge_health_wear

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface SensorDao {
    @Upsert // Upsert is a combination of insert and update
    suspend fun insertSensorData(sensorData: SensorCollect)

    @Delete
    suspend fun deleteSensorData(sensorData: SensorCollect)

    @Query("DELETE FROM SensorCollect")
    suspend fun deleteAllSensorData()

    @Query("SELECT * FROM SensorCollect ORDER BY timestamp ASC")
    suspend fun getSensorsOrderedByTimestamp(): List<SensorCollect> // Use a LiveData object to observe changes in the database
}