package com.example.edge_health;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.MessageClient;
import com.google.android.gms.wearable.MessageEvent;
public class MainActivity extends AppCompatActivity implements MessageClient.OnMessageReceivedListener{
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = Room.databaseBuilder(this, SensorDatabase.class, "sensor-database-final").build();
        sensorDao = db.sensorDao();

        queue = Volley.newRequestQueue(this);
        VolleyRequest req = new VolleyRequest(this);

        // TODO: New user database where to store the authentication jwt token and the user data
        // If there's no user data, then the user should be redirected to the register Activity
        // Where a form will be displayed to the user to fill in the data
        // If the user data is already stored but the token is either expired or not present,
        // then the user should be redirected to the login Activity

        Thread onCreateSync = new Thread(new Runnable() {
            @Override
            public void run() {
                SensorsCollect[] syncData = sensorDao.getFirst20NotSent();

                while (syncData.length < 20){


                    syncData = sensorDao.getFirst20NotSent();
                }
            }
        });

        Wearable.getMessageClient(this).addListener(this);

        VolleyCallback callback = new VolleyCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                Log.d("Response", response.toString());
                // Get the TextView apiResponse from the activity_main.xml and set the response of email and password
                TextView apiResponse = findViewById(R.id.apiResponse);
                try {
                    apiResponse.setText(response.getJSONArray("users").toString());
                } catch (Exception e) {
                    Log.d("Error", e.toString());
                }
            }

            @Override
            public void onError(VolleyError error) {
                Log.d("Error", error.toString());
            }
        };

        // get the connect button from the activity_main.xml and when it is clicked, send a request to the server
        findViewById(R.id.connectButton).setOnClickListener(v -> {
            req.sendRequest(callback, "/api/users/all", Request.Method.GET, null);
        });



    }
    @Override
    public void onMessageReceived(MessageEvent messageEvent) {

        if (messageEvent.getPath().equals("/sensores")) {
            try {
                JSONObject respuesta = new JSONObject(new String(messageEvent.getData()));
                Log.d("Message", respuesta.toString());
                watchSensors = new SensorsCollect(new String(messageEvent.getData()));

                // Insert the data into the database
                SensorsCollect finalwatchSensors = watchSensors;
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        sensorDao.insert(finalwatchSensors);
                    }
                });


                if (prevWatchSensors != null) {
                    if (!watchSensors.equals(prevWatchSensors)) {
                        t.start();
                    }
                } else {
                    t.start();
                }

                TextView gx = findViewById(R.id.gX);
                TextView gy = findViewById(R.id.gY);
                TextView gz = findViewById(R.id.gZ);

                gx.setText(String.format("%.2f", watchSensors.gyroscopeX));
                gy.setText(String.format("%.2f", watchSensors.gyroscopeY));
                gz.setText(String.format("%.2f", watchSensors.gyroscopeZ));

                TextView ax = findViewById(R.id.aX);
                TextView ay = findViewById(R.id.aY);
                TextView az = findViewById(R.id.aZ);

                ax.setText(String.format("%.2f", watchSensors.accelerometerX));
                ay.setText(String.format("%.2f", watchSensors.accelerometerY));
                az.setText(String.format("%.2f", watchSensors.accelerometerZ));

                TextView hr = findViewById(R.id.heartRate);
                hr.setText(String.format("%.2f", watchSensors.heartRate));


                TextView light = findViewById(R.id.light);
                light.setText(String.format("%.2f", watchSensors.light));

                TextView steps = findViewById(R.id.stepCounter);
                steps.setText(new Integer(watchSensors.steps).toString());



            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}