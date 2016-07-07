package io.virtualapp.abs.orm;

import android.database.sqlite.SQLiteDatabase;

/**
 * 数据库升级监听器
 *
 * @author Lody
 */
public interface DbUpdateListener {
    /**
     * @param db         数据库
     * @param oldVersion 旧版本
     * @param newVersion 新版本
     */
    void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion);
}