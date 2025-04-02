package com.example.mainproject;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Calendar;

public class expense_tracker extends AppCompatActivity {

    private TextView balanceAmount, totalExpensesAmount;
    private EditText expenseName, expenseAmount, expenseDate;
    private Button addExpenseButton, viewExpensesButton;

    private DataBaseHelper dbHelper;
    private double budget = 0.0, balance = 0.0, totalExpenses = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_tracker);

        dbHelper = new DataBaseHelper(this);

        balanceAmount = findViewById(R.id.balance_amount);
        totalExpensesAmount = findViewById(R.id.total_expenses_amount);
        expenseName = findViewById(R.id.expense_name);
        expenseAmount = findViewById(R.id.expense_amount);
        expenseDate = findViewById(R.id.expense_date);
        addExpenseButton = findViewById(R.id.add_expense_button);
        viewExpensesButton = findViewById(R.id.view_expenses_button);

        // Load budget and expenses from DB
        budget = dbHelper.getBudget();
        totalExpenses = dbHelper.getTotalExpenses();
        balance = budget - totalExpenses;

        balanceAmount.setText("₹" + balance);
        totalExpensesAmount.setText("₹" + totalExpenses);

        addExpenseButton.setOnClickListener(v -> addExpense());
        viewExpensesButton.setOnClickListener(v -> startActivity(new Intent(expense_tracker.this, ExpenseListActivity.class)));
        expenseDate.setOnClickListener(v -> showDatePickerDialog());
    }

    private void addExpense() {
        String name = expenseName.getText().toString().trim();
        String amountStr = expenseAmount.getText().toString().trim();
        String date = expenseDate.getText().toString().trim();

        if (name.isEmpty() || amountStr.isEmpty() || date.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount = Double.parseDouble(amountStr);

        if (amount > balance) {
            Toast.makeText(this, "Insufficient Balance!", Toast.LENGTH_SHORT).show();
            return;
        }

        dbHelper.insertExpense(name, amount, date);

        totalExpenses += amount;
        balance = budget - totalExpenses;

        balanceAmount.setText("₹" + balance);
        totalExpensesAmount.setText("₹" + totalExpenses);

        expenseName.setText("");
        expenseAmount.setText("");
        expenseDate.setText("");

        Toast.makeText(this, "Expense Added!", Toast.LENGTH_SHORT).show();
    }

    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String selectedDate = selectedYear + "-" + (selectedMonth + 1) + "-" + selectedDay;
                    expenseDate.setText(selectedDate);
                },
                year, month, day
        );

        datePickerDialog.show();
    }

}
