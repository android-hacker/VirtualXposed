package com.lody.virtual.client.hook.patchs.mount;

import com.lody.virtual.client.hook.base.Patch;
import com.lody.virtual.client.hook.base.PatchDelegate;
import com.lody.virtual.client.hook.binders.MountServiceBinderDelegate;

import mirror.android.os.ServiceManager;


/**
 * @author Lody
 */
@Patch({GetVolumeList.class, Mkdirs.class,})
public class MountServicePatch extends PatchDelegate<MountServiceBinderDelegate> {

	@Override
	protected MountServiceBinderDelegate createHookDelegate() {
		return new MountServiceBinderDelegate();
	}

	@Override
	public void inject() throws Throwable {
		getHookDelegate().replaceService("mount");
	}

	@Override
	public boolean isEnvBad() {
		return ServiceManager.getService.call("mount") != getHookDelegate();
	}
}
