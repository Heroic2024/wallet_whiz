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

        // Open the SQLite database
        db = openOrCreateDatabase("UserDB", MODE_PRIVATE, null);

        btnLogin.setOnClickListener(view -> {
            // Clear any previous errors
            edtEmail.setError(null);
            edtPassword.setError(null);

            String email = edtEmail.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();
            boolean valid = true;

            // Check if email field is empty
            if (email.isEmpty()) {
                edtEmail.setError("Email is required!");
                valid = false;
            } else if (!isValidEmail(email)) {
                edtEmail.setError("Please enter a valid email!");
                valid = false;
            }

            // Check if password field is empty
            if (password.isEmpty()) {
                edtPassword.setError("Password is required!");
                valid = false;
            }

            // If any validation error exists, exit without proceeding further
            if (!valid) {
                return;
            }

            // Hash the entered password
            String hashedPassword = hashPassword(password);
            Cursor cursor = db.rawQuery("SELECT name FROM users WHERE email = ? AND password = ?",
                    new String[]{email, hashedPassword});

            if (cursor.moveToFirst()) {
                String name = cursor.getString(0);

                // Save login details to SharedPreferences
                SharedPreferences preferences = getSharedPreferences("WalletWhizPrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("username", name);
                editor.putString("email", email);
                editor.apply();

                Toast.makeText(activity_auth.this, "Login Successful!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(activity_auth.this, activity_budget.class);
                startActivity(intent);
                finish();
            } else {
                // Show errors inline on both fields when credentials are invalid
                edtEmail.setError("Invalid email or password!");
                edtPassword.setError("Invalid email or password!");
            }
            cursor.close();
        });

        // Redirect to the signup screen if the user doesn't have an account
        txtSignup.setOnClickListener(view -> {
            Intent intent = new Intent(activity_auth.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }

    // Validate email format using patterns
    private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    // Hash the password using SHA-256
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
