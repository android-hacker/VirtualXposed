package com.lody.virtual.client.hook.patchs.am;

import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.IInterface;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.local.VActivityManager;
import com.lody.virtual.os.VUserHandle;

import java.lang.reflect.Method;

/**
 * @author Lody
 *
 */
/* package */ class StartService extends Hook {

	@Override
	public String getName() {
		return "startService";
	}

	@Override
	public Object call(Object who, Method method, Object... args) throws Throwable {
		IInterface appThread = (IInterface) args[0];
		Intent service = (Intent) args[1];
		String resolvedType = (String) args[2];
		if (service.getComponent() != null
				&& getHostPkg().equals(service.getComponent().getPackageName())) {
			// for server process
			return method.invoke(who, args);
		}
		service.setDataAndType(service.getData(), resolvedType);

		ServiceInfo serviceInfo = VirtualCore.get().resolveServiceInfo(service, VUserHandle.myUserId());
		if (serviceInfo != null) {
			String pkgName = serviceInfo.packageName;
			if (pkgName.equals(getHostPkg())) {
				return method.invoke(who, args);
			}
			return VActivityManager.get().startService(appThread, service, resolvedType);
		}
		return method.invoke(who, args);
	}

	@Override
	public boolean isEnable() {
		return isAppProcess() || isServiceProcess();
	}
}
