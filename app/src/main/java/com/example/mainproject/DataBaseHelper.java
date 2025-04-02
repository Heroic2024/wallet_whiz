package com.example.mainproject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DataBaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "WalletWhizDB";
    private static final int DATABASE_VERSION = 2; // Incremented version

    private static final String TABLE_USERS = "Users";
    private static final String COLUMN_USER_ID = "id";
    private static final String COLUMN_HAS_BUDGET = "has_set_budget";

    private static final String TABLE_BUDGET = "Budget";
    private static final String COLUMN_BUDGET_ID = "id";
    private static final String COLUMN_AMOUNT = "amount";

    private static final String TABLE_EXPENSES = "Expenses";
    private static final String COLUMN_EXPENSE_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_EXPENSE_AMOUNT = "amount";
    private static final String COLUMN_DATE = "date";

    public DataBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            String createUsersTable = "CREATE TABLE IF NOT EXISTS " + TABLE_USERS + " (" +
                    COLUMN_USER_ID + " INTEGER PRIMARY KEY, " +
                    COLUMN_HAS_BUDGET + " INTEGER DEFAULT 0);";
            db.execSQL(createUsersTable);

            String createBudgetTable = "CREATE TABLE IF NOT EXISTS " + TABLE_BUDGET + " (" +
                    COLUMN_BUDGET_ID + " INTEGER PRIMARY KEY, " +
                    COLUMN_AMOUNT + " DOUBLE);";
            db.execSQL(createBudgetTable);

            String createExpensesTable = "CREATE TABLE IF NOT EXISTS " + TABLE_EXPENSES + " (" +
                    COLUMN_EXPENSE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_NAME + " TEXT, " +
                    COLUMN_EXPENSE_AMOUNT + " DOUBLE, " +
                    COLUMN_DATE + " TEXT);";
            db.execSQL(createExpensesTable);

            // Insert default user entry (single-user scenario)
            db.execSQL("INSERT OR IGNORE INTO " + TABLE_USERS + " (" + COLUMN_USER_ID + ") VALUES (1);");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BUDGET);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EXPENSES);
        onCreate(db);
    }

    // Check if budget is already set
    public boolean hasSetBudget() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COLUMN_HAS_BUDGET + " FROM " + TABLE_USERS + " WHERE " + COLUMN_USER_ID + " = 1", null);

        boolean hasBudget = false;
        if (cursor.moveToFirst()) {
            hasBudget = cursor.getInt(0) == 1;
        }
        cursor.close();
        return hasBudget;
    }

    // Set the flag indicating the user has set a budget
    public void setHasBudget() {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_HAS_BUDGET, 1);
        db.update(TABLE_USERS, values, COLUMN_USER_ID + " = ?", new String[]{"1"});
    }

    // Insert budget
    public void insertBudget(double amount) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_BUDGET_ID, 1); // Ensure only one entry
        values.put(COLUMN_AMOUNT, amount);
        db.insertWithOnConflict(TABLE_BUDGET, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    // Retrieve budget
    public double getBudget() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COLUMN_AMOUNT + " FROM " + TABLE_BUDGET + " WHERE " + COLUMN_BUDGET_ID + " = 1", null);

        if (cursor.moveToFirst()) {
            double budget = cursor.getDouble(0);
            cursor.close();
            return budget;
        }
        cursor.close();
        return 0.0; // Default budget
    }

    // Insert Expense
    public void insertExpense(String name, double amount, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_EXPENSE_AMOUNT, amount);
        values.put(COLUMN_DATE, date);
        db.insert(TABLE_EXPENSES, null, values);
    }

    // Get Total Expenses
    public double getTotalExpenses() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM(" + COLUMN_EXPENSE_AMOUNT + ") FROM " + TABLE_EXPENSES, null);

        if (cursor.moveToFirst()) {
            double totalExpenses = cursor.getDouble(0);
            cursor.close();
            return totalExpenses;
        }
        cursor.close();
        return 0.0;
    }
}
