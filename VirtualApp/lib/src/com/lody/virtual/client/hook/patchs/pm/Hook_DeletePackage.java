package com.lody.virtual.client.hook.patchs.pm;

import java.lang.reflect.Method;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.hook.base.Hook;

import android.content.pm.IPackageDeleteObserver2;

/**
 * @author Lody
 *
 *
 * @see android.content.pm.IPackageManager#deletePackage(String,
 *      IPackageDeleteObserver2, int, int)
 */
/* package */ class Hook_DeletePackage extends Hook<PackageManagerPatch> {
	/**
	 * 这个构造器必须有,用于依赖注入.
	 *
	 * @param patchObject
	 *            注入对象
	 */
	public Hook_DeletePackage(PackageManagerPatch patchObject) {
		super(patchObject);
	}

	@Override
	public String getName() {
		return "deletePackage";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		String pkgName = (String) args[0];
		if (isAppPkg(pkgName)) {
			try {
				VirtualCore.getCore().uninstallApp(pkgName);
				IPackageDeleteObserver2 observer = (IPackageDeleteObserver2) args[1];
				if (observer != null) {
					observer.onPackageDeleted(pkgName, 0, "Delete success.");
				}
			} catch (Throwable e) {
				// Ignore
			}
			return 0;
		}
		return method.invoke(who, args);
	}

}
