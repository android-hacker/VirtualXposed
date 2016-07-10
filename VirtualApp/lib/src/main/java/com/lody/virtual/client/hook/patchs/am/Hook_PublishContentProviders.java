package com.lody.virtual.client.hook.patchs.am;

import java.lang.reflect.Method;
import java.util.List;

import com.lody.virtual.client.local.LocalContentManager;
import com.lody.virtual.client.hook.base.Hook;

import android.app.IActivityManager;

/**
 * @author Lody
 *
 */
@SuppressWarnings("unchecked")
/* package */ class Hook_PublishContentProviders extends Hook<ActivityManagerPatch> {
	/**
	 * 这个构造器必须有,用于依赖注入.
	 *
	 * @param patchObject
	 *            注入对象
	 */
	public Hook_PublishContentProviders(ActivityManagerPatch patchObject) {
		super(patchObject);
	}

	@Override
	public String getName() {
		return "publishContentProviders";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		List<IActivityManager.ContentProviderHolder> providers = (List<IActivityManager.ContentProviderHolder>) args[1];
		LocalContentManager.getDefault().publishContentProviders(providers);
		return method.invoke(who, args);
	}

	@Override
	public boolean isEnable() {
		return isAppProcess();
	}
}
