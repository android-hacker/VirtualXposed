package com.lody.virtual.client.hook.patchs.wifi;

import java.lang.reflect.Method;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.hook.utils.HookUtils;

/**
 * @author Lody
 *
 *
 * @see android.net.wifi.IWifiManager#setWifiEnabled(boolean)
 */
/* package */ class SetWifiEnabled extends Hook {

	@Override
	public String getName() {
		return "setWifiEnabled";
	}

	@Override
	public Object call(Object who, Method method, Object... args) throws Throwable {
		// 部分设备有callingPkg参数，部分设备没有
		HookUtils.replaceFirstAppPkg(args);
		return method.invoke(who, args);
	}
}
