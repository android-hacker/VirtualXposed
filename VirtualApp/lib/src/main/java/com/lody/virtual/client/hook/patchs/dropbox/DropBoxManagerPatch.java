package com.lody.virtual.client.hook.patchs.dropbox;

import android.content.Context;

import com.lody.virtual.client.hook.base.PatchDelegate;
import com.lody.virtual.client.hook.base.ResultStaticHook;
import com.lody.virtual.client.hook.binders.DropBoxBinderDelegate;

import mirror.android.os.ServiceManager;

/**
 * @author Lody
 */
public class DropBoxManagerPatch extends PatchDelegate<DropBoxBinderDelegate> {

	@Override
	protected DropBoxBinderDelegate createHookDelegate() {
		return new DropBoxBinderDelegate();
	}

	@Override
	public void inject() throws Throwable {
		getHookDelegate().replaceService(Context.DROPBOX_SERVICE);
	}

	@Override
	protected void onBindHooks() {
		super.onBindHooks();
		addHook(new ResultStaticHook("getNextEntry", null));
	}

	@Override
	public boolean isEnvBad() {
		return getHookDelegate() != ServiceManager.getService.call(Context.DROPBOX_SERVICE);
	}
}
