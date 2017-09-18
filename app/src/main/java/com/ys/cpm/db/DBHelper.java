package com.ys.cpm.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.Log;

import com.ys.cpm.PlayActivity;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * DBHelper
 */

public class DBHelper extends SQLiteOpenHelper { //todo kotlin转换

    public static String TAG = "db";

    protected Class<?>[] mClazz;

    public DBHelper(Context context, String name, int version) {
        super(context, /*BuildConfig.DEBUG?Environment.getExternalStorageDirectory().getAbsolutePath()+File.separator+name:*/name, null, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        for (Class<?> cls : mClazz) {
            db.execSQL(createTableSql(cls));
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        for (Class<?> cls : mClazz) {
            db.execSQL(dropTableSql(cls));
        }
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        for (Class<?> cls : mClazz) {
            db.execSQL(dropTableSql(cls));
        }
        onCreate(db);
    }

    public static String dropTableSql(Class clazz) {
        return "DROP TABLE IF EXISTS " + clazz.getSimpleName() + ";";
    }

    public static String createFt3TableSql(Class clz) {
        return "CREATE VIRTUAL TABLE " + clz.getSimpleName() + " USING fts3 " +
                createTableColumn(clz, false);
    }


    public static String createTableSql(Class clazz) {
        return "CREATE TABLE IF NOT EXISTS " + clazz.getSimpleName() +
                createTableColumn(clazz, true);
    }

    private static String createTableColumn(Class clazz, boolean isNeedAdd_ID) {
        Field[] fields = clazz.getFields();
        StringBuilder sb = new StringBuilder();
        sb.append(" ( ");
        List<String> cols = new ArrayList<>();
        if (isNeedAdd_ID)
            cols.add(BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT");
        for (Field field : fields) {
            if (field.getType() == String.class && field.isAnnotationPresent(Column.class)) {
                StringBuilder cb = new StringBuilder();
                try {
                    String name = (String) field.get(null);

                    cb.append(name);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                    return null;
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                    return null;
                }
                Column col = field.getAnnotation(Column.class);
                if (col.type() == Column.Type.INTEGER) {
                    cb.append(" INTEGER");
                } else if (col.type() == Column.Type.BLOB) {
                    cb.append(" BLOB");
                } else {
                    cb.append(" TEXT");
                }
                if (col.notnull()) {
                    cb.append(" NOT NULL");
                }
                if (col.unique()) {
                    cb.append(" UNIQUE ");
                }
                cols.add(cb.toString());
            }
        }
        sb.append(TextUtils.join(",", cols));
        sb.append(");");
        Log.d(TAG, sb.toString());
        return sb.toString();
    }
}