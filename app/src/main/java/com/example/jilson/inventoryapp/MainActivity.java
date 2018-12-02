package com.example.jilson.inventoryapp;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.jilson.inventoryapp.data.InventoryContract;
import com.example.jilson.inventoryapp.data.InventoryContract.BookStoreEntry;
import com.example.jilson.inventoryapp.data.InventoryDbHelper;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final String LOG_TAG = MainActivity.class.getName();
    private InventoryDbHelper mDbHelper = new InventoryDbHelper(this);
    private BookCursorAdapter mCursorAdapter;
    private static final int BOOK_LOADER = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView bookListView = (ListView)findViewById(R.id.list);

        View emptyView = findViewById(R.id.empty_view);
        bookListView.setEmptyView(emptyView);

        mCursorAdapter = new BookCursorAdapter(this,null);
        bookListView.setAdapter(mCursorAdapter);


        getLoaderManager().initLoader(BOOK_LOADER,null,this);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });


        bookListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                Intent intent = new Intent(MainActivity.this,EditorActivity.class);
                Uri currentBookUri = ContentUris.withAppendedId(BookStoreEntry.CONTENT_URI,id);
                intent.setData(currentBookUri);
                startActivity(intent);
            }
        });

    }


    private void insertData(){
        // Insert into database.
        ContentValues values = new ContentValues();
        values.put(BookStoreEntry.COLUMN_PRODUCT_NAME,"product A ");
        values.put(BookStoreEntry.COLUMN_PRODUCT_PRICE,100);
        values.put(BookStoreEntry.COLUMN_PRODUCT_QUANTITY,9);
        values.put(BookStoreEntry.COLUMN_PRODUCT_SUPPLIER_NAME,"supplier A");
        values.put(BookStoreEntry.COLUMN_PRODUCT_SUPPLIER_PH_NO,1234567890);

        SQLiteDatabase sqLiteDatabase = mDbHelper.getWritableDatabase();
        long insertStatus = sqLiteDatabase.insert(BookStoreEntry.TABLE_NAME,null,values);
        if(insertStatus == -1)
            Log.v(LOG_TAG,"Error  Insertion failed");
        else
            Log.v(LOG_TAG,"Successfully inseted row at "+insertStatus);
    }



    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int i, @Nullable Bundle bundle) {
        String[] projection = {
                BookStoreEntry._ID,
                BookStoreEntry.COLUMN_PRODUCT_NAME,
                BookStoreEntry.COLUMN_PRODUCT_PRICE,
                BookStoreEntry.COLUMN_PRODUCT_QUANTITY};

        return new CursorLoader(this,
                BookStoreEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        mCursorAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }
}
