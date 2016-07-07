package io.virtualapp.abs.orm;

import java.lang.reflect.Field;

import io.virtualapp.abs.orm.annotation.ID;

/**
 * @author Lody
 */
public class SQLMaker {

    /**
     * 构造<b>创建表</b>的语句
     *
     * @param tableInfo 表信息
     * @return 创建表的SQL语句
     */
    public static String createTable(TableInfo tableInfo) {
        StringBuilder statement = new StringBuilder();

        statement.append("CREATE TABLE ").append("'")
                .append(tableInfo.tableName).append("'")
                .append(" (");

        if (tableInfo.containID) {
            DataType dataType = SQLTypeParser.getDataType(tableInfo.primaryField);
            if (dataType == null) {
                throw new IllegalArgumentException("Type of " + tableInfo.primaryField.getType().getName() + " is not support in WelikeDB.");
            }
            statement.append("'").append(tableInfo.primaryField.getName()).append("'");
            switch (dataType) {
                case INTEGER:
                    statement.append(" INTEGER PRIMARY KEY ");
                    ID id = tableInfo.primaryField.getAnnotation(ID.class);
                    if (id != null && id.autoIncrement()) {
                        statement.append("AUTOINCREMENT");
                    }
                    break;
                default:
                    statement
                            .append("  ")
                            .append(dataType.name())
                            .append(" PRIMARY KEY");
            }

            statement.append(",");


        } else {
            statement.append("'_id' INTEGER PRIMARY KEY AUTOINCREMENT,");
        }


        for (Field field : tableInfo.fieldToDataTypeMap.keySet()) {
            DataType dataType = tableInfo.fieldToDataTypeMap.get(field);
            statement.append("'").append(field.getName()).append("'")
                    .append(" ")
                    .append(dataType.name());
            if (!dataType.nullable) {
                statement.append(" NOT NULL");
            }
            statement.append(",");
        }
        //删掉最后一个逗号
        statement.deleteCharAt(statement.length() - 1);
        statement.append(")");

        return statement.toString();
    }

    /**
     * 构建 插入一个Bean 的语句.
     *
     * @param o
     * @return
     */
    public static String insertIntoTable(Object o) {
        TableInfo tableInfo = TableBuilder.from(o.getClass());
        StringBuilder statement = new StringBuilder();
        statement.append("INSERT INTO ").append(tableInfo.tableName).append(" ");
        statement.append("VALUES(");

        if (tableInfo.containID) {
            DataType primaryDataType = SQLTypeParser.getDataType(tableInfo.primaryField);
            switch (primaryDataType) {
                case INTEGER:
                    statement.append("NULL,");
                    break;
                default:
                    try {
                        statement
                                .append(ValueConverter.valueToString(primaryDataType, tableInfo.primaryField, o))
                                .append(",");
                    } catch (IllegalAccessException e) {
                    }
                    break;
            }

        } else {
            statement.append("NULL,");
        }

        for (Field field : tableInfo.fieldToDataTypeMap.keySet()) {
            DataType dataType = tableInfo.fieldToDataTypeMap.get(field);
            try {
                statement.append(ValueConverter.valueToString(dataType, field, o)).append(",");
            } catch (IllegalAccessException e) {
                //不会发生...
            }
        }
        statement.deleteCharAt(statement.length() - 1);
        statement.append(")");

        return statement.toString();

    }

    /**
     * 根据where条件创建选择语句
     *
     * @param tableInfo
     * @param where
     * @return
     */
    public static String findByWhere(TableInfo tableInfo, String where) {
        StringBuilder statement = new StringBuilder("SELECT * FROM ");
        statement
                .append(tableInfo.tableName)
                .append(" ")
                .append("WHERE ")
                .append(where);

        return statement.toString();
    }


    /**
     * 根据where条件创建删除语句
     *
     * @param tableInfo
     * @param where
     * @return
     */
    public static String deleteByWhere(TableInfo tableInfo, String where) {
        StringBuilder statement = new StringBuilder("DELETE FROM ");
        statement
                .append(tableInfo.tableName)
                .append(" ")
                .append("WHERE ")
                .append(where);

        return statement.toString();
    }

    /**
     * 根据where条件创建更新语句
     *
     * @param tableInfo
     * @param bean
     * @param where
     * @return
     */
    public static String updateByWhere(TableInfo tableInfo, Object bean, String where) {
        StringBuilder builder = new StringBuilder("UPDATE ");

        builder.append(tableInfo.tableName).append(" SET ");

        for (Field f : tableInfo.fieldToDataTypeMap.keySet()) {

            try {
                builder.append(f.getName())
                        .append(" = ")
                        .append(ValueConverter.valueToString(
                                SQLTypeParser.getDataType(f.getType()),
                                f.get(bean))).append(",");
            } catch (Throwable e) {
            }
        }

        builder.deleteCharAt(builder.length() - 1);//删除最后一个逗号

        builder.append(" WHERE ");
        builder.append(where);
        return builder.toString();
    }

    /**
     * 创建选中table的语句
     *
     * @param tableName
     * @return
     */
    public static String selectTable(String tableName) {
        return "SELECT * FROM " + tableName;
    }

}
