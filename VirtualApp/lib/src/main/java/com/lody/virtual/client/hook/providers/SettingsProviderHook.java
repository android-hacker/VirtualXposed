package com.lody.virtual.client.hook.providers;

import android.net.Uri;
import android.os.Bundle;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Lody
 */

public class SettingsProviderHook extends ExternalProviderHook {

	private static final String TAG = SettingsProviderHook.class.getSimpleName();

	public static final int METHOD_GET = 0;
	public static final int METHOD_PUT = 0;

	private static final Map<String, String> PRE_SET_VALUES = new HashMap<>();

	static {
		PRE_SET_VALUES.put("user_setup_complete", "1");
	}


	public SettingsProviderHook(Object base) {
		super(base);
	}

	public static int getMethodType(String method) {
		if (method.startsWith("GET_")) {
			return METHOD_GET;
		}
		if (method.startsWith("PUT_")) {
			return METHOD_PUT;
		}
		return -1;
	}

	public static boolean isSecureMethod(String method) {
		return method.endsWith("secure");
	}


	@Override
	public Bundle call(Method method, Object[] args) throws InvocationTargetException, IllegalAccessException {
		String methodName = (String) args[args.length - 3];
		String arg = (String) args[args.length - 2];
		int methodType = getMethodType(methodName);
		if (METHOD_GET == methodType) {
			String presetValue = PRE_SET_VALUES.get(arg);
			if (presetValue != null) {
				Bundle res = new Bundle();
				res.putString("value", presetValue);
				return res;
			}
		}
		if (METHOD_PUT == methodType) {
			if (isSecureMethod(methodName)) {
				return null;
			}
		}
		return super.call(method, args);
	}

	@Override
	public Uri insert(Method method, Object[] args) throws InvocationTargetException, IllegalAccessException {
		return super.insert(method, args);
	}

	@Override
	protected void processArgs(Method method, Object... args) {
		super.processArgs(method, args);
	}
}
