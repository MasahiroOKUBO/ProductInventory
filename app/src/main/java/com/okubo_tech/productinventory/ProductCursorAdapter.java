package com.okubo_tech.productinventory;

/**
 * Created by okubo on 2016/12/10.
 * Cursor Adapter Class.
 */

import android.content.res.Resources;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.okubo_tech.productinventory.data.ProductContract.ProductEntry;
import com.okubo_tech.productinventory.CatalogActivity;
import com.okubo_tech.productinventory.data.ProductProvider;

import static android.R.attr.data;

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
    public void bindView(View view, final Context context, Cursor cursor) {
        // find view
        TextView nameTextView = (TextView) view.findViewById(R.id.list_item_name);
        ImageView imageImageView = (ImageView) view.findViewById(R.id.photo_image_view);
        TextView priceTextView = (TextView) view.findViewById(R.id.list_item_price);
        TextView quantityTextView = (TextView) view.findViewById(R.id.list_item_quantity);
        Button plusButton = (Button)view.findViewById(R.id.list_item_plus_button);
        Button minusButton = (Button)view.findViewById(R.id.list_item_minus_button);

        // column index
        int idColumnIndex = cursor.getColumnIndex(ProductEntry._ID);
        int nameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME);
        int imageColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_IMAGE);
        int priceColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_QUANTITY);


        // get attributes from the Cursor.
        String productId = cursor.getString(idColumnIndex);
        String productName = cursor.getString(nameColumnIndex);
        int productPrice = cursor.getInt(priceColumnIndex);
        int productQuantity = cursor.getInt(quantityColumnIndex);
        byte[] productImageByte = cursor.getBlob(imageColumnIndex);

        Bitmap productImage = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_empty_store);

        if (productImageByte != null){
            productImage = BitmapFactory.decodeByteArray(productImageByte, 0, productImageByte.length);
        }

        // set value
        nameTextView.setText(productName);
        priceTextView.setText(String.valueOf(productPrice));
        quantityTextView.setText(String.valueOf(productQuantity));
        imageImageView.setImageBitmap(productImage);

        // setup Button
        final int currentQuantity = productQuantity;
        final long id = Long.parseLong(productId);

        plusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context, "plusButton", Toast.LENGTH_SHORT).show();

                // update
                ContentResolver resolver = view.getContext().getContentResolver();
                ContentValues values = new ContentValues();
                values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, currentQuantity + 1);
                Uri uri =  ContentUris.withAppendedId(
                        ProductEntry.CONTENT_URI,
                        id);
                resolver.update(uri, values, null, null);
            }
        });

        minusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context, "minusButton", Toast.LENGTH_SHORT).show();

                if (currentQuantity > 0){

                    // update
                    ContentResolver resolver = view.getContext().getContentResolver();
                    ContentValues values = new ContentValues();
                    values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, currentQuantity - 1);
                    Uri uri =  ContentUris.withAppendedId(
                            ProductEntry.CONTENT_URI,
                            id);
                    resolver.update(uri, values, null, null);
                } else {
                    Toast.makeText(context, "quantity can not be minus!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}
