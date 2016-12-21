package com.lody.virtual.client.hook.patchs.mount;

import com.lody.virtual.client.hook.base.Patch;
import com.lody.virtual.client.hook.base.PatchBinderDelegate;

import mirror.android.os.mount.IMountService;

/**
 * @author Lody
 */
@Patch({GetVolumeList.class, Mkdirs.class,})
public class MountServicePatch extends PatchBinderDelegate {

	public MountServicePatch() {
		super(IMountService.Stub.TYPE, "mount");
	}
}
