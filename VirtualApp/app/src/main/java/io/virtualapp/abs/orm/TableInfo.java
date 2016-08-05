package io.virtualapp.abs.orm;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * @author Lody 储存Table信息
 */
public class TableInfo {

	/**
	 * 是否包含ID
	 */
	public boolean containID;
	/**
	 * 主键字段
	 */
	public Field primaryField;

	/**
	 * 表名
	 */
	public String tableName;

	/**
	 * 字段表
	 */
	public Map<Field, DataType> fieldToDataTypeMap;

	/**
	 * 创建table的语句
	 */
	public String createTableStatement;

	/**
	 * 是否已经创建
	 */
	public boolean isCreate = false;

	public Method afterTableCreateMethod;

}
