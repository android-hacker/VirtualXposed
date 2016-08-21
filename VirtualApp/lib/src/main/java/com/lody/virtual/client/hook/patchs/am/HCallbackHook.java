package com.lody.virtual.client.hook.patchs.am;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Handler;
import android.os.Message;

import com.lody.virtual.client.VClientImpl;
import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.interfaces.Injectable;
import com.lody.virtual.client.local.VActivityManager;
import com.lody.virtual.helper.proto.AppSetting;
import com.lody.virtual.helper.utils.ComponentUtils;
import com.lody.virtual.helper.utils.VLog;

import mirror.android.app.ActivityThread;

/**
 * @author Lody
 * @see Handler.Callback
 */
public class HCallbackHook implements Handler.Callback, Injectable {


	public static final int LAUNCH_ACTIVITY = ActivityThread.H.LAUNCH_ACTIVITY.get();

	private static final String TAG = HCallbackHook.class.getSimpleName();
	private static final HCallbackHook sCallback = new HCallbackHook();


	private boolean mCalling = false;
	private Handler.Callback otherCallback;

	private HCallbackHook() {
	}

	public static HCallbackHook getDefault() {
		return sCallback;
	}

	private static Handler getH() {
		return ActivityThread.mH.get(VirtualCore.mainThread());
	}

	private static Handler.Callback getHCallback() {
		try {
			Handler handler = getH();
			return mirror.android.os.Handler.mCallback.get(handler);
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public boolean handleMessage(Message msg) {
		if (!mCalling) {
			mCalling = true;
			try {
				if (LAUNCH_ACTIVITY == msg.what) {
					if (!handleLaunchActivity(msg)) {
						return true;
					}
				}
				if (otherCallback != null) {
					return otherCallback.handleMessage(msg);
				}
			} finally {
				mCalling = false;
			}
		}
		return false;
	}

	private boolean handleLaunchActivity(Message msg) {
		Object r = msg.obj;
		// StubIntent
		Intent stubIntent = ActivityThread.ActivityClientRecord.intent.get(r);
		// TargetIntent
		Intent targetIntent = stubIntent.getParcelableExtra("_VA_|_intent_");

		ComponentName component = targetIntent.getComponent();
		String packageName = component.getPackageName();

		AppSetting appSetting = VirtualCore.getCore().findApp(packageName);
		if (appSetting == null) {
			return true;
		}
		ActivityInfo stubActInfo = stubIntent.getParcelableExtra("_VA_|_stub_activity_");
		ActivityInfo targetActInfo = stubIntent.getParcelableExtra("_VA_|_target_activity_");

		if (stubActInfo == null || targetActInfo == null) {
			return true;
		}
		String processName = ComponentUtils.getProcessName(targetActInfo);
		if (!VClientImpl.getClient().isBound()) {
			int targetUser = stubIntent.getIntExtra("_VA_|_user_id_", 0);
			VActivityManager.get().ensureAppBound(processName, appSetting.packageName, targetUser);
			getH().sendMessageDelayed(Message.obtain(msg), 5);
			return false;
		}
		ClassLoader appClassLoader = VClientImpl.getClient().getClassLoader(targetActInfo.applicationInfo);
		targetIntent.setExtrasClassLoader(appClassLoader);
		boolean error = false;
		try {
			targetIntent.putExtra("_VA_|_stub_activity_", stubActInfo);
			targetIntent.putExtra("_VA_|_target_activity_", targetActInfo);
		} catch (Throwable e) {
			error = true;
			VLog.w(TAG, "Directly putExtra failed: %s.", e.getMessage());
		}
		if (error && Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
			ClassLoader oldParent = getClass().getClassLoader().getParent();
			mirror.java.lang.ClassLoader.parent.set(getClass().getClassLoader(), appClassLoader);
			try {
				targetIntent.putExtra("_VA_|_stub_activity_", stubActInfo);
				targetIntent.putExtra("_VA_|_target_activity_", targetActInfo);
			} catch (Throwable e) {
				VLog.w(TAG, "Secondly putExtra failed: %s.", e.getMessage());
			}
			mirror.java.lang.ClassLoader.parent.set(getClass().getClassLoader(), oldParent);
		}
		ActivityThread.ActivityClientRecord.intent.set(r, targetIntent);
		ActivityThread.ActivityClientRecord.activityInfo.set(r, targetActInfo);
		return true;
	}

	@Override
	public void inject() throws Throwable {
		otherCallback = getHCallback();
		mirror.android.os.Handler.mCallback.set(getH(), this);
	}

	@Override
	public boolean isEnvBad() {
		Handler.Callback callback = getHCallback();
		boolean envBad = callback != this;
		if (callback != null && envBad) {
			VLog.d(TAG, "HCallback has bad, other callback = " + callback);
		}
		return envBad;
	}

}
