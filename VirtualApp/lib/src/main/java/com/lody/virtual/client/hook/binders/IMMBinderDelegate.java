package com.lody.virtual.client.hook.binders;


import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.IInterface;

import com.lody.virtual.client.hook.base.HookBinderDelegate;

import mirror.com.android.internal.view.inputmethod.InputMethodManager;

/**
 * @author Lody
 *
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class IMMBinderDelegate extends HookBinderDelegate {

	@Override
	protected IInterface createInterface() {
		Object inputMethodManager = getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		return InputMethodManager.mService.get(inputMethodManager);
	}
}
