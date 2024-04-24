package com.example.edge_health;
import static com.android.volley.VolleyLog.TAG;

import android.content.Context;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONObject;

public class VolleyRequest {
    private Integer statusCode = null;
    private RequestQueue queue = null;
    private final String url = "http://10.42.0.1:80";

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
    public JSONObject sendRequest(final VolleyCallback callback, String endpoint, int method, JSONObject body) {
        resp = new JSONObject();
        Log.d(TAG, "Llega a la req");
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(method, url + endpoint , null,

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
                // Handle errors here
                throw new RuntimeException("Error: " + error);

            }
        });
        Log.d("ERR","LLEGAAAAA");
        // What does this line do?
        queue.add(jsonObjectRequest);
        Log.d("a",resp.toString());
        return resp;
    }
}
