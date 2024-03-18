package com.example.edge_health.presentation;
import static com.android.volley.VolleyLog.TAG;

import android.content.Context;
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

public class VolleyRequest {
    private Integer statusCode = null;
    private RequestQueue queue = null;
    private final String url = "http://10.42.0.1:5000/";

    private JSONObject resp = null;

    public VolleyRequest(Context ctx){
        queue = Volley.newRequestQueue(ctx);
    }

    /**
     * Ejemplo de como enviar una petición GET al servidor que está montado en el propio ordenador
     *
     * Creditos: https://stackoverflow.com/questions/26015610/get-http-status-code-for-successful-requests-with-volley
     *
     *
     */
    public JSONObject sendRequest() {
        resp = new JSONObject();
        Log.d(TAG, "Llega a la req");
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,

                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // Handle the successful JSON response here
                        System.out.println("Response: " + response.toString());
                        System.out.println("Status Code: " + statusCode);

                        resp = response;
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Handle errors here
                throw new RuntimeException("Error: " + error);
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
        return resp;
    }
}
