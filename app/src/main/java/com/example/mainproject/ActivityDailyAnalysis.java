package com.example.mainproject;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ArrayAdapter;

import androidx.appcompat.app.AppCompatActivity;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.List;

public class ActivityDailyAnalysis extends AppCompatActivity {

    private TextView txtSelectedDate, txtTotalExpense, txtExpenseCount, txtAvgExpense, txtHighestExpense;
    private ListView lvExpenses;
    private Button btnSelectDate;

    private DataBaseHelper dbHelper;
    private DecimalFormat currencyFormat = new DecimalFormat("₹0.00");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_analysis); // Make sure the XML file is named activity_analysis.xml

        dbHelper = new DataBaseHelper(this);

        // Bind views
        txtSelectedDate = findViewById(R.id.txt_selected_date);
        txtTotalExpense = findViewById(R.id.txt_total_expense);
        txtExpenseCount = findViewById(R.id.txt_expense_count);
        txtAvgExpense = findViewById(R.id.txt_avg_expense);
        txtHighestExpense = findViewById(R.id.txt_highest_expense);
        lvExpenses = findViewById(R.id.lv_expenses);
        btnSelectDate = findViewById(R.id.btn_select_date);

        // Set button listener
        btnSelectDate.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(ActivityDailyAnalysis.this,
                    (view, year1, monthOfYear, dayOfMonth) -> {
                        String selectedDate = String.format("%04d-%02d-%02d", year1, monthOfYear + 1, dayOfMonth);
                        txtSelectedDate.setText(selectedDate);
                        analyzeDate(selectedDate);
                    }, year, month, day);

            datePickerDialog.show(); // <- THIS IS CRITICAL
        });
    }

    private void showDatePicker() {
        final Calendar calendar = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                ActivityDailyAnalysis.this,
                (view, year, month, dayOfMonth) -> {
                    String date = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth);
                    txtSelectedDate.setText(date);
                    analyzeDate(date);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.show();
    }

    private void analyzeDate(String date) {
        double total = dbHelper.getTotalExpenseByDate(date);
        int count = dbHelper.getExpenseCountByDate(date);
        double highest = dbHelper.getHighestExpenseByDate(date);
        double average = count > 0 ? total / count : 0.0;

        txtTotalExpense.setText(currencyFormat.format(total));
        txtExpenseCount.setText(String.valueOf(count));
        txtHighestExpense.setText(currencyFormat.format(highest));
        txtAvgExpense.setText(currencyFormat.format(average));

        List<String> expensesList = dbHelper.getExpensesByDate(date);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, expensesList);
        lvExpenses.setAdapter(adapter);
    }
}
