package com.lody.virtual.client.hook.patchs.search;

import android.content.ComponentName;
import android.content.pm.ActivityInfo;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.hook.base.Hook;

import java.lang.reflect.Method;

/**
 * @author Lody
 *
 *
 * @see android.app.ISearchManager#getSearchableInfo(ComponentName)
 */
/* package */ class Hook_GetSearchableInfo extends Hook {

	@Override
	public String getName() {
		return "getSearchableInfo";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		ComponentName component = (ComponentName) args[0];
		if (component != null) {
			ActivityInfo activityInfo = VirtualCore.getPM().getActivityInfo(component, 0);
			if (activityInfo != null && isAppPkg(activityInfo.packageName)) {
				return null;
			}
		}
		return method.invoke(who, args);
	}
}
