package com.lody.virtual.client.hook.patchs.pm;

import java.lang.reflect.Method;
import java.util.List;

import com.lody.virtual.client.local.LocalPackageManager;
import com.lody.virtual.client.hook.base.Hook;

import android.content.Intent;
import android.content.pm.ResolveInfo;

/**
 * @author Lody
 *
 *
 * @see android.content.pm.IPackageManager
 *
 *      原型： public List<ResolveInfo> queryIntentActivities(Intent intent, String
 *      resolvedType, int flags, int userId)
 */
@SuppressWarnings("unchecked")
/* package */ class Hook_QueryIntentActivities extends Hook<PackageManagerPatch> {

	/**
	 * 这个构造器必须有,用于依赖注入.
	 *
	 * @param patchObject
	 *            注入对象
	 */
	public Hook_QueryIntentActivities(PackageManagerPatch patchObject) {
		super(patchObject);
	}

	@Override
	public String getName() {
		return "queryIntentActivities";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {

		List<ResolveInfo> result = (List<ResolveInfo>) method.invoke(who, args);

		List<ResolveInfo> pluginResult = LocalPackageManager.getInstance().queryIntentActivities((Intent) args[0],
				(String) args[1], (Integer) args[2]);
		if (pluginResult != null && !pluginResult.isEmpty()) {
			result.addAll(pluginResult);
		}
		return result;
	}
}
