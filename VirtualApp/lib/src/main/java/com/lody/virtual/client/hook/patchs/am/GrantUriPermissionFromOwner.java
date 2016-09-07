package com.lody.virtual.client.hook.patchs.am;

import java.lang.reflect.Method;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.hook.utils.HookUtils;

/**
 * @author Lody
 *
 *
 *         原型： public void grantUriPermissionFromOwner(IBinder owner, int
 *         fromUid, String targetPkg, Uri uri, int mode);
 */
/* package */ class GrantUriPermissionFromOwner extends Hook {

	@Override
	public String getName() {
		return "grantUriPermissionFromOwner";
	}

	@Override
	public Object call(Object who, Method method, Object... args) throws Throwable {
		HookUtils.replaceFirstAppPkg(args);
		return method.invoke(who, args);
	}

	@Override
	public boolean isEnable() {
		return isAppProcess();
	}
}
