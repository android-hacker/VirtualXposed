package com.lody.virtual.client.hook.patchs.am;

import android.app.IActivityManager;
import android.app.IApplicationThread;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.env.Constants;
import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.local.LocalContentManager;

import java.lang.reflect.Method;

/**
 * @author Lody
 *
 *
 * @see IActivityManager#getContentProvider(IApplicationThread, String, int,
 *      boolean) 原型: public ContentProviderHolder
 *      getContentProvider(IApplicationThread caller, String name)
 */
/* package */ class Hook_GetContentProvider extends Hook<ActivityManagerPatch> {

	/**
	 * 这个构造器必须有,用于依赖注入.
	 *
	 * @param patchObject
	 *            注入对象
	 */
	public Hook_GetContentProvider(ActivityManagerPatch patchObject) {
		super(patchObject);
	}

	@Override
	public String getName() {
		return "getContentProvider";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		int N = getProviderNameIndex();
		String name = (String) args[N];
		if (Constants.GMS_PKG.equals(name)) {
			// 隔离Gms
			return null;
		}
		if (!VirtualCore.getCore().isHostProvider(name)) {
			IActivityManager.ContentProviderHolder holder = LocalContentManager.getDefault().getContentProvider(name);
			if (holder != null) {
				return holder;
			}
		}
		return method.invoke(who, args);
	}

	public int getProviderNameIndex() {
		return 1;
	}

	@Override
	public boolean isEnable() {
		return isAppProcess();
	}
}
