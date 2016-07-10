package com.lody.virtual.helper.compat;

import java.lang.reflect.Field;

/**
 * @author Lody
 */

public class ClassLoaderCompat {

	public static ClassLoader setParent(ClassLoader classLoader, ClassLoader parent) {
		ClassLoader oldParent = classLoader.getParent();
		try {
			Field parentField = ClassLoader.class.getDeclaredField("parent");
			parentField.setAccessible(true);
			parentField.set(classLoader, parent);
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return oldParent;
	}

}
