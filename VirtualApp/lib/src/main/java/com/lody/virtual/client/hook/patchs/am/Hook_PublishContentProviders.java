package com.lody.virtual.client.hook.patchs.am;

import android.app.IActivityManager;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.local.LocalContentManager;

import java.lang.reflect.Method;
import java.util.List;

/**
 * @author Lody
 *
 */
@SuppressWarnings("unchecked")
/* package */ class Hook_PublishContentProviders extends Hook {

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
