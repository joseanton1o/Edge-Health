package com.example.edge_health;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class SensorsCollect {
    @PrimaryKey
    public long timestamp;
    @ColumnInfo(name = "heart_rate")
    public double heartRate;
    @ColumnInfo(name = "light")
    public double light;
    @ColumnInfo(name = "accelerometerX")
    public double accelerometerX;
    @ColumnInfo(name = "accelerometerY")
    public double accelerometerY;
    @ColumnInfo(name = "accelerometerZ")
    public double accelerometerZ;
    @ColumnInfo(name = "gyroscopeX")
    public double gyroscopeX;
    @ColumnInfo(name = "gyroscopeY")
    public double gyroscopeY;
    @ColumnInfo(name = "gyroscopeZ")
    public double gyroscopeZ;
    @ColumnInfo(name = "steps")
    public int steps;

    @ColumnInfo(name = "sent")
    public boolean sent;
    public SensorsCollect(){
        // Everything is initialized to 0
        steps = 0;
        heartRate = 0;
        light = 0;
        accelerometerX = 0;
        accelerometerY = 0;
        accelerometerZ = 0;
        gyroscopeX = 0;
        gyroscopeY = 0;
        gyroscopeZ = 0;
        sent = false;
    }
    public SensorsCollect(String jsonData) {
        try {
            org.json.JSONObject jsonObject = new org.json.JSONObject(jsonData);
            this.timestamp = jsonObject.getLong("timestamp");
            this.heartRate = jsonObject.getDouble("heartRate");
            this.light = jsonObject.getDouble("light");
            this.accelerometerX = jsonObject.getJSONArray("accelerometer").getDouble(0);
            this.accelerometerY = jsonObject.getJSONArray("accelerometer").getDouble(1);
            this.accelerometerZ = jsonObject.getJSONArray("accelerometer").getDouble(2);
            this.gyroscopeX = jsonObject.getJSONArray("gyroscope").getDouble(0);
            this.gyroscopeY = jsonObject.getJSONArray("gyroscope").getDouble(1);
            this.gyroscopeZ = jsonObject.getJSONArray("gyroscope").getDouble(2);
            this.steps = jsonObject.getInt("stepCounter");
            this.sent = false;
        } catch (org.json.JSONException e) {
            e.printStackTrace();
        }
    }

    public void dataSent(){
        this.sent = true;
    }

    /**
     * This method is used to compare two objects of the same class,
     * compares the integer part of each attribute
     * @param other
     * @return
     */
    public boolean equals(SensorsCollect other){

        if ((int) this.heartRate != (int) other.heartRate) return false;
        if ((int) this.light != (int) other.light) return false;
        if ((int) this.accelerometerX != (int) other.accelerometerX) return false;
        if ((int) this.accelerometerY != (int) other.accelerometerY) return false;
        if ((int) this.accelerometerZ != (int) other.accelerometerZ) return false;
        if ((int) this.gyroscopeX != (int) other.gyroscopeX) return false;
        if ((int) this.gyroscopeY != (int) other.gyroscopeY) return false;
        if ((int) this.gyroscopeZ != (int) other.gyroscopeZ) return false;
        if (this.steps != other.steps) return false;
        return true;

    }

}
