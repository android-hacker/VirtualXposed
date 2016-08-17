package com.lody.virtual.client.hook.patchs.am;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.IBinder;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.local.VActivityManager;
import com.lody.virtual.helper.ExtraConstants;
import com.lody.virtual.helper.proto.VActRedirectResult;
import com.lody.virtual.helper.proto.VRedirectActRequest;
import com.lody.virtual.helper.utils.ComponentUtils;
import com.lody.virtual.os.VUserHandle;

/**
 * @author Lody
 *
 */
/* package */ class ActivityUtils {

	public static boolean replaceIntent(IBinder resultTo, Object[] args, int intentIndex) {
		final Intent targetIntent = (Intent) args[intentIndex];
		ActivityInfo targetActInfo = VirtualCore.getCore().resolveActivityInfo(targetIntent, VUserHandle.myUserId());
		if (targetActInfo != null) {
			String pkgName = targetActInfo.packageName;
			if (!VirtualCore.getCore().isAppInstalled(pkgName)) {
				return true;
			}
			// Create Redirect Request
			VRedirectActRequest req = new VRedirectActRequest(targetActInfo, targetIntent.getFlags());
			req.fromHost = !VirtualCore.getCore().isVAppProcess();
			req.resultTo = resultTo;
			// Get Request Result
			VActRedirectResult result = VActivityManager.getInstance().redirectTargetActivity(req);
			if (result == null || result.stubActInfo == null) {
				return false;
			}
			ActivityInfo selectStubActInfo = result.stubActInfo;
			// Target App's ProcessName
			String plugProcName = ComponentUtils.getProcessName(targetActInfo);
			// Mapping
			Intent stubIntent = new Intent();
			stubIntent.setClassName(selectStubActInfo.packageName, selectStubActInfo.name);
			stubIntent.setFlags(result.flags);
			stubIntent.putExtra(ExtraConstants.EXTRA_TARGET_INTENT, targetIntent);
			stubIntent.putExtra(ExtraConstants.EXTRA_STUB_ACT_INFO, selectStubActInfo);
			stubIntent.putExtra(ExtraConstants.EXTRA_TARGET_ACT_INFO, targetActInfo);
			args[intentIndex] = stubIntent;
			return true;
		}
		return true;
	}

	private static boolean isContainFlag(Intent intent, int flag) {
		return (intent.getFlags() & flag) != 0;
	}

	private static void removeFlag(Intent intent, int flag) {
		intent.setFlags(intent.getFlags() & ~flag);
	}

	private static boolean addFlagIfExist(int flag, Intent intent, Intent redirectIntent) {
		if (isContainFlag(intent, flag)) {
			redirectIntent.addFlags(flag);
			return true;
		}
		return false;
	}

}
