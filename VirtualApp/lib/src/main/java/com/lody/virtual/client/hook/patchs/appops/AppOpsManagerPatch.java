package com.lody.virtual.client.hook.patchs.appops;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;

import com.lody.virtual.client.hook.base.PatchDelegate;
import com.lody.virtual.client.hook.base.ReplaceLastPkgHook;
import com.lody.virtual.client.hook.base.StaticHook;
import com.lody.virtual.client.hook.binders.AppOpsBinderDelegate;

import java.lang.reflect.Method;

import mirror.android.os.ServiceManager;

/**
 * @author Lody
 *
 *  Fuck the AppOpsService.
 *
 * @see android.app.AppOpsManager
 */
@TargetApi(Build.VERSION_CODES.KITKAT)
public class AppOpsManagerPatch extends PatchDelegate<AppOpsBinderDelegate> {

	@Override
	protected AppOpsBinderDelegate createHookDelegate() {
		return new AppOpsBinderDelegate();
	}

	@Override
	public void inject() throws Throwable {
		getHookDelegate().replaceService(Context.APP_OPS_SERVICE);
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
		public BaseHook(String name, int uidIndex, int pkgIndex) {
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

	@Override
	public boolean isEnvBad() {
		return ServiceManager.getService.call(Context.APP_OPS_SERVICE) != getHookDelegate();
	}

}
