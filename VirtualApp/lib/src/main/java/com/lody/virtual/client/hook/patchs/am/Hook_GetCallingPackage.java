package com.lody.virtual.client.hook.patchs.am;

import java.lang.reflect.Method;

import com.lody.virtual.client.core.AppSandBox;
import com.lody.virtual.client.hook.base.Hook;

import android.os.IBinder;

/**
 * @author Lody
 *
 *
 * @see android.app.IActivityManager#getCallingPackage(IBinder)
 */
/* package */ class Hook_GetCallingPackage extends Hook<ActivityManagerPatch> {
	/**
	 * 这个构造器必须有,用于依赖注入.
	 *
	 * @param patchObject
	 *            注入对象
	 */
	public Hook_GetCallingPackage(ActivityManagerPatch patchObject) {
		super(patchObject);
	}

	@Override
	public String getName() {
		return "getCallingPackage";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		return AppSandBox.getInstalledPackages()[0];
	}

	@Override
	public boolean isEnable() {
		return isAppProcess();
	}
}
