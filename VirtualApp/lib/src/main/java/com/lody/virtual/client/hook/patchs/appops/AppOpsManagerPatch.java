package com.lody.virtual.client.hook.patchs.appops;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;

import com.lody.virtual.client.hook.base.PatchBinderDelegate;
import com.lody.virtual.client.hook.base.ReplaceLastPkgHook;
import com.lody.virtual.client.hook.base.StaticHook;

import java.lang.reflect.Method;

import mirror.android.os.ServiceManager;
import mirror.com.android.internal.app.IAppOpsService;

/**
 * @author Lody
 *
 * Fuck the AppOpsService.
 *
 * @see android.app.AppOpsManager
 */
@TargetApi(Build.VERSION_CODES.KITKAT)
public class AppOpsManagerPatch extends PatchBinderDelegate {

	public AppOpsManagerPatch() {
		super(IAppOpsService.Stub.TYPE, Context.APP_OPS_SERVICE);
	}

	@Override
	protected void onBindHooks() {
		super.onBindHooks();
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
		addHook(new BaseHook("noteProxyOperation", 2, 3));
		addHook(new ReplaceLastPkgHook("resetAllModes"));
	}

	private class BaseHook extends StaticHook {
		final int pkgIndex;
		final int uidIndex;

		BaseHook(String name, int uidIndex, int pkgIndex) {
			super(name);
			this.pkgIndex = pkgIndex;
			this.uidIndex = uidIndex;
		}

		@Override
		public boolean beforeCall(Object who, Method method, Object... args) {
			if (pkgIndex != -1 && args.length > pkgIndex && args[pkgIndex] instanceof String) {
				args[pkgIndex] = getHostPkg();
			}
			if (uidIndex != -1 && args[uidIndex] instanceof Integer) {
				args[uidIndex] = getRealUid();
			}
			return true;
		}
	}
}
