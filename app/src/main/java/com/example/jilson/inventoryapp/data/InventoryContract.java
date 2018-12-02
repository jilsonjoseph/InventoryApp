package com.example.jilson.inventoryapp.data;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Jilson on 06-09-2018.
 */

public class InventoryContract {

    /**
     * name for entire content provider, to must be unique so package name used
     */
    public static final String CONTENT_AUTHORITY = "com.example.jilson.inventoryapp";

    /**
     * Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
     * the content provider.
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /**
     * possible path that can be appended to base uri
     */
    public static final String PATH_BOOKS = "books";


    // private constructor to prevent intantiating the contract class
    InventoryContract(){}

    public static final class BookStoreEntry implements BaseColumns{

        /** The content URI to access the book data in the provider */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_BOOKS);

        /** Name of database table for products */
        public final static String TABLE_NAME = "products";

        /**
         * Unique ID number for the product (only for use in the database table).
         *
         * Type: INTEGER
         */
        public final static String _ID = BaseColumns._ID;

        /**
         * Name of the product.
         *
         * Type: TEXT
         */
        public final static String COLUMN_PRODUCT_NAME ="name";

        /**
         * Price of the product.
         *
         * Type: INTEGER
         */
        public final static String COLUMN_PRODUCT_PRICE = "price";

        /**
         * Quantity of the product.
         *
         * Type: INTEGER
         */
        public final static String COLUMN_PRODUCT_QUANTITY = "quantity";

        /**
         * Supplier of the product.
         *
         * Type: TEXT
         */
        public final static String COLUMN_PRODUCT_SUPPLIER_NAME = "supplier_name";

        /**
         * Phone number of Supplier.
         *
         * Type: INTEGER
         */
        public final static String COLUMN_PRODUCT_SUPPLIER_PH_NO = "supplier_ph_no";



    }

}
