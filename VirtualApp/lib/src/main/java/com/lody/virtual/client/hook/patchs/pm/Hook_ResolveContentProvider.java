package com.lody.virtual.client.hook.patchs.pm;

import java.lang.reflect.Method;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.local.VPackageManager;

/**
 * @author Lody
 *
 *
 *         原型: public ProviderInfo resolveContentProvider(String name, int
 *         flags, int userId);
 * @see android.content.pm.IPackageManager#resolveContentProvider(String, int,
 *      int)
 */
/* package */ class Hook_ResolveContentProvider extends Hook {

	@Override
	public String getName() {
		return "resolveContentProvider";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		String name = (String) args[0];
		int flags = (int) args[1];
		return VPackageManager.getInstance().resolveContentProvider(name, flags);
	}
}
