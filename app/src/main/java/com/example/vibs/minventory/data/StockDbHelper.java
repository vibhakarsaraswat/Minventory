package com.example.vibs.minventory.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.vibs.minventory.data.StockContract.StocksEntry;

public class StockDbHelper extends SQLiteOpenHelper {

    public static final String LOG_TAG = StockDbHelper.class.getSimpleName();
    private static final String DATABASE_NAME = "minstocks.db";
    private static final int DATABASE_VERSION = 1;

    public StockDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // This is called when the database is created for the first time.
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create a String that contains the SQL statement to create the 'stock' table
        String SQL_CREATE_STOCKS_TABLE = "CREATE TABLE " + StocksEntry.TABLE_NAME + " (" +
                StocksEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                StocksEntry.COLUMN_NAME + " TEXT NOT NULL," +
                StocksEntry.COLUMN_PRICE + " INTEGER NOT NULL DEFAULT 0," +
                StocksEntry.COLUMN_QUANTITY_IN_STOCK + " INTEGER DEFAULT 0," +
                StocksEntry.COLUMN_IMAGE + " BLOB );";

        // Execute the SQL statement
        db.execSQL(SQL_CREATE_STOCKS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public Cursor readStock() {
        SQLiteDatabase db = getReadableDatabase();
        String[] projection = {
                StocksEntry._ID,
                StocksEntry.COLUMN_NAME,
                StocksEntry.COLUMN_PRICE,
                StocksEntry.COLUMN_QUANTITY_IN_STOCK,
                StocksEntry.COLUMN_IMAGE
        };

        Cursor cursor = db.query(
                StocksEntry.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                null
        );
        return cursor;
    }
}
