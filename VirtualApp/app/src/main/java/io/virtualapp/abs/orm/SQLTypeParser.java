package io.virtualapp.abs.orm;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import io.virtualapp.abs.orm.annotation.NotNull;
import io.virtualapp.abs.orm.annotation.Transient;

/**
 * @author Lody 解析Java的<b>数据类型</b>,将其转换为对应的SQL类型. 2015/8/17 :支持 boolean 类型.
 * @version 2.2
 */
public class SQLTypeParser {

	/**
	 * 根据字段类型匹配它在数据库中的对应类型.
	 *
	 * @param field
	 *            field
	 */
	public static DataType getDataType(Field field) {
		Class<?> clazz = field.getType();
		if (clazz == (String.class)) {
			return DataType.TEXT.nullable((field.getAnnotation(NotNull.class) == null));
		} else if (clazz == (int.class) || clazz == (Integer.class)) {
			return DataType.INTEGER.nullable((field.getAnnotation(NotNull.class) == null));
		} else if (clazz == (float.class) || clazz == (Float.class)) {
			return DataType.FLOAT.nullable((field.getAnnotation(NotNull.class) == null));
		} else if (clazz == (long.class) || clazz == (Long.class)) {
			return DataType.BIGINT.nullable((field.getAnnotation(NotNull.class) == null));
		} else if (clazz == (double.class) || clazz == (Double.class)) {
			return DataType.DOUBLE.nullable((field.getAnnotation(NotNull.class) == null));
		} else if (clazz == (boolean.class) || clazz == (Boolean.class)) {
			return DataType.INTEGER.nullable((field.getAnnotation(NotNull.class) == null));
		}
		throw new IllegalArgumentException("Unknown data Type : " + field.getType());
	}

	/**
	 * 根据字段类型匹配它在数据库中的对应类型.
	 *
	 * @param clazz
	 *            class
	 */
	public static DataType getDataType(Class<?> clazz) {
		if (clazz == (String.class)) {
			return DataType.TEXT;
		} else if (clazz == (int.class) || clazz == (Integer.class)) {
			return DataType.INTEGER;
		} else if (clazz == (float.class) || clazz == (Float.class)) {
			return DataType.FLOAT;
		} else if (clazz == (long.class) || clazz == (Long.class)) {
			return DataType.BIGINT;
		} else if (clazz == (double.class) || clazz == (Double.class)) {
			return DataType.DOUBLE;
		} else if (clazz == (boolean.class) || clazz == (Boolean.class)) {
			return DataType.INTEGER;
		}
		return null;
	}

	/**
	 * 字段类型与数据类型是否匹配?
	 *
	 * @param field
	 *            file
	 * @param dataType
	 *            dataType
	 */
	public static boolean matchType(Field field, DataType dataType) {
		DataType fieldDataType = getDataType(field.getType());

		return dataType != null && fieldDataType == (dataType);
	}

	/**
	 * 字段是否可以被数据库忽略?
	 *
	 * @param field
	 *            field
	 */
	public static boolean isIgnore(Field field) {
		return Modifier.isStatic(field.getModifiers()) || field.getAnnotation(Transient.class) != null;
	}

}
