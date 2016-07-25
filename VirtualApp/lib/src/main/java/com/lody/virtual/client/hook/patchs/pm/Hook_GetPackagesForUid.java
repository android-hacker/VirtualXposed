package com.lody.virtual.client.hook.patchs.pm;

import android.os.Process;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.local.LocalProcessManager;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
	public boolean beforeHook(Object who, Method method, Object... args) {
		int uid = (int) args[0];
		return uid == Process.myUid();
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		Object invokeResult = method.invoke(who, args);
		List<String> pluginPkgs = new ArrayList<>();
		try {
			List<String> pkgs = LocalProcessManager.getProcessPkgList(Process.myPid());
			pluginPkgs.addAll(pkgs);
		} catch (Exception e) {
			e.printStackTrace();
		}
		List<String> originPkgs = new ArrayList<>();
		if (invokeResult != null && invokeResult instanceof String[]) {
			String[] pkgs = ((String[]) invokeResult);
			Collections.addAll(originPkgs, pkgs);
		}
		String[] res;

		if (originPkgs.size() == 1) {
			res = pluginPkgs.toArray(new String[pluginPkgs.size()]);
		} else {
			originPkgs.remove(getHostPkg());
			pluginPkgs.addAll(originPkgs);
			res = pluginPkgs.toArray(new String[pluginPkgs.size()]);
		}
		return res;
	}

	@Override
	public boolean isEnable() {
		return isAppProcess();
	}
}
