package com.lody.virtual.client.hook.patchs.am;

import android.app.ActivityManager;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.local.VActivityManager;
import com.lody.virtual.helper.ExtraConstants;
import com.lody.virtual.helper.utils.ArrayUtils;
import com.lody.virtual.helper.utils.VLog;
import com.lody.virtual.os.VUserHandle;

import java.lang.reflect.Method;

/**
 * @author Lody
 */
/* package */ class StartActivity extends BaseStartActivity {

	@Override
	public String getName() {
		return "startActivity";
	}

	private static int getUserId(Intent intent) {
		int userId = VUserHandle.myUserId();
		if (VirtualCore.getCore().isMainProcess()) {
			intent.setExtrasClassLoader(StartActivity.class.getClassLoader());
			userId = intent.getIntExtra(ExtraConstants.EXTRA_TARGET_USER, userId);
		}
		return userId;
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		super.onHook(who, method, args);
		int intentIndex = ArrayUtils.indexOfFirst(args, Intent.class);
		int resultToIndex = ArrayUtils.indexOfObject(args, IBinder.class, 2);

		Intent targetIntent = (Intent) args[intentIndex];
		IBinder resultTo = resultToIndex != -1 ? (IBinder) args[resultToIndex] : null;
		String resultWho = null;
		int requestCode = 0;
		Bundle options = ArrayUtils.getFirst(args, Bundle.class);
		if (resultTo != null) {
			resultWho = (String) args[resultToIndex + 1];
			requestCode = (int) args[resultToIndex + 2];
		}
		int userId = getUserId(targetIntent);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
			args[intentIndex - 1] = getHostPkg();
		}

		ActivityInfo targetActInfo = VirtualCore.getCore().resolveActivityInfo(targetIntent, userId);
		if (targetActInfo == null) {
			return method.invoke(who, args);
		}
		String packageName = targetActInfo.packageName;
		if (!isAppPkg(packageName)) {
			return method.invoke(who, args);
		}
		Intent resultIntent = VActivityManager.get().startActivity(targetIntent, targetActInfo, resultTo, options, userId);
		if (resultIntent == null) {
			if (resultTo != null) {
				VActivityManager.get().sendActivityResult(resultTo, resultWho, requestCode);
			}
			return 0;
		}

		args[intentIndex] = resultIntent;
		return method.invoke(who, args);
	}

	@Override
	public Object afterHook(Object who, Method method, Object[] args, Object result) throws Throwable {
		int res = (int) result;
		if (res == ActivityManager.START_TASK_TO_FRONT) {
			VLog.w(getName(), "Hit <START_TASK_TO_FRONT>.");
		}
		return res;
	}
}
