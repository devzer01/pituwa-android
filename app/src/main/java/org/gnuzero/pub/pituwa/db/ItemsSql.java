package org.gnuzero.pub.pituwa.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

/**
 * Created by nayanahettiarachchi on 20/08/2017.
 */

public final class ItemsSql
{
    private ItemsSql() {};

    public static class ItemEntry implements BaseColumns {
        public static final String TABLE_NAME = "items";
        public static final String COLUMN_ID = "id";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_IMAGE_URL = "imgUrl";
        public static final String COLUMN_CONTENT = "content";
        public static final String COLUMN_DATE = "date";
        public static final String COLUMN_CATEGORY_TITLE = "categoryTitle";
        public static final String COLUMN_ITEM_DESCRIPTION = "itemDescription";
        public static final String COLUMN_MY_LIKE = "myLike";
        public static final String COLUMN_CREATE_AT = "createAt";
        public static final String COLUMN_LIKES_COUNT = "likesCount";
        public static final String COLUMN_CATEGORY_ID = "categoryId";
        public static final String COLUMN_VIDEO_URL = "videoUrl";
    }
}

