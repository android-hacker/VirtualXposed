package com.lody.virtual.client.hook.patchs.am;

import java.lang.reflect.Method;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.local.LocalServiceManager;
import com.lody.virtual.client.hook.base.Hook;

import android.app.IApplicationThread;
import android.app.IServiceConnection;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.IBinder;

/**
 * @author Lody
 *
 *         <p/>
 *         原型: public int bindService(IApplicationThread caller, IBinder token,
 *         Intent service, String resolvedType, IServiceConnection connection,
 *         int flags, String callingPackage, int userId)
 */
/* package */ class Hook_BindService extends Hook<ActivityManagerPatch> {
	/**
	 * 这个构造器必须有,用于依赖注入.
	 *
	 * @param patchObject
	 *            注入对象
	 */
	public Hook_BindService(ActivityManagerPatch patchObject) {
		super(patchObject);
	}

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
			if (isAppPkg(pkgName)) {
				return LocalServiceManager.getInstance().bindService(caller.asBinder(), token, service, resolvedType,
						connection, flags);
			}
		}
		return method.invoke(who, args);
	}

	@Override
	public boolean isEnable() {
		return isAppProcess();
	}
}
