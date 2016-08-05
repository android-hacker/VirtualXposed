package io.virtualapp.abs.orm;

/**
 * @author Lody
 *         <p>
 *         数据库支持的数据类型的枚举类
 */
public enum DataType {
	/**
	 * int类型
	 */
	INTEGER,
	/**
	 * String类型
	 */
	TEXT,
	/**
	 * float类型
	 */
	FLOAT,
	/**
	 * long类型
	 */
	BIGINT,
	/**
	 * double类型
	 */
	DOUBLE;

	boolean nullable = true;

	/**
	 * 数据类型是否允许为null
	 */
	public DataType nullable(boolean nullable) {
		this.nullable = nullable;
		return this;
	}

}
