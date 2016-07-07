package io.virtualapp.abs.orm;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import io.virtualapp.abs.orm.annotation.ID;
import io.virtualapp.abs.orm.annotation.Table;

/**
 * @author Lody
 *         创建Table的辅助类
 */
public class TableBuilder {

    /*package*/ static final Map<Class<?>, TableInfo> sCache = new HashMap<>();

    /**
     * 根据传入的Bean的Class将其映射为一个TableInfo.
     *
     * @param clazz class
     */
    public static TableInfo from(Class<?> clazz) {
        TableInfo tableInfo = sCache.get(clazz);
        if (tableInfo != null) {
            return tableInfo;
        }
        tableInfo = new TableInfo();
        //Table注解解析
        Table table = clazz.getAnnotation(Table.class);
        if (table != null) {
            String afterTableCreateMethod = table.afterTableCreate();
            if (afterTableCreateMethod != null && afterTableCreateMethod.trim().length() > 0) {
                try {
                    Method method = clazz.getDeclaredMethod(afterTableCreateMethod, Orm.class);
                    if (method != null && Modifier.isStatic(method.getModifiers())) {
                        method.setAccessible(true);
                        tableInfo.afterTableCreateMethod = method;
                    }
                } catch (Throwable ignored) {
                }
            }
        }
        if (table != null && table.name().trim().length() != 0) {
            tableInfo.tableName = table.name();
        } else {
            tableInfo.tableName = clazz.getName().replace(".", "_");
        }

        Map<Field, DataType> fieldEnumMap = new HashMap<>();
        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            //如果这个字段加了ignore注解,我们就跳过
            if (SQLTypeParser.isIgnore(field)) {
                continue;
            }
            DataType dataType = SQLTypeParser.getDataType(field);
            if (dataType != null) {
                fieldEnumMap.put(field, dataType);
            } else {
                throw new IllegalArgumentException("Type of " + field.getClass().getName() + "is not support in database.");
            }
        }//end
        tableInfo.fieldToDataTypeMap = fieldEnumMap;
        buildPrimaryIDForTableInfo(tableInfo);
        tableInfo.createTableStatement = SQLMaker.createTable(tableInfo);

        synchronized (sCache) {
            sCache.put(clazz, tableInfo);
        }
        return tableInfo;
    }

    /**
     * 为一个Bean匹配一个ID字段,如果ID字段不存在,使用默认的_id替代.
     *
     * @param info info
     */
    private static TableInfo buildPrimaryIDForTableInfo(TableInfo info) {

        Field idField = null;
        ID id;
        for (Field field : info.fieldToDataTypeMap.keySet()) {
            id = field.getAnnotation(ID.class);
            if (id != null) {
                idField = field;
                break;
            }
        }//end
        if (idField != null) {
            //从字段表中移除ID
            info.fieldToDataTypeMap.remove(idField);
            info.containID = true;
            info.primaryField = idField;
        } else {
            info.containID = false;
            info.primaryField = null;
        }

        return info;
    }

    /**
     * 根据表名匹配TableInfo
     *
     * @param tableName tableName
     */
    public static TableInfo findTableInfoByName(String tableName) {

        for (TableInfo tableInfo : sCache.values()) {
            if (tableInfo.tableName.equals(tableName)) {
                return tableInfo;
            }
        }

        return null;
    }

    /**
     * 清除留在内存中的TableInfo缓存
     */
    public static void clearCache() {
        sCache.clear();
    }

}


