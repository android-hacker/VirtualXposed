package com.lody.virtual.client.hook.patchs.wifi;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.hook.utils.HookUtils;

import java.lang.reflect.Method;

/**
 * @author Lody
 *
 *
 * @see android.net.wifi.IWifiManager#setWifiEnabled(boolean)
 */
/* package */ class Hook_SetWifiEnabled extends Hook {

	@Override
	public String getName() {
		return "setWifiEnabled";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		// 部分设备有callingPkg参数，部分设备没有
		HookUtils.replaceFirstAppPkg(args);
		return method.invoke(who, args);
	}
}
