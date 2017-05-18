package com.example.vibs.minventory.data;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by vibhakar.sarswat on 5/16/2017.
 */

public final class StockContract {

    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    public StockContract() {
    }

    public static final String CONTENT_AUTHORITY = "com.example.vibs.minventory";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_STOCKS = "stocks";

    public static final class StocksEntry implements BaseColumns {

        /**
         * The content URI to access the item data in the provider
         */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_STOCKS);

        /**
         * Name of database table for Items
         */
        public final static String TABLE_NAME = "stocks";
        public final static String _ID = BaseColumns._ID;
        public final static String COLUMN_NAME = "name";
        public final static String COLUMN_PRICE = "price";
        public final static String COLUMN_QUANTITY_IN_STOCK = "quantity_in_stock";
        public final static String COLUMN_IMAGE = "image";

        /**
         public static final String COLUMN_QUANTITY_TO_ORDER = "quantity_to_stock";
         public static final String COLUMN_SUPPLIER_NAME = "supplier_name";
         public static final String COLUMN_SUPPLIER_EMAIL = "supplier_email"; */
    }
}
