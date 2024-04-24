package com.example.edge_health;

import androidx.appcompat.app.AppCompatActivity;

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
    //////////////////////////////////////////

    //Sensores reloj

    ////////////////////////////////////////
    Boolean watch_connected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        testSensorsDataJSON();
        queue = Volley.newRequestQueue(this);
        VolleyRequest req = new VolleyRequest(this);

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
                watch_connected=true;
                Log.d("Message", respuesta.toString());

                SensorsData.getInstance().addDataFromJson(respuesta);



            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void testSensorsDataJSON() {
        SensorsData sensorsData = SensorsData.getInstance();

        try {

            // Test ingesting data to the SensorsData object
            ArrayList<Double> data = new ArrayList<>();
            data.add(1.0);
            data.add(1.0);
            data.add(1.0);
            Date nowDate = new Date();
            Long now = nowDate.getTime() / 1000;
            sensorsData.addGyroscopeData(data, now);
            sensorsData.addGyroscopeData(data, now);

            sensorsData.addAccelerometerData(data, now);
            sensorsData.addAccelerometerData(data, now);

            sensorsData.addLightData(2.0, nowDate);

            sensorsData.addHeartRateData(2, nowDate);

            sensorsData.addStepsData(0);

            JSONObject retreivedData = sensorsData.getSensorsData();
            Log.d("RetreivedData", retreivedData.toString());

            JSONObject json = new JSONObject("{" +
                    "\"Gyroscope\": {" + "\"x\": [1.0, 2.0, 3.0], \"y\": [1.0, 2.0, 3.0], \"z\": [1.0, 2.0, 3.0]" + "}," +
                    "\"GyroscopeTimestamp\": [1, 2, 3]," +
                    "\"Accelerometer\": {" + "\"x\": [1.0, 2.0, 3.0], \"y\": [1.0, 2.0, 3.0], \"z\": [1.0, 2.0, 3.0]" + "}," +
                    "\"AccelerometerTimestamp\": [1, 2, 3]," +
                    "\"Light\": [2.0, 2.0, 2.0]," +
                    "\"LightTimestamp\": [1, 2, 3]," +
                    "\"HeartRate\": [ 2, 2, 2]," +
                    "\"HeartRateTimestamp\": [1, 2, 3]," +
                    "\"Steps\": 0" +
                    "}"
            );


            Log.d("JSON", json.toString());
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

    }

}