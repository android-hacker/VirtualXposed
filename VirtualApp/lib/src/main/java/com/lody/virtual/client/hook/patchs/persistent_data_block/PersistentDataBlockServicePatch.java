package com.lody.virtual.client.hook.patchs.persistent_data_block;

import com.lody.virtual.client.hook.base.PatchDelegate;
import com.lody.virtual.client.hook.base.ResultStaticHook;
import com.lody.virtual.client.hook.binders.PersistentDataBlockServiceBinderDelegate;

import mirror.android.os.ServiceManager;


/**
 * @author Lody
 */

public class PersistentDataBlockServicePatch
		extends
		PatchDelegate<PersistentDataBlockServiceBinderDelegate> {

	@Override
	protected PersistentDataBlockServiceBinderDelegate createHookDelegate() {
		return new PersistentDataBlockServiceBinderDelegate();
	}

	@Override
	public void inject() throws Throwable {
		getHookDelegate().replaceService("persistent_data_block");
	}

	@Override
	protected void onBindHooks() {
		super.onBindHooks();
		addHook(new ResultStaticHook("write", -1));
		addHook(new ResultStaticHook("read", new byte[0]));
		addHook(new ResultStaticHook("wipe", null));
		addHook(new ResultStaticHook("getDataBlockSize", 0));
		addHook(new ResultStaticHook("getMaximumDataBlockSize", 0));
		addHook(new ResultStaticHook("setOemUnlockEnabled", 0));
		addHook(new ResultStaticHook("getOemUnlockEnabled", false));
	}

	@Override
	public boolean isEnvBad() {
		return getHookDelegate() != ServiceManager.getService.call("persistent_data_block");
	}
}
