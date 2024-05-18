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

public class SingUpActivity extends Activity {

    private EditText fullName, email, password, confirmPassword, username;
    private Button signUp;
    private TextView login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sing_up);

        // Find views
        fullName = findViewById(R.id.editTextFullName);
        username = findViewById(R.id.editTextUsername);
        email = findViewById(R.id.editTextEmail);
        password = findViewById(R.id.editTextPassword);
        confirmPassword = findViewById(R.id.editTextConfirmPassword);
        signUp = findViewById(R.id.buttonSignUp);
        login = findViewById(R.id.textViewLogin);

        // Set click listener for Login button
        login.setOnClickListener(v -> {
            // Login activity
            Intent intent = new Intent(SingUpActivity.this, LoginActivity.class);
            finish();
            startActivity(intent);
        });

        // Set click listener for SignUp button
        signUp.setOnClickListener(v -> {
            // Perform SignUp
            performSignUp();
        });


    }

    private void performSignUp() {
        // Retrieve input values
        String fullNameStr = fullName.getText().toString().trim();
        String emailStr = email.getText().toString().trim();
        String passwordStr = password.getText().toString().trim();
        String confirmPasswordStr = confirmPassword.getText().toString().trim();
        String usernameStr = username.getText().toString().trim();
        // We will send these values to server then server will validate them

        VolleyCallback callback = new VolleyCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                // Handle success response
                // Show a popup with a button to go to login page
                Toast.makeText(SingUpActivity.this, "Success: " + response.toString(),
                                Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(SingUpActivity.this, LoginActivity.class);
                startActivity(intent);
            }

            @Override
            public void onError(VolleyError error) {
                // Handle error response
                // Show a popup
                Toast.makeText(SingUpActivity.this, "Error: " + error.toString(), Toast.LENGTH_SHORT).show();
            }
        };

        // Send request to server
        JSONObject body = new JSONObject();
        try {
            body.put("fullName", fullNameStr);
            body.put("username", usernameStr);
            body.put("email", emailStr);
            body.put("password", passwordStr);
            body.put("confirmPassword", confirmPasswordStr);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Send request to server
        VolleyRequest request = new VolleyRequest(this);
        request.sendRequest(callback, "/api/users/create", Request.Method.POST, body);

    }
}
