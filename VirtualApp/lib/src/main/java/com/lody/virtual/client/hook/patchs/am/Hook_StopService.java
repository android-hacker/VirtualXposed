package com.lody.virtual.client.hook.patchs.am;

import java.lang.reflect.Method;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.local.LocalServiceManager;

import android.app.IApplicationThread;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

/**
 * @author Lody
 *
 *         原型: public int stopService(IApplicationThread caller, Intent service,
 *         String resolvedType, int userId)
 */
/* package */ class Hook_StopService extends Hook {

	@Override
	public String getName() {
		return "stopService";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		IApplicationThread caller = (IApplicationThread) args[0];
		Intent intent = (Intent) args[1];
		String resolvedType = (String) args[2];
		ComponentName componentName = intent.getComponent();
		PackageManager pm = VirtualCore.getPM();
		if (componentName == null) {
			ResolveInfo resolveInfo = pm.resolveService(intent, 0);
			if (resolveInfo != null && resolveInfo.serviceInfo != null) {
				componentName = new ComponentName(resolveInfo.serviceInfo.packageName, resolveInfo.serviceInfo.name);
			}
		}
		if (componentName != null) {
			String pkgName = componentName.getPackageName();
			if (isAppPkg(pkgName)) {
				return LocalServiceManager.getInstance().stopService(caller.asBinder(), intent, resolvedType);
			}
		}
		return method.invoke(who, args);
	}

	@Override
	public boolean isEnable() {
		return isAppProcess() || isServiceProcess();
	}
}
