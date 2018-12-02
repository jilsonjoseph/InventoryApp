package com.example.jilson.inventoryapp;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.app.LoaderManager;
import android.provider.ContactsContract;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jilson.inventoryapp.data.InventoryContract;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private EditText mNameEditText;
    private EditText mPriceEditText;
    private TextView mQuantityTextView;
    private EditText mSupplierEditText;
    private EditText mSupplierNoEditText;
    private Button mQminusButton;
    private Button mQplusButton;

    private Button mOrderButton;

    private Uri mCurrentBookUri;

    /** tracks if any view is changed*/
    private boolean mBookHasChanged = false;

    private static final int EXISTING_BOOK_LOADER = 1;

    private static final String LOG_TAG = EditorActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        mNameEditText = findViewById(R.id.name);
        mPriceEditText = findViewById(R.id.price);
        mQuantityTextView = findViewById(R.id.quantity);
        mSupplierEditText = findViewById(R.id.supplier_name);
        mSupplierNoEditText = findViewById(R.id.supplier_no);
        mQminusButton = findViewById(R.id.button_q_minus);
        mQplusButton = findViewById(R.id.button_q_plus);
        mOrderButton = findViewById(R.id.button_order);


        Intent intent = getIntent();
        mCurrentBookUri = intent.getData();

        mNameEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mQuantityTextView.setOnTouchListener(mTouchListener);
        mSupplierEditText.setOnTouchListener(mTouchListener);
        mSupplierNoEditText.setOnTouchListener(mTouchListener);
        mQminusButton.setOnTouchListener(mTouchListener);
        mQplusButton.setOnTouchListener(mTouchListener);

        mQminusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int currentQuantity = Integer.parseInt(mQuantityTextView.getText().toString());
                if (currentQuantity > 0) {
                    currentQuantity--;
                    mQuantityTextView.setText(Integer.toString(currentQuantity));
                }else {
                    Toast.makeText(getBaseContext(),R.string.no_negative_quantity,
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        mQplusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int currentQuantity = Integer.parseInt(mQuantityTextView.getText().toString());
                mQuantityTextView.setText(Integer.toString(++currentQuantity));
            }
        });

        if(mCurrentBookUri == null){

            // Change title to Add a Book in Add mode
            setTitle(getString(R.string.editor_activity_title_new_book));

            // Order button is  not required in Add mode
            mOrderButton.setVisibility(View.INVISIBLE);

        }else{
            // Change title to Edit a Book in Edit mode
            setTitle(getString(R.string.editor_activity_title_edit_book));

            // Order button to call
            mOrderButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Uri currentSupplierNo = Uri.parse("tel:"+mSupplierNoEditText.getText());
                    Intent callIntent = new Intent(Intent.ACTION_DIAL,currentSupplierNo);
                    startActivity(callIntent);
                }
            });
            getLoaderManager().initLoader(EXISTING_BOOK_LOADER,null, this);
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new book, hide the "Delete" menu item.
        if (mCurrentBookUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // doing insert
                if(validateAndSaveBook())
                // exit activity
                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the pet hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mBookHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // validates inputs before calling save method
    private boolean validateAndSaveBook(){
        ContentValues contentValues = new ContentValues();

        // getting all the fields
        String bookName = mNameEditText.getText().toString().trim();
        String bookPrice = mPriceEditText.getText().toString().trim();
        String bookQuantity = mQuantityTextView.getText().toString().trim();
        String bookSupplierName = mSupplierEditText.getText().toString().trim();
        String bookSupplierNo = mSupplierNoEditText.getText().toString().trim();
        // quantity cannot be set to less than zero in editor
        int quantity = Integer.parseInt(bookQuantity);

        // If all details are empty entry is not inserted user may have press save by accident
        if(TextUtils.isEmpty(bookName) && TextUtils.isEmpty(bookPrice)  &&
                TextUtils.isEmpty(bookSupplierName) && TextUtils.isEmpty(bookSupplierNo ) && quantity == 0){
            return true;
        }
        // Check if name is empty
        if(TextUtils.isEmpty(bookName)){
            Toast.makeText(this,R.string.enter_book_name,Toast.LENGTH_SHORT).show();
            return false;
        }

        // Check price is empty
        int price =0;
        if(TextUtils.isEmpty(bookPrice)){
            Toast.makeText(this,R.string.enter_book_price,Toast.LENGTH_SHORT).show();
            return false;
        }else {
            price = Integer.parseInt(bookPrice);
        }

        // Check supplier name empty
        if(TextUtils.isEmpty(bookSupplierName)){
            Toast.makeText(this,R.string.enter_supplier_name,Toast.LENGTH_SHORT).show();
            return false;
        }

        // Check if supplier name empty or invalid
        long supplierNo =0;
        if(TextUtils.isEmpty(bookSupplierNo)){
            Toast.makeText(this,R.string.enter_supplier_no,Toast.LENGTH_SHORT).show();
            return false;
        }else{
            try{
                supplierNo = Long.parseLong(bookSupplierNo);
            }catch (NumberFormatException e){
                Toast.makeText(this,R.string.enter_valid_supplier_no,Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        contentValues.put(InventoryContract.BookStoreEntry.COLUMN_PRODUCT_NAME,bookName);
        contentValues.put(InventoryContract.BookStoreEntry.COLUMN_PRODUCT_PRICE,price);
        contentValues.put(InventoryContract.BookStoreEntry.COLUMN_PRODUCT_QUANTITY,quantity);
        contentValues.put(InventoryContract.BookStoreEntry.COLUMN_PRODUCT_SUPPLIER_NAME,bookSupplierName);
        contentValues.put(InventoryContract.BookStoreEntry.COLUMN_PRODUCT_SUPPLIER_PH_NO,supplierNo);
        saveBook(contentValues);
        return true;
    }


    private  void saveBook(ContentValues contentValues){


        if(mCurrentBookUri == null){
            // Insert a new book into the provider, returning the content URI for the new pet.
            Uri newUri = getContentResolver().insert(InventoryContract.BookStoreEntry.CONTENT_URI, contentValues);

            // Show a toast message depending on whether or not the insertion was successful
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_insert_book_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_insert_book_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }else{
            // Otherwise this is an EXISTING book, so update the pet with content URI: mCurrentBook
            // and pass in the new ContentValues. Pass in null for the selection and selection args
            // because mCurrentBookUri will already identify the correct row in the database that
            // we want to modify.
            int rowsAffected = getContentResolver().update(mCurrentBookUri, contentValues, null, null);

            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.editor_update_book_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_update_book_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                deleteBook();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the pet.
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
     * Perform the deletion of the pet in the database.
     */
    private void deleteBook() {
        // Only perform the delete if this is an existing pet.
        if (mCurrentBookUri != null) {
            // Call the ContentResolver to delete the pet at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentPetUri
            // content URI already identifies the book that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentBookUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_book_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_book_successful),
                        Toast.LENGTH_SHORT).show();
            }
            // Close the activity
            finish();
        }
    }


    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    // OnTouchListener that listens for any user touches on a View, implying that they are modifying
    // the view, and we change the mBookHasChanged boolean to true.

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mBookHasChanged = true;
            return false;
        }
    };


    @Override
    public void onBackPressed() {
        // If the book hasn't changed, continue with handling back button press
        if (!mBookHasChanged) {
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

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Since the editor shows all book attributes, define a projection that contains
        // all columns from the  table
        String[] projection = {
                InventoryContract.BookStoreEntry._ID,
                InventoryContract.BookStoreEntry.COLUMN_PRODUCT_NAME,
                InventoryContract.BookStoreEntry.COLUMN_PRODUCT_PRICE,
                InventoryContract.BookStoreEntry.COLUMN_PRODUCT_QUANTITY,
                InventoryContract.BookStoreEntry.COLUMN_PRODUCT_SUPPLIER_NAME,
                InventoryContract.BookStoreEntry.COLUMN_PRODUCT_SUPPLIER_PH_NO};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                mCurrentBookUri,         // Query the content URI for the current book
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of book attributes
            int nameColumnIndex = cursor.getColumnIndex(InventoryContract.BookStoreEntry.COLUMN_PRODUCT_NAME);
            int priceColumnIndex = cursor.getColumnIndex(InventoryContract.BookStoreEntry.COLUMN_PRODUCT_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(InventoryContract.BookStoreEntry.COLUMN_PRODUCT_QUANTITY);
            int supplierNameColumnIndex = cursor.getColumnIndex(InventoryContract.BookStoreEntry.COLUMN_PRODUCT_SUPPLIER_NAME);
            int supplierPhNoColumnIndex = cursor.getColumnIndex(InventoryContract.BookStoreEntry.COLUMN_PRODUCT_SUPPLIER_PH_NO);

            // Extract out the value from the Cursor for the given column index
            String name = cursor.getString(nameColumnIndex);
            int price = cursor.getInt(priceColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            String supplierName = cursor.getString(supplierNameColumnIndex);
            long supplierPhNo = cursor.getLong(supplierPhNoColumnIndex);


            // Update the views on the screen with the values from the database
            mNameEditText.setText(name);
            mPriceEditText.setText(Integer.toString(price));
            mQuantityTextView.setText(Integer.toString(quantity));
            mSupplierEditText.setText(supplierName);
            mSupplierNoEditText.setText(Long.toString(supplierPhNo));

        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }
}
