package com.lody.virtual.helper.utils;

/**
 * @author Lody
 *
 */
public class ArrayIndex {

	public static int indexOfFirst(Object[] array, Class<?> type) {
		if (!isEmpty(array)) {
			int N = -1;
			for (Object one : array) {
				N++;
				if (one != null && type == one.getClass()) {
					return N;
				}
			}
		}
		return -1;
	}

	public static int indexOfFirstAssignable(Object[] array, Class<?> type) {
		if (!isEmpty(array)) {
			int N = -1;
			for (Object one : array) {
				N++;
				if (one != null && type.isAssignableFrom(one.getClass())) {
					return N;
				}
			}
		}
		return -1;
	}

	public static int indexOfAssignable(Object[] array, Class<?> type, int sequence) {
		if (!isEmpty(array)) {
			int N = -1;
			for (Object one : array) {
				N++;
				if (one != null && type.isAssignableFrom(one.getClass())) {
					if (--sequence <= 0) {
						return N;
					}
				}
			}
		}
		return -1;
	}

	public static int indexOf(Object[] array, Class<?> type, int sequence) {
		if (!isEmpty(array)) {
			int N = -1;
			for (Object one : array) {
				N++;
				if (one != null && one.getClass() == type) {
					if (--sequence <= 0) {
						return N;
					}
				}
			}
		}
		return -1;
	}

	public static int indexOfLast(Object[] array, Class<?> type) {
		if (!isEmpty(array)) {
			for(int N=array.length; N>0; N--) {
				Object one = array[N-1];
				if (one != null && one.getClass() == type) {
					return N-1;
				}
			}
		}
		return -1;
	}

	public static <T> boolean isEmpty(T[] array) {
		return array == null || array.length == 0;
	}

	@SuppressWarnings("unchecked")
	public static <T> T getSecond(Object[] args, Class<?> clazz) {
		int index = indexOf(args, clazz, 2);
		if (index != -1) {
			return (T) args[index];
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public static <T> T getFirst(Object[] args, Class<?> clazz) {
		int index = indexOfFirst(args, clazz);
		if (index != -1) {
			return (T) args[index];
		}
		return null;
	}
}
