package com.ys.cpm.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import java.util.ArrayList;
import java.util.List;

/**
 * SQLiteOperator
 */

public class SQLiteOperator { //todo kotlin转换

    private final SQLiteOpenHelper mHelper;

    public SQLiteOperator(SQLiteOpenHelper helper) {
        mHelper = helper;
    }

    private SQLiteDatabase getWritable() {
        return mHelper.getWritableDatabase();
    }

    private SQLiteDatabase getReadable() {
        return mHelper.getReadableDatabase();
    }

    public final Cursor query(Class<?> table, String[] columns, String selection,
                              String[] selectionArgs, String groupBy, String having, String orderBy,
                              String limit) {
        return query(table.getSimpleName(), columns, selection, selectionArgs, groupBy, having,
                orderBy, limit);
    }

    public final Cursor query(String table, String[] columns, String selection,
                              String[] selectionArgs, String groupBy, String having, String orderBy,
                              String limit) {
        SQLiteDatabase db = getReadable();
        return db.query(table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
    }

    public final Cursor query(String sql, String[] selectionArgs) {
        SQLiteDatabase db = getReadable();
        return db.rawQuery(sql, selectionArgs);
    }

    public final int count(Class<?> table, String where) {
        return count(table.getSimpleName(), where);
    }

    public final int count(String table, String where) {
        SQLiteDatabase db = getReadable();
        String sqlStr = "select count(*) from " + table;
        if (where != null) sqlStr += " where " + where;
        Cursor mCount = db.rawQuery(sqlStr, null);
        mCount.moveToFirst();
        int count = mCount.getInt(0);
        mCount.close();
        return count;
    }

    public final Cursor query(String table, String selection, String[] selectionArgs) {
        return query(table, null, selection, selectionArgs, null, null, null, null);
    }

    public final Cursor query(Class<?> table, String selection, String[] selectionArgs) {
        return query(table.getSimpleName(), selection, selectionArgs);
    }

    public final Cursor query(Class<?> table, String[] columns, String selection,
                              String[] selectionArgs) {
        return query(table.getSimpleName(), columns, selection, selectionArgs);
    }

    public final Cursor query(String table, String[] columns, String selection,
                              String[] selectionArgs) {
        return query(table, columns, selection, selectionArgs, null, null, null, null);
    }

    /**
     * 批量删除
     *
     * @param table
     * @param rows  根据valuesList里面的key和value来删除记录
     * @param where 删除条件
     * @param keys  删除条件中对应的值，从value里取
     * @return
     */
    public final int delete(Class<?> table, List<ContentValues> rows, String where, String[] keys) {
        return delete(table.getSimpleName(), rows, where, keys);
    }

    public final int delete(String table, List<ContentValues> rows, String where, String[] keys) {
        SQLiteDatabase db = getWritable();
        int deleteCount = 0;
        db.beginTransaction();
        try {
            if (rows != null) {
                for (ContentValues values : rows) {
                    int len = keys.length;
                    String[] args = new String[len];
                    for (int i = 0; i < len; i++) {
                        args[i] = String.valueOf(values.get(keys[i]));
                    }
                    deleteCount += db.delete(table, where, args);
                }
            } else {
                db.delete(table, null, null);
            }
            db.setTransactionSuccessful();
        } finally {
            try {
                db.endTransaction();
            } catch (Exception ignored) {
            }
        }
        return deleteCount;
    }

    public final int delete(Class<?> table, String whereClause,
                            String[] whereArgs) {
        return delete(table.getSimpleName(), whereClause, whereArgs);
    }

    public final int delete(String table, String whereClause,
                            String[] whereArgs) {
        SQLiteDatabase db = getWritable();
        return db.delete(table, whereClause, whereArgs);
    }

    public final void deleteDuplicateRows(String table, String groupColumns) {
        String sql = "DELETE FROM " + table
                + " WHERE " + BaseColumns._ID + " NOT IN (SELECT max(" + BaseColumns._ID + ") from "
                + table
                + " group by " + groupColumns + ");";
        SQLiteDatabase db = getWritable();
        db.execSQL(sql);
    }

    /**
     * 批量更新
     *
     * @param table
     * @param values
     * @param whereClause
     * @param whereArgs
     * @param orderBy
     * @param limit
     * @return
     */
    public final int update(Class<?> table, ContentValues values,
                            String whereClause, String[] whereArgs, String orderBy, String limit) {
        return update(table.getSimpleName(), values, whereClause, whereArgs, orderBy, limit);
    }

    /**
     * 批量更新
     *
     * @param table
     * @param values
     * @param whereClause
     * @param whereArgs
     * @param orderBy
     * @param limit
     * @return
     */
    public final int update(String table, ContentValues values,
                            String whereClause, String[] whereArgs, String orderBy, String limit) {
        SQLiteDatabase db = getWritable();
        String updateWhereClause = BaseColumns._ID + "=?";
        String[] columns = new String[]{BaseColumns._ID};
        Cursor cursor = db.query(table, columns, whereClause, whereArgs, null,
                null, orderBy, limit);
        int updateCount = 0;
        db.beginTransaction();
        try {
            while (cursor.moveToNext()) {
                String[] updateArgs = new String[]{cursor.getString(0)};
                updateCount += db.update(table, values, updateWhereClause,
                        updateArgs);
            }
            db.setTransactionSuccessful();
        } finally {
            try {
                db.endTransaction();
            } catch (Exception ignored) {
            }
            cursor.close();
        }
        return updateCount;
    }

    public final int updateSet(Class<?> tableClass, String set, List<ContentValues> valuesList, String... whereKeys) {
        String table = tableClass.getSimpleName();
        SQLiteDatabase db = getWritable();
        int updateCount = 0;
        db.beginTransaction();
        try {
            for (ContentValues values : valuesList) {
                StringBuilder where = new StringBuilder();
                for (String key : whereKeys) {
                    if (where.length() > 0) where.append(" AND ");
                    where.append(key);
                    where.append("=");
                    where.append(String.valueOf(values.get(key)));
                }
                String sql = "UPDATE " + table + " SET " + set + " WHERE " + where;
                db.execSQL(sql);
                updateCount++;
            }
            db.setTransactionSuccessful();
        } finally {
            try {
                db.endTransaction();
            } catch (Exception ignored) {

            }

        }
        return updateCount;
    }

    /**
     * 批量更新
     *
     * @param table
     * @param valuesList 检索字段加更新字段
     * @param whereKeys  检索字段如 SongColumns.song_id
     * @return
     */
    public final int update(Class<?> table, List<ContentValues> valuesList, String... whereKeys) {
        return update(table.getSimpleName(), valuesList, whereKeys);
    }

    public final int update(String table, List<ContentValues> valuesList, String... whereKeys) {
        SQLiteDatabase db = getWritable();
        int updateCount = 0;
        db.beginTransaction();
        try {
            for (ContentValues values : valuesList) {
                StringBuilder where = new StringBuilder();
                ArrayList<String> argsList = new ArrayList<String>();
                for (String key : whereKeys) {
                    if (where.length() > 0) where.append(" AND ");
                    where.append(key);
                    where.append("=?");
                    argsList.add(String.valueOf(values.get(key)));
                    values.remove(key);
                }
                String[] args = argsList.toArray(new String[argsList.size()]);
                updateCount += db.update(table, values, where.toString(), args);
            }
            db.setTransactionSuccessful();
        } finally {
            try {
                db.endTransaction();
            } catch (Exception ignored) {
            }
        }
        return updateCount;
    }

    public final int update(Class<?> table, ContentValues values,
                            String whereClause, String[] whereArgs) {
        return update(table.getSimpleName(), values, whereClause, whereArgs);
    }

    public final int update(String table, ContentValues values,
                            String whereClause, String[] whereArgs) {
        SQLiteDatabase db = getWritable();
        return db.update(table, values, whereClause, whereArgs);
    }

    public final void update(Class<?> table, String set, String where) {
        update(table.getSimpleName(), set, where);
    }

    public final void update(String table, String set, String where) {
        String sql = "UPDATE " + table + " SET " + set + " WHERE " + where;
        SQLiteDatabase db = getWritable();
        try {
            db.execSQL(sql);
        } catch (SQLiteException ignore) {
        }
    }

    public final void update(String table, String set) {
        String sql = "UPDATE " + table + " SET " + set;
        SQLiteDatabase db = getWritable();
        try {
            db.execSQL(sql);
        } catch (SQLiteException ignore) {
        }
    }

    /**
     * 当不存在匹配数据时，插入新行
     *
     * @param table
     * @param whereClause
     * @param whereArgs
     * @param values
     */
    synchronized public final void updateOrInsert(String table, ContentValues values, String whereClause,
                                                  String[] whereArgs) {
        SQLiteDatabase db = getWritable();
        Cursor c = db.query(table, null, whereClause, whereArgs, null, null,
                null);
        int matchCount = c.getCount();
        c.close();
        if (matchCount > 0) {
            // update
            db.update(table, values, whereClause, whereArgs);
        } else {
            // insert
            db.insert(table, null, values);
        }
    }

    public final void updateOrInsert(Class<?> table, ContentValues values, String whereClause,
                                     String[] whereArgs) {
        updateOrInsert(table.getSimpleName(), values, whereClause, whereArgs);
    }

    public final void insert(String table, ContentValues values) {
        SQLiteDatabase db = getWritable();
        db.insert(table, null, values);
    }

    public final void insert(Class<?> table, ContentValues values) {
        insert(table.getSimpleName(), values);
    }

    synchronized public final void insert(Class<?> table, List<ContentValues> rows) {
        SQLiteDatabase db = getWritable();
        String tableName = table.getSimpleName();
        db.beginTransaction();
        try {
            for (ContentValues row : rows) {
                db.insert(tableName, null, row);
            }
            db.setTransactionSuccessful();
        } finally {
            try {
                db.endTransaction();
            } catch (Exception ignored) {
            }
        }
    }

    public final void insertIfNotExist(Class<?> table, ContentValues row) {
        SQLiteDatabase db = getWritable();
        String tableName = table.getSimpleName();
        db.insertWithOnConflict(tableName, null, row, SQLiteDatabase.CONFLICT_IGNORE);
    }

    public final void insertIfNotExist(Class<?> table, List<ContentValues> rows) {
        SQLiteDatabase db = getWritable();
        String tableName = table.getSimpleName();
        db.beginTransaction();
        try {
            for (ContentValues row : rows) {
                db.insertWithOnConflict(tableName, null, row, SQLiteDatabase.CONFLICT_IGNORE);
            }
            db.setTransactionSuccessful();
        } finally {
            try {
                db.endTransaction();
            } catch (Exception ignored) {
            }
        }
    }

    public final void insertOrReplace(Class<?> table, ContentValues row) {
        SQLiteDatabase db = getWritable();
        String tableName = table.getSimpleName();
        db.insertWithOnConflict(tableName, null, row, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public final void insertOrReplace(Class<?> table, List<ContentValues> rows) {
        SQLiteDatabase db = getWritable();
        String tableName = table.getSimpleName();
        db.beginTransaction();
        try {
            for (ContentValues row : rows) {
                db.insertWithOnConflict(tableName, null, row, SQLiteDatabase.CONFLICT_REPLACE);
            }
            db.setTransactionSuccessful();
        } finally {
            try {
                db.endTransaction();
            } catch (Exception ignored) {

            }
        }
    }

    public final int insertOrUpdate(Class<?> table, List<ContentValues> rows, String whereClause,
                                    String[] queryKeys) {
        SQLiteDatabase db = getWritable();
        Cursor cursor = null;
        String tableName = table.getSimpleName();
        db.beginTransaction();
        int c = 0;
        try {
            for (ContentValues values : rows) {
                int qs = queryKeys.length;
                String[] args = new String[qs];
                for (int j = 0; j < qs; j++) {
                    args[j] = values.getAsString(queryKeys[j]);
                }
                cursor = db.query(tableName, null, whereClause, args, null, null, null);
                int matchCount = cursor.getCount();
                cursor.close();
                if (matchCount > 0) {
                    // update
                    db.update(tableName, values, whereClause, args);
                } else {
                    // insert
                    db.insert(tableName, null, values);
                }
                c++;
                cursor.close();
            }
            db.setTransactionSuccessful();
        } finally {
            try {
                db.endTransaction();
            } catch (Exception ignored) {
            }
            if (cursor != null) cursor.close();
        }
        return c;
    }

    public final int insertIfNotExist(Class<?> table, List<ContentValues> rows, String whereClause,
                                      String[] queryKeys) {
        SQLiteDatabase db = getWritable();
        Cursor cursor = null;
        String tableName = table.getSimpleName();
        db.beginTransaction();
        int c = 0;
        try {
            for (ContentValues values : rows) {
                int qs = queryKeys.length;
                String[] args = new String[qs];
                for (int j = 0; j < qs; j++) {
                    args[j] = values.getAsString(queryKeys[j]);
                }
                cursor = db.query(tableName, null, whereClause, args, null, null, null);
                if (cursor.getCount() == 0) {
                    db.insert(tableName, null, values);
                    c++;
                }
                cursor.close();
            }
            db.setTransactionSuccessful();
        } finally {
            try {
                db.endTransaction();
            } catch (Exception ignored) {
            }
            if (cursor != null) cursor.close();
        }
        return c;
    }

    public final void close() {
        mHelper.close();
    }
}