package com.okubo_tech.productinventory.data;

/**
 * Created by okubo on 2016/12/10.
 * contract class.
 */

import android.net.Uri;
import android.content.ContentResolver;
import android.provider.BaseColumns;

public final class ProductContract {

    private ProductContract() {
    }

    public static final String CONTENT_AUTHORITY = "com.okubo_tech.productinventory";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_PRODUCTS = "products";


    public static final class ProductEntry implements BaseColumns {

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_PRODUCTS);
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCTS;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCTS;
        public final static String TABLE_NAME = "products";

        public final static String _ID = BaseColumns._ID;
        public final static String COLUMN_PRODUCT_NAME = "name";
        public final static String COLUMN_PRODUCT_PRICE = "price";
        public final static String COLUMN_PRODUCT_QUANTITY = "quantity";
        public final static String COLUMN_PRODUCT_SUPPLIER = "supplier";
        public final static String COLUMN_PRODUCT_DESCRIPTION = "description";
        public final static String COLUMN_PRODUCT_TEMPERATURE = "temperature";


        public static final int TEMPERATURE_NOMAL = 0;
        public static final int TEMPERATURE_COLD = 1;
        public static final int TEMPERATURE_FREEZE = 2;

        public static boolean isValidTemperature(int gender) {
            if (gender == TEMPERATURE_NOMAL || gender == TEMPERATURE_COLD || gender == TEMPERATURE_FREEZE) {
                return true;
            }
            return false;
        }
    }

}

