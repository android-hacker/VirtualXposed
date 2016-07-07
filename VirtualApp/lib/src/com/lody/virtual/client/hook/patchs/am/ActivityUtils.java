package com.lody.virtual.client.hook.patchs.am;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.IBinder;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.local.LocalActivityManager;
import com.lody.virtual.client.local.LocalProcessManager;
import com.lody.virtual.helper.ExtraConstants;
import com.lody.virtual.helper.proto.VActRedirectResult;
import com.lody.virtual.helper.proto.VRedirectActRequest;
import com.lody.virtual.helper.utils.ComponentUtils;

/**
 * @author Lody
 *
 */
/* package */ class ActivityUtils {

	public static boolean replaceIntent(IBinder resultTo, Object[] args, int intentIndex) {
		final Intent targetIntent = (Intent) args[intentIndex];
		ActivityInfo targetActInfo = VirtualCore.getCore().resolveActivityInfo(targetIntent);
		if (targetActInfo != null) {
			String pkgName = targetActInfo.packageName;
			if (!VirtualCore.getCore().isAppInstalled(pkgName)) {
				return true;
			}
			// Create Redirect Request
			VRedirectActRequest req = new VRedirectActRequest(targetActInfo, targetIntent.getFlags());
			req.resultTo = resultTo;
			// Get Request Result
			VActRedirectResult result = LocalActivityManager.getInstance().redirectTargetActivity(req);
			if (result == null || result.stubActInfo == null || result.intercepted) {
				return false;
			}
			ActivityInfo selectStubActInfo = result.stubActInfo;
			// Target App's ProcessName
			String plugProcName = ComponentUtils.getProcessName(targetActInfo);
			// Mapping
			LocalProcessManager.mapProcessName(selectStubActInfo.processName, plugProcName);
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
