package com.lody.virtual.client.hook.binders;

import com.android.internal.view.IInputMethodManager;
import com.lody.virtual.client.hook.base.HookBinder;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.IBinder;
import android.os.ServiceManager;

/**
 * @author Lody
 *
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class HookIMMBinder extends HookBinder<IInputMethodManager> {

	@Override
	protected IBinder queryBaseBinder() {
		return ServiceManager.getService(Context.INPUT_METHOD_SERVICE);
	}

	@Override
	protected IInputMethodManager createInterface(IBinder baseBinder) {
		return IInputMethodManager.Stub.asInterface(baseBinder);
	}
}
