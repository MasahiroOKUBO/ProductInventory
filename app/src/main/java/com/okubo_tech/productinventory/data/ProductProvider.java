package com.okubo_tech.productinventory.data;

/**
 * Created by okubo on 2016/12/10.
 *
 * content provider
 */

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.okubo_tech.productinventory.data.ProductContract.ProductEntry;


public class ProductProvider extends ContentProvider {

    public static final String LOG_TAG = ProductProvider.class.getSimpleName();

    private static final int PRODUCTS = 100;
    private static final int PRODUCT_ID = 101;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(
                ProductContract.AUTHORITY,
                ProductContract.PATH_PRODUCT,
                PRODUCTS);
        sUriMatcher.addURI(
                ProductContract.AUTHORITY,
                ProductContract.PATH_PRODUCT + "/#",
                PRODUCT_ID);
    }

    private ProductDbHelper mDbHelper;

    /** Override **/
    @Override
    public boolean onCreate() {
        mDbHelper = new ProductDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] args, String order) {

        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor cursor;

        int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                cursor = db.query(
                        ProductEntry.TABLE_NAME,
                        projection,
                        selection,
                        args,
                        null,
                        null,
                        order);
                break;
            case PRODUCT_ID:
                selection = ProductEntry._ID + "=?";
                args = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = db.query(
                        ProductEntry.TABLE_NAME,
                        projection,
                        selection,
                        args,
                        null,
                        null,
                        order);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return insertProduct(uri, values);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] args) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return updateProduct(uri, contentValues, selection, args);
            case PRODUCT_ID:
                selection = ProductEntry._ID + "=?";
                args = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateProduct(uri, contentValues, selection, args);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                rowsDeleted = database.delete(
                        ProductEntry.TABLE_NAME,
                        selection,
                        selectionArgs);
                break;
            case PRODUCT_ID:
                selection = ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(
                        ProductEntry.TABLE_NAME,
                        selection,
                        selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsDeleted;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return ProductEntry.CONTENT_LIST_TYPE;
            case PRODUCT_ID:
                return ProductEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    /** helper **/

    private Uri insertProduct(Uri uri, ContentValues values) {

        /** sanity check **/
        String name = values.getAsString(ProductEntry.COLUMN_PRODUCT_NAME);
        Integer price = values.getAsInteger(ProductEntry.COLUMN_PRODUCT_PRICE);
        Integer quantity = values.getAsInteger(ProductEntry.COLUMN_PRODUCT_QUANTITY);
        String supplier = values.getAsString(ProductEntry.COLUMN_PRODUCT_SUPPLIER);
        String supplier_email = values.getAsString(ProductEntry.COLUMN_PRODUCT_SUPPLIER_EMAIL);
        String description = values.getAsString(ProductEntry.COLUMN_PRODUCT_DESCRIPTION);
        Integer temperature = values.getAsInteger(ProductEntry.COLUMN_PRODUCT_TEMPERATURE);

        if (name == null) {
            throw new IllegalArgumentException("requires a name");
        }
        if (price != null && price < 0) {
            throw new IllegalArgumentException("requires valid price");
        }
        if (quantity != null && quantity < 0) {
            throw new IllegalArgumentException("requires valid quantity");
        }
        if (temperature == null || !ProductEntry.isValidTemperature(temperature)) {
            throw new IllegalArgumentException("requires valid temperature");
        }
        if (supplier_email == null) {
            throw new IllegalArgumentException("requires a supplier_email");
        }

        /** Insert **/
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        long id = db.insert(
                ProductEntry.TABLE_NAME,
                null,
                values);
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        /** notify Contents Resolver  **/
        getContext().getContentResolver().notifyChange(uri, null);

        /** return  **/
        return ContentUris.withAppendedId(uri, id);
    }

    private int updateProduct(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        /** sanity check **/
        if (values.containsKey(ProductEntry.COLUMN_PRODUCT_NAME)) {
            String name = values.getAsString(ProductEntry.COLUMN_PRODUCT_NAME);
            if (name == null) {
                throw new IllegalArgumentException("requires a name");
            }
        }

        if (values.containsKey(ProductEntry.COLUMN_PRODUCT_PRICE)) {
            Integer price = values.getAsInteger(ProductEntry.COLUMN_PRODUCT_PRICE);
            if (price != null && price < 0) {
                throw new IllegalArgumentException("requires valid price");
            }
        }

        if (values.containsKey(ProductEntry.COLUMN_PRODUCT_QUANTITY)) {
            Integer quantity = values.getAsInteger(ProductEntry.COLUMN_PRODUCT_QUANTITY);
            if (quantity != null && quantity < 0) {
                throw new IllegalArgumentException("requires valid price");
            }
        }

        if (values.containsKey(ProductEntry.COLUMN_PRODUCT_TEMPERATURE)) {
            Integer temperature = values.getAsInteger(ProductEntry.COLUMN_PRODUCT_TEMPERATURE);
            if (temperature == null || !ProductEntry.isValidTemperature(temperature)) {
                throw new IllegalArgumentException("requires valid temperature");
            }
        }

        if (values.size() == 0) {
            return 0;
        }

        /** update **/
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        int rowsUpdated = database.update(
                ProductEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs);

        /** notify Contents Resolver (if updated) **/
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        /** return  **/
        return rowsUpdated;
    }




}
