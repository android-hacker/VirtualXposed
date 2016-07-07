package io.virtualapp.abs.orm;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.virtualapp.VApp;
import io.virtualapp.abs.reflect.Reflect;

/**
 * @author Lody
 *         <p>
 * @version 2.0
 */
public class Orm {

    /**
     * 缓存创建的数据库,以便防止数据库冲突.
     */
    /*package*/ static final Map<String, Orm> gCache = new HashMap<>();

    /**
     * 数据库配置
     */
    private SqLiteConfig sqLiteConfig;
    /**
     * 内部操纵的数据库执行类
     */
    private SQLiteDatabase db;

    /**
     * 默认构造器
     *
     * @param config config
     */
    private Orm(SqLiteConfig config) {
        this.sqLiteConfig = config;
        String saveDir = config.getSaveDir();
        if (saveDir != null
                && saveDir.trim().length() > 0) {
            this.db = createDbFileOnSDCard(saveDir,
                    config.getDbName());
        } else {
            this.db = new SqLiteDbHelper(VApp.getApp().getApplicationContext(), config.getDbName(),
                    config.getDbVersion(), config.getDbUpdateListener())
                    .getWritableDatabase();
        }

    }

    /**
     * 根据配置取得用于操纵数据库的WeLikeDao实例
     *
     * @param config config
     * @return 数据库引擎
     */
    public static Orm get(SqLiteConfig config) {
        if (config.getDbName() == null) {
            throw new IllegalArgumentException("DBName is null in SqLiteConfig.");
        }
        Orm dao = gCache.get(config.getDbName());
        if (dao == null) {
            dao = new Orm(config);
            synchronized (gCache) {
                gCache.put(config.getDbName(), dao);
            }
        } else {//更换配置
            dao.applyConfig(config);
        }

        return dao;
    }

    /**
     * 取得操纵数据库的WeLikeDao实例
     *
     * @param dbName 数据库名
     */
    public static Orm get(String dbName) {
        SqLiteConfig config = new SqLiteConfig();
        config.setDbName(dbName);
        return get(config);
    }

    /**
     * 取得操纵数据库的WeLikeDao实例
     *
     * @param dbVersion 数据库版本
     * @return 数据库引擎
     */
    public static Orm get(int dbVersion) {
        SqLiteConfig config = new SqLiteConfig();
        config.setDbVersion(dbVersion);
        return get(config);
    }

    /**
     * 取得操纵数据库的WeLikeDao实例
     *
     * @param listener 数据库升级监听器
     * @return 数据库引擎
     */
    public static Orm get(DbUpdateListener listener) {
        SqLiteConfig config = new SqLiteConfig();
        config.setDbUpdateListener(listener);
        return get(config);
    }

    /**
     * 取得操纵数据库的WeLikeDao实例
     *
     * @param dbName    数据库名
     * @param dbVersion 数据库版本
     * @return 数据库引擎
     */
    public static Orm get(String dbName, int dbVersion) {
        SqLiteConfig config = new SqLiteConfig();
        config.setDbName(dbName);
        config.setDbVersion(dbVersion);
        return get(config);
    }

    /**
     * 取得操纵数据库的WeLikeDao实例
     *
     * @param dbName    数据库名
     * @param dbVersion 数据库版本
     * @param listener  数据库监听器
     * @return 数据库引擎
     */
    public static Orm get(String dbName, int dbVersion, DbUpdateListener listener) {
        SqLiteConfig config = new SqLiteConfig();
        config.setDbName(dbName);
        config.setDbVersion(dbVersion);
        config.setDbUpdateListener(listener);
        return get(config);
    }

    /**
     * 配置为新的参数(不改变数据库名).
     *
     * @param config 数据库配置副本
     */
    private void applyConfig(SqLiteConfig config) {
        this.sqLiteConfig.debugMode = config.debugMode;
        this.sqLiteConfig.setDbUpdateListener(config.getDbUpdateListener());
    }

    /**
     * 释放数据库
     */
    public void release() {
        gCache.clear();
    }


    /**
     * 在SD卡的指定目录上创建数据库文件
     *
     * @param sdcardPath sd卡路径
     * @param dbFileName 数据库文件名
     */
    private SQLiteDatabase createDbFileOnSDCard(String sdcardPath,
                                                String dbFileName) {
        File dbFile = new File(sdcardPath, dbFileName);
        if (!dbFile.exists()) {
            try {
                if (dbFile.createNewFile()) {
                    return SQLiteDatabase.openOrCreateDatabase(dbFile, null);
                }
            } catch (IOException e) {
                throw new RuntimeException("Unable to create db file on " + dbFile.getAbsolutePath());
            }
        } else {
            //数据库文件已经存在,无需再次创建.
            return SQLiteDatabase.openOrCreateDatabase(dbFile, null);
        }
        return null;
    }

    /**
     * 如果表不存在,需要创建它.
     *
     * @param clazz class
     */
    private void createTableIfNeed(Class<?> clazz) {
        TableInfo tableInfo = TableBuilder.from(clazz);
        if (tableInfo.isCreate) {
            return;
        }
        if (!isTableExist(tableInfo)) {
            String sql = SQLMaker.createTable(tableInfo);
            db.execSQL(sql);
            Method afterTableCreateMethod = tableInfo.afterTableCreateMethod;
            if (afterTableCreateMethod != null) {
                //如果afterTableMethod存在,就调用它
                try {
                    afterTableCreateMethod.invoke(null, this);
                } catch (Throwable ignored) {
                }
            }
        }
    }

    /**
     * 判断表是否存在?
     *
     * @param table 需要盘的的表
     */
    private boolean isTableExist(TableInfo table) {

        Cursor cursor = null;
        try {
            String sql = "SELECT COUNT(*) AS c FROM sqlite_master WHERE type ='table' AND name ='"
                    + table.tableName + "' ";
            cursor = db.rawQuery(sql, null);
            if (cursor != null && cursor.moveToNext()) {
                int count = cursor.getInt(0);
                if (count > 0) {
                    return true;
                }
            }

        } catch (Throwable ignored) {

        } finally {
            if (cursor != null)
                cursor.close();
        }

        return false;
    }

    /**
     * 删除全部的表
     */
    public void dropAllTable() {

        Cursor cursor = db.rawQuery(
                "SELECT name FROM sqlite_master WHERE type ='table'", null);
        if (cursor != null) {
            cursor.moveToFirst();
            while (cursor.moveToNext()) {
                try {
                    dropTable(cursor.getString(0));
                } catch (SQLException ignored) {
                }
            }
        }
        if (cursor != null) {
            cursor.close();
        }

    }

    /**
     * 取得数据库中的表的数量
     *
     * @return 表的数量
     */
    public int tableCount() {
        Cursor cursor = db.rawQuery(
                "SELECT name FROM sqlite_master WHERE type ='table'", null);
        int count = cursor == null ? 0 : cursor.getCount();
        if (cursor != null) {
            cursor.close();
        }
        return count;

    }

    /**
     * 取得数据库中的所有表名组成的List.
     */
    public List<String> getTableList() {
        Cursor cursor = db.rawQuery(
                "SELECT name FROM sqlite_master WHERE type ='table'", null);
        List<String> tableList = new ArrayList<>();
        if (cursor != null) {
            cursor.moveToFirst();
            while (cursor.moveToNext()) {
                tableList.add(cursor.getString(0));
            }
            cursor.close();
        }
        return tableList;

    }

    /**
     * 删除一张表
     *
     * @param beanClass 表所对应的类
     */
    public void dropTable(Class<?> beanClass) {
        TableInfo tableInfo = TableBuilder.from(beanClass);
        dropTable(tableInfo.tableName);
        tableInfo.isCreate = false;
    }

    /**
     * 删除一张表
     *
     * @param tableName 表名
     */
    public void dropTable(String tableName) {
        String statement = "DROP TABLE IF EXISTS " + tableName;
        db.execSQL(statement);
        TableInfo tableInfo = TableBuilder.findTableInfoByName(tableName);
        if (tableInfo != null) {
            tableInfo.isCreate = false;
        }
    }

    /**
     * 存储一个Bean.
     *
     * @param bean bean
     * @return 数据库引擎
     */
    public <T> Orm save(T bean) {
        createTableIfNeed(bean.getClass());
        String statement = SQLMaker.insertIntoTable(bean);
        db.execSQL(statement);
        return this;

    }

    /**
     * 存储多个Bean.
     *
     * @param beans beans
     * @return 数据库引擎
     */
    public <T> Orm save(T[] beans) {
        for (Object o : beans) {
            save(o);
        }
        return this;
    }

    /**
     * 存储多个Bean.
     *
     * @param beans 要存储的Bean
     * @return 数据库引擎
     */
    public <T> Orm save(List<T> beans) {

        for (Object o : beans) {
            save(o);
        }

        return this;
    }

    /**
     * 寻找Bean对应的全部数据
     *
     * @param clazz Bean实体类
     * @return 查询到的数据列表
     */
    public <T> List<T> all(Class<?> clazz) {
        createTableIfNeed(clazz);
        TableInfo tableInfo = TableBuilder.from(clazz);
        String statement = SQLMaker.selectTable(tableInfo.tableName);
        List<T> list = new ArrayList<>();
        Cursor cursor = db.rawQuery(statement, null);
        while (cursor.moveToNext()) {

            T object = Reflect.on(clazz).create().get();

            if (tableInfo.containID) {
                DataType dataType = SQLTypeParser.getDataType(tableInfo.primaryField);
                String idFieldName = tableInfo.primaryField.getName();
                ValueConverter.setKeyValue(cursor, object, tableInfo.primaryField, dataType, cursor.getColumnIndex(idFieldName));
            }

            for (Field field : tableInfo.fieldToDataTypeMap.keySet()) {
                DataType dataType = tableInfo.fieldToDataTypeMap.get(field);
                ValueConverter.setKeyValue(cursor, object, field, dataType, cursor.getColumnIndex(field.getName()));
            }
            list.add(object);

        }
        cursor.close();


        return list;

    }

    public <T> WhereBuilder one(Class<T> clazz) {
        return new WhereBuilder(this, clazz);
    }

    /**
     * 根据where语句寻找Bean
     *
     * @param clazz Bean实体类
     * @param where 查询语句
     * @return 查询结果
     */
    public <T> List<T> many(Class<?> clazz, String where) {
        createTableIfNeed(clazz);
        TableInfo tableInfo = TableBuilder.from(clazz);
        String statement = SQLMaker.findByWhere(tableInfo, where);
        List<T> list = new ArrayList<>();
        Cursor cursor = db.rawQuery(statement, null);
        while (cursor.moveToNext()) {

            T object = Reflect.on(clazz).create().get();
            if (tableInfo.containID) {
                DataType dataType = SQLTypeParser.getDataType(tableInfo.primaryField);
                String idFieldName = tableInfo.primaryField.getName();
                ValueConverter.setKeyValue(cursor, object, tableInfo.primaryField, dataType, cursor.getColumnIndex(idFieldName));
            }
            for (Field field : tableInfo.fieldToDataTypeMap.keySet()) {
                DataType dataType = tableInfo.fieldToDataTypeMap.get(field);
                ValueConverter.setKeyValue(cursor, object, field, dataType, cursor.getColumnIndex(field.getName()));
            }//end for
            list.add(object);
        }//end while
        cursor.close();

        return list;

    }

    /**
     * 根据where语句删除Bean
     *
     * @param clazz class
     */
    public Orm delete(Class<?> clazz, String where) {
        createTableIfNeed(clazz);
        TableInfo tableInfo = TableBuilder.from(clazz);
        String statement = SQLMaker.deleteByWhere(tableInfo, where);
        try {
            db.execSQL(statement);
        } catch (SQLException ignored) {
        }

        return this;
    }

    /**
     * 删除指定ID的bean
     *
     * @param tableClass class
     * @param id         id
     * @return 删除的Bean
     */
    public Orm delete(Class<?> tableClass, Object id) {
        createTableIfNeed(tableClass);
        TableInfo tableInfo = TableBuilder.from(tableClass);
        DataType dataType = SQLTypeParser.getDataType(id.getClass());
        if (dataType != null && tableInfo.primaryField != null) {
            //判断ID类型是否与数据类型匹配
            boolean match = SQLTypeParser.matchType(tableInfo.primaryField, dataType);
            if (!match) {//不匹配,抛出异常
                throw new IllegalArgumentException("Type of "
                        + id.getClass().getName()
                        + " is not the primary type, should be "
                        + tableInfo.primaryField.getType().getName());
            }
        }
        String idValue = ValueConverter.valueToString(dataType, id);
        String statement = SQLMaker.deleteByWhere(tableInfo, tableInfo.primaryField == null ?
                "_id" :
                tableInfo.primaryField.getName() + " = " + idValue);
        try {
            db.execSQL(statement);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return this;
    }

    public <T> Orm delete(T bean) {
        Class<?> clazz = bean.getClass();
        createTableIfNeed(clazz);
        TableInfo tableInfo = TableBuilder.from(clazz);
        try {
            Object id = tableInfo.primaryField.get(bean);
            delete(clazz, id);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return this;
    }

    /**
     * 根据给定的where更新数据
     *
     * @param tableClass 数据库Bean实体类
     * @param where      where语句
     * @param bean       Bean对象
     * @return 数据库引擎
     */
    public Orm update(Class<?> tableClass, String where, Object bean) {
        createTableIfNeed(tableClass);
        TableInfo tableInfo = TableBuilder.from(tableClass);
        String statement = SQLMaker.updateByWhere(tableInfo, bean, where);
        db.execSQL(statement);

        return this;
    }

    /**
     * 根据给定的id更新数据
     */
    public Orm update(Class<?> tableClass, Object id, Object bean) {
        createTableIfNeed(tableClass);
        TableInfo tableInfo = TableBuilder.from(tableClass);
        StringBuilder subStatement = new StringBuilder();
        if (tableInfo.containID) {
            subStatement.append(tableInfo.primaryField.getName()).append(" = ").append(
                    ValueConverter.valueToString(SQLTypeParser.getDataType(tableInfo.primaryField), id));
        } else {
            subStatement.append("_id = ").append((int) id);
        }
        update(tableClass, subStatement.toString(), bean);

        return this;
    }

    /**
     * 根据ID查找Bean
     *
     * @param tableClass 表名
     * @param id         ID
     * @return 查询结果
     */
    public <T> T one(Class<?> tableClass, Object id) {
        createTableIfNeed(tableClass);
        TableInfo tableInfo = TableBuilder.from(tableClass);
        DataType dataType = SQLTypeParser.getDataType(id.getClass());
        if (dataType != null) {
            //判断ID类型是否与数据类型匹配
            boolean match = SQLTypeParser.matchType(tableInfo.primaryField, dataType) || tableInfo.primaryField == null;
            if (!match) {//不匹配,抛出异常
                throw new IllegalArgumentException("Type of " + id.getClass().getName()
                        + " is not the primary type, should be " + tableInfo.primaryField.getType().getName());
            }
            String idValue = ValueConverter.valueToString(dataType, id);
            String statement = SQLMaker.findByWhere(tableInfo, tableInfo.primaryField == null ? "_id" : tableInfo.primaryField.getName() + " = " + idValue);

            Cursor cursor = db.rawQuery(statement, null);
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                T bean = Reflect.on(tableClass).create().get();
                for (Field field : tableInfo.fieldToDataTypeMap.keySet()) {
                    DataType fieldType = tableInfo.fieldToDataTypeMap.get(field);
                    ValueConverter.setKeyValue(cursor, bean, field, fieldType, cursor.getColumnIndex(field.getName()));
                }
                try {
                    Reflect.on(bean).set(tableInfo.containID ? tableInfo.primaryField.getName() : "_id", id);
                } catch (Throwable e) {
                    //我们允许Bean没有id字段,因此此异常可以忽略
                }
                cursor.close();
                return bean;
            }

        }
        return null;
    }

    /**
     * 通过VACUUM命令压缩数据库
     */
    public void vacuum() {
        db.execSQL("VACUUM");
    }

    /**
     * 调用本方法会释放当前数据库占用的内存,
     * 调用后请确保你不会在接下来的代码中继续用到本实例.
     */
    public void destroy() {
        gCache.remove(sqLiteConfig.getDbName());
        this.sqLiteConfig = null;
        this.db = null;
    }

    /**
     * 取得内部操纵的SqliteDatabase.
     *
     * @return SQLiteDatabase
     */
    public SQLiteDatabase getDatabase() {
        return db;
    }

    /**
     * 内部数据库监听器,负责派发接口.
     */
    private class SqLiteDbHelper extends SQLiteOpenHelper {

        private final DbUpdateListener dbUpdateListener;

        public SqLiteDbHelper(Context context, String name, int version,
                              DbUpdateListener dbUpdateListener) {
            super(context, name, null, version);
            this.dbUpdateListener = dbUpdateListener;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            if (dbUpdateListener != null) {
                dbUpdateListener.onUpgrade(db, oldVersion, newVersion);
            } else { //干掉所有的表
                dropAllTable();
            }
        }

    }

}
