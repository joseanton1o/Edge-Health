package com.example.edge_health;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.locks.ReentrantLock;

import kotlin.Pair;

public class SensorsData {
    private ReentrantLock gyroscopeLock = new ReentrantLock();
    private ReentrantLock accelerometerLock = new ReentrantLock();
    private ReentrantLock lightLock = new ReentrantLock();
    private ReentrantLock heartRateLock = new ReentrantLock();
    private ReentrantLock stepsLock = new ReentrantLock();

    // Watch sensors
    // Gyroscope is an arraylist of 3 arrays, one for each axis
    private ArrayList<ArrayList<Double>> Gyroscope = new ArrayList<>();
    private ArrayList<Long> GyroscopeTimestamp = new ArrayList<>();
    private ArrayList<ArrayList<Double>> Accelerometer = new ArrayList<>();
    private ArrayList<Long> AccelerometerTimestamp = new ArrayList<>();
    private ArrayList<Double> Light = new ArrayList<>();
    private ArrayList<Long> LightTimestamp = new ArrayList<>();
    private ArrayList<Integer> HeartRate = new ArrayList<>();
    private ArrayList<Long> HeartRateTimestamp = new ArrayList<>();
    private Integer accumulatedSteps = 0;

    private static SensorsData instance = null;

    // Keys for JSON
    private static final ArrayList<String> keys = new ArrayList<String>() {{
        add("x");
        add("y");
        add("z");
    }};

    // Constructor ************************************************************
    private SensorsData() {
        for (int i = 0; i < 3; i++) {
            Gyroscope.add(new ArrayList<Double>());
            Accelerometer.add(new ArrayList<Double>());
        }
    }

    // Singleton **************************************************************
    public static SensorsData getInstance() {
        if (instance == null) {
            synchronized (SensorsData.class) {
                if (instance == null) {
                    instance = new SensorsData();
                }
            }
        }
        return instance;
    }

    // addJSON **************************************************************

    public void addDataFromJson(JSONObject data) {
        try {
            JSONObject gyroscope = data.getJSONObject("Gyroscope");

            gyroscopeLock.lock();
            try {
                Gyroscope.get(0).addAll((ArrayList) gyroscope.get("x"));
                Gyroscope.get(1).addAll((ArrayList) gyroscope.get("y"));
                Gyroscope.get(2).addAll((ArrayList) gyroscope.get("z"));

                GyroscopeTimestamp.addAll((ArrayList) data.get("GyroscopeTimestamp"));
            }
            finally {
                gyroscopeLock.unlock();
            }

            JSONObject accelerometer = data.getJSONObject("Accelerometer");
            accelerometerLock.lock();
            try {
                Accelerometer.get(0).addAll((ArrayList) accelerometer.get("x"));
                Accelerometer.get(1).addAll((ArrayList) accelerometer.get("y"));
                Accelerometer.get(2).addAll((ArrayList) accelerometer.get("z"));

                AccelerometerTimestamp.addAll((ArrayList) data.get("AccelerometerTimestamp"));
            }
            finally {
                accelerometerLock.unlock();
            }

            ArrayList<Double> light = (ArrayList<Double>) data.get("Light");
            ArrayList<Long> lightTimestamp = (ArrayList<Long>) data.get("LightTimestamp");
            lightLock.lock();
            try {
                Light.addAll(light);
                LightTimestamp.addAll(lightTimestamp);
            }
            finally {
                lightLock.unlock();
            }

            ArrayList<Integer> heartRate = (ArrayList<Integer>) data.get("HeartRate");
            ArrayList<Long> heartRateTimestamp = (ArrayList<Long>) data.get("HeartRateTimestamp");
            heartRateLock.lock();
            try {
                HeartRate.addAll(heartRate);
            }
            finally {
                heartRateLock.unlock();
            }

            stepsLock.lock();
            try {
                accumulatedSteps = Integer.parseInt(data.getString("Steps"));
            }
            finally {
                stepsLock.unlock();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // Setters **************************************************************
// Setters **************************************************************
    public void addGyroscopeData(ArrayList<Double> data, Long timestamp) {
        gyroscopeLock.lock();
        try {
            for (int i = 0; i < 3; i++) {
                Gyroscope.get(i).add(data.get(i));
            }
            GyroscopeTimestamp.add(timestamp);
        } finally {
            gyroscopeLock.unlock();
        }
    }
    public void addAccelerometerData(ArrayList<Double> data, Long timestamp) {
        accelerometerLock.lock();
        try {
            for (int i = 0; i < 3; i++) {
                Accelerometer.get(i).add(data.get(i));
            }
            AccelerometerTimestamp.add(timestamp);
        } finally {
            accelerometerLock.unlock();
        }
    }
    public void addLightData(Double data, Date timestamp) {
        lightLock.lock();
        try {
            Light.add((data));
            LightTimestamp.add(timestamp.getTime() / 1000); // Unix timestamp (seconds)
        } finally {
            lightLock.unlock();
        }
    }
    public void addHeartRateData(Integer data, Date timestamp) {
        heartRateLock.lock();
        try {
            HeartRate.add(data);
            HeartRateTimestamp.add(timestamp.getTime() / 1000); // Unix timestamp (seconds)
        } finally {
            heartRateLock.unlock();
        }
    }
    public void addStepsData(Integer data) {
        stepsLock.lock();
        try {
            accumulatedSteps += data;
        } finally {
            stepsLock.unlock();
        }
    }
    // ************************************************************************

    // To JSON **************************************************************
    private <T,Y> JSONArray getPairJSONArray(ArrayList<Pair<T, Y>> data, String key1, String key2) throws JSONException {
        JSONArray jsonArray = new JSONArray();
        for (Pair<T, Y> pair : data) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(key1, pair.getFirst().toString());
            jsonObject.put(key2, pair.getSecond().toString());
            jsonArray.put(jsonObject);
        }
        return jsonArray;
    }

    private <T> JSONArray getJSONArray(ArrayList<T> data, String key) throws JSONException {
        JSONArray jsonArray = new JSONArray();
        for (T item : data) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(key, item.toString());
            jsonArray.put(jsonObject);
        }
        return jsonArray;
    }
    private <T> JSONArray getJSONArray(ArrayList<T> data) throws JSONException {
        JSONArray jsonArray = new JSONArray();
        for (T item : data) {
            jsonArray.put(item.toString());
        }
        return jsonArray;
    }
    private <T> JSONObject getMultiLevelArraylist(ArrayList<ArrayList<T>> data, ArrayList<String> keys) throws JSONException, IllegalArgumentException{
        // Check precondition
        if (data.size() != keys.size()) {
            throw new IllegalArgumentException("Length of keys must be equal to the length of data");
        }

        JSONObject jsonArray = new JSONObject();
        for (int i = 0; i < data.size(); i++) {
            ArrayList<T> innerList = data.get(i);
            String key = keys.get(i);
            jsonArray.put(key, this.getJSONArray(innerList));
        }
        return jsonArray;
    }

    public JSONObject getSensorsData() {
        JSONObject json = new JSONObject();

        try {
            json.put("Gyroscope", this.getMultiLevelArraylist(Gyroscope, keys));
            json.put("GyroscopeTimestamp", this.getJSONArray(GyroscopeTimestamp));

            json.put("Accelerometer", this.getMultiLevelArraylist(Accelerometer, keys));
            json.put("AccelerometerTimestamp", this.getJSONArray(AccelerometerTimestamp));


            json.put("Light", this.getJSONArray(Light));
            json.put("LightTimestamp", this.getJSONArray(LightTimestamp));

            json.put("HeartRate", this.getJSONArray(HeartRate));
            json.put("HeartRateTimestamp", this.getJSONArray(HeartRateTimestamp));


            json.put("Steps", accumulatedSteps.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        clearData();
        return json;
    }

    // ************************************************************************

    public synchronized void clearData() {
        gyroscopeLock.lock();
        try {
            Gyroscope.clear();
            GyroscopeTimestamp.clear();
        } finally {
            gyroscopeLock.unlock();
        }
        accelerometerLock.lock();
        try {
            Accelerometer.clear();
            AccelerometerTimestamp.clear();
        } finally {
            accelerometerLock.unlock();
        }
        lightLock.lock();
        try {
            Light.clear();
        } finally {
            lightLock.unlock();
        }
        heartRateLock.lock();
        try {
            HeartRate.clear();
        } finally {
            heartRateLock.unlock();
        }
        stepsLock.lock();
        try {
            accumulatedSteps = 0;
        } finally {
            stepsLock.unlock();
        }
    }
}
