package com.lody.virtual.client.hook.patchs.window.session;

import java.lang.reflect.Method;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.helper.utils.ArrayIndex;

import android.view.WindowManager;

/**
 * @author Lody
 *
 */
/* package */ abstract class BaseHook_ReplacePkgName extends Hook<WindowSessionPatch> {

	private int cacheIndex = -1;
	/**
	 * 这个构造器必须有,用于依赖注入.
	 *
	 * @param patchObject
	 *            注入对象
	 */
	public BaseHook_ReplacePkgName(WindowSessionPatch patchObject) {
		super(patchObject);
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		if (cacheIndex == -1) {
			cacheIndex = ArrayIndex.indexOfFirst(args, WindowManager.LayoutParams.class);
		}
		if (cacheIndex != -1) {
			WindowManager.LayoutParams attrs = (WindowManager.LayoutParams) args[cacheIndex];
			if (attrs != null) {
				String pkgName = attrs.packageName;
				if (isAppPkg(pkgName)) {
					attrs.packageName = getHostPkg();
				}
			}
		}
		return method.invoke(who, args);
	}
}
