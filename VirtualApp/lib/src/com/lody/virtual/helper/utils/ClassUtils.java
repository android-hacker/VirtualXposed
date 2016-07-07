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
}
