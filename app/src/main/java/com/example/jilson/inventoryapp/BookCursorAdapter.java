package com.example.jilson.inventoryapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jilson.inventoryapp.data.InventoryContract;



public class BookCursorAdapter extends CursorAdapter{
    public BookCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        // Find individual views in list item layout and modify
        TextView nameTextView = (TextView) view.findViewById(R.id.name);
        TextView priceTextView = (TextView) view.findViewById(R.id.price);
        TextView quantityTextView = (TextView) view.findViewById(R.id.quantity);

        // Find the columns of book attribute
        int nameColumnIndex = cursor.getColumnIndex(InventoryContract.BookStoreEntry.COLUMN_PRODUCT_NAME);
        int priceColumnIndex = cursor.getColumnIndex(InventoryContract.BookStoreEntry.COLUMN_PRODUCT_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(InventoryContract.BookStoreEntry.COLUMN_PRODUCT_QUANTITY);
        int idColumnIndex = cursor.getColumnIndex(InventoryContract.BookStoreEntry._ID);

        // Read the book attributes from the Cursor for the current book
        String bookName = cursor.getString(nameColumnIndex);
        int bookPrice = cursor.getInt(priceColumnIndex);
        final int bookQuantity = cursor.getInt(quantityColumnIndex);
        final int bookId = cursor.getInt(idColumnIndex);



        // Update the TextViews with the attributes for the current book
        nameTextView.setText(bookName);
        priceTextView.setText(Integer.toString(bookPrice));
        quantityTextView.setText(Integer.toString(bookQuantity));

        Button sellButton = (Button)view.findViewById(R.id.sell_button);
        sellButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri currentBookUri = ContentUris.withAppendedId(InventoryContract.BookStoreEntry.CONTENT_URI,bookId);
                updateBookQuantity(context,currentBookUri,bookQuantity);
            }
        });

    }

    private void updateBookQuantity(Context context, Uri bookUri, int currentQuantity){
        if(currentQuantity>0) {
            currentQuantity --;
            ContentValues contentValues = new ContentValues();
            contentValues.put(InventoryContract.BookStoreEntry.COLUMN_PRODUCT_QUANTITY,currentQuantity);
            int noOfRowsUpdated = context.getContentResolver().update(bookUri,contentValues,null,null);
            if(noOfRowsUpdated > 0)
                Toast.makeText(context.getApplicationContext(),R.string.quantity_updated,Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(context.getApplicationContext(),R.string.quantity_updation_failed,Toast.LENGTH_SHORT).show();
        }else
            Toast.makeText(context.getApplicationContext(),R.string.out_of_stock,Toast.LENGTH_SHORT).show();
    }
}
