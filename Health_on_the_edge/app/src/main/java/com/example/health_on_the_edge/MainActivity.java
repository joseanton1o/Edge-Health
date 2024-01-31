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
    private final String url = "http://10.42.0.2:5000/";


    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        queue = Volley.newRequestQueue(this);

        sendRequest();
    }

    /**
     * Ejemplo de como enviar una petición GET al servidor que está montado en el propio ordenador
     *
     * Creditos: https://stackoverflow.com/questions/26015610/get-http-status-code-for-successful-requests-with-volley
     *
     *
     */
    private void sendRequest() {

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,

                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // Handle the successful JSON response here
                        System.out.println("Response: " + response.toString());
                        System.out.println("Status Code: " + statusCode);

                        // Get the textView called apiResponse and set the response as its text
                        TextView textView = (TextView) findViewById(R.id.apiResponse);
                        try {
                            textView.setText(response.getString("msg"));
                        } catch (Exception e) {
                            textView.setText(e.toString());
                            System.out.println("Error: " + e);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Handle errors here
                TextView textView = (TextView) findViewById(R.id.apiResponse);

                textView.setText(error.toString());
            }
        }) {
            @Override
            protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                // Get the status code from the response
                statusCode = response.statusCode;
                System.out.println("Status Code: " + statusCode);

                // Here you can perform any action with the response headers and/or body

                return super.parseNetworkResponse(response);
            }
        };
        // What does this line do?
        queue.add(jsonObjectRequest);
    }


}