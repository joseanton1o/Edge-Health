package com.example.edge_health.Databases;

import androidx.room.Database;

import com.example.edge_health.Databases.SensorDao;
import com.example.edge_health.SensorsCollect;

@Database(
        entities = {SensorsCollect.class},
        version = 1
)
abstract public class SensorDatabase extends androidx.room.RoomDatabase{
    public abstract SensorDao sensorDao();

}
