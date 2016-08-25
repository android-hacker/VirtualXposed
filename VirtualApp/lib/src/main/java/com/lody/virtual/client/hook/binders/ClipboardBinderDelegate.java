package com.lody.virtual.client.hook.binders;


import android.content.Context;
import android.os.IInterface;

import com.lody.virtual.client.hook.base.HookBinderDelegate;

import mirror.android.content.ClipboardManager;

/**
 * @author Lody
 *
 */
public class ClipboardBinderDelegate extends HookBinderDelegate {

	@Override
	protected IInterface createInterface() {
		getContext().getSystemService(Context.CLIPBOARD_SERVICE);
		return ClipboardManager.sService.get();
	}
}
