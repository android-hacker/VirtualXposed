package com.lody.virtual.client.hook.patchs.camera;

import com.lody.virtual.client.hook.base.Patch;
import com.lody.virtual.client.hook.base.PatchObject;
import com.lody.virtual.client.hook.binders.HookCameraBinder;

import android.annotation.TargetApi;
import android.content.Context;
import android.hardware.ICameraService;
import android.os.Build;
import android.os.ServiceManager;

/**
 * @author Lody
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
@Patch({Hook_Connect.class, Hook_ConnectDevice.class, Hook_ConnectLegacy.class, Hook_ConnectPro.class,})
public class CameraPatch extends PatchObject<ICameraService, HookCameraBinder> {

	@Override
	protected HookCameraBinder initHookObject() {
		return new HookCameraBinder();
	}

	@Override
	public void inject() throws Throwable {
		getHookObject().injectService(Context.CAMERA_SERVICE);
	}

	@Override
	public boolean isEnvBad() {
		return getHookObject() != ServiceManager.getService(Context.CAMERA_SERVICE);
	}
}
