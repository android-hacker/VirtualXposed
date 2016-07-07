package io.virtualapp.abs.orm;

import java.io.Serializable;

/**
 * @author Lody
 *         <p>
 *         数据库配置信息
 */
public class SqLiteConfig implements Serializable {

    //==============================================================
    //                          常量
    //==============================================================
    public static String DEFAULT_DB_NAME = "sql.db";

    //==============================================================
    //                          字段
    //==============================================================
    /**
     * 是否为DEBUG模式
     */
    public boolean debugMode = true;
    /**
     * 数据库名
     */
    private String dbName = DEFAULT_DB_NAME;
    /**
     * 数据库升级监听器
     */
    private DbUpdateListener dbUpdateListener;
    private String saveDir;
    private int dbVersion = 1;

    /**
     * 取得数据库的名称
     *
     * @return dbName
     */
    public String getDbName() {
        return dbName;
    }

    /**
     * 设置数据库的名称
     *
     * @param dbName name
     */
    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    /**
     * 取得数据库升级监听器
     *
     * @return listener
     */
    public DbUpdateListener getDbUpdateListener() {
        return dbUpdateListener;
    }

    /**
     * 设置数据库升级监听器
     *
     * @param dbUpdateListener listener
     */
    public void setDbUpdateListener(DbUpdateListener dbUpdateListener) {
        this.dbUpdateListener = dbUpdateListener;
    }

    /**
     * 取得数据库保存目录
     *
     * @return dir
     */
    public String getSaveDir() {
        return saveDir;
    }

    /**
     * 设置数据库的保存目录
     *
     * @param saveDir saveDir
     */
    public void setSaveDir(String saveDir) {
        this.saveDir = saveDir;
    }

    /**
     * 获取DB的版本号
     *
     * @return version
     */
    public int getDbVersion() {
        return dbVersion;
    }

    /**
     * 设置DB的版本号
     *
     * @param dbVersion version
     */
    public void setDbVersion(int dbVersion) {
        this.dbVersion = dbVersion;
    }
}
