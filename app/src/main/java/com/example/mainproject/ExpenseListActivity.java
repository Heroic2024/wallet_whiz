package com.example.mainproject;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

public class ExpenseListActivity extends AppCompatActivity {

    private ListView expenseList;
    private ArrayList<String> expenseItems;
    // New list to store the unique row ids for each expense
    private ArrayList<Long> expenseIds;
    private ArrayAdapter<String> adapter;
    private SQLiteDatabase database;
    // Variable to keep track of the selected item
    private int selectedIndex = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_list);

        expenseList = findViewById(R.id.expense_list);
        expenseItems = new ArrayList<>();
        expenseIds = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, expenseItems);
        expenseList.setAdapter(adapter);

        database = openOrCreateDatabase("WalletWhizDB", MODE_PRIVATE, null);
        // Ensure the Expenses table exists. Adjust schema as needed.
        database.execSQL("CREATE TABLE IF NOT EXISTS Expenses(rowid INTEGER PRIMARY KEY, name TEXT, amount REAL, date TEXT)");

        loadExpenses();

        // Handle list item selection
        expenseList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedIndex = position;
                // Optionally, provide a visual indication for selection.
            }
        });

        // Modify button handling
        Button btnModify = findViewById(R.id.btn_modify);
        btnModify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (selectedIndex == -1) {
                    Toast.makeText(ExpenseListActivity.this, "Please select an expense to modify", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Get current expense details
                long expenseId = expenseIds.get(selectedIndex);
                String currentEntry = expenseItems.get(selectedIndex);
                // Assume the format: "name - ₹amount (date)"
                // We try to parse this roughly:
                String[] parts = currentEntry.split(" - ₹| \\(");
                if (parts.length < 3) {
                    Toast.makeText(ExpenseListActivity.this, "Selected expense format is invalid", Toast.LENGTH_SHORT).show();
                    return;
                }
                String currentName = parts[0];
                String currentAmount = parts[1];
                String currentDate = parts[2].replace(")", "");

                // Build a dialog to modify the expense.
                AlertDialog.Builder builder = new AlertDialog.Builder(ExpenseListActivity.this);
                builder.setTitle("Modify Expense");

                // Create a layout to hold the inputs
                View dialogView = getLayoutInflater().inflate(R.layout.dialog_modify_expense, null);
                final EditText edtName = dialogView.findViewById(R.id.dialog_edt_name);
                final EditText edtAmount = dialogView.findViewById(R.id.dialog_edt_amount);
                final EditText edtDate = dialogView.findViewById(R.id.dialog_edt_date);

                // Pre-fill with current values
                edtName.setText(currentName);
                edtAmount.setText(currentAmount);
                edtDate.setText(currentDate);

                builder.setView(dialogView);

                builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String newName = edtName.getText().toString().trim();
                        String newAmountStr = edtAmount.getText().toString().trim();
                        String newDate = edtDate.getText().toString().trim();

                        if (newName.isEmpty() || newAmountStr.isEmpty() || newDate.isEmpty()) {
                            Toast.makeText(ExpenseListActivity.this, "All fields are required", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        double newAmount;
                        try {
                            newAmount = Double.parseDouble(newAmountStr);
                        } catch (NumberFormatException e) {
                            Toast.makeText(ExpenseListActivity.this, "Invalid amount", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        ContentValues cv = new ContentValues();
                        cv.put("name", newName);
                        cv.put("amount", newAmount);
                        cv.put("date", newDate);

                        int rows = database.update("Expenses", cv, "rowid=?", new String[]{String.valueOf(expenseId)});
                        if (rows > 0) {
                            Toast.makeText(ExpenseListActivity.this, "Expense updated", Toast.LENGTH_SHORT).show();
                            loadExpenses();
                        } else {
                            Toast.makeText(ExpenseListActivity.this, "Update failed", Toast.LENGTH_SHORT).show();
                        }
                        selectedIndex = -1;
                    }
                });

                builder.setNegativeButton("Cancel", null);
                builder.show();
            }
        });

        // Delete button handling
        Button btnDelete = findViewById(R.id.btn_delete);
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (selectedIndex == -1) {
                    Toast.makeText(ExpenseListActivity.this, "Please select an expense to delete", Toast.LENGTH_SHORT).show();
                    return;
                }

                long expenseId = expenseIds.get(selectedIndex);
                // Confirm deletion
                new AlertDialog.Builder(ExpenseListActivity.this)
                        .setTitle("Delete Expense")
                        .setMessage("Are you sure you want to delete this expense?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            int rows = database.delete("Expenses", "rowid=?", new String[]{String.valueOf(expenseId)});
                            if (rows > 0) {
                                Toast.makeText(ExpenseListActivity.this, "Expense deleted", Toast.LENGTH_SHORT).show();
                                loadExpenses();
                            } else {
                                Toast.makeText(ExpenseListActivity.this, "Deletion failed", Toast.LENGTH_SHORT).show();
                            }
                            selectedIndex = -1;
                        })
                        .setNegativeButton("No", null)
                        .show();
            }
        });

        // Analysis button handling
        Button btnAnalysis = findViewById(R.id.btn_analysis);
        btnAnalysis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ExpenseListActivity.this, DailyAnalysisActivity.class);
                startActivity(intent);
            }
        });
    }

    // Load expenses from the database
    private void loadExpenses() {
        Cursor cursor = database.rawQuery("SELECT rowid, name, amount, date FROM Expenses;", null);
        expenseItems.clear();
        expenseIds.clear();

        while (cursor.moveToNext()) {
            long rowId = cursor.getLong(0);
            String name = cursor.getString(1);
            double amount = cursor.getDouble(2);
            String date = cursor.getString(3);
            expenseItems.add(name + " - ₹" + amount + " (" + date + ")");
            expenseIds.add(rowId);
        }

        cursor.close();
        adapter.notifyDataSetChanged();
    }
}
