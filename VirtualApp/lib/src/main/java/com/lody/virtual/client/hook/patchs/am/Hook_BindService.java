package com.lody.virtual.client.hook.patchs.am;

import java.lang.reflect.Method;

import com.lody.virtual.client.core.AppSandBox;
import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.env.HackServiceConnection;
import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.local.LocalServiceManager;

import android.app.Application;
import android.app.IApplicationThread;
import android.app.IServiceConnection;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.IBinder;

/**
 * @author Lody
 *         <p>
 *         <p>
 *         原型: public int bindService(IApplicationThread caller, IBinder token,
 *         Intent service, String resolvedType, IServiceConnection connection,
 *         int flags, String callingPackage, int userId)
 */
/* package */ class Hook_BindService extends Hook {

	@Override
	public String getName() {
		return "bindService";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		IApplicationThread caller = (IApplicationThread) args[0];
		IBinder token = (IBinder) args[1];
		Intent service = (Intent) args[2];
		String resolvedType = (String) args[3];
		IServiceConnection connection = (IServiceConnection) args[4];
		int flags = (int) args[5];
		ServiceInfo serviceInfo = VirtualCore.getCore().resolveServiceInfo(service);
		if (serviceInfo != null) {
			String pkgName = serviceInfo.packageName;
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.L) {
				service.setComponent(new ComponentName(serviceInfo.packageName, serviceInfo.name));
			}
			if (isAppPkg(pkgName)) {
				Application application = AppSandBox.getApplication(pkgName);
				connection = new HackServiceConnection(application, connection);
				return LocalServiceManager.getInstance().bindService(caller.asBinder(), token, service, resolvedType,
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
