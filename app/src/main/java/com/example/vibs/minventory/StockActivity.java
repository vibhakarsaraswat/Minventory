package com.example.vibs.minventory;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;


import com.example.vibs.minventory.data.StockContract.StocksEntry;
import com.example.vibs.minventory.data.StockDbHelper;

public class StockActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    public static final String LOG_TAG = StockActivity.class.getSimpleName();
    private static final int STOCK_LOADER = 0;
    private byte[] mSampleImageByteArray;
    private TextView textview_quantity_in_stock;
    private ImageView sellItemImageView;
    StockDbHelper dbHelper;
    StockCursorAdapter mCursorAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(StockActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        // Find the ListView which will be populated with the items data
        ListView itemListView = (ListView) findViewById(R.id.list);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = findViewById(R.id.empty_view);
        itemListView.setEmptyView(emptyView);

        mCursorAdapter = new StockCursorAdapter(this, null);
        itemListView.setAdapter(mCursorAdapter);


        // Setup the List Item click listener
        itemListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                // Create new intent to go to {@link EditorActivity}
                Intent intent = new Intent(StockActivity.this, EditorActivity.class);

                Uri currentItemUri = ContentUris.withAppendedId(StocksEntry.CONTENT_URI, id);
                intent.setData(currentItemUri);

                startActivity(intent);
            }
        });


        // Kick off the loader
        getLoaderManager().initLoader(STOCK_LOADER, null, this);
    }

/** --------------------------------------------------------------------------------------------- */
    /**
     * Helper method to insert hardcoded pet data into the database. For debugging purposes only.
     */
    private void insertItem() {

        Drawable ImageDrawable = ResourcesCompat.getDrawable(getResources(), R.drawable.twisterbar, null);
        Bitmap sampleImageBitmap = ((BitmapDrawable) ImageDrawable).getBitmap();
        mSampleImageByteArray = ImageHelper.convertBitmapToBlob(sampleImageBitmap);

        // Create a ContentValues object where column names are the keys,
        // and Twisterbar's attributes are the values.
        ContentValues values = new ContentValues();
        values.put(StocksEntry.COLUMN_NAME, "Twister Bar");
        values.put(StocksEntry.COLUMN_PRICE, 10);
        values.put(StocksEntry.COLUMN_QUANTITY_IN_STOCK, 200);
        values.put(StocksEntry.COLUMN_IMAGE, mSampleImageByteArray);


        // Insert a new row for Twister Bar into the provider using the ContentResolver.
        // Use the StocksEntry#CONTENT_URI to indicate that we want to insert
        // into the stocks database table.
        // Receive the new content URI that will allow us to access Twister's data in the future.
        Uri newUri = getContentResolver().insert(StocksEntry.CONTENT_URI, values);
    }
/** --------------------------------------------------------------------------------------------- */
    /**
     * Helper method to delete all pets in the database.
     */
    private void deleteAllItems() {
        int rowsDeleted = getContentResolver().delete(StocksEntry.CONTENT_URI, null, null);
        Log.v("StockActivity", rowsDeleted + " rows deleted stocks table");
    }

    /**
     * ---------------------------------------------------------------------------------------------
     */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_stock, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                insertItem();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                deleteAllItems();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                StocksEntry._ID,
                StocksEntry.COLUMN_NAME,
                StocksEntry.COLUMN_PRICE,
                StocksEntry.COLUMN_QUANTITY_IN_STOCK,
                StocksEntry.COLUMN_IMAGE};

        return new CursorLoader(this, StocksEntry.CONTENT_URI, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }
}
