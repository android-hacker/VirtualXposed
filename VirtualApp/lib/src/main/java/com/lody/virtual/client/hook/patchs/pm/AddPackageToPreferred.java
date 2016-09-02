package com.lody.virtual.client.hook.patchs.pm;

import java.lang.reflect.Method;

import com.lody.virtual.client.hook.base.Hook;

/**
 * @author Lody
 *
 * @see android.content.pm.IPackageManager#addPackageToPreferred(String)
 *
 *
 */
/* package */ class AddPackageToPreferred extends Hook {

	@Override
	public String getName() {
		return "addPackageToPreferred";
	}

	@Override
	public Object call(Object who, Method method, Object... args) throws Throwable {
		return 0;
	}
}
