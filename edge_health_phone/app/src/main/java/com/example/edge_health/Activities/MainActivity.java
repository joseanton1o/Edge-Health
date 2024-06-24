package com.example.edge_health.Activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import com.example.edge_health.Databases.TokenDatabase;
import com.example.edge_health.R;
import com.example.edge_health.Databases.SensorDao;
import com.example.edge_health.Databases.SensorDatabase;
import com.example.edge_health.SensorsCollect;
import com.example.edge_health.Services.SensorsService;
import com.example.edge_health.Token;
import com.example.edge_health.VolleyCallback;
import com.example.edge_health.VolleyRequest;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

public class MainActivity extends AppCompatActivity{
    private Integer statusCode = null;
    private RequestQueue queue = null;
    private final String url = "http://10.42.0.1/";
    ArrayList<String> testDataReceived = new ArrayList<>();
    private Context context = this;
    private String nodeId = null;

    private SensorDatabase db = null;
    private SensorDao sensorDao = null;


    private SensorsCollect watchSensors = null;
    private SensorsCollect prevWatchSensors = null;


    private ArrayList dashboardData;

    private BarDataSet dashboardDataSet;

    private BarChart dashboardChart;

    private BarData dashboardBarData;


    // Selector section
    ArrayList<String> selectorOptions = new ArrayList();
    Integer selectedOption = 0;
    // End of selector section
    private int numberOfDataPoints;
    private Token token;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Check if the user is logged in
        TokenDatabase tokenDb = Room.databaseBuilder(this, TokenDatabase.class, "token-database")
                .allowMainThreadQueries().build(); // Query is really fast so it's okay to run it on the main thread
        token = tokenDb.tokenDao().getById(1);
        if (token == null){
            // Launch login activity for testing
            Intent loginIntent = new Intent(this, LoginActivity.class);
            finish();
            startActivity(loginIntent);
        }
        else {
            /*
            // volley request sync
            VolleyRequest checkTokenReq = new VolleyRequest(this);
            checkTokenReq.addHeader("Authorization", "Bearer " + token.getToken());


            // User services will be always available so no need to check for conexion
            checkTokenReq.sendRequestSync("/api/users/all", Request.Method.GET, new JSONObject(), error -> {
                Log.d("Error", error.toString());
                Log.d("Error", error.networkResponse.toString());
                if (error.networkResponse.statusCode >= 400) {
                    Log.d("Error", "Token is invalid");
                    Intent loginIntent = new Intent(context, LoginActivity.class);
                    finish();
                    startActivity(loginIntent);
                }
            });
             */




            db = Room.databaseBuilder(this, SensorDatabase.class, "sensor-final-database").build();
            sensorDao = db.sensorDao();

            dashboardData = new ArrayList<BarEntry>();

            dashboardChart = findViewById(R.id.barChart);

            selectorOptions.add("Last 24 hours");
            selectorOptions.add("Last 7 days");
            selectorOptions.add("Last 31 days");

            // Get the selector from the activity_main.xml
            Button selector = findViewById(R.id.selectOpt);
            selector.setOnClickListener(v -> {
                // Change the selected option
                AlertDialog.Builder builder = new AlertDialog.Builder(this);

                builder.setTitle("Select an option");


                ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, selectorOptions);


                builder.setSingleChoiceItems(adapter, selectedOption, (dialog, which) -> {
                    selectedOption = which;
                    long lastTimestamp;
                    if (selectedOption == 0) {
                        lastTimestamp = new Date().getTime() - 86400 * 1000;
                        numberOfDataPoints = 24;
                    } else if (selectedOption == 1) {
                        lastTimestamp = new Date().getTime() - 604800 * 1000;
                        numberOfDataPoints = 7;
                    } else {
                        lastTimestamp = new Date().getTime() - (long) 2678400 * 1000;
                        numberOfDataPoints = 31;
                    }
                    Thread test = new Thread(new Runnable() {

                        @Override
                        public void run() {
                            dashboardData = new ArrayList<BarEntry>();
                            long yesterday = new Date().getTime() - 86400 * 1000;
                            Log.d("Yesterday", yesterday + "");
                            ArrayList<Pair<Integer, SensorsCollect>> normalizedData = normalizeData(sensorDao.getFromTimestamp(lastTimestamp / 1000));
                            Log.d("NormalizedData", (new Date().getTime() - 86400 * 10000) + " " + new Date().getTime());

                            Log.d("NormalizedData", normalizedData.toString());
                            int prev = 0;
                            for (Pair<Integer, SensorsCollect> element : normalizedData) {
                                if (element.first < prev) {
                                    continue; // Skip the element if it's less than the previous element
                                }
                                for (int i = prev; i < element.first; i++) {
                                    dashboardData.add(new BarEntry(i, 0));
                                }


                                dashboardData.add(new BarEntry(element.first, (float) element.second.heartRate));
                                prev = element.first + 1;

                            }

                            for (int i = prev; i < numberOfDataPoints; i++) {
                                dashboardData.add(new BarEntry(i, 0));
                            }

                            dashboardDataSet = new BarDataSet(dashboardData, "Dashboard Data");

                            dashboardBarData = new BarData(dashboardDataSet);

                            dashboardDataSet.setColors(ColorTemplate.MATERIAL_COLORS);
                            dashboardDataSet.setValueTextColor(Color.BLACK);
                            dashboardDataSet.setValueTextSize(16f);

                            Log.d("Data", dashboardData.toString());

                        }
                    });
                    // Ensure test thread is not started or terminated before starting it
                    test.start();


                    try {
                        // Join the test thread only if it's alive
                        test.join();
                        setDataToChart();

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        // Handle interrupted exception
                        // Thread.currentThread().interrupt();
                    }
                    dialog.dismiss();

                });

                builder.show();
            });


            // End of selector section

            queue = Volley.newRequestQueue(this);
            VolleyRequest req = new VolleyRequest(this);

            // pointer to the current activity
            MainActivity currentActivity = this;
            VolleyCallback callback = new VolleyCallback() {
                @Override
                public void onSuccess(JSONObject response) {
                    Log.d("Response", response.toString());
                    // Get the TextView apiResponse from the activity_main.xml and set the response of email and password

                    try {

                    } catch (Exception e) {
                        Log.d("Error", e.toString());
                    }
                }

                @Override
                public void onError(VolleyError error) {
                    Log.d("Error", error.toString());

                    // if the error is 401, then the token is expired and the user should be redirected to the login Activity
                    if (error.networkResponse.statusCode == 401) {
                        Intent loginIntent = new Intent(currentActivity, LoginActivity.class);
                        finish();
                        startActivity(loginIntent);
                    }

                }
            };
            Intent intent = new Intent(this, SensorsService.class); // val intent = Intent(applicationContext, SensorsService::class.java)


            VolleyRequest syncReq = new VolleyRequest(context);
            Thread onCreateSync = new Thread(new Runnable() {
                @Override
                public void run() {
                    Log.d("SyncData", "Syncing data");
                    SensorsCollect[] syncData = sensorDao.getFirst20NotSent();
                    syncReq.addHeader("Authorization", "Bearer " + token.getToken());
                    Log.d("SyncData", syncData.toString());
                    stopService(intent); // stopService(intent
                    //while (syncData.length > 0) {
                        for (SensorsCollect data : syncData) {
                            // Send the data to the server
                            JSONObject dataJson = data.toJson();
                            //
                            Log.d("SyncData", dataJson.toString());

                            syncReq.sendRequest(callback, "/api/sensors/provision", Request.Method.POST, dataJson);

                            sensorDao.setSent(data.timestamp);
                        }
                        syncData = sensorDao.getFirst20NotSent();
                    //}
                    startForegroundService(intent); // startService(intent)

                }
            });

            //onCreateSync.start();
            // get the connect button from the activity_main.xml and when it is clicked, send a request to the server
            findViewById(R.id.connectButton).setOnClickListener(v -> {
                onCreateSync.start();
                //req.sendRequest(callback, "/api/users/all", Request.Method.GET, null);
            });


            startForegroundService(intent); // startService(intent)
        }
    }


    private void setDataToChart(){
        // Depending on the selected option, we will need to display the data in a different way

        SensorsCollect [] data;
/*
        switch (selectedOption) {
            case 0:
                data = sensorDao.getFromTimestamp(new Date().getTime() - 86400 * 1000);
                break;
            case 1:
                data = sensorDao.getFromTimestamp(new Date().getTime() -  604800 * 1000);
                break;
            case 2:
                data = sensorDao.getFromTimestamp(new Date().getTime() - (long) 2592000 * 1000);
                break;

            default:
                data = sensorDao.getFromTimestamp(new Date().getTime() - 86400 * 1000);
                break;
        }
*/
        ArrayList<String> xAxisLables = new ArrayList();
        if (selectedOption == 0) {
            Log.d("Data", "daily data");
            // Add labels "00:00", "00:59", "01:00", "01:59", etc
            for (int i = 0; i < this.numberOfDataPoints; i++) {
                xAxisLables.add(String.format("%02d:00", i));
            }
        } else if (selectedOption == 1) {
            numberOfDataPoints = 7;
            // Add labels "Monday", "Tuesday", "Wednesday"

            xAxisLables.add("Sunday");
            xAxisLables.add("Monday");
            xAxisLables.add("Tuesday");
            xAxisLables.add("Wednesday");
            xAxisLables.add("Thursday");
            xAxisLables.add("Friday");
            xAxisLables.add("Saturday");

        } else {
            Log.d("Data", "monthly data number of data points: " + this.numberOfDataPoints + "   selected option: " + selectedOption);
            // Add labels "1", "2", "3", "4", etc
            for (int i = 1; i < this.numberOfDataPoints + 1; i++) {
                xAxisLables.add(String.format("%d", i));
            }
        }

        // Set the data to the chart
        // we need intervals of 1 hour, data will be the average of the data from the sensors within that hour
        // data is ordered by timestamp which means that the data is already ordered by hour
        // We need a way to get that batch of data
        // What we need basically is data from timestamp to timestamp + 1 hour
        // meaning a list of lists of data
        // once we have that list of lists of data, we can calculate the average of the data within an inner list
        // then we can add that average to the chart

        // This should be wrapped in a function
        // TODO: Add a check to see if the data is empty if it is then raise an error
        Calendar cal = Calendar.getInstance();



        dashboardChart.getXAxis().setValueFormatter(new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                // Set logic to return the correct label, in my case will be
                // TODO: Put data in 1 hour intervals

                if (selectedOption == 1) {
                    return xAxisLables.get(((int) value) % 7);
                }
                return xAxisLables.get((int) value);
            }
        });
        Log.d("Data", dashboardData.toString());
        dashboardChart.setData(dashboardBarData);

        cal.setTime(new Date()); // Reset the calendar to the current date

    }

    private ArrayList<Pair<Integer, SensorsCollect>> normalizeData(SensorsCollect[] data) {
        // Case 1 and 2 are identical as we manage data in the same way (daily data) however case 0 is hourly data
        HashMap<Integer,SensorsCollect> normalizedData = new HashMap<>();

        Integer lastHour = -1; // Set the current hour to -1 so that the first hour is 0

        if (selectedOption == 2) {
            lastHour = 0; // Set the current day to 0 so that the first day is 1
        }

        Calendar cal = Calendar.getInstance();

        Double averageHeartRate = 0.0;
        Double averageLight = 0.0;
        Integer steps = 0;

        Integer batchElements = 1;

        for (SensorsCollect element : data) {
            if (element.heartRate < 0.1) { // Watch out for floating point errors!!!!!!!!
                continue;
            }
            Log.d("Element", element.toString());
            Date currentDate = new Date(element.timestamp * 1000);
            cal.setTime(currentDate);
            Integer currentHour = -1;
            if (selectedOption == 0) {
                currentHour = cal.get(Calendar.HOUR_OF_DAY);
            }
            else if (selectedOption == 1) {
                currentHour = cal.get(Calendar.DAY_OF_WEEK) - 1;
            }
            else {
                currentHour = cal.get(Calendar.DAY_OF_MONTH);
                if (currentHour > 29) {
                    new Exception("Error: Current hour is greater than 29");
                }
            }

            if (currentHour != lastHour) { // We have a new hour
                Log.d("NewHour", currentHour.toString());
                if (lastHour != -1){
                    // Calculate the average of the data
                    averageHeartRate = averageHeartRate / batchElements;
                    averageLight = averageLight / batchElements;

                    SensorsCollect dataBatch = new SensorsCollect();
                    dataBatch.heartRate = averageHeartRate;
                    dataBatch.light = averageLight;
                    dataBatch.steps = steps;
                    dataBatch.timestamp = batchElements; // hack to calculate average later on

                    // Create the Pair float ArrayList<SensorsCollect> and add it to the dataBatch
                    // Add the pair to the normalizedData
                    SensorsCollect search = normalizedData.get(currentHour);
                    if (normalizedData.get(currentHour) == null) {
                        normalizedData.put(currentHour, dataBatch);
                    }
                    else {
                        dataBatch.heartRate = (dataBatch.heartRate + search.heartRate) / (batchElements + search.timestamp);
                        dataBatch.light = (dataBatch.light + search.light) / (batchElements + search.timestamp);
                        dataBatch.steps = dataBatch.steps; // only the last value is important
                        normalizedData.replace(currentHour, dataBatch);
                    }

                }
                // Re-initialize the variables
                batchElements = 1;
                averageHeartRate = element.heartRate;
                averageLight = element.light;
                steps = element.steps;

                lastHour = currentHour;
            }
            else {
                batchElements += 1;

                averageHeartRate += element.heartRate;
                averageLight += element.light;
                steps = element.steps;
                // Calculate the average of the data
            }

        }

    ArrayList<Pair<Integer, SensorsCollect>> normalizedDataList = new ArrayList<>();
        for (Integer key : normalizedData.keySet()) {
            normalizedDataList.add(new Pair<>(key, normalizedData.get(key)));
        }

        return normalizedDataList;
    }

}