package com.lody.virtual.client.hook.patchs.pm;

import java.lang.reflect.Method;
import java.util.List;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.hook.utils.HookUtils;

/**
 * @author Lody
 *
 * @see android.content.pm.IPackageManager#getPreferredActivities(List, List,
 *      String)
 *
 */
/* package */ class GetPreferredActivities extends Hook {

	@Override
	public String getName() {
		return "getPreferredActivities";
	}

	@Override
	public Object call(Object who, Method method, Object... args) throws Throwable {
		HookUtils.replaceLastAppPkg(args);
		return method.invoke(who, args);
	}
}
