package com.example.mainproject;

import android.content.ContentValues;
import android.content.Intent;
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

public class MainActivity extends AppCompatActivity {

    EditText edtName, edtEmail, edtPassword, edtConfirmPassword;
    Button btnSignup;
    TextView txtLogin;
    SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        edtName = findViewById(R.id.edt_name);
        edtEmail = findViewById(R.id.edt_signup_email);
        edtPassword = findViewById(R.id.edt_signup_password);
        edtConfirmPassword = findViewById(R.id.edt_confirm_password);
        btnSignup = findViewById(R.id.btn_signup);
        txtLogin = findViewById(R.id.txt_login);

        // Initialize SQLite Database
        db = openOrCreateDatabase("UserDB", MODE_PRIVATE, null);
        db.execSQL("CREATE TABLE IF NOT EXISTS users(id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, email TEXT UNIQUE, password TEXT)");

        btnSignup.setOnClickListener(view -> {
            String name = edtName.getText().toString().trim();
            String email = edtEmail.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();
            String confirmPassword = edtConfirmPassword.getText().toString().trim();

            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(MainActivity.this, "All fields are required!", Toast.LENGTH_SHORT).show();
            } else if (!password.equals(confirmPassword)) {
                Toast.makeText(MainActivity.this, "Passwords do not match!", Toast.LENGTH_SHORT).show();
            } else if (checkUserExists(email)) {
                Toast.makeText(MainActivity.this, "Email already registered!", Toast.LENGTH_SHORT).show();
            } else {
                String hashedPassword = hashPassword(password);
                if (hashedPassword == null) {
                    Toast.makeText(MainActivity.this, "Error hashing password!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (insertUser(name, email, hashedPassword)) {
                    Toast.makeText(MainActivity.this, "Signup Successful!", Toast.LENGTH_SHORT).show();

                    // Redirect to Login Activity
                    Intent intent = new Intent(MainActivity.this, activity_auth.class);
                    startActivity(intent);
                    finish(); // Close Signup Activity
                } else {
                    Toast.makeText(MainActivity.this, "Signup Failed!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Redirect to Login Screen
        txtLogin.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, activity_auth.class);
            startActivity(intent);
            finish();
        });
    }

    // Insert user data into SQLite
    private boolean insertUser(String name, String email, String password) {
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("email", email);
        values.put("password", password);
        return db.insert("users", null, values) != -1;
    }

    // Check if the email already exists
    private boolean checkUserExists(String email) {
        Cursor cursor = db.rawQuery("SELECT * FROM users WHERE email = ?", new String[]{email});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    // Secure password hashing with SHA-256
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (db != null && db.isOpen()) {
            db.close();
        }
    }
}
