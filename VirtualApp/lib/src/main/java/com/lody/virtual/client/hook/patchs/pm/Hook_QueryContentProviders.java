package com.lody.virtual.client.hook.patchs.pm;

import com.lody.virtual.client.hook.base.Hook;

import java.lang.reflect.Method;

/**
 * @author Lody
 *
 *         <p/>
 *         Android 4.4+
 *
 *         @see android.content.pm.IPackageManager#queryContentProviders(String, int, int)
 *
 */
@SuppressWarnings("unchecked")
/* package */ class Hook_QueryContentProviders extends Hook {

	@Override
	public String getName() {
		return "queryContentProviders";
	}

	@Override
	public boolean beforeHook(Object who, Method method, Object... args) {
		return false;
	}
	

}
