package com.okubo_tech.productinventory;

/**
 * Created by okubo on 2016/12/10.
 * Cursor Adapter Class.
 */


import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.okubo_tech.productinventory.data.ProductContract.ProductEntry;


public class ProductCursorAdapter extends CursorAdapter {

    /**
     * Constructr
     **/
    public ProductCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    /**
     * Override
     **/
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // find view
        TextView nameTextView = (TextView) view.findViewById(R.id.list_item_name);
        TextView priceTextView = (TextView) view.findViewById(R.id.list_item_price);
        TextView quantityTextView = (TextView) view.findViewById(R.id.list_item_quantity);
        TextView supplierTextView = (TextView) view.findViewById(R.id.list_item_supplier);

        // column index
        int nameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME);
        int priceColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_QUANTITY);
        int supplierColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_SUPPLIER);

        // get attributes from the Cursor.
        String productName = cursor.getString(nameColumnIndex);
        int productPrice = cursor.getInt(priceColumnIndex);
        int productQuantity = cursor.getInt(quantityColumnIndex);
        String productSupplier = cursor.getString(supplierColumnIndex);


        if (TextUtils.isEmpty(productSupplier)) {
            productSupplier = context.getString(R.string.unknown_supplier);
        }

        // set value
        nameTextView.setText(productName);
        priceTextView.setText(String.valueOf(productPrice));
        quantityTextView.setText(String.valueOf(productQuantity));
        supplierTextView.setText(productSupplier);
    }
}
