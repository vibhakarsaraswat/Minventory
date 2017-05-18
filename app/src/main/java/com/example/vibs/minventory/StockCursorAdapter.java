package com.example.vibs.minventory;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.example.vibs.minventory.data.StockContract.StocksEntry;


public class StockCursorAdapter extends CursorAdapter {

    private ImageView sellItemImageView;
    private TextView textview_quantity_in_stock;

    public StockCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
        // this.activity = context;
    }


    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {

        // Find fields to populate in inflated template
        ImageView imageView_item = (ImageView) view.findViewById(R.id.iv_item);
        ImageView sellItemImageView = (ImageView) view.findViewById(R.id.iv_sellItem);

        TextView textview_name = (TextView) view.findViewById(R.id.tv_name);
        TextView textview_price = (TextView) view.findViewById(R.id.tv_price);
        final TextView textview_quantity_in_stock = (TextView) view.findViewById(R.id.tv_quantity_in_stock);

        // Find the Columns of item attributes that we're interested in
        int idIndex = cursor.getColumnIndex(StocksEntry._ID);
        String name = cursor.getString(cursor.getColumnIndex(StocksEntry.COLUMN_NAME));
        int price = cursor.getInt(cursor.getColumnIndex(StocksEntry.COLUMN_PRICE));
        final int quantity = cursor.getInt(cursor.getColumnIndex(StocksEntry.COLUMN_QUANTITY_IN_STOCK));

        byte[] blob = cursor.getBlob(cursor.getColumnIndex(StocksEntry.COLUMN_IMAGE));
        Bitmap imageBitmap = ImageHelper.convertBlobToBitmap(blob);

        textview_name.setText(name);
        textview_price.setText(String.valueOf(price));
        imageView_item.setImageBitmap(imageBitmap);

        // Get data from cursor
        int id = cursor.getInt(idIndex);
        final Uri uri = Uri.parse(StocksEntry.CONTENT_URI + "/" + id);
        // Create popup dialog to record a sale on click
        sellItemImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int rowsAffected = 0;
                int availableQuantity = Integer.parseInt(String.valueOf(quantity));

                if (availableQuantity == 0) {
                    Toast.makeText(context, "Sale could not be recorded", Toast.LENGTH_SHORT).show();
                }

                if (availableQuantity > 0) {
                    availableQuantity--;
                    textview_quantity_in_stock.setText(String.valueOf(availableQuantity));

                    ContentValues updateValues = new ContentValues();
                    updateValues.put(StocksEntry.COLUMN_QUANTITY_IN_STOCK, availableQuantity);
                    rowsAffected = context.getContentResolver().update(uri, updateValues, null, null);
                }

                if (rowsAffected > 0) {
                    Toast.makeText(context, "Sale has been recorded", Toast.LENGTH_SHORT).show();
                    context.getContentResolver().notifyChange(uri, null);
                }
            }
        });
        textview_quantity_in_stock.setText(String.valueOf(quantity));
    }
}
