package com.lody.virtual.client.hook.patchs.am;

import java.lang.reflect.Field;

import com.lody.virtual.client.core.AppSandBox;
import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.interfaces.Injectable;
import com.lody.virtual.helper.ExtraConstants;
import com.lody.virtual.helper.compat.ActivityRecordCompat;
import com.lody.virtual.helper.compat.ClassLoaderCompat;
import com.lody.virtual.helper.proto.AppInfo;
import com.lody.virtual.helper.utils.ComponentUtils;
import com.lody.virtual.helper.utils.VLog;

import android.app.ActivityThread;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Handler;
import android.os.Message;

/**
 * @author Lody
 *         <p>
 *         <p>
 *         注入我们的Callback到
 *         <h1>ActivityThread$H</h1>.
 * @see Handler.Callback
 * @see ActivityThread
 */
public class HCallbackHook implements Handler.Callback, Injectable {

	public static final int LAUNCH_ACTIVITY = 100;

	private static final String TAG = HCallbackHook.class.getSimpleName();
	private static final HCallbackHook sCallback = new HCallbackHook();
	private static Field f_h;
	private static Field f_handleCallback;

	static {
		try {
			f_h = ActivityThread.class.getDeclaredField("mH");
			f_handleCallback = Handler.class.getDeclaredField("mCallback");
			f_h.setAccessible(true);
			f_handleCallback.setAccessible(true);
		} catch (NoSuchFieldException e) {
			// Ignore
		}
	}

	private boolean calling = false;
	/**
	 * 其它插件化可能也会注入Activity$H, 这里要保留其它插件化的Callback引用，我们的Callback完事后再调用它的。
	 */
	private Handler.Callback otherCallback;

	private HCallbackHook() {
	}

	public static HCallbackHook getDefault() {
		return sCallback;
	}

	public static Handler getH() {
		try {
			return (Handler) f_h.get(VirtualCore.mainThread());
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return null;
	}

	private static Handler.Callback getHCallback() {
		try {
			Handler handler = getH();
			return (Handler.Callback) f_handleCallback.get(handler);
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public boolean handleMessage(Message msg) {
		if (!calling) {
			calling = true;
			try {
				if (LAUNCH_ACTIVITY == msg.what) {
					handleLaunchActivity(msg);
				}
				if (otherCallback != null) {
					return otherCallback.handleMessage(msg);
				}
			} finally {
				calling = false;
			}
		}
		return false;
	}

	private void handleLaunchActivity(Message msg) {
		Object r = msg.obj;
		// StubIntent
		Intent stubIntent = ActivityRecordCompat.getIntent(r);
		// TargetIntent
		Intent targetIntent = stubIntent.getParcelableExtra(ExtraConstants.EXTRA_TARGET_INTENT);

		ComponentName component = targetIntent.getComponent();
		String pkgName = component.getPackageName();

		AppInfo appInfo = VirtualCore.getCore().findApp(pkgName);

		if (appInfo == null) {
			return;
		}
		ActivityInfo stubActInfo = stubIntent.getParcelableExtra(ExtraConstants.EXTRA_STUB_ACT_INFO);
		ActivityInfo targetActInfo = stubIntent.getParcelableExtra(ExtraConstants.EXTRA_TARGET_ACT_INFO);
		ActivityInfo callerActInfo = stubIntent.getParcelableExtra(ExtraConstants.EXTRA_CALLER);

		if (stubActInfo == null || targetActInfo == null) {
			return;
		}
		String processName = ComponentUtils.getProcessName(targetActInfo);
		AppSandBox.install(processName, targetActInfo.packageName);

		ClassLoader pluginClassLoader = appInfo.getClassLoader();

		targetIntent.setExtrasClassLoader(pluginClassLoader);

		boolean error = false;
		try {
			targetIntent.putExtra(ExtraConstants.EXTRA_STUB_ACT_INFO, stubActInfo);
			targetIntent.putExtra(ExtraConstants.EXTRA_TARGET_ACT_INFO, targetActInfo);
			targetIntent.putExtra(ExtraConstants.EXTRA_CALLER, callerActInfo);
		} catch (Throwable e) {
			error = true;
			VLog.w(TAG, "Directly putExtra failed: %s.", e.getMessage());
		}
		if (error && Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
			// 4.4以下的设备会出现这个BUG(unParcel找不到类加载器),
			// 只能通过注入Class.forName所使用的类加载器来解决了...
			ClassLoader oldParent = ClassLoaderCompat.setParent(getClass().getClassLoader(), pluginClassLoader);
			try {
				targetIntent.putExtra(ExtraConstants.EXTRA_STUB_ACT_INFO, stubActInfo);
				targetIntent.putExtra(ExtraConstants.EXTRA_TARGET_ACT_INFO, targetActInfo);
				targetIntent.putExtra(ExtraConstants.EXTRA_CALLER, callerActInfo);
			} catch (Throwable e) {
				VLog.w(TAG, "Secondly putExtra failed: %s.", e.getMessage());
			}
			ClassLoaderCompat.setParent(getClass().getClassLoader(), oldParent);
		}
		ActivityRecordCompat.setIntent(r, targetIntent);
		ActivityRecordCompat.setActivityInfo(r, targetActInfo);
	}

	@Override
	public void inject() throws Throwable {
		otherCallback = getHCallback();
		f_handleCallback.set(getH(), this);
	}

	@Override
	public boolean isEnvBad() {
		Handler.Callback callback = getHCallback();
		boolean envBad = callback != this;
		if (envBad) {
			VLog.d(TAG, "HCallback has bad, other callback = " + callback);
		}
		return envBad;
	}

}
