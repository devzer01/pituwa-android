package org.gnuzero.pub.pituwa.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by nayanahettiarachchi on 20/08/2017.
 */

public class ItemsReaderDbHelper extends SQLiteOpenHelper {

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + ItemsSql.ItemEntry.TABLE_NAME + " (" +
                    ItemsSql.ItemEntry.COLUMN_ID + " LONG PRIMARY KEY," +
                    ItemsSql.ItemEntry.COLUMN_TITLE + " TEXT," +
                    ItemsSql.ItemEntry.COLUMN_IMAGE_URL + " TEXT, " +
                    ItemsSql.ItemEntry.COLUMN_CONTENT + " TEXT, " +
                    ItemsSql.ItemEntry.COLUMN_DATE + " TEXT, " +
                    ItemsSql.ItemEntry.COLUMN_CATEGORY_TITLE + " TEXT," +
                    ItemsSql.ItemEntry.COLUMN_ITEM_DESCRIPTION + " TEXT, " +
                    ItemsSql.ItemEntry.COLUMN_MY_LIKE + " INT, " +
                    ItemsSql.ItemEntry.COLUMN_CREATE_AT + " INT, " +
                    ItemsSql.ItemEntry.COLUMN_LIKES_COUNT + " INT, " +
                    ItemsSql.ItemEntry.COLUMN_CATEGORY_ID + " INT, " +
                    ItemsSql.ItemEntry.COLUMN_VIDEO_URL + " TEXT " + ")";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + ItemsSql.ItemEntry.TABLE_NAME;

    public static final int DATABASE_VERSION = 6;
    public static final String DATABASE_NAME = "PituwaItems.db";

    public ItemsReaderDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}