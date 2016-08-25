package com.lody.virtual.client.hook.patchs.imms;

import com.lody.virtual.client.hook.base.PatchDelegate;
import com.lody.virtual.client.hook.binders.IMMSBinderDelegate;

import mirror.android.os.ServiceManager;


/**
 * @author Lody
 *
 */
public class MmsPatch extends PatchDelegate<IMMSBinderDelegate> {
	@Override
	protected IMMSBinderDelegate createHookDelegate() {
		return new IMMSBinderDelegate();
	}

	@Override
	public void inject() throws Throwable {
		getHookDelegate().replaceService("imms");
	}

	@Override
	public boolean isEnvBad() {
		return getHookDelegate() != ServiceManager.getService.call("imms");
	}
}
