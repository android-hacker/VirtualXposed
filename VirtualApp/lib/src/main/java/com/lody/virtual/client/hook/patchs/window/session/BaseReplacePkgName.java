package com.lody.virtual.client.hook.patchs.window.session;

import android.view.WindowManager;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.helper.utils.ArrayUtils;

import java.lang.reflect.Method;

/**
 * @author Lody
 *
 */
/*package*/ abstract class BaseReplacePkgName extends Hook {

	private int cacheIndex = -1;

	@Override
	public Object call(Object who, Method method, Object... args) throws Throwable {
		if (cacheIndex == -1) {
			cacheIndex = ArrayUtils.indexOfFirst(args, WindowManager.LayoutParams.class);
		}
		if (cacheIndex != -1) {
			WindowManager.LayoutParams attrs = (WindowManager.LayoutParams) args[cacheIndex];
			if (attrs != null) {
				attrs.packageName = getHostPkg();
			}
		}
		return method.invoke(who, args);
	}
}
