package com.lody.virtual.helper.bundle;

import java.util.Comparator;

import android.content.ComponentName;
import android.text.TextUtils;

public class ComponentNameComparator implements Comparator<ComponentName> {

	public static final ComponentNameComparator sComparator = new ComponentNameComparator();

	@Override
	public int compare(ComponentName lhs, ComponentName rhs) {
		if (lhs == null && rhs == null) {
			return 0;
		} else if (lhs != null && rhs == null) {
			return 1;
		} else if (lhs == null) {
			return -1;
		} else {
			if (TextUtils.equals(lhs.getPackageName(), rhs.getPackageName())
					&& TextUtils.equals(lhs.getShortClassName(), rhs.getShortClassName())) {
				return 0;
			} else {
				return lhs.compareTo(rhs);
			}
		}
	}
}