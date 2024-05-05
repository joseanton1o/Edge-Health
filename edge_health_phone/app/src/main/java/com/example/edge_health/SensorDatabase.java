package com.example.edge_health;

import androidx.room.Database;

@Database(
        entities = {SensorsCollect.class},
        version = 1
)
abstract class SensorDatabase extends androidx.room.RoomDatabase{
    abstract SensorDao sensorDao();

}
