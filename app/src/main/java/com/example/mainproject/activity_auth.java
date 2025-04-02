package com.example.mainproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class activity_auth extends AppCompatActivity {

    EditText edtEmail, edtPassword;
    Button btnLogin;
    TextView txtSignup;
    SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        edtEmail = findViewById(R.id.edt_email);
        edtPassword = findViewById(R.id.edt_password);
        btnLogin = findViewById(R.id.btn_login);
        txtSignup = findViewById(R.id.txt_signup);

        // Open existing SQLite Database
        db = openOrCreateDatabase("UserDB", MODE_PRIVATE, null);
        SharedPreferences preferences = getSharedPreferences("WalletWhizPrefs", MODE_PRIVATE);
        boolean isBudgetSet = preferences.getBoolean("isBudgetSet", false);

        if (isBudgetSet) {
            // Skip budget screen and go directly to home
            Intent intent = new Intent(activity_auth.this, activity_hp.class);
            startActivity(intent);
            finish();
        } else {
            // Show budget screen for first-time users
            Intent intent = new Intent(activity_auth.this, activity_budget.class);
            startActivity(intent);
            finish();
        }

        btnLogin.setOnClickListener(view -> {
            String email = edtEmail.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(activity_auth.this, "All fields are required!", Toast.LENGTH_SHORT).show();
            } else if (authenticateUser(email, password)) {
                Toast.makeText(activity_auth.this, "Login Successful!", Toast.LENGTH_SHORT).show();

                // Redirect to Activity Budget Screen
                Intent intent = new Intent(activity_auth.this, activity_budget.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(activity_auth.this, "Invalid email or password!", Toast.LENGTH_SHORT).show();
            }
        });

        // Redirect to SignUp Page
        txtSignup.setOnClickListener(view -> {
            Intent intent = new Intent(activity_auth.this, MainActivity.class);
            startActivity(intent);
        });
    }

    // Authenticate User by hashing input password and comparing with stored hash
    private boolean authenticateUser(String email, String password) {
        String hashedPassword = hashPassword(password); // Hash the input password

        Cursor cursor = db.rawQuery("SELECT * FROM users WHERE email = ? AND password = ?",
                new String[]{email, hashedPassword});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    // Hash password using SHA-256 (Same as MainActivity)
    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }
}
