package com.lody.virtual.client.hook.patchs.am;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.IBinder;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.local.LocalActivityManager;
import com.lody.virtual.helper.ExtraConstants;
import com.lody.virtual.helper.proto.VActRedirectResult;
import com.lody.virtual.helper.proto.VRedirectActRequest;
import com.lody.virtual.helper.utils.ArrayIndex;
import com.lody.virtual.helper.utils.ComponentUtils;

import java.lang.reflect.Method;

/**
 * @author Lody
 *
 */
/* package */ class Hook_StartActivity extends Hook {

	@Override
	public String getName() {
		return "startActivity";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		int intentIndex = ArrayIndex.indexOfFirst(args, Intent.class);
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
		ActivityInfo targetActInfo = VirtualCore.getCore().resolveActivityInfo(targetIntent);
		if (targetActInfo != null) {
			String pkgName = targetActInfo.packageName;
			if (!VirtualCore.getCore().isAppInstalled(pkgName)) {
				return method.invoke(who, args);
			}
			// Create Redirect Request
			VRedirectActRequest req = new VRedirectActRequest(targetActInfo, targetIntent.getFlags());
			req.fromHost = !VirtualCore.getCore().isVAppProcess();
			req.resultTo = resultTo;
			// Get Request Result
			VActRedirectResult result = LocalActivityManager.getInstance().redirectTargetActivity(req);
			if (result == null) {
				return 0;
			}
			if (result.intercepted) {
				return 0;
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
		}
		return method.invoke(who, args);
	}

}
