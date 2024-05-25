package com.example.edge_health;
import static com.android.volley.VolleyLog.TAG;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class VolleyRequest {
    private Integer statusCode = null;
    private RequestQueue queue = null;
    private final String url = "http://192.168.0.34:80";

    private JSONObject resp = null;
    private Map<String, String> headers = new HashMap<String, String>();
    public VolleyRequest(Context ctx){
        queue = Volley.newRequestQueue(ctx);
        // Default headers for the request
        headers.put("Content-Type", "application/json");
        headers.put("Connection", "keep-alive");
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }
    public void addHeader(String key, String value){
        headers.put(key, value);
    }
    /**
     * Ejemplo de como enviar una petición GET al servidor que está montado en el propio ordenador
     *
     * Creditos: https://stackoverflow.com/questions/26015610/get-http-status-code-for-successful-requests-with-volley
     *
     *
     */
    public JSONObject sendRequest(final VolleyCallback callback, String endpoint, int method, JSONObject body) {
        resp = new JSONObject();
        Log.d(TAG, "Llega a la req");
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(method, url + endpoint , body,

                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // Handle the successful JSON response here
                        System.out.println("Response: " + response.toString());

                        resp = response;
                        callback.onSuccess(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                callback.onError(error);

            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return headers;
            }
        };
        Log.d("ERR","LLEGAAAAA");
        // What does this line do?
        queue.add(jsonObjectRequest);
        Log.d("a",resp.toString());
        return resp;
    }
}
