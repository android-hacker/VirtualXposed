package com.lody.virtual.client.hook.patchs.am;

import java.lang.reflect.Method;

import com.lody.virtual.client.local.LocalServiceManager;
import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.hook.utils.HookUtils;

import android.content.Intent;

/**
 * @author Lody
 *
 * @see android.app.IActivityManager#peekService(Intent, String, String)
 */
/* package */ class Hook_PeekService extends Hook<ActivityManagerPatch> {

	/**
	 * 这个构造器必须有,用于依赖注入.
	 *
	 * @param patchObject
	 *            注入对象
	 */
	public Hook_PeekService(ActivityManagerPatch patchObject) {
		super(patchObject);
	}

	@Override
	public String getName() {
		return "peekService";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		HookUtils.replaceLastAppPkg(args);
		Intent service = (Intent) args[0];
		String resolvedType = (String) args[1];
		return LocalServiceManager.getInstance().peekService(service, resolvedType);
	}

	@Override
	public boolean isEnable() {
		return isAppProcess();
	}
}
