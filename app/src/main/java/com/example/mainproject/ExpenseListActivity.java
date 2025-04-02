package com.example.mainproject;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

public class ExpenseListActivity extends AppCompatActivity {

    private ListView expenseList;
    private ArrayList<String> expenseItems;
    private ArrayAdapter<String> adapter;
    private SQLiteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_list);

        expenseList = findViewById(R.id.expense_list);
        expenseItems = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, expenseItems);
        expenseList.setAdapter(adapter);

        database = openOrCreateDatabase("WalletWhizDB", MODE_PRIVATE, null);
        loadExpenses();
    }

    private void loadExpenses() {
        Cursor cursor = database.rawQuery("SELECT name, amount, date FROM Expenses;", null);
        expenseItems.clear();

        while (cursor.moveToNext()) {
            String name = cursor.getString(0);
            double amount = cursor.getDouble(1);
            String date = cursor.getString(2);
            expenseItems.add(name + " - ₹" + amount + " (" + date + ")");
        }

        cursor.close();
        adapter.notifyDataSetChanged();
    }
}
