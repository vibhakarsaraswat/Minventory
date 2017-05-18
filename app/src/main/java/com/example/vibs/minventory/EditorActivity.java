package com.example.vibs.minventory;

import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.view.Menu;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.widget.Toast;

import com.example.vibs.minventory.data.StockContract.StocksEntry;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.NumberFormat;

public class EditorActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    public static final String LOG_TAG = EditorActivity.class.getSimpleName();

    private static final int RESULT_LOAD_IMAGE = 1;

    private static final int EXISTING_STOCK_LOADER = 0;

    private Uri mCurrentItemUri;
    private EditText mNameEditText;
    private EditText mPriceEditText;
    private EditText mQuantityInStockEditText;
    private byte[] mImageByteArray;
    private ImageView mItemImageView;
    private Button mSelectImageButton;

    Button decreaseQuantityInStockButton;
    Button increaseQuantityInStockButton;

    Button decreaseQuantityToOrderButton;
    Button increaseQuantityToOrderButton;

    private EditText mQuantityToOrderEditText;
    private EditText mSupplierEmail;
    private Button mConfirmOrder;

    /**
     * Boolean flag that keeps track of whether the Item has been edited (true) or not (false)
     */
    private boolean mItemtHasChanged = false;

    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the mItemHasChanged boolean to true.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mItemtHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Examine the intent that was used to launch this activity,
        // in order to figure out if we're creating a new Item or editing an existing one.
        Intent intent = getIntent();
        mCurrentItemUri = intent.getData();

        if (mCurrentItemUri == null) {
            // This is a new Item, so change the app bar to say "Add an Item"
            setTitle(getString(R.string.ea_title_new_item));

            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a Item that hasn't been created yet.)
            invalidateOptionsMenu();
        } else {
            // Otherwise this is an existing Item, so change app bar to say "Edit Item"
            setTitle(getString(R.string.ea_title_edit_item));

            // Initialize a loader to read the Item data from the database
            // and display the current values in the editor
            getLoaderManager().initLoader(EXISTING_STOCK_LOADER, null, this);
        }

        mNameEditText = (EditText) findViewById(R.id.et_ea_name);
        mPriceEditText = (EditText) findViewById(R.id.et_ea_price);
        mQuantityInStockEditText = (EditText) findViewById(R.id.et_ea_quantity_in_stock);
        mQuantityToOrderEditText = (EditText) findViewById(R.id.et_ea_quantity_to_order);
        mSupplierEmail = (EditText) findViewById(R.id.et_ea_supplier_email);
        mConfirmOrder = (Button) findViewById(R.id.bt_ea_confirm_order);


        mConfirmOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Read from input fields and validating that Name Field has not left empty.

                if (mNameEditText.getText().toString().trim().isEmpty()) {
                    mNameEditText.setError("Item Name can't be empty");
                    return;
                }

                if (mQuantityToOrderEditText.getText().toString().isEmpty()) {
                    mQuantityToOrderEditText.setError("Order Quantity can't be empty");
                    return;
                }

                if (mSupplierEmail.getText().toString().isEmpty()) {
                    mSupplierEmail.setError("Supplier Email can't be empty!");
                    return;
                }

                String itemName = mNameEditText.getText().toString().trim();
                int OrderQuantity = Integer.parseInt(mQuantityToOrderEditText.getText().toString());

                if (OrderQuantity == 0) {
                    mQuantityToOrderEditText.setError("Order Quantity can't be Zero");
                    return;
                }

                // Use an intent to launch an email app.
                // Send the order summary in the email body.
                // Display the order summary on the screen
                String message = "Dear Supplier, \n\n Pls send " + OrderQuantity + " units of " +
                        itemName + ".\n\n Thanks / GB Glas AB";

                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:")); // only email apps should handle this
                intent.putExtra(Intent.EXTRA_SUBJECT, (getString(R.string.order_summary_email_subject) + itemName));
                intent.putExtra(Intent.EXTRA_TEXT, message);

                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }

            }
        });

        // Finding Quantity Buttons and changing quantity accordingly.
        decreaseQuantityInStockButton = (Button) findViewById(R.id.bt_ea_decrement_stock);
        increaseQuantityInStockButton = (Button) findViewById(R.id.bt_ea_increment_stock);

        decreaseQuantityInStockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mQuantityInStockEditText.getText().toString().isEmpty()) {
                    Toast.makeText(getBaseContext(), getString(R.string.unable_to_decrease_quantity), Toast.LENGTH_SHORT).show();
                } else {
                    int stockQuantity = Integer.parseInt(mQuantityInStockEditText.getText().toString());
                    if (stockQuantity > 0) {
                        stockQuantity--;
                        mQuantityInStockEditText.setText(Integer.toString(stockQuantity));
                    } else {
                        Toast.makeText(getBaseContext(), getString(R.string.unable_to_decrease_quantity), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        increaseQuantityInStockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mQuantityInStockEditText.getText().toString().isEmpty()) {
                    Toast.makeText(getBaseContext(), getString(R.string.unable_to_increase_quantity), Toast.LENGTH_SHORT).show();
                } else {
                    int stockQuantity = Integer.parseInt(mQuantityInStockEditText.getText().toString());
                    stockQuantity++;
                    mQuantityInStockEditText.setText(Integer.toString(stockQuantity));
                }
            }
        });

        // Finding Quantity Buttons and changing quantity accordingly.
        decreaseQuantityToOrderButton = (Button) findViewById(R.id.bt_ea_decrement_order);
        increaseQuantityToOrderButton = (Button) findViewById(R.id.bt_ea_increment_order);

        decreaseQuantityToOrderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mQuantityToOrderEditText.getText().toString().isEmpty()) {
                    Toast.makeText(getBaseContext(), getString(R.string.unable_to_decrease_quantity), Toast.LENGTH_SHORT).show();
                } else {
                    int OrderQuantity = Integer.parseInt(mQuantityToOrderEditText.getText().toString());
                    if (OrderQuantity > 0) {
                        OrderQuantity--;
                        mQuantityToOrderEditText.setText(Integer.toString(OrderQuantity));
                    } else {
                        Toast.makeText(getBaseContext(), getString(R.string.unable_to_decrease_quantity), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        increaseQuantityToOrderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mQuantityToOrderEditText.getText().toString().isEmpty()) {
                    Toast.makeText(getBaseContext(), getString(R.string.unable_to_increase_quantity), Toast.LENGTH_SHORT).show();
                } else {
                    int OrderQuantity = Integer.parseInt(mQuantityToOrderEditText.getText().toString());
                    OrderQuantity++;
                    mQuantityToOrderEditText.setText(Integer.toString(OrderQuantity));
                }
            }
        });

        mItemImageView = (ImageView) findViewById(R.id.iv_ea_item_image);
        mSelectImageButton = (Button) findViewById(R.id.bt_ea_select_image);

        // Opening Image Gallery Intent
        mSelectImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, RESULT_LOAD_IMAGE);
            }
        });

        mNameEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mQuantityInStockEditText.setOnTouchListener(mTouchListener);
        mSelectImageButton.setOnTouchListener(mTouchListener);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();

            InputStream inputStream;

            try {
                inputStream = getContentResolver().openInputStream(imageUri);
                Bitmap selectedImage = BitmapFactory.decodeStream(inputStream);
                mItemImageView.setImageBitmap(selectedImage);
                // Temp code to store image
                mImageByteArray = ImageHelper.convertBitmapToBlob(selectedImage);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(this, "unable to open image", Toast.LENGTH_LONG).show();
            }
        }
    }

    /**********************************************************************************************/
    // This will populate the MENU option in the Title Bar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    // This will remove the DELETE option if adding a New Item
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new Item, hide the "Delete" menu item.
        if (mCurrentItemUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    // This will check what MENU option is clicked and take appropriate action
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Read from input fields and validating that Name Field has not left empty.
        if (mItemImageView.getDrawable() == null) {
            mSelectImageButton.setError("Image can't be empty!");
            return false;
        }

        // Read from input fields and validating that Name Field has not left empty.
        if (mNameEditText.getText().toString().isEmpty()) {
            mNameEditText.setError("Name can't be empty!");
            return false;
        }

        // Read from input fields and validating that Price Field has not left empty.
        if (mPriceEditText.getText().toString().isEmpty()) {
            mPriceEditText.setError("Price can't be empty!");
            return false;
        }

        // Read from input fields and validating that Quantity Field has not left empty.
        if (mQuantityInStockEditText.getText().toString().isEmpty()) {
            mQuantityInStockEditText.setError("Price can't be empty!");
            return false;
        }

        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                saveItem();
                // Exit activity
                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the Item hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mItemtHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This method is called when the back button is pressed.
     */
    @Override
    public void onBackPressed() {
        // If the Item hasn't changed, continue with handling back button press
        if (!mItemtHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    /**********************************************************************************************/

    private void saveItem() {
        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String nameString = mNameEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String quantityInStockString = mQuantityInStockEditText.getText().toString().trim();

        Bitmap photo = ((BitmapDrawable) mItemImageView.getDrawable()).getBitmap();
        mImageByteArray = ImageHelper.convertBitmapToBlob(photo);


        // Check if this is supposed to be a new Item
        // and check if all the fields in the editor are blank
        if (mCurrentItemUri == null &&
                TextUtils.isEmpty(nameString) && TextUtils.isEmpty(priceString) &&
                TextUtils.isEmpty(quantityInStockString) && (mItemImageView.getDrawable() == null)) {
            // Since no fields were modified, we can return early without creating a new Item.
            // No need to create ContentValues and no need to do any ContentProvider operations.
            return;
        }

        ContentValues values = new ContentValues();
        values.put(StocksEntry.COLUMN_NAME, nameString);
        values.put(StocksEntry.COLUMN_PRICE, priceString);
        values.put(StocksEntry.COLUMN_QUANTITY_IN_STOCK, quantityInStockString);
        //values.put(StocksEntry.COLUMN_IMAGE, mImageByteArray);
        values.put(StocksEntry.COLUMN_IMAGE, mImageByteArray);

        // Determine if this is a new or existing Item by checking if mCurrentItemUri is null or not
        if (mCurrentItemUri == null) {
            Uri newUri = getContentResolver().insert(StocksEntry.CONTENT_URI, values);

            // Show a toast message depending on whether or not the insertion was successful.
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_insert_item_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_insert_item_successful),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            // Otherwise this is an EXISTING Item, so update the Item with content URI: mCurrentItemUri
            // and pass in the new ContentValues. Pass in null for the selection and selection args
            // because mCurrentItemUri will already identify the correct row in the database that
            // we want to modify.
            int rowsAffected = getContentResolver().update(mCurrentItemUri, values, null, null);

            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.editor_update_item_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_update_item_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
        String[] projection = {
                StocksEntry._ID,
                StocksEntry.COLUMN_NAME,
                StocksEntry.COLUMN_PRICE,
                StocksEntry.COLUMN_QUANTITY_IN_STOCK,
                StocksEntry.COLUMN_IMAGE};

        return new CursorLoader(this, mCurrentItemUri, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the Columns of item attributes that we're interested in
            int nameColumnIndex = cursor.getColumnIndex(StocksEntry.COLUMN_NAME);
            int priceColumnIndex = cursor.getColumnIndex(StocksEntry.COLUMN_PRICE);
            int quantityInStockColumnIndex = cursor.getColumnIndex(StocksEntry.COLUMN_QUANTITY_IN_STOCK);
            int imageColumnIndex = cursor.getColumnIndex(StocksEntry.COLUMN_IMAGE);

            // Extract out the value from the Cursor for the given column index
            String name = cursor.getString(nameColumnIndex);
            String price = cursor.getString(priceColumnIndex);
            String quantityInStock = cursor.getString(quantityInStockColumnIndex);
            byte[] blob = cursor.getBlob(imageColumnIndex);
            Bitmap imageBitmap = ImageHelper.convertBlobToBitmap(blob);

            // Update the views on the screen with the values from the database
            mNameEditText.setText(name);
            mPriceEditText.setText(price);
            mQuantityInStockEditText.setText(quantityInStock);
            mItemImageView.setImageBitmap(imageBitmap);

        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     * @param discardButtonClickListener is the click listener for what to do when
     *                                   the user confirms they want to discard their changes
     */
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the Item.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Prompt the user to confirm that they want to delete this Item.
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the Item.
                deleteItem();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the Item.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the item in the database.
     */
    private void deleteItem() {
        // Only perform the delete if this is an existing Item.
        if (mCurrentItemUri != null) {
            // Call the ContentResolver to delete the Item at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentItemUri
            // content URI already identifies the Item that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentItemUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_item_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_item_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        // Close the activity
        finish();
    }

}
