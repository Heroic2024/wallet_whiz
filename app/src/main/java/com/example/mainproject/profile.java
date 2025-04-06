package com.example.mainproject;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class profile extends AppCompatActivity {

    private TextView txtUsername, txtEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize views
        txtUsername = findViewById(R.id.txt_username);
        txtEmail = findViewById(R.id.txt_email);

        // Retrieve saved user data from SharedPreferences
        SharedPreferences preferences = getSharedPreferences("WalletWhizPrefs", MODE_PRIVATE);
        String username = preferences.getString("username", "User Name");
        String email = preferences.getString("email", "useremail@example.com");

        // Set the retrieved values to the TextViews
        txtUsername.setText(username);
        txtEmail.setText(email);
    }
}
