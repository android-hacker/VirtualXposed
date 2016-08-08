package io.virtualapp.abs.orm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Lody 注解在字段上表示主键.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ID {
	/**
	 * 只对Integer类型的ID字段有效
	 *
	 * @return 是否为自增长
	 */
	boolean autoIncrement() default false;
}
