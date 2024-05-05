package com.example.edge_health;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Query;
import androidx.room.Upsert;

@Dao
public interface SensorDao {
    @Upsert
    void insert(SensorsCollect sensorsCollect);

    @Delete
    void delete(SensorsCollect sensorsCollect);

    @Query("DELETE FROM SensorsCollect")
    void deleteAll();

    @Query("SELECT * FROM SensorsCollect")
    SensorsCollect[] getAll();

    @Query("SELECT * FROM SensorsCollect WHERE sent = 0 ORDER BY timestamp ASC LIMIT 20 ")
    SensorsCollect[] getFirst20NotSent();
}
