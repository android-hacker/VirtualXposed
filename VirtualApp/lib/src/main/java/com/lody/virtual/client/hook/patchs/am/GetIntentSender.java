package com.lody.virtual.client.hook.patchs.am;

import android.app.ActivityManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;

import com.lody.virtual.client.VClientImpl;
import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.local.VActivityManager;
import com.lody.virtual.client.stub.KeepService;
import com.lody.virtual.helper.ExtraConstants;
import com.lody.virtual.os.VUserHandle;

import java.lang.reflect.Method;

/**
 * @author Lody
 * @see android.app.ActivityManagerNative#getIntentSender(int, String, IBinder,
 *      String, int, Intent[], String[], int, Bundle, int)
 */
/* package */ class GetIntentSender extends BaseStartActivity {

	@Override
	public String getName() {
		return "getIntentSender";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		String creator = (String) args[1];
		args[1] = getHostPkg();
		String[] resolvedTypes = (String[]) args[6];
		int type = (int) args[0];
		if (args[5] instanceof Intent[]) {
			Intent[] intents = (Intent[]) args[5];
			if (intents.length > 0) {
				Intent intent = intents[intents.length - 1];
				if (resolvedTypes != null && resolvedTypes.length > 0) {
					intent.setDataAndType(intent.getData(), resolvedTypes[resolvedTypes.length - 1]);
				}
				Intent proxyIntent = redirectIntentSender(type, creator, intent);
				if (proxyIntent != null) {
					intents[intents.length - 1] = proxyIntent;
				}
			}
		}
		if (args.length > 7 && args[7] instanceof Integer) {
            args[7] = PendingIntent.FLAG_UPDATE_CURRENT;
        }
		args[0] = ActivityManager.INTENT_SENDER_SERVICE;
		args[6] = new String[] {null};
		IInterface sender = (IInterface) method.invoke(who, args);
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2 && sender != null && creator != null) {
			VActivityManager.get().addPendingIntent(sender.asBinder(), creator);
		}
		return sender;
	}

	private Intent redirectIntentSender(int type, String creator, Intent intent) {
		if (type == ActivityManager.INTENT_SENDER_ACTIVITY || type == ActivityManager.INTENT_SENDER_SERVICE) {
			ActivityInfo activityInfo = VirtualCore.getCore().resolveActivityInfo(intent, VUserHandle.myUserId());
			if (activityInfo == null || !isAppPkg(activityInfo.packageName)) {
				return null;
			}
			Intent newIntent = intent.cloneFilter();
			newIntent.setClass(getHostContext(), KeepService.class);
			newIntent.putExtra("_VA_|_vuid_", VClientImpl.getClient().getVUid());
			newIntent.putExtra("_VA_|_type_", type);
			newIntent.putExtra("_VA_|_intent_", intent);
			newIntent.putExtra("_VA_|_what_", ExtraConstants.WHAT_PENDING_INTENT);
			newIntent.putExtra("_VA_|_creator_", creator);
			return newIntent;
		}
		return null;
	}

	@Override
	public boolean isEnable() {
		return isAppProcess();
	}

}
