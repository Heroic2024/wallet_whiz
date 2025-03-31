package com.example.mainproject;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mainproject.R;

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

        // Login Button Click Listener
        btnLogin.setOnClickListener(view -> {
            String email = edtEmail.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(activity_auth.this, "All fields are required!", Toast.LENGTH_SHORT).show();
            } else if (authenticateUser(email, password)) {
                Toast.makeText(activity_auth.this, "Login Successful!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(activity_auth.this, MainActivity.class);
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

    // Check if user exists in the database
    private boolean authenticateUser(String email, String password) {
        Cursor cursor = db.rawQuery("SELECT * FROM users WHERE email = ? AND password = ?", new String[]{email, password});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }
}
