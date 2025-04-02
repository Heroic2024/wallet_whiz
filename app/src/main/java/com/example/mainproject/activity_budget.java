package com.example.mainproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.database.sqlite.SQLiteDatabase;
import android.content.ContentValues;
import android.widget.Toast;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatActivity;

public class activity_budget extends AppCompatActivity {

    private EditText edtBudget;
    private Button btnSetBudget;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget);

        SharedPreferences preferences = getSharedPreferences("WalletWhizPrefs", MODE_PRIVATE);
        boolean isBudgetSet = preferences.getBoolean("isBudgetSet", false);

        edtBudget = findViewById(R.id.edt_budget);
        btnSetBudget = findViewById(R.id.btn_set_budget);

        btnSetBudget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String budget = edtBudget.getText().toString();

                if (budget.isEmpty()) {
                    Toast.makeText(activity_budget.this, "Please enter a budget", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Save budget to SharedPreferences
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean("isBudgetSet", true);
                editor.putString("budgetValue", budget);  // Store budget value
                editor.apply();

                // Save budget in database
                SQLiteDatabase database = openOrCreateDatabase("WalletWhizDB", MODE_PRIVATE, null);
                ContentValues values = new ContentValues();
                values.put("amount", Double.parseDouble(budget));

                int rowsAffected = database.update("Budget", values, null, null);
                if (rowsAffected == 0) {
                    database.insert("Budget", null, values);
                }

                database.close();

                // Move to home page
                Intent intent = new Intent(activity_budget.this, activity_hp.class);
                intent.putExtra("BUDGET_AMOUNT", budget);
                startActivity(intent);
                finish();
            }
        });
    }
}
