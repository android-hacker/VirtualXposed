package com.lody.virtual.client.hook.patchs.search;

import java.lang.reflect.Method;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.hook.base.Hook;

import android.content.ComponentName;
import android.content.pm.ActivityInfo;

/**
 * @author Lody
 *
 *
 * @see android.app.ISearchManager#getSearchableInfo(ComponentName)
 */
/* package */ class GetSearchableInfo extends Hook {

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
