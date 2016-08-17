package com.lody.virtual.client.hook.patchs.pm;

import android.content.ComponentName;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.local.VPackageManager;
import com.lody.virtual.os.VUserHandle;

import java.lang.reflect.Method;

import static android.content.pm.PackageManager.GET_DISABLED_COMPONENTS;

/**
 * @author Lody
 *
 *
 *         原型: public ActivityInfo getActivityInfo(ComponentName className, int
 *         flags, int userId)
 *
 */
/* package */ class Hook_GetActivityInfo extends Hook {

	@Override
	public String getName() {
		return "getActivityInfo";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		ComponentName componentName = (ComponentName) args[0];
		if (getHostPkg().equals(componentName.getPackageName())) {
			return method.invoke(who, args);
		}
		int userId = isAppProcess() ? VUserHandle.myUserId() : VUserHandle.USER_OWNER;
		if (args.length > 2 && args[2] instanceof Integer) {
			userId = (int) args[2];
		}
		int flags = (int) args[1];
		if ((flags & GET_DISABLED_COMPONENTS) == 0) {
			flags |= GET_DISABLED_COMPONENTS;
		}
		return VPackageManager.getInstance().getActivityInfo(componentName, flags, userId);
	}

	@Override
	public boolean isEnable() {
		return isAppProcess();
	}
}
