package com.lody.virtual.helper.utils;

import android.util.Log;

import com.lody.virtual.BuildConfig;

/**
 * @author Lody
 *
 */
public class VLog {

	public static boolean OPEN_LOG = BuildConfig.DEBUG;

	public static void i(String tag, String msg, Object... format) {
		if (OPEN_LOG) {
			Log.i(tag, String.format(msg, format));
		}
	}

	public static void d(String tag, String msg, Object... format) {
		if (OPEN_LOG) {
			Log.d(tag, String.format(msg, format));
		}
	}

	public static void w(String tag, String msg, Object... format) {
		if (OPEN_LOG) {
			Log.w(tag, String.format(msg, format));
		}
	}

	public static void e(String tag, String msg, Object... format) {
		if (OPEN_LOG) {
			Log.e(tag, String.format(msg, format));
		}
	}

	public static void v(String tag, String msg, Object... format) {
		if (OPEN_LOG) {
			Log.v(tag, String.format(msg, format));
		}
	}
}
