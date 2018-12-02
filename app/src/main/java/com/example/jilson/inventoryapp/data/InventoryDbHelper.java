package com.example.jilson.inventoryapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.jilson.inventoryapp.data.InventoryContract.BookStoreEntry;

/**
 * Created by Jilson on 06-09-2018.
 */

public final class InventoryDbHelper extends SQLiteOpenHelper{

    // name of database file
    private static final String DATABASE_NAME = "products_info.db";

    // database version, should be incremented if database schema is changed
    private static final int DATABASE_VERSION = 1;

    // constructor
    public InventoryDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        //String that contains the SQL statement to create the products table
        String SQL_CREATE_PRODUCTS_TABLE = "CREATE TABLE "+ BookStoreEntry.TABLE_NAME + " ("
                + BookStoreEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + BookStoreEntry.COLUMN_PRODUCT_NAME + " TEXT, "
                + BookStoreEntry.COLUMN_PRODUCT_PRICE + " INTEGER NOT NULL, "
                + BookStoreEntry.COLUMN_PRODUCT_QUANTITY + " INTEGER DEFAULT 0, "
                + BookStoreEntry.COLUMN_PRODUCT_SUPPLIER_NAME + " TEXT, "
                + BookStoreEntry.COLUMN_PRODUCT_SUPPLIER_PH_NO + " INTEGER);";


        // Execute the SQL statement
        sqLiteDatabase.execSQL(SQL_CREATE_PRODUCTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        //nothing to do here as of now
    }
}
