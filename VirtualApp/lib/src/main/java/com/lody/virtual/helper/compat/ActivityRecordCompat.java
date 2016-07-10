package com.lody.virtual.helper.compat;

import java.lang.reflect.Field;

import android.content.Intent;
import android.content.pm.ActivityInfo;

/**
 * @author Lody
 *
 */
public class ActivityRecordCompat {

	private static Field f_intent;

	private static Field f_PackageInfo;

	private static Field f_activityInfo;
	/**
	 * 确保字段已经初始化
	 * 
	 * @param r
	 *            ActivityClientRecord对象
	 */
	private static void ensureFieldInit(Object r) {
		if (f_intent == null) {
			try {
				f_intent = r.getClass().getDeclaredField("intent");
				f_intent.setAccessible(true);
			} catch (Throwable e) {
				// Ignore
			}
		}

		if (f_PackageInfo == null) {
			try {
				f_PackageInfo = r.getClass().getDeclaredField("packageInfo");
				f_PackageInfo.setAccessible(true);
			} catch (Throwable e) {
				// Ignore
			}
		}
		if (f_activityInfo == null) {
			try {
				f_activityInfo = r.getClass().getDeclaredField("activityInfo");
				f_activityInfo.setAccessible(true);
			} catch (Throwable e) {
				// Ignore
			}
		}
	}

	public static Intent getIntent(Object r) {
		ensureFieldInit(r);
		try {
			return (Intent) f_intent.get(r);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	public static void setIntent(Object r, Intent intent) {
		ensureFieldInit(r);
		try {
			if (f_intent != null) {
				f_intent.set(r, intent);
			}
		} catch (Throwable e) {
			// Ignore
		}
	}
	public static void setActivityInfo(Object r, ActivityInfo activityInfo) {
		ensureFieldInit(r);
		try {
			if (f_activityInfo != null) {
				f_activityInfo.set(r, activityInfo);
			}
		} catch (Throwable e) {
			// Ignore
		}
	}

	public static ActivityInfo getActivityInfo(Object r) {
		ensureFieldInit(r);
		try {
			if (f_activityInfo != null) {
				return (ActivityInfo) f_activityInfo.get(r);
			}
		} catch (Throwable e) {
			// Ignore
		}
		return null;
	}

}
