package com.lody.virtual.helper.utils;

import android.util.Log;

/**
 * @author Lody
 *
 */
public class XLog {

	public static boolean OPEN_LOG = true;

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

}
