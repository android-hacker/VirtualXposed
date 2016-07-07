package com.lody.virtual.client.hook.patchs.am;

import java.lang.reflect.Method;

import com.lody.virtual.client.local.LocalServiceManager;
import com.lody.virtual.client.hook.base.Hook;

import android.app.IServiceConnection;

/**
 * @author Lody
 *
 *         原型: public boolean unbindService(IServiceConnection connection)
 */
/* package */ class Hook_UnbindService extends Hook<ActivityManagerPatch> {
	/**
	 * 这个构造器必须有,用于依赖注入.
	 *
	 * @param patchObject
	 *            注入对象
	 */
	public Hook_UnbindService(ActivityManagerPatch patchObject) {
		super(patchObject);
	}

	@Override
	public String getName() {
		return "unbindService";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		IServiceConnection connection = (IServiceConnection) args[0];
		return LocalServiceManager.getInstance().unbindService(connection);
	}

	@Override
	public boolean isEnable() {
		return isAppProcess();
	}
}
