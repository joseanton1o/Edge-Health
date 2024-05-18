package com.example.edge_health.Databases;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Query;
import androidx.room.Upsert;

import com.example.edge_health.SensorsCollect;

@Dao
public interface SensorDao {
    @Upsert
    public void insert(SensorsCollect sensorsCollect);

    @Delete
    public void delete(SensorsCollect sensorsCollect);

    @Query("DELETE FROM SensorsCollect")
    public void deleteAll();

    @Query("SELECT * FROM SensorsCollect")
    public SensorsCollect[] getAll();

    @Query("SELECT * FROM SensorsCollect WHERE sent = 0 ORDER BY timestamp ASC LIMIT 20 ")
    public SensorsCollect[] getFirst20NotSent();

    @Query("UPDATE SensorsCollect SET sent = 1 WHERE timestamp = :timestamp")
    public void setSent(long timestamp);

    // Get data from timestamp to now
    @Query("SELECT * FROM SensorsCollect WHERE timestamp >= :timestamp")
    public SensorsCollect[] getFromTimestamp(long timestamp);
}
