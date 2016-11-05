package com.lody.virtual.client.hook.patchs.am;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.IInterface;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.ipc.VActivityManager;

import java.lang.reflect.Method;

/**
 * @author Lody
 *
 *         原型: public int stopService(IApplicationThreadKitkat caller, Intent service,
 *         String resolvedType, int userId)
 */
/* package */ class StopService extends Hook {

	@Override
	public String getName() {
		return "stopService";
	}

	@Override
	public Object call(Object who, Method method, Object... args) throws Throwable {
		IInterface caller = (IInterface) args[0];
		Intent intent = (Intent) args[1];
		String resolvedType = (String) args[2];
		intent.setDataAndType(intent.getData(), resolvedType);
		ComponentName componentName = intent.getComponent();
		PackageManager pm = VirtualCore.getPM();
		if (componentName == null) {
			ResolveInfo resolveInfo = pm.resolveService(intent, 0);
			if (resolveInfo != null && resolveInfo.serviceInfo != null) {
				componentName = new ComponentName(resolveInfo.serviceInfo.packageName, resolveInfo.serviceInfo.name);
			}
		}
		if (componentName != null && !getHostPkg().equals(componentName.getPackageName())) {
			return VActivityManager.get().stopService(caller, intent, resolvedType);
		}
		return method.invoke(who, args);
	}

	@Override
	public boolean isEnable() {
		return isAppProcess() || isServerProcess();
	}
}
