package com.example.mainproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
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

        // Retrieve budget amount from Intent
        String budget = getIntent().getStringExtra("BUDGET_AMOUNT");

        // Set balance to budget value (if it's not null)
        if (budget != null && !budget.isEmpty()) {
            txtBalance.setText("₹" + budget);
        }

        // Click event to redirect to Expense Tracker page
        btnAddExpense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity_hp.this, expense_tracker.class);
                intent.putExtra("BUDGET_AMOUNT", budget != null ? Double.parseDouble(budget) : 0.0);
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
