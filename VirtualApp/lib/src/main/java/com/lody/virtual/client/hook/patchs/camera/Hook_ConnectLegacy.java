package com.lody.virtual.client.hook.patchs.camera;

import android.hardware.ICameraClient;
import android.hardware.camera2.utils.BinderHolder;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.hook.utils.HookUtils;

import java.lang.reflect.Method;

/**
 * @author Lody
 *
 * @see android.hardware.ICameraService#connect(ICameraClient, int, String, int,
 *      BinderHolder)
 *
 */

public class Hook_ConnectLegacy extends Hook {

	@Override
	public String getName() {
		return "connectLegacy";
	}

	@Override
	public boolean beforeHook(Object who, Method method, Object... args) {
		HookUtils.replaceAppPkg(args);
		if (args[args.length - 2] instanceof Integer) {
			args[args.length - 2] = VirtualCore.getCore().myUid();
		}
		return super.beforeHook(who, method, args);
	}
}
