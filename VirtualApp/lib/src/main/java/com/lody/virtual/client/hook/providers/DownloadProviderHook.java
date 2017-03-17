package com.lody.virtual.client.hook.providers;

import android.net.Uri;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author Lody
 */

class DownloadProviderHook extends ExternalProviderHook {

	DownloadProviderHook(Object base) {
		super(base);
	}

    @Override
    protected void processArgs(Method method, Object... args) {
        // empty
    }

    @Override
	public Uri insert(Method method, Object[] args) throws InvocationTargetException, IllegalAccessException {
		return super.insert(method, args);
	}


}
