package com.lody.virtual.client.hook.patchs.pm;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.local.VPackageManager;

import java.lang.reflect.Method;

/**
 * @author Lody
 *
 *         <p/>
 *         Android 4.4+
 *
 * @see android.content.pm.IPackageManager#queryContentProviders(String, int,
 *      int)
 *
 */
@SuppressWarnings("unchecked")
/* package */ class Hook_QueryContentProviders extends Hook {

	@Override
	public String getName() {
		return "queryContentProviders";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		String processName = (String) args[0];
		int flags = (int) args[2];
		return VPackageManager.getInstance().queryContentProviders(processName, flags, 0);
	}

}
