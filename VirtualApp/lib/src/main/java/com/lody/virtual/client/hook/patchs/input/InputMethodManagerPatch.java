package com.lody.virtual.client.hook.patchs.input;

import android.content.Context;
import android.view.inputmethod.InputMethodManager;

import com.android.internal.view.IInputMethodManager;
import com.lody.virtual.client.hook.base.Patch;
import com.lody.virtual.client.hook.base.PatchObject;
import com.lody.virtual.client.hook.binders.HookIMMBinder;

import java.lang.reflect.Field;

/**
 * @author Lody
 *
 *
 * @see InputMethodManager
 * @see IInputMethodManager
 *
 */
@Patch({StartInput.class, WindowGainedFocus.class})
public class InputMethodManagerPatch extends PatchObject<IInputMethodManager, HookIMMBinder> {

	@Override
	protected HookIMMBinder initHookObject() {
		return new HookIMMBinder();
	}

	@Override
	public void inject() throws Throwable {
		InputMethodManager inputMethodManager = getInstance();
		if (inputMethodManager != null) {
			Field f_mService = InputMethodManager.class.getDeclaredField("mService");
			f_mService.setAccessible(true);
			f_mService.set(inputMethodManager, getHookObject().getProxyObject());
		}
		getHookObject().injectService(Context.INPUT_METHOD_SERVICE);
	}

	private InputMethodManager getInstance() {
		try {
			return InputMethodManager.getInstance();
		} catch (NoSuchMethodError e) {
			// Ignore
		}
		try {
			Field f_sInstance = InputMethodManager.class.getDeclaredField("sInstance");
			f_sInstance.setAccessible(true);
			return (InputMethodManager) f_sInstance.get(null);
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	protected void applyHooks() {
		super.applyHooks();
	}

	@Override
	public boolean isEnvBad() {
		try {
			Field f_mService = InputMethodManager.class.getDeclaredField("mService");
			f_mService.setAccessible(true);
			InputMethodManager inputMethodManager = InputMethodManager.getInstance();
			IInputMethodManager service = (IInputMethodManager) f_mService.get(inputMethodManager);
			return service != getHookObject().getProxyObject();
		} catch (Throwable e) {
			// Ignore
		}
		return false;
	}

}
