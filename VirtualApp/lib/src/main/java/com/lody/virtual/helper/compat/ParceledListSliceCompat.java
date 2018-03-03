package com.lody.virtual.helper.compat;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

import mirror.android.content.pm.ParceledListSlice;
import mirror.android.content.pm.ParceledListSliceJBMR2;

/**
 * @author Lody
 *
 */
public class ParceledListSliceCompat {

	public static boolean isReturnParceledListSlice(Method method) {
		return method != null && method.getReturnType() == ParceledListSlice.TYPE;
	}

	public static  Object create(List list) {
		if (ParceledListSliceJBMR2.ctor != null) {
			return ParceledListSliceJBMR2.ctor.newInstance(list);
		} else {
			Object slice = ParceledListSlice.ctor.newInstance();
			for (Object item : list) {
				ParceledListSlice.append.call(slice, item);
			}
			ParceledListSlice.setLastSlice.call(slice, true);
			return slice;
		}
	}

	public static List getList(Object parceledList) {
		if (parceledList == null || parceledList.getClass() != ParceledListSlice.TYPE) {
			return Collections.EMPTY_LIST;
		}

		if (ParceledListSliceJBMR2.getList != null) {
			return ParceledListSliceJBMR2.getList.call(parceledList);
		} else {
			return ParceledListSlice.getList.call(parceledList);
		}
	}
}
