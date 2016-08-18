package com.lody.virtual.client.hook.patchs.am;

import java.lang.reflect.Method;
import java.util.List;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.local.VActivityManager;
import com.lody.virtual.helper.proto.AppTaskInfo;

import android.app.ActivityManager;

/**
 * @author Lody
 *
 * @see android.app.IActivityManager#getTasks(int, int)
 *
 */
@SuppressWarnings("unchecked")
/* package */ class Hook_GetTasks extends Hook {

	@Override
	public String getName() {
		return "getTasks";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		List<ActivityManager.RunningTaskInfo> runningTaskInfos = (List<ActivityManager.RunningTaskInfo>) method
				.invoke(who, args);
		for (int i = 0; i < runningTaskInfos.size(); i++) {
			ActivityManager.RunningTaskInfo info = runningTaskInfos.get(i);
			AppTaskInfo taskInfo = VActivityManager.get().getTaskInfo(info.id);
			if (taskInfo != null) {
				info.topActivity = taskInfo.topActivity;
				info.baseActivity = taskInfo.baseActivity;
			}
		}
		return runningTaskInfos;
	}

	@Override
	public boolean isEnable() {
		return isAppProcess();
	}
}
