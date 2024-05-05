package com.example.edge_health_wear

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [SensorCollect::class],
    version = 1
)
abstract class SensorDatabase: RoomDatabase(){
    abstract val sensorDao: SensorDao
}