package com.example.health_on_the_edge;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    private Integer statusCode = null;
    private RequestQueue queue = null;
    private final String url = "http://10.42.0.1/";


    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        queue = Volley.newRequestQueue(this);
        VolleyRequest req = new VolleyRequest(this);


        VolleyCallback callback = new VolleyCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                Log.d("Response", response.toString());
                // Get the TextView apiResponse from the activity_main.xml and set the response of email and password
                TextView apiResponse = findViewById(R.id.apiResponse);
                try{
                    apiResponse.setText(response.getJSONArray("users").toString());
                }catch (Exception e){
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

}