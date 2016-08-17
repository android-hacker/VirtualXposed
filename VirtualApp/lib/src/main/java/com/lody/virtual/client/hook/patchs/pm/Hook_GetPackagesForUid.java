package com.lody.virtual.client.hook.patchs.pm;

import com.lody.virtual.client.VClientImpl;
import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.local.VPackageManager;
import com.lody.virtual.helper.utils.VLog;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * @author Lody
 *
 *
 * @see android.content.pm.IPackageManager#getPackagesForUid(int)
 */
/* package */ class Hook_GetPackagesForUid extends Hook {


	@Override
	public String getName() {
		return "getPackagesForUid";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		int uid = (int) args[0];
		if (uid == VirtualCore.getCore().myUid()) {
			if (isServiceProcess()) {
				return method.invoke(who, args);
			} else {
				// From native?
				uid = VClientImpl.getClient().getVUid();
			}
		}
		String[] res = VPackageManager.getInstance().getPackagesForUid(uid);
		VLog.d(getName(), "getPackagesForUid : %d return %s", uid, Arrays.toString(res));
		if (res == null) {
			return method.invoke(who, args);
		}
		return res;
	}

	@Override
	public boolean isEnable() {
		return isAppProcess() || isServiceProcess();
	}
}
