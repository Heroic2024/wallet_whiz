package com.example.mainproject;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Calendar;

public class expense_tracker extends AppCompatActivity {

    private TextView balanceAmount, totalExpensesAmount;
    private EditText expenseName, expenseAmount, expenseDate;
    private Button addExpenseButton, viewExpensesButton;

    private DataBaseHelper dbHelper;
    private double budget = 0.0, balance = 0.0, totalExpenses = 0.0;

    // Flag to ensure DatePickerDialog is shown only once at a time.
    private boolean isDatePickerShowing = false;

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

        // When date field is clicked, show DatePickerDialog (with single-time triggering).
        expenseDate.setOnClickListener(v -> {
            if (!isDatePickerShowing) {
                showDatePickerDialog();
            }
        });
    }

    private void addExpense() {
        // Clear previous error messages
        expenseName.setError(null);
        expenseAmount.setError(null);
        expenseDate.setError(null);

        String name = expenseName.getText().toString().trim();
        String amountStr = expenseAmount.getText().toString().trim();
        String date = expenseDate.getText().toString().trim();
        boolean valid = true;

        // Validate the input fields
        if (name.isEmpty()) {
            expenseName.setError("Expense name is required");
            valid = false;
        }
        if (amountStr.isEmpty()) {
            expenseAmount.setError("Expense amount is required");
            valid = false;
        }
        if (date.isEmpty()) {
            expenseDate.setError("Expense date is required");
            valid = false;
        }
        if (!valid) {
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            expenseAmount.setError("Enter a valid number");
            return;
        }

        if (amount > balance) {
            expenseAmount.setError("Expense exceeds available balance");
            Toast.makeText(this, "Insufficient Balance!", Toast.LENGTH_SHORT).show();
            return;
        }

        // If the expense amount is equal to or more than 80% of the current balance, show a warning alert
        if (amount >= 0.8 * balance) {
            new AlertDialog.Builder(this)
                    .setTitle("High Expense Warning")
                    .setMessage("The expense amount is 80% or more of your current balance. Do you want to continue?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            proceedAddingExpense(name, amount, date);
                        }
                    })
                    .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                    .show();
        } else {
            // Proceed normally if the warning condition is not met.
            proceedAddingExpense(name, amount, date);
        }
    }

    // Method to insert expense into the database and update UI.
    private void proceedAddingExpense(String name, double amount, String date) {
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

    // Show date picker with future dates disabled; ensure it's shown only once per tap.
    private void showDatePickerDialog() {
        isDatePickerShowing = true;
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
        // Disable future dates
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.setOnDismissListener(dialog -> isDatePickerShowing = false);
        datePickerDialog.show();
    }
}
