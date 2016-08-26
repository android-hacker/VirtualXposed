package com.lody.virtual.client.hook.patchs.window;

import android.content.Context;
import android.os.Build;

import com.lody.virtual.client.hook.base.Patch;
import com.lody.virtual.client.hook.base.PatchDelegate;
import com.lody.virtual.client.hook.base.StaticHook;
import com.lody.virtual.client.hook.binders.WindowBinderDelegate;

import mirror.android.os.ServiceManager;
import mirror.android.view.Display;
import mirror.android.view.WindowManagerGlobal;
import mirror.com.android.internal.policy.PhoneWindow;

/**
 * @author Lody
 */
@Patch({OverridePendingAppTransition.class, OverridePendingAppTransitionInPlace.class, OpenSession.class,
		SetAppStartingWindow.class})
public class WindowManagerPatch extends PatchDelegate<WindowBinderDelegate> {

	@Override
	protected WindowBinderDelegate createHookDelegate() {
		return new WindowBinderDelegate();
	}

	@Override
	public void inject() throws Throwable {
		getHookDelegate().replaceService(Context.WINDOW_SERVICE);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
			if (WindowManagerGlobal.sWindowManagerService != null) {
				WindowManagerGlobal.sWindowManagerService.set(getHookDelegate().getProxyInterface());
			}
		} else {
			if (Display.sWindowManager != null) {
				Display.sWindowManager.set(getHookDelegate().getProxyInterface());
			}
		}
		if (PhoneWindow.Class != null) {
			PhoneWindow.sWindowManager.set(getHookDelegate().getProxyInterface());
		}
	}

	@Override
	protected void onBindHooks() {
		super.onBindHooks();
		addHook(new StaticHook("addAppToken"));
		addHook(new StaticHook("setScreenCaptureDisabled"));
	}

	@Override
	public boolean isEnvBad() {
		return ServiceManager.getService.call(Context.WINDOW_SERVICE) != getHookDelegate();
	}
}
