package com.lody.virtual.client.hook.patchs.am;

import android.app.ApplicationThreadNative;
import android.app.IApplicationThread;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.IBinder;

import com.android.internal.content.ReferrerIntent;
import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.local.VActivityManager;
import com.lody.virtual.helper.ExtraConstants;
import com.lody.virtual.helper.compat.IApplicationThreadCompat;
import com.lody.virtual.helper.proto.VActRedirectResult;
import com.lody.virtual.helper.proto.VRedirectActRequest;
import com.lody.virtual.helper.utils.ArrayUtils;
import com.lody.virtual.os.VUserHandle;

import java.lang.reflect.Method;
import java.util.Collections;

/**
 * @author Lody
 */
/* package */ class Hook_StartActivity extends Hook_BaseStartActivity {

	@Override
	public String getName() {
		return "startActivity";
	}

	@Override
	public boolean beforeHook(Object who, Method method, Object... args) {
		return super.beforeHook(who, method, args);
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		super.onHook(who, method, args);
		int intentIndex = ArrayUtils.indexOfFirst(args, Intent.class);
		int resultToIndex;
		if (Build.VERSION.SDK_INT <= 15) {
			resultToIndex = 5;
		} else if (Build.VERSION.SDK_INT <= 17) {
			resultToIndex = 3;
		} else if (Build.VERSION.SDK_INT <= 19) {
			resultToIndex = 4;
		} else {
			resultToIndex = 4;
		}
		IBinder resultTo = (IBinder) args[resultToIndex];
		final Intent targetIntent = (Intent) args[intentIndex];

		ActivityInfo targetActInfo = VirtualCore.getCore().resolveActivityInfo(targetIntent, VUserHandle.myUserId());
		if (targetActInfo == null) {
			return method.invoke(who, args);
		}
		String packageName = targetActInfo.packageName;
		if (!VirtualCore.getCore().isAppInstalled(packageName)) {
			return method.invoke(who, args);
		}
		// Create Redirect Request
		VRedirectActRequest req = new VRedirectActRequest(targetActInfo, targetIntent.getFlags());
		req.fromHost = !VirtualCore.getCore().isVAppProcess();
		if (req.fromHost) {
			int userId = targetIntent.getIntExtra(ExtraConstants.EXTRA_TARGET_USER, -9);
			if (userId != -9) {
				int appId = VUserHandle.getAppId(targetActInfo.applicationInfo.uid);
				targetActInfo.applicationInfo.uid = VUserHandle.getUid(userId, appId);
			}
		}
		req.resultTo = resultTo;
		// Get Request Result
		VActRedirectResult result = VActivityManager.get().redirectTargetActivity(req);
		if (result == null || result.justReturn) {
			return 0;
		}
		if (result.newIntentToken != null) {
			IApplicationThread appThread = ApplicationThreadNative.asInterface(result.targetClient);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
				ReferrerIntent referrerIntent = new ReferrerIntent(targetIntent, packageName);
				IApplicationThreadCompat.scheduleNewIntent(appThread, Collections.singletonList(referrerIntent),
						result.newIntentToken);
			} else {
				IApplicationThreadCompat.scheduleNewIntent(appThread, Collections.singletonList(targetIntent),
						result.newIntentToken);
			}
			return 0;
		}
		// Workaround: issue #33 START
		if (result.replaceToken != null) {
			args[resultToIndex] = result.replaceToken;
		}
		// Workaround: issue #33 END
		ActivityInfo selectStubActInfo = result.stubActInfo;
		if (selectStubActInfo == null) {
			return method.invoke(who, args);
		}
		ActivityInfo callerActInfo = null;
		if (resultTo != null) {
			callerActInfo = VActivityManager.get().getActivityInfo(resultTo);
		}
		// Mapping
		Intent stubIntent = new Intent();
		stubIntent.setClassName(selectStubActInfo.packageName, selectStubActInfo.name);
		stubIntent.setFlags(result.flags);
		stubIntent.putExtra(ExtraConstants.EXTRA_TARGET_INTENT, targetIntent);
		stubIntent.putExtra(ExtraConstants.EXTRA_STUB_ACT_INFO, selectStubActInfo);
		stubIntent.putExtra(ExtraConstants.EXTRA_TARGET_ACT_INFO, targetActInfo);
		if (callerActInfo != null) {
			stubIntent.putExtra(ExtraConstants.EXTRA_CALLER, callerActInfo);
		}
		args[intentIndex] = stubIntent;
		return method.invoke(who, args);
	}
}
