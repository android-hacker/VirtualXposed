package com.lody.virtual.client.hook.patchs.persistent_data_block;

import com.lody.virtual.client.hook.base.PatchBinderDelegate;
import com.lody.virtual.client.hook.base.ResultStaticHook;

import mirror.android.service.persistentdata.IPersistentDataBlockService;

/**
 * @author Lody
 */
public class PersistentDataBlockServicePatch extends PatchBinderDelegate {

	public PersistentDataBlockServicePatch() {
		super(IPersistentDataBlockService.Stub.TYPE, "persistent_data_block");
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
}
