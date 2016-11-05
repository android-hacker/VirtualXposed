package com.lody.virtual.client.hook.patchs.am;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.ComponentInfo;
import android.os.Build;
import android.os.IInterface;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.ipc.VActivityManager;
import com.lody.virtual.client.stub.StubPendingActivity;
import com.lody.virtual.client.stub.StubPendingReceiver;
import com.lody.virtual.client.stub.StubPendingService;
import com.lody.virtual.helper.compat.ActivityManagerCompat;
import com.lody.virtual.os.VUserHandle;

import java.lang.reflect.Method;

/**
 * @author Lody
 */
/* package */ class GetIntentSender extends BaseStartActivity {

	@Override
	public String getName() {
		return "getIntentSender";
	}

	@Override
	public Object call(Object who, Method method, Object... args) throws Throwable {
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
		args[6] = new String[] {null};
		IInterface sender = (IInterface) method.invoke(who, args);
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2 && sender != null && creator != null) {
			VActivityManager.get().addPendingIntent(sender.asBinder(), creator);
		}
		return sender;
	}

	private Intent redirectIntentSender(int type, String creator, Intent intent) {
		Intent newIntent = intent.cloneFilter();
		boolean ok = false;

		switch (type) {
			case ActivityManagerCompat.INTENT_SENDER_ACTIVITY: {

				ComponentInfo info = VirtualCore.get().resolveActivityInfo(intent, VUserHandle.myUserId());
				if (info != null) {
					ok = true;
					newIntent.setClass(getHostContext(), StubPendingActivity.class);
					newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				}

			} break;

			case ActivityManagerCompat.INTENT_SENDER_SERVICE: {
				ComponentInfo info = VirtualCore.get().resolveServiceInfo(intent, VUserHandle.myUserId());
				if (info != null) {
					ok= true;
					newIntent.setClass(getHostContext(), StubPendingService.class);
				}

			} break;

			case ActivityManagerCompat.INTENT_SENDER_BROADCAST: {
				ok = true;
				newIntent.setClass(getHostContext(), StubPendingReceiver.class);
			} break;

		}

		if (!ok) {
			return null;
		}

		newIntent.putExtra("_VA_|_user_id_", VUserHandle.myUserId());
		newIntent.putExtra("_VA_|_intent_", intent);
		newIntent.putExtra("_VA_|_creator_", creator);
		newIntent.putExtra("_VA_|_from_inner_", true);

		return newIntent;
	}

	@Override
	public boolean isEnable() {
		return isAppProcess();
	}

}
