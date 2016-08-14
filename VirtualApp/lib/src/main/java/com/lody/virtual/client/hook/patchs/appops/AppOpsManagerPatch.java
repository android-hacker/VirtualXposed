package com.lody.virtual.client.hook.patchs.appops;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.ServiceManager;

import com.android.internal.app.IAppOpsService;
import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.hook.base.PatchObject;
import com.lody.virtual.client.hook.base.ReplaceCallingPkgHook;
import com.lody.virtual.client.hook.base.ReplaceLastPkgHook;
import com.lody.virtual.client.hook.base.StaticHook;
import com.lody.virtual.client.hook.binders.HookAppOpsBinder;

import java.lang.reflect.Method;

/**
 * @author Lody
 *
 *
 *
 *         Fuck the AppOpsService.
 *
 * @see android.app.AppOpsManager
 * @see IAppOpsService
 */
@TargetApi(Build.VERSION_CODES.KITKAT)
public class AppOpsManagerPatch extends PatchObject<IAppOpsService, HookAppOpsBinder> {

	@Override
	protected HookAppOpsBinder initHookObject() {
		return new HookAppOpsBinder();
	}

	@Override
	public void inject() throws Throwable {
		getHookObject().injectService(Context.APP_OPS_SERVICE);
	}

	@Override
	protected void applyHooks() {
		super.applyHooks();
		addHook(new BaseHook("checkOperation", 1, 2));
		addHook(new BaseHook("noteOperation", 1, 2));
		addHook(new BaseHook("startOperation", 2, 3));
		addHook(new BaseHook("finishOperation", 2, 3));
		addHook(new BaseHook("startWatchingMode", -1, 1));
		addHook(new BaseHook("checkPackage", 0, 1));
		addHook(new BaseHook("getOpsForPackage", 0, 1));
		addHook(new BaseHook("setMode", 1, 2));
		addHook(new BaseHook("checkAudioOperation", 2, 3));
		addHook(new BaseHook("setAudioRestriction", 2, -1));
		addHook(new ReplaceCallingPkgHook("noteProxyOperation"));
		addHook(new ReplaceLastPkgHook("resetAllModes"));
	}

	private class BaseHook extends StaticHook {
		final int pkgIndex;
		final int uidIndex;
		public BaseHook(String name, int uidIndex, int pkgIndex) {
			super(name);
			this.pkgIndex = pkgIndex;
			this.uidIndex = uidIndex;
		}

		@Override
		public boolean beforeHook(Object who, Method method, Object... args) {
			if (pkgIndex != -1 && args.length > pkgIndex && args[pkgIndex] instanceof String) {
				String pkg = (String) args[pkgIndex];
				if (isAppPkg(pkg)) {
					args[pkgIndex] = getHostPkg();
				}
			}
			if (uidIndex != -1 && args.length > uidIndex && args[uidIndex] instanceof Integer) {
				args[uidIndex] = VirtualCore.getCore().myUid();
			}
			return true;
		}
	}

	@Override
	public boolean isEnvBad() {
		return ServiceManager.getService(Context.APP_OPS_SERVICE) != getHookObject();
	}

}
