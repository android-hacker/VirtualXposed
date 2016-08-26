package com.lody.virtual.client.hook.patchs.am;

import android.app.IServiceConnection;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.IBinder;
import android.os.IInterface;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.local.VActivityManager;
import com.lody.virtual.os.VUserHandle;

import java.lang.reflect.Method;

/**
 * @author Lody
 */
/* package */ class BindService extends Hook {

	@Override
	public String getName() {
		return "bindService";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		IInterface caller = (IInterface) args[0];
		IBinder token = (IBinder) args[1];
		Intent service = (Intent) args[2];
		String resolvedType = (String) args[3];
		IServiceConnection connection = (IServiceConnection) args[4];
		int flags = (int) args[5];
		int userId = VUserHandle.myUserId();
		if (isServiceProcess()) {
			userId = service.getIntExtra("_VA_|_user_id_", VUserHandle.USER_NULL);
		}
		ServiceInfo serviceInfo = VirtualCore.get().resolveServiceInfo(service, userId);
		if (serviceInfo != null) {
			String pkgName = serviceInfo.packageName;
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				service.setComponent(new ComponentName(serviceInfo.packageName, serviceInfo.name));
			}
			if (isAppPkg(pkgName)) {
				return VActivityManager.get().bindService(caller.asBinder(), token, service, resolvedType,
						connection, flags);
			}
		}
		return method.invoke(who, args);
	}

	@Override
	public boolean isEnable() {
		return isAppProcess() || isServiceProcess();
	}
}
