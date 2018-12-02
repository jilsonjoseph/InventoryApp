package com.example.jilson.inventoryapp.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

/**
 * Created by Jilson on 09-10-2018.
 */

/**
 * {@link ContentProvider} for Inventory app.
 */
public class BookProvider extends ContentProvider {

    public static final int BOOKS = 100;

    private static final int BOOK_ID = 101;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_BOOKS, BOOKS);

        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_BOOKS+"/#",BOOK_ID);
    }

    /** Tag for the log messages */
    public static final String LOG_TAG = BookProvider.class.getSimpleName();

    /* database helper object */
    InventoryDbHelper mDbHelper;

    /**
     * Initialize the provider and the database helper object.
     */
    @Override
    public boolean onCreate() {
        mDbHelper = new InventoryDbHelper(getContext());
        return true;
    }

    /**
     * Perform the query for the given URI. using the given projection, selection, selection arguments, and sort order.
     */
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
            case BOOKS:
                // For the PETS code, query the pets table directly with the given
                // projection, selection, selection arguments, and sort order. The cursor
                // could contain multiple rows of the pets table.
                cursor = database.query(InventoryContract.BookStoreEntry.TABLE_NAME,projection,selection,
                        selectionArgs, null,null,sortOrder);
                break;
            case BOOK_ID:
                // For the PET_ID code, extract out the ID from the URI.

                selection = InventoryContract.BookStoreEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };

                // This will perform a query on the pets table where the _id equals 3 to return a
                // Cursor containing that row of the table.
                cursor = database.query(InventoryContract.BookStoreEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        // Set notification URI on the Cursor,
        // so we know what content URI the Cursor was created for.
        // If the data at this URI changes, then we know we need to update the Cursor.
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    /**
     * Insert new data into the provider with the given ContentValues.
     */
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                return insertBook(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Insert a pet into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private Uri insertBook(Uri uri, ContentValues values) {

        // Check that the name is not null
        String name = values.getAsString(InventoryContract.BookStoreEntry.COLUMN_PRODUCT_NAME);
        if (name == null) {
            throw new IllegalArgumentException("Book requires a name");
        }
        // product price is allowed to be zero but not negative
        Integer price = values.getAsInteger(InventoryContract.BookStoreEntry.COLUMN_PRODUCT_PRICE);
        if (price != null && price < 0) {
            throw new IllegalArgumentException("Price cannot be  negative");
        }

        // product quantity is allowed to be zero but not negative
        Integer quantity = values.getAsInteger(InventoryContract.BookStoreEntry.COLUMN_PRODUCT_QUANTITY);
        if (quantity != null && quantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be  negative");
        }

        // Check that the supplier name is not null
        String supplierName = values.getAsString(InventoryContract.BookStoreEntry.COLUMN_PRODUCT_SUPPLIER_NAME);
        if (supplierName == null) {
            throw new IllegalArgumentException("Supplier name is required");
        }

        // Check that the supplier no is not 0
        Long supplier_number = values.getAsLong(InventoryContract.BookStoreEntry.COLUMN_PRODUCT_SUPPLIER_PH_NO);
        Log.v(LOG_TAG,"supplier No in insert method = "+supplier_number);
        if (supplier_number != null && supplier_number < 0 ) {
            throw new IllegalArgumentException("Supplier phone no cannot be less than 0");
        }

        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Insert the new product with the given values
        long id = database.insert(InventoryContract.BookStoreEntry.TABLE_NAME, null, values);

        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        // Notify all listeners that the data has changed for the pet content URI
        getContext().getContentResolver().notifyChange(uri, null);


        // Return the new URI with the ID (of the newly inserted row) appended at the end
        return ContentUris.withAppendedId(uri, id);
    }

    /**
     * Updates the data at the given selection and selection arguments, with the new ContentValues.
     */
    @Override
    public int update(Uri uri, ContentValues contentValues, String selection,
                      String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                return updateBook(uri, contentValues, selection, selectionArgs);
            case BOOK_ID:
                // For the BOOK_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = InventoryContract.BookStoreEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updateBook(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    /**
     * Update books in the database with the given content values. Apply the changes to the rows
     * specified in the selection and selection arguments (which could be 0 or 1 or more books).
     * Return the number of rows that were successfully updated.
     */
    private int updateBook(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }

        // If BookStoreEntry.COLUMN_PRODUCT_NAME key is present
        // check that the name value is not null.
        if (values.containsKey(InventoryContract.BookStoreEntry.COLUMN_PRODUCT_NAME)) {
            String name = values.getAsString(InventoryContract.BookStoreEntry.COLUMN_PRODUCT_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Pet requires a name");
            }
        }

        // If BookStoreEntry.COLUMN_PRODUCT_PRICE key is present
        // check that the price value is not null.
        if (values.containsKey(InventoryContract.BookStoreEntry.COLUMN_PRODUCT_PRICE)) {
            // product price is allowed to be zero but not negative
            Integer price = values.getAsInteger(InventoryContract.BookStoreEntry.COLUMN_PRODUCT_PRICE);
            if (price != null && price < 0) {
                throw new IllegalArgumentException("Price cannot be negative");
            }
        }

        // If BookStoreEntry.COLUMN_PRODUCT_QUANTITY key is present
        // check that the quantity value is not null.
        if (values.containsKey(InventoryContract.BookStoreEntry.COLUMN_PRODUCT_QUANTITY)) {
            // product quantity is allowed to be zero but not negative
            Integer quantity = values.getAsInteger(InventoryContract.BookStoreEntry.COLUMN_PRODUCT_QUANTITY);
            if (quantity != null && quantity < 0) {
                throw new IllegalArgumentException("Quantity cannot be negative");
            }
        }

        // If BookStoreEntry.COLUMN_PRODUCT_SUPPLIER_NAME key is present
        // check that the supplier name value is not null.
        if (values.containsKey(InventoryContract.BookStoreEntry.COLUMN_PRODUCT_SUPPLIER_NAME)) {
            // Check that the supplier name is not null
            String supplierName = values.getAsString(InventoryContract.BookStoreEntry.COLUMN_PRODUCT_SUPPLIER_NAME);
            if (supplierName == null) {
                throw new IllegalArgumentException("Supplier name is required");
            }
        }


        // If BookStoreEntry.COLUMN_PRODUCT_SUPPLIER_PH_NO key is present
        // check that the quantity value is not null.
        if (values.containsKey(InventoryContract.BookStoreEntry.COLUMN_PRODUCT_SUPPLIER_PH_NO)) {
            // Check that the supplier no is not 0
            Long supplier_number = values.getAsLong(InventoryContract.BookStoreEntry.COLUMN_PRODUCT_SUPPLIER_PH_NO);
            if (supplier_number != null && supplier_number < 0 ) {
                throw new IllegalArgumentException("Supplier phone no cannot be less than 0");
            }
        }



        // Otherwise, get writeable database to update the data
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(InventoryContract.BookStoreEntry.TABLE_NAME, values, selection, selectionArgs);
        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        // Return the number of rows updated
        return rowsUpdated;


    }


    /**
     * Delete the data at the given selection and selection arguments.
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Track the number of rows that were deleted
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                // Delete all rows that match the selection and selection args
                rowsDeleted = database.delete(InventoryContract.BookStoreEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case BOOK_ID:
                // Delete a single row given by the ID in the URI
                selection = InventoryContract.BookStoreEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(InventoryContract.BookStoreEntry.TABLE_NAME, selection, selectionArgs);
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

    /**
     * Returns the MIME type of data for the content URI.
     */
    @Override
    public String getType(Uri uri) {

        return null;
    }
}