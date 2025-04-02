package com.example.mainproject;

import android.content.ContentValues;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Calendar;

public class expense_tracker extends AppCompatActivity {

    private TextView balanceAmount, totalExpensesAmount;
    private EditText expenseName, expenseAmount, expenseDate;
    private Button addExpenseButton;

    private SQLiteDatabase database;
    private double budget = 0.0, balance = 0.0, totalExpenses = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_tracker);

        balanceAmount = findViewById(R.id.balance_amount);
        totalExpensesAmount = findViewById(R.id.total_expenses_amount);
        expenseName = findViewById(R.id.expense_name);
        expenseAmount = findViewById(R.id.expense_amount);
        expenseDate = findViewById(R.id.expense_date);
        addExpenseButton = findViewById(R.id.add_expense_button);

        database = openOrCreateDatabase("WalletWhizDB", MODE_PRIVATE, null);
        createTables();

        // Get budget from "activity_hp"
        Intent intent = getIntent();
        if (intent.hasExtra("BUDGET_AMOUNT")) {
            budget = intent.getDoubleExtra("BUDGET_AMOUNT", 0.0);
            //saveBudgetIfNotExists();
            Toast.makeText(this, "Received Budget: "+budget, Toast.LENGTH_LONG).show();
            updateBudget(budget);
        }

        // Load saved budget and expenses (only totals now)
        loadBudget();
        loadExpenses();

        addExpenseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addExpense();
            }
        });

        expenseDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

    }

    private void createTables() {
        try {
            database.execSQL("CREATE TABLE IF NOT EXISTS Budget(id INTEGER PRIMARY KEY, amount DOUBLE);");
            database.execSQL("CREATE TABLE IF NOT EXISTS Expenses(id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, amount DOUBLE, date TEXT);");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**private void saveBudgetIfNotExists() {
        Cursor cursor = database.rawQuery("SELECT amount FROM Budget LIMIT 1;", null);
        if (!cursor.moveToFirst()) {
            ContentValues values = new ContentValues();
            values.put("amount", budget);
            database.insert("Budget", null, values);
        }
        cursor.close();
    }**/

    private void updateBudget(double newBudget) {
        ContentValues values = new ContentValues();
        values.put("amount", newBudget);

        int rowsAffected = database.update("Budget", values, null, null);

        if (rowsAffected == 0) {
            // If no existing budget, insert new one
            database.insert("Budget", null, values);
        }

        loadBudget(); // Refresh displayed budget
    }

    private void loadBudget() {
        Cursor cursor = database.rawQuery("SELECT amount FROM Budget LIMIT 1;", null);
        if (cursor.moveToFirst()) {
            budget = cursor.getDouble(0);
        }
        cursor.close();

        balance = budget; // Initially, balance equals budget (expenses will deduct later)
        balanceAmount.setText("₹" + balance);
    }

    private void loadExpenses() {
        Cursor cursor = database.rawQuery("SELECT amount FROM Expenses;", null);
        totalExpenses = 0.0;
        while (cursor.moveToNext()) {
            totalExpenses += cursor.getDouble(0);
        }
        cursor.close();

        balance = budget - totalExpenses;
        totalExpensesAmount.setText("₹" + totalExpenses);
        balanceAmount.setText("₹" + balance);
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

        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("amount", amount);
        values.put("date", date);
        database.insert("Expenses", null, values);

        totalExpenses += amount;
        balance = budget - totalExpenses;

        balanceAmount.setText("₹" + balance);
        totalExpensesAmount.setText("₹" + totalExpenses);

        Toast.makeText(this, "Expense Added!", Toast.LENGTH_SHORT).show();

        // Clear input fields
        expenseName.setText("");
        expenseAmount.setText("");
        expenseDate.setText("");
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
