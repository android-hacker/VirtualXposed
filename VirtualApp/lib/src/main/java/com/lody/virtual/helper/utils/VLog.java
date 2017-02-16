package com.lody.virtual.helper.utils;

import android.os.Bundle;
import android.util.Log;

import java.util.Set;

/**
 * @author Lody
 *
 */
public class VLog {

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

	public static void v(String tag, String msg, Object... format) {
		if (OPEN_LOG) {
			Log.v(tag, String.format(msg, format));
		}
	}

	public static String toString(Bundle bundle){
		if(bundle==null)return null;
		if(Reflect.on(bundle).get("mParcelledData")!=null){
			Set<String> keys=bundle.keySet();
			StringBuilder stringBuilder=new StringBuilder("Bundle[");
			if(keys!=null) {
				for (String key : keys) {
					stringBuilder.append(key);
					stringBuilder.append("=");
					stringBuilder.append(bundle.get(key));
					stringBuilder.append(",");
				}
			}
			stringBuilder.append("]");
			return stringBuilder.toString();
		}
		return bundle.toString();
	}

	public static String getStackTraceString(Throwable tr) {
		return Log.getStackTraceString(tr);
	}

	public static void printStackTrace(String tag) {
		Log.e(tag, getStackTraceString(new Exception()));
	}

	public static void e(String tag, Throwable e) {
		Log.e(tag, getStackTraceString(e));
	}
}
