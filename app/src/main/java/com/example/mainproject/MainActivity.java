package com.example.mainproject;

import android.content.ContentValues;
import android.content.SharedPreferences;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Patterns;
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

        // Open or create the SQLite database and ensure the users table exists
        db = openOrCreateDatabase("UserDB", MODE_PRIVATE, null);
        db.execSQL("CREATE TABLE IF NOT EXISTS users(id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, email TEXT UNIQUE, password TEXT)");

        btnSignup.setOnClickListener(view -> {
            // Reset any previous errors
            edtName.setError(null);
            edtEmail.setError(null);
            edtPassword.setError(null);
            edtConfirmPassword.setError(null);

            String name = edtName.getText().toString().trim();
            String email = edtEmail.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();
            String confirmPassword = edtConfirmPassword.getText().toString().trim();
            boolean valid = true;

            // Validate Name
            if (name.isEmpty()) {
                edtName.setError("Name is required!");
                valid = false;
            } else if (!isValidName(name)) {
                edtName.setError("Name must be at least 3 characters and only contain letters and spaces!");
                valid = false;
            }

            // Validate Email
            if (email.isEmpty()) {
                edtEmail.setError("Email is required!");
                valid = false;
            } else if (!isValidEmail(email)) {
                edtEmail.setError("Please enter a valid email!");
                valid = false;
            }

            // Validate Password and Confirm Password
            if (password.isEmpty()) {
                edtPassword.setError("Password is required!");
                valid = false;
            }
            if (confirmPassword.isEmpty()) {
                edtConfirmPassword.setError("Confirm password is required!");
                valid = false;
            }
            if (!password.equals(confirmPassword)) {
                edtConfirmPassword.setError("Passwords do not match!");
                valid = false;
            } else if (!isValidPassword(password)) {
                edtPassword.setError("Password must be at least 8 characters long, with uppercase, lowercase, and a digit!");
                valid = false;
            }

            // Check if email already exists
            if (checkUserExists(email)) {
                edtEmail.setError("Email already registered!");
                valid = false;
            }

            // If any validation error exists, don't proceed further
            if (!valid) {
                return;
            }

            // Hash the password
            String hashedPassword = hashPassword(password);
            if (hashedPassword == null) {
                Toast.makeText(MainActivity.this, "Error hashing password!", Toast.LENGTH_SHORT).show();
                return;
            }
            // Insert user into the database
            if (insertUser(name, email, hashedPassword)) {
                // Save user details in SharedPreferences
                SharedPreferences preferences = getSharedPreferences("WalletWhizPrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("username", name);
                editor.putString("email", email);
                editor.apply();

                Toast.makeText(MainActivity.this, "Signup Successful!", Toast.LENGTH_SHORT).show();

                // Redirect to the login screen (activity_auth.java)
                Intent intent = new Intent(MainActivity.this, activity_auth.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(MainActivity.this, "Signup Failed!", Toast.LENGTH_SHORT).show();
            }
        });

        // Redirect to the login screen if the user already has an account
        txtLogin.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, activity_auth.class);
            startActivity(intent);
            finish();
        });
    }

    // Insert a new user into the database
    private boolean insertUser(String name, String email, String password) {
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("email", email);
        values.put("password", password);
        return db.insert("users", null, values) != -1;
    }

    // Check if a user with the given email already exists
    private boolean checkUserExists(String email) {
        Cursor cursor = db.rawQuery("SELECT * FROM users WHERE email = ?", new String[]{email});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
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

    // Validate email format
    private boolean isValidEmail(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    // Validate name: at least 3 characters long and contains only letters and spaces.
    private boolean isValidName(String name) {
        return name.length() >= 3 && name.matches("^[A-Za-z\\s]+$");
    }

    // Validate password strength: minimum 8 characters, at least one uppercase letter, one lowercase letter, and one number.
    private boolean isValidPassword(String password) {
        if (password.length() < 8) {
            return false;
        }
        if (!password.matches(".*[A-Z].*")) {
            return false;
        }
        if (!password.matches(".*[a-z].*")) {
            return false;
        }
        if (!password.matches(".*[0-9].*")) {
            return false;
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (db != null && db.isOpen()) {
            db.close();
        }
    }
}
