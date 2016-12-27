package com.lody.virtual.client.hook.patchs.dropbox;

import android.content.Context;

import com.lody.virtual.client.hook.base.PatchBinderDelegate;
import com.lody.virtual.client.hook.base.ResultStaticHook;

import mirror.com.android.internal.os.IDropBoxManagerService;

/**
 * @author Lody
 */
public class DropBoxManagerPatch extends PatchBinderDelegate {
	public DropBoxManagerPatch() {
		super(IDropBoxManagerService.Stub.TYPE, Context.DROPBOX_SERVICE);
	}

	@Override
	protected void onBindHooks() {
		super.onBindHooks();
		addHook(new ResultStaticHook("getNextEntry", null));
	}
}
