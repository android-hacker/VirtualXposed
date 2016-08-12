package com.lody.virtual.client.hook.providers;

import android.net.Uri;
import android.os.Bundle;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author Lody
 */

public class SettingsProviderHook extends ExternalProviderHook {

	private static final String TAG = SettingsProviderHook.class.getSimpleName();

	public static final int METHOD_GET = 0;
	public static final int METHOD_PUT = 0;

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

	@Override
	public Bundle call(Method method, Object[] args) throws InvocationTargetException, IllegalAccessException {
		String methodName = (String) args[args.length - 3];
		String arg = (String) args[args.length - 2];
		int methodType = getMethodType(methodName);
		if (METHOD_GET == methodType && "user_setup_complete".equals(arg)) {
			Bundle res = new Bundle();
			res.putString("value", "1");
			return res;
		} else if (METHOD_PUT == methodType && "package_verifier_user_consent".equals(arg)) {
			return null;
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
