package com.lody.virtual.client.hook.patchs.pm;

import java.lang.reflect.Method;

import com.lody.virtual.client.fixer.ComponentFixer;
import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.local.VPackageManager;

import android.content.ComponentName;
import android.content.pm.ProviderInfo;

/**
 * @author Lody
 *
 *
 *         原型: public ActivityInfo getServiceInfo(ComponentName className, int
 *         flags, int userId)
 *
 */
/* package */ class Hook_GetProviderInfo extends Hook {

	@Override
	public String getName() {
		return "getProviderInfo";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		ComponentName componentName = (ComponentName) args[0];
		int flags = (int) args[1];
		if (getHostPkg().equals(componentName.getPackageName())) {
			return method.invoke(who, args);
		}
		ProviderInfo providerInfo = VPackageManager.getInstance().getProviderInfo(componentName, flags);
		if (providerInfo != null) {
			ComponentFixer.fixUid(providerInfo.applicationInfo);
		}
		return providerInfo;
	}

}
