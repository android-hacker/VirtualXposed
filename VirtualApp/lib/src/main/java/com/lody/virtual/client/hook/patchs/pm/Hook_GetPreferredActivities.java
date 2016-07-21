package com.lody.virtual.client.hook.patchs.pm;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.hook.utils.HookUtils;

import java.lang.reflect.Method;
import java.util.List;

/**
 * @author Lody
 *
 * @see android.content.pm.IPackageManager#getPreferredActivities(List, List,
 *      String)
 *
 */
/* package */ class Hook_GetPreferredActivities extends Hook {

	@Override
	public String getName() {
		return "getPreferredActivities";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		HookUtils.replaceLastAppPkg(args);
		return method.invoke(who, args);
	}
}
