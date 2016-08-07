package com.lody.virtual.client.hook.patchs.pm;

import static android.content.pm.PackageManager.GET_DISABLED_COMPONENTS;

import java.lang.reflect.Method;

import com.lody.virtual.client.fixer.ComponentFixer;
import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.local.LocalPackageManager;

import android.content.ComponentName;
import android.content.pm.ActivityInfo;

/**
 * @author Lody
 *
 *
 *         原型: public ActivityInfo getServiceInfo(ComponentName className, int
 *         flags, int userId)
 *
 */
/* package */ class Hook_GetReceiverInfo extends Hook {

	@Override
	public String getName() {
		return "getReceiverInfo";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		ComponentName componentName = (ComponentName) args[0];
		if (getHostPkg().equals(componentName.getPackageName())) {
			return method.invoke(who, args);
		}
		int flags = (int) args[1];
		if ((flags & GET_DISABLED_COMPONENTS) == 0) {
			flags |= GET_DISABLED_COMPONENTS;
		}
		ActivityInfo receiverInfo = LocalPackageManager.getInstance().getReceiverInfo(componentName, flags);
		if (receiverInfo != null) {
			ComponentFixer.fixUid(receiverInfo.applicationInfo);
		}
		return receiverInfo;
	}
}
