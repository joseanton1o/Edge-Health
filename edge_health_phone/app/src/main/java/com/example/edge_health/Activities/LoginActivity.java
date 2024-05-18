package com.example.edge_health.Activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.room.Room;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.example.edge_health.Databases.TokenDatabase;
import com.example.edge_health.R;
import com.example.edge_health.Token;
import com.example.edge_health.VolleyCallback;
import com.example.edge_health.VolleyRequest;

import org.json.JSONObject;

public class LoginActivity extends Activity {

    private EditText username;
    private EditText password;
    private Button login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        TextView singUp = findViewById(R.id.textViewSignUp);
        username = findViewById(R.id.editTextUsername);
        password = findViewById(R.id.editTextPassword);
        login = findViewById(R.id.buttonLogin);

        // Set click listener for Login button
        login.setOnClickListener(v -> {
            // Perform Login
            performLogin();
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            finish();
            startActivity(intent);
        });

        singUp.setOnClickListener(v -> {
            // Sing up activity
            //
            Intent intent = new Intent(LoginActivity.this, SingUpActivity.class);
            finish();
            startActivity(intent);
        });
    }

    private void performLogin() {
        // Guard clause to check if email and password are empty
        if (username.getText().toString().isEmpty() || password.getText().toString().isEmpty()) {
            Toast.makeText(getApplicationContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Perform login

        VolleyCallback callback = new VolleyCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                try {
                    if (response.has("token")) {
                        String token = response.getString("token");
                        Token tokenObj = new Token(token, 1);
                        TokenDatabase db = Room.databaseBuilder(getApplicationContext(),
                                TokenDatabase.class, "token-database").allowMainThreadQueries().build();

                        db.tokenDao().insert(tokenObj);
                    } else {
                        Toast.makeText(getApplicationContext(), "Invalid email or password", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(VolleyError error) {
                Toast.makeText(getApplicationContext(), "Invalid email or password", Toast.LENGTH_SHORT).show();
            }
        };

        // Send request to server
        JSONObject body = new JSONObject();
        try {
            body.put("username", username.getText().toString());
            body.put("password", password.getText().toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Send request to server
        VolleyRequest request = new VolleyRequest(this);
        request.sendRequest(callback, "/api/users/login", Request.Method.POST, body);


    }

}
