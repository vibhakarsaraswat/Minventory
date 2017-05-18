package com.example.vibs.minventory.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.vibs.minventory.data.StockDbHelper;
import com.example.vibs.minventory.data.StockContract.StocksEntry;

/**
 * Created by vibhakar.sarswat on 5/16/2017.
 */

public class StockProvider extends ContentProvider {

    /**
     * Tag for the log messages
     */
    public static final String LOG_TAG = StockProvider.class.getSimpleName();

    private static final int STOCKS = 100;
    private static final int STOCKS_ID = 101;

    // Create a UriMatcher object.
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(StockContract.CONTENT_AUTHORITY, StockContract.PATH_STOCKS, STOCKS);
        sUriMatcher.addURI(StockContract.CONTENT_AUTHORITY, StockContract.PATH_STOCKS + "/#", STOCKS_ID);
    }

    /**
     * Database helper that will provide us access to the database
     */
    private StockDbHelper mDbHelper;

    @Override
    public boolean onCreate() {
        // To access our database, we instantiate our subclass of SQLiteOpenHelper
        // and pass the context, which is the current activity.
        mDbHelper = new StockDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case STOCKS:
                cursor = database.query(StocksEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case STOCKS_ID:
                selection = StocksEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                cursor = database.query(StocksEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    /**
     * ----------------------------------------------------------------------------------------------
     */
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case STOCKS:
                return insertItem(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    private Uri insertItem(Uri uri, ContentValues values) {
        String name = values.getAsString(StocksEntry.COLUMN_NAME);
        if (name == null) {
            throw new IllegalArgumentException("item requires a name");
        }

        Integer price = values.getAsInteger(StocksEntry.COLUMN_PRICE);
        if (price != null && price < 0) {
            throw new IllegalArgumentException("item requires a valid price");
        }

        // No need to check for ZERO quantity as an ITem can be added with ZERO quantity.

        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Insert the new item with the given values
        long id = database.insert(StocksEntry.TABLE_NAME, null, values);
        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        // Notify all listeners that the data has changed for the pet content URI
        getContext().getContentResolver().notifyChange(uri, null);

        return ContentUris.withAppendedId(uri, id);
    }

    /**
     * ----------------------------------------------------------------------------------------------
     */


    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Track the number of rows that were deleted
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case STOCKS:
                // Delete all rows that match the selection and selection args
                rowsDeleted = database.delete(StocksEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case STOCKS_ID:
                // Delete a single row given by the ID in the URI
                selection = StocksEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(StocksEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        // If 1 or more rows were deleted, then notify all listeners that the data at the
        // given URI has changed
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows deleted
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case STOCKS:
                return updateItem(uri, contentValues, selection, selectionArgs);
            case STOCKS_ID:
                // For the STOCKS_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = StocksEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateItem(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    /**
     * Update Items in the database with the given content values. Apply the changes to the rows
     * specified in the selection and selection arguments (which could be 0 or 1 or more Items).
     * Return the number of rows that were successfully updated.
     */
    private int updateItem(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // If the {@link StocksEntry#COLUMN_NAME} key is present,
        // check that the name value is not null.
        if (values.containsKey(StocksEntry.COLUMN_NAME)) {
            String name = values.getAsString(StocksEntry.COLUMN_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Item requires a name");
            }
        }

        // If the {@link StockEntry#COLUMN_PRICE} key is present,
        if (values.containsKey(StocksEntry.COLUMN_PRICE)) {
            Integer price = values.getAsInteger(StocksEntry.COLUMN_PRICE);
            if (price == null || price <= 0) {
                throw new IllegalArgumentException("Item requires valid price");
            }
        }

        // If the {@link StockEntry#COLUMN_QUANTITY_IN_STOCK} key is present,
        if (values.containsKey(StocksEntry.COLUMN_QUANTITY_IN_STOCK)) {
            Integer quantityInStock = values.getAsInteger(StocksEntry.COLUMN_QUANTITY_IN_STOCK);
            if (quantityInStock == null || quantityInStock < 0) {
                throw new IllegalArgumentException("Item requires valid stock quantity");
            }
        }

        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }

        // Otherwise, get writeable database to update the data
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(StocksEntry.TABLE_NAME, values, selection, selectionArgs);

        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows updated
        return rowsUpdated;
    }
}
