package com.example.edge_health;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.json.JSONObject;

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

    @ColumnInfo(name = "userStatus")
    public String userStatus;

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
        userStatus = "none";
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
            this.userStatus = jsonObject.getString("userState");
            this.sent = false;
        } catch (org.json.JSONException e) {
            e.printStackTrace();
        }
    }

    public JSONObject toJson(){
        JSONObject json = new JSONObject();
        try {
            json.put("timestamp", this.timestamp);
            json.put("beats_per_min", this.heartRate);
            json.put("light", this.light);
            json.put("accelerometer_x", this.accelerometerX);
            json.put("accelerometer_y", this.accelerometerY);
            json.put("accelerometer_z", this.accelerometerZ);
            json.put("gyroscope_x", this.gyroscopeX);
            json.put("gyroscope_y", this.gyroscopeY);
            json.put("gyroscope_z", this.gyroscopeZ);
            json.put("step_counter", this.steps);
            json.put("user_status", this.userStatus);
        } catch (org.json.JSONException e) {
            e.printStackTrace();
        }
        return json;
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
        return true;

    }

    @Override
    public String toString() {
        return "SensorsCollect{" +
                "timestamp=" + timestamp +
                ", heartRate=" + heartRate +
                ", light=" + light +
                ", accelerometerX=" + accelerometerX +
                ", accelerometerY=" + accelerometerY +
                ", accelerometerZ=" + accelerometerZ +
                ", gyroscopeX=" + gyroscopeX +
                ", gyroscopeY=" + gyroscopeY +
                ", gyroscopeZ=" + gyroscopeZ +
                ", steps=" + steps +
                ", userStatus='" + userStatus + '\'' +
                ", sent=" + sent +
                '}';
    }

}
