package com.example.mainproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.database.sqlite.SQLiteDatabase;
import android.content.SharedPreferences;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.database.Cursor;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class activity_hp extends AppCompatActivity {

    private TextView txtBalance;
    private LinearLayout btnAddExpense, btnTransfer, btnOthers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hp);

        txtBalance = findViewById(R.id.txt_balance);
        btnAddExpense = findViewById(R.id.btn_add_expense);
        btnTransfer = findViewById(R.id.btn_transfer); // "History" button
        btnOthers = findViewById(R.id.btn_others);

        SharedPreferences preferences = getSharedPreferences("WalletWhizPrefs", MODE_PRIVATE);
        String budget = preferences.getString("budgetValue", null);

        // If budget is not found in SharedPreferences, fetch from the database
        if (budget == null || budget.isEmpty() || budget.equals("0")) {
            SQLiteDatabase database = null;
            Cursor cursor = null;
            try {
                database = openOrCreateDatabase("WalletWhizDB", MODE_PRIVATE, null);
                cursor = database.rawQuery("SELECT amount FROM Budget LIMIT 1;", null);
                if (cursor.moveToFirst()) {
                    budget = String.valueOf(cursor.getDouble(0));
                }
            } finally {
                if (cursor != null) cursor.close();
                if (database != null) database.close();
            }
        }

        // Check if an Intent contains a new budget amount
        String intentBudget = getIntent().getStringExtra("BUDGET_AMOUNT");
        if (intentBudget != null && !intentBudget.isEmpty()) {
            budget = intentBudget;
            // Save it to SharedPreferences so it's persistent
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("budgetValue", budget);
            editor.apply();
        }

        // Set budget to the text field
        if (txtBalance != null) {
            txtBalance.setText("₹" + budget);
        } else {
            Toast.makeText(this, "Error: txtBalance not initialized", Toast.LENGTH_SHORT).show();
        }

        // Capture the computed budget in a final variable for inner classes
        final String finalBudget = budget;

        // Click event to redirect to Expense Tracker page
        btnAddExpense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                double budgetAmount = 0.0;
                try {
                    if (finalBudget != null && !finalBudget.isEmpty()) {
                        budgetAmount = Double.parseDouble(finalBudget);
                    }
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    budgetAmount = 0.0;
                }
                Intent intent = new Intent(activity_hp.this, expense_tracker.class);
                intent.putExtra("BUDGET_AMOUNT", budgetAmount);
                startActivity(intent);
            }
        });

        // Click event to redirect to Expense List (History) page
        btnTransfer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity_hp.this, ExpenseListActivity.class);
                startActivity(intent);
            }
        });

        // Click event to redirect to Others page
        btnOthers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity_hp.this, others.class);
                startActivity(intent);
            }
        });
    }
}
