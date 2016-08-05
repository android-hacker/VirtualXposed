package com.lody.virtual.client.hook.providers;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.helper.utils.VLog;

import android.net.Uri;
import android.os.Bundle;

/**
 * @author Lody
 */

public class SettingsProviderHook extends ProviderHook {

	private static final String TAG = SettingsProviderHook.class.getSimpleName();

	public SettingsProviderHook(Object base) {
		super(base);
	}

	@Override
	public Bundle call(Method method, Object[] args) throws InvocationTargetException, IllegalAccessException {
		VLog.d(TAG, "call %s", Arrays.toString(args));
		if (args[1] instanceof String) {
			String methodName = (String) args[1];
			if (methodName.endsWith("secure")) {
				try {
					return super.call(method, args);
				} catch (InvocationTargetException e) {
					if (e.getCause() instanceof SecurityException) {
						return null;
					}
					throw e;
				}
			}
		}
		return super.call(method, args);
	}

	@Override
	public Uri insert(Method method, Object[] args) throws InvocationTargetException, IllegalAccessException {
		VLog.d(TAG, "insert %s", Arrays.toString(args));
		return super.insert(method, args);
	}

	@Override
	protected void processArgs(Method method, Object... args) {
		if (args != null && args.length > 0 && args[0] instanceof String) {
			String pkg = (String) args[0];
			if (VirtualCore.getCore().isAppInstalled(pkg)) {
				args[0] = VirtualCore.getCore().getHostPkg();
			}
		}
	}
}
