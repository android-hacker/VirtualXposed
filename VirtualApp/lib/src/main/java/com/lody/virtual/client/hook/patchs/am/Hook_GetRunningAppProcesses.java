package com.lody.virtual.client.hook.patchs.am;

import android.app.ActivityManager;
import android.os.Process;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.local.LocalProcessManager;

import java.lang.reflect.Method;
import java.util.List;

/**
 * @author Lody
 *
 */
@SuppressWarnings("unchecked")
/* package */ class Hook_GetRunningAppProcesses extends Hook {

	@Override
	public String getName() {
		return "getRunningAppProcesses";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		List<ActivityManager.RunningAppProcessInfo> infoList = (List<ActivityManager.RunningAppProcessInfo>) method
				.invoke(who, args);
		if (infoList != null) {
			int myUid = Process.myUid();
			for (ActivityManager.RunningAppProcessInfo info : infoList) {
				if (info.uid == myUid && LocalProcessManager.isAppPid(info.pid)) {
					List<String> pkgList = LocalProcessManager.getProcessPkgList(info.pid);
					String processName = LocalProcessManager.getAppProcessName(info.pid);
					if (processName != null) {
						info.processName = processName;
					}
					info.pkgList = pkgList.toArray(new String[pkgList.size()]);
				}
			}
		}
		return infoList;
	}
}
