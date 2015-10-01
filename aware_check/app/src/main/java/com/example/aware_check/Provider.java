package com.example.aware_check;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.Environment;
import android.provider.BaseColumns;
import android.util.Log;

import com.aware.utils.DatabaseHelper;

import java.sql.SQLException;
import java.util.HashMap;


/**
 * Created by GK on 9/28/15.
 */
public class Provider extends ContentProvider {

    public static String AUTHORITY = "com.aware.provider.BD_data.example";
    public static final int DATABASE_VERSION = 1;

    private static final int EXAMPLE = 1;
    private static final int EXAMPLE_ID = 2;

    public static final class Example_Data implements BaseColumns {
        private Example_Data() {
        }

        ;
        /**
         * Your ContentProvider table content URI.<br/>
         * The last segment needs to match your database table name
         */
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/BD_data_example");

        /**
         * How your data collection is identified internally in Android (vnd.android.cursor.dir). <br/>
         * It needs to be /vnd.aware.plugin.XXX where XXX is your plugin name (no spaces!).
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.aware.BD_data.example";

        /**
         * How each row is identified individually internally in Android (vnd.android.cursor.item). <br/>
         * It needs to be /vnd.aware.plugin.XXX where XXX is your plugin name (no spaces!).
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.aware.BD_data.example";

        public static final String _ID = "_id";
        public static final String TIMESTAMP = "timestamp";
        public static final String DEVICE_ID = "device_id";
        public static final String SENS_TYPE = "sensor_type";

    }

    public static final String DATABASE_NAME = Environment
            .getExternalStorageDirectory() + "/AWARE/BD_data_example.db";
    public static final String[] DATABASE_TABLES = {"BD_data_example"};
    public static final String[] TABLES_FIELDS = {
            Example_Data._ID + " integer primary key autoincrement," +
                    Example_Data.TIMESTAMP + " real default 0," +
                    Example_Data.DEVICE_ID + " text default ''," +
                    Example_Data.SENS_TYPE + " text default ''," +
                    "UNIQUE (" + Example_Data.TIMESTAMP + "," + Example_Data.DEVICE_ID + ")"
    };

    private static UriMatcher sUriMatcher = null;
    private static HashMap<String, String> tableMap = null;
    private static DatabaseHelper databaseHelper = null;
    private static SQLiteDatabase database = null;

    private boolean initializeDB() {
        if (databaseHelper == null) {
            databaseHelper = new DatabaseHelper(getContext(), DATABASE_NAME, null, DATABASE_VERSION, DATABASE_TABLES, TABLES_FIELDS);
        }
        if ((database == null || !database.isOpen())) {
            database = databaseHelper.getWritableDatabase();
        }
        return (database != null);
    }

    @Override
    public boolean onCreate() {
        AUTHORITY = getContext().getPackageName() + ".provider.BD_data.example"; //make AUTHORITY dynamic
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(Provider.AUTHORITY, DATABASE_TABLES[0], EXAMPLE); //URI for all records
        sUriMatcher.addURI(Provider.AUTHORITY, DATABASE_TABLES[0]+"/#", EXAMPLE_ID); //URI for a single record

        tableMap = new HashMap<String, String>();
        tableMap.put(Example_Data._ID, Example_Data._ID);
        tableMap.put(Example_Data.TIMESTAMP, Example_Data.TIMESTAMP);
        tableMap.put(Example_Data.DEVICE_ID, Example_Data.DEVICE_ID);
        tableMap.put(Example_Data.SENS_TYPE, Example_Data.SENS_TYPE);

        return true; //let Android know that the database is ready to be used.
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,String sortOrder) {
        return null;
    }

    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case EXAMPLE:
                return Example_Data.CONTENT_TYPE;
            case EXAMPLE_ID:
                return Example_Data.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }
    @Override
    public Uri insert(Uri uri, ContentValues new_values) {
        if( ! initializeDB() ) {
            Log.w(AUTHORITY,"Database unavailable...");
            return null;
        }

        ContentValues values = (new_values != null) ? new ContentValues(new_values) : new ContentValues();

        switch (sUriMatcher.match(uri)) {
            case EXAMPLE:
                long _id = database.insert(DATABASE_TABLES[0],Example_Data.DEVICE_ID, values);
                if (_id > 0) {
                    Uri dataUri = ContentUris.withAppendedId(Example_Data.CONTENT_URI, _id);
                    getContext().getContentResolver().notifyChange(dataUri, null);
                    return dataUri;
                }
                try {
                    throw new SQLException("Failed to insert row into " + uri);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);

        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        if( ! initializeDB() ) {
            Log.w(AUTHORITY, "Database unavailable...");
            return 0;
        }

        int count = 0;
        switch (sUriMatcher.match(uri)) {
            case EXAMPLE:
                count = database.delete(DATABASE_TABLES[0], selection,selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        if( ! initializeDB() ) {
            Log.w(AUTHORITY,"Database unavailable...");
            return 0;
        }

        int count = 0;
        switch (sUriMatcher.match(uri)) {
            case EXAMPLE:
                count = database.update(DATABASE_TABLES[0], values, selection, selectionArgs);
                break;
            default:
                database.close();
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }
}
