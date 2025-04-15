package com.example.mainproject;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Calendar;
import java.util.List;
import java.util.ArrayList;
import java.text.DecimalFormat;

public class DailyAnalysisActivity extends AppCompatActivity {

    private EditText etAnalysisMonth;
    private Button btnAnalyze;
    private TextView tvTotalExpense, tvExpenseCount, tvHighestExpense;
    private ListView lvExpenseDetails;
    private DataBaseHelper dbHelper;
    private boolean isDatePickerShowing = false;
    // Formatter for amounts
    private DecimalFormat df = new DecimalFormat("#,##0.00");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Ensure the correct layout file name is used
        setContentView(R.layout.activity_daily_analysis);

        // Initialize views
        etAnalysisMonth = findViewById(R.id.et_analysis_month);
        btnAnalyze = findViewById(R.id.btn_analyze);
        tvTotalExpense = findViewById(R.id.tv_total_expense);
        tvExpenseCount = findViewById(R.id.tv_expense_count);
        tvHighestExpense = findViewById(R.id.tv_highest_expense);
        lvExpenseDetails = findViewById(R.id.lv_expense_details);

        dbHelper = new DataBaseHelper(this);

        // Set up Month Picker for the analysis month field.
        // Although DatePickerDialog returns a full date, we'll use only the "YYYY-MM" portion.
        etAnalysisMonth.setOnClickListener(v -> {
            if (!isDatePickerShowing) {
                showMonthPickerDialog();
            }
        });

        // Analyze button: perform monthly expense analysis
        btnAnalyze.setOnClickListener(v -> {
            Toast.makeText(this, "Analyze button clicked", Toast.LENGTH_SHORT).show(); // Debug message
            String selectedMonth = etAnalysisMonth.getText().toString().trim();
            if (selectedMonth.isEmpty()) {
                etAnalysisMonth.setError("Please select a month");
            } else {
                analyzeMonthlyExpenses(selectedMonth);
            }
        });
    }

    // Show DatePickerDialog and format output as "YYYY-MM" (ignoring the day)
    private void showMonthPickerDialog() {
        isDatePickerShowing = true;
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    // Format month with 2 digits and ignore the day.
                    String formattedMonth = String.format("%02d", selectedMonth + 1);
                    String monthValue = selectedYear + "-" + formattedMonth;
                    etAnalysisMonth.setText(monthValue);
                    Toast.makeText(this, "Month Selected: " + monthValue, Toast.LENGTH_SHORT).show(); // Debug message
                },
                year, month, calendar.get(Calendar.DAY_OF_MONTH)
        );
        // Disable future dates
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.setOnDismissListener(dialog -> isDatePickerShowing = false);
        datePickerDialog.show();
    }

    // Perform monthly expense analysis using the month prefix in date strings (e.g., "2025-04")
    private void analyzeMonthlyExpenses(String month) {
        // Retrieve data from the database helper methods using the "YYYY-MM" prefix.
        double totalExpense = dbHelper.getTotalExpenseByMonth(month);
        int expenseCount = dbHelper.getExpenseCountByMonth(month);
        double highestExpense = dbHelper.getHighestExpenseByMonth(month);
        List<String> expenseDetails = dbHelper.getExpensesByMonth(month);

        // Debug messages to verify data retrieval
        Toast.makeText(this, "Total: " + totalExpense + ", Count: " + expenseCount, Toast.LENGTH_SHORT).show();

        // Update UI with formatted results
        tvTotalExpense.setText("Total Expense: ₹" + df.format(totalExpense));
        tvExpenseCount.setText("Number of Expenses: " + expenseCount);
        tvHighestExpense.setText("Highest Expense: ₹" + df.format(highestExpense));

        // Display the list of expense details. If none, show a placeholder message.
        if (expenseDetails == null || expenseDetails.isEmpty()) {
            expenseDetails = new ArrayList<>();
            expenseDetails.add("No expenses found for this month.");
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, expenseDetails);
        lvExpenseDetails.setAdapter(adapter);
    }
}
