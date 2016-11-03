package com.lody.virtual.client.hook.patchs.am;

import android.content.pm.PackageManager;

import com.lody.virtual.client.env.SpecialComponentList;
import com.lody.virtual.client.hook.base.Hook;

import java.lang.reflect.Method;

/**
 * @author Lody
 *
 */
/*package*/ class CheckPermission extends Hook {

	@Override
	public String getName() {
		return "checkPermission";
	}

	@Override
	public Object call(Object who, Method method, Object... args) throws Throwable {
		String permission = (String) args[0];
		if (SpecialComponentList.isWhitePermission(permission)) {
			return PackageManager.PERMISSION_GRANTED;
		}
		if (permission.startsWith("com.google")) {
			return PackageManager.PERMISSION_GRANTED;
		}
		args[args.length - 1] = getRealUid();
		return method.invoke(who, args);
	}

	@Override
	public boolean isEnable() {
		return isAppProcess();
	}

}
