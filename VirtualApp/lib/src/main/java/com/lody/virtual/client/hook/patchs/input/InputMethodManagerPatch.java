package com.lody.virtual.client.hook.patchs.input;

import android.content.Context;

import com.lody.virtual.client.hook.base.Patch;
import com.lody.virtual.client.hook.base.PatchDelegate;
import com.lody.virtual.client.hook.binders.IMMBinderDelegate;

import mirror.com.android.internal.view.inputmethod.InputMethodManager;

/**
 * @author Lody
 *
 *
 */
@Patch({StartInput.class, WindowGainedFocus.class})
public class InputMethodManagerPatch extends PatchDelegate<IMMBinderDelegate> {

	@Override
	protected IMMBinderDelegate createHookDelegate() {
		return new IMMBinderDelegate();
	}

	@Override
	public void inject() throws Throwable {
		Object inputMethodManager = getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		InputMethodManager
				.mService.set(inputMethodManager, getHookDelegate().getProxyInterface());
		getHookDelegate().replaceService(Context.INPUT_METHOD_SERVICE);
	}


	@Override
	public boolean isEnvBad() {
		Object inputMethodManager = getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		return InputMethodManager
				.mService.get(inputMethodManager) != getHookDelegate().getBaseInterface();
	}

}