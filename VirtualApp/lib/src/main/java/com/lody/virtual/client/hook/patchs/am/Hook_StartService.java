package com.lody.virtual.client.hook.patchs.am;

import android.app.IApplicationThread;
import android.content.Intent;
import android.content.pm.ServiceInfo;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.local.LocalServiceManager;
import com.lody.virtual.client.hook.base.Hook;

import java.lang.reflect.Method;

/**
 * @author Lody
 *
 *
 *         原型: public ComponentName startService( IApplicationThread caller,
 *         Intent service, String resolvedType, String callingPackage, int
 *         userId )
 */
/* package */ class Hook_StartService extends Hook {

	@Override
	public String getName() {
		return "startService";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		IApplicationThread appThread = (IApplicationThread) args[0];
		Intent service = (Intent) args[1];
		String resolvedType = (String) args[2];
		if (service != null
				&& service.getComponent() != null
				&& getHostPkg().equals(service.getComponent().getPackageName())) {
			// for server process
			return method.invoke(who, args);
		}
		ServiceInfo serviceInfo = VirtualCore.getCore().resolveServiceInfo(service);
		if (serviceInfo != null) {
			String pkgName = serviceInfo.packageName;
			if (pkgName.equals(getHostPkg())) {
				return method.invoke(who, args);
			}
			if (isAppPkg(pkgName)) {
				return LocalServiceManager.getInstance().startService(appThread.asBinder(), service, resolvedType);
			}
		}
		return method.invoke(who, args);
	}

	@Override
	public boolean isEnable() {
		return isAppProcess() || isServiceProcess();
	}
}
