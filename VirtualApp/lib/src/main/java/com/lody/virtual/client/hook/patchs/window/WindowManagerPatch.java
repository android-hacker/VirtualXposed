package com.lody.virtual.client.hook.patchs.window;

import android.content.Context;
import android.os.Build;

import com.lody.virtual.client.hook.base.Patch;
import com.lody.virtual.client.hook.base.PatchBinderDelegate;
import com.lody.virtual.client.hook.base.StaticHook;

import mirror.android.view.Display;
import mirror.android.view.IWindowManager;
import mirror.android.view.WindowManagerGlobal;
import mirror.com.android.internal.policy.PhoneWindow;

/**
 * @author Lody
 */
@Patch({OverridePendingAppTransition.class, OverridePendingAppTransitionInPlace.class, OpenSession.class,
		SetAppStartingWindow.class})
public class WindowManagerPatch extends PatchBinderDelegate {

	public WindowManagerPatch() {
		super(IWindowManager.Stub.TYPE, Context.WINDOW_SERVICE);
	}

	@Override
	public void inject() throws Throwable {
		super.inject();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
			if (WindowManagerGlobal.sWindowManagerService != null) {
				WindowManagerGlobal.sWindowManagerService.set(getHookDelegate().getProxyInterface());
			}
		} else {
			if (Display.sWindowManager != null) {
				Display.sWindowManager.set(getHookDelegate().getProxyInterface());
			}
		}
		if (PhoneWindow.TYPE != null) {
			PhoneWindow.sWindowManager.set(getHookDelegate().getProxyInterface());
		}
	}

	@Override
	protected void onBindHooks() {
		super.onBindHooks();
		addHook(new StaticHook("addAppToken"));
		addHook(new StaticHook("setScreenCaptureDisabled"));
	}
}
