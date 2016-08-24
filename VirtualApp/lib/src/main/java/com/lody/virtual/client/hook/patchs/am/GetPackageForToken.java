package com.lody.virtual.client.hook.patchs.am;

import java.lang.reflect.Method;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.local.VActivityManager;

import android.os.IBinder;

/**
 * @author Lody
 *
 *         String getPackageForToken(IBinder token);
 *
 */

public class GetPackageForToken extends Hook {

	@Override
	public String getName() {
		return "getPackageForToken";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		IBinder token = (IBinder) args[0];
		String pkg = VActivityManager.get().getPackageForToken(token);
		if (pkg != null) {
			return pkg;
		}
		return super.onHook(who, method, args);
	}
}
