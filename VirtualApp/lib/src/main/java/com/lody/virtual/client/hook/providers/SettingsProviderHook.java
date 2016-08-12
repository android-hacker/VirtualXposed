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

	public SettingsProviderHook(Object base) {
		super(base);
	}

	@Override
	public Bundle call(Method method, Object[] args) throws InvocationTargetException, IllegalAccessException {
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
