package com.example.mainproject;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

public class expense_tracker extends AppCompatActivity {

    private TextView balanceAmount, totalExpensesAmount;
    private EditText expenseName, expenseAmount, expenseDate;
    private Button addExpenseButton;
    private ListView expenseList;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> expenseItems;

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
        expenseList = findViewById(R.id.expense_list);

        expenseItems = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, expenseItems);
        expenseList.setAdapter(adapter);

        database = openOrCreateDatabase("WalletWhizDB", MODE_PRIVATE, null);
        createTables();

        // Get budget from "activity_hp"
        Intent intent = getIntent();
        if (intent.hasExtra("BUDGET_AMOUNT")) {
            budget = intent.getDoubleExtra("BUDGET_AMOUNT", 0.0);
            saveBudgetIfNotExists();
        }

        // Load saved budget and expenses
        loadBudget();
        loadExpenses();

        addExpenseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addExpense();
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

    private void saveBudgetIfNotExists() {
        Cursor cursor = database.rawQuery("SELECT amount FROM Budget LIMIT 1;", null);
        if (!cursor.moveToFirst()) {
            ContentValues values = new ContentValues();
            values.put("amount", budget);
            database.insert("Budget", null, values);
        }
        cursor.close();
    }

    private void loadBudget() {
        Cursor cursor = database.rawQuery("SELECT amount FROM Budget LIMIT 1;", null);
        if (cursor.moveToFirst()) {
            budget = cursor.getDouble(0);
        }
        cursor.close();

        balance = budget;
        balanceAmount.setText("₹" + balance);
    }

    private void loadExpenses() {
        Cursor cursor = database.rawQuery("SELECT name, amount, date FROM Expenses;", null);
        totalExpenses = 0.0;
        expenseItems.clear();

        while (cursor.moveToNext()) {
            String name = cursor.getString(0);
            double amount = cursor.getDouble(1);
            String date = cursor.getString(2);
            totalExpenses += amount;
            expenseItems.add(name + " - ₹" + amount + " (" + date + ")");
        }

        cursor.close();
        balance = budget - totalExpenses;
        totalExpensesAmount.setText("₹" + totalExpenses);
        balanceAmount.setText("₹" + balance);
        adapter.notifyDataSetChanged();
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

        expenseItems.add(name + " - ₹" + amount + " (" + date + ")");
        adapter.notifyDataSetChanged();

        expenseName.setText("");
        expenseAmount.setText("");
        expenseDate.setText("");
    }
}
