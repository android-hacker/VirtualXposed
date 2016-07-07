package com.lody.virtual.helper.compat;

import java.lang.reflect.Method;
import java.util.List;

import android.content.pm.ParceledListSlice;
import android.os.Parcelable;

/**
 * @author Lody
 *
 */
@SuppressWarnings("unchecked")
public class ParceledListSliceCompat {

	public static boolean isReturnParceledListSlice(Method method) {
		return method != null && method.getReturnType() == ParceledListSlice.class;
	}

	public static <T extends Parcelable> ParceledListSlice<T> create(List<T> list) {
		try {
			return new ParceledListSlice<T>(list);
		} catch (Throwable e) {
			try {
				ParceledListSlice<T> slice = ParceledListSlice.class.newInstance();
				Method m_append = ParceledListSlice.class.getMethod("append", Parcelable.class);
				for (T item : list) {
					m_append.invoke(slice, item);
				}
				try {
					Method m_setLastSlice = ParceledListSlice.class.getMethod("setLastSlice", boolean.class);
					m_setLastSlice.invoke(slice, true);
				} catch (Throwable err) {
					err.printStackTrace();
				}
				return slice;
			} catch (Throwable notHappen) {
				notHappen.printStackTrace();
			}
			return null;
		}
	}

}
