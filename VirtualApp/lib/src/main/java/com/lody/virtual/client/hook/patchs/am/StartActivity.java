package com.lody.virtual.client.hook.patchs.am;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.TypedValue;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.local.ActivityClientRecord;
import com.lody.virtual.client.local.VActivityManager;
import com.lody.virtual.helper.compat.ActivityManagerCompat;
import com.lody.virtual.helper.utils.ArrayUtils;
import com.lody.virtual.helper.utils.ComponentUtils;
import com.lody.virtual.helper.utils.VLog;
import com.lody.virtual.os.VUserHandle;

import java.lang.reflect.Method;

import mirror.android.app.ActivityThread;

/**
 * @author Lody
 */
/* package */ class StartActivity extends BaseStartActivity {

	@Override
	public String getName() {
		return "startActivity";
	}


	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		super.onHook(who, method, args);
		int intentIndex = ArrayUtils.indexOfFirst(args, Intent.class);
		int resultToIndex = ArrayUtils.indexOfObject(args, IBinder.class, 2);

		String resolvedType = (String) args[intentIndex + 1];
		Intent intent = (Intent) args[intentIndex];
		IBinder resultTo = resultToIndex >= 0 ? (IBinder) args[resultToIndex] : null;
		int userId = VUserHandle.myUserId();

		if (ComponentUtils.isStubComponent(intent)) {
			return method.invoke(who, args);
		}
		ActivityInfo activityInfo = VirtualCore.get().resolveActivityInfo(intent, userId);
		if (activityInfo == null) {
			return method.invoke(who, args);
		}
		if (resultTo != null) {
			ActivityClientRecord r = VActivityManager.get().getActivityRecord(resultTo);
			if (r != null) {
				TypedValue out = new TypedValue();
				Resources.Theme theme = r.activity.getResources().newTheme();
				theme.applyStyle(activityInfo.getThemeResource(), true);
				if (theme.resolveAttribute(android.R.attr.windowAnimationStyle, out, true)) {

					TypedArray array = theme.obtainStyledAttributes(out.data,
							new int[]{
									android.R.attr.activityOpenEnterAnimation,
									android.R.attr.activityOpenExitAnimation
							});

					r.activity.overridePendingTransition(array.getResourceId(0, 0), array.getResourceId(1, 0));
					array.recycle();
				}
			}
		}

		intent.setDataAndType(intent.getData(), resolvedType);
		String resultWho = null;
		int requestCode = 0;
		Bundle options = ArrayUtils.getFirst(args, Bundle.class);
		if (resultTo != null) {
			resultWho = (String) args[resultToIndex + 1];
			requestCode = (int) args[resultToIndex + 2];
		}

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
			args[intentIndex - 1] = getHostPkg();
		}


		String packageName = activityInfo.packageName;
		if (!isAppPkg(packageName)) {
			return method.invoke(who, args);
		}
		int res = VActivityManager.get().startActivity(intent, activityInfo, resultTo, options, userId);
		if (res != 0 && resultTo != null && requestCode > 0) {
			ActivityThread.sendActivityResult.call(
					VirtualCore.mainThread(),
					resultTo,
					resultWho,
					requestCode,
					0,
					null
			);
		}
		return res;
	}

	@Override
	public Object afterHook(Object who, Method method, Object[] args, Object result) throws Throwable {
		int res = (int) result;
		if (res == ActivityManagerCompat.START_TASK_TO_FRONT) {
			VLog.w(getName(), "Hit <START_TASK_TO_FRONT>.");
		}
		return res;
	}
}
