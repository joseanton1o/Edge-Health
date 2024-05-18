package com.example.edge_health.Databases;

import androidx.room.Database;

import com.example.edge_health.SensorsCollect;
import com.example.edge_health.Token;

@Database(
        entities = {Token.class},
        version = 1
)
abstract public class TokenDatabase extends androidx.room.RoomDatabase{
    public abstract TokenDao tokenDao();

}
