package com.lody.virtual.helper.utils;

/**
 * @author Lody
 *
 */
public class ClassUtils {

	public static boolean isClassExist(String className) {
		try {
			Class.forName(className);
			return true;
		} catch (ClassNotFoundException e) {
			return false;
		}
	}

	public static void fixArgs(Class<?>[] types, Object[] args) {
		for (int i = 0; i < types.length; i++) {
			if (types[i] == int.class && args[i] == null) {
				args[i] = 0;
			} else if (types[i] == boolean.class && args[i] == null) {
				args[i] = false;
			}
		}
	}
}
