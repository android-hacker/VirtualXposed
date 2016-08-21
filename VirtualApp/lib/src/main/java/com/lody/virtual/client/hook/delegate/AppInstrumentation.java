package com.lody.virtual.client.hook.delegate;

import android.app.Activity;
import android.app.Application;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.IBinder;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.fixer.ActivityFixer;
import com.lody.virtual.client.fixer.ContextFixer;
import com.lody.virtual.client.interfaces.Injectable;
import com.lody.virtual.client.local.LocalActivityRecord;
import com.lody.virtual.client.local.VActivityManager;
import com.lody.virtual.helper.compat.ActivityManagerCompat;
import com.lody.virtual.helper.compat.BundleCompat;

/**
 * @author Lody
 */
public final class AppInstrumentation extends InstrumentationDelegate implements Injectable {

	private static final String TAG = AppInstrumentation.class.getSimpleName();
	private static AppInstrumentation gDefault;

	private AppInstrumentation(Instrumentation base) {
		super(base);
	}

	public static AppInstrumentation getDefault() {
		if (gDefault == null) {
			synchronized (AppInstrumentation.class) {
				if (gDefault == null) {
					gDefault = create();
				}
			}
		}
		return gDefault;
	}

	private static AppInstrumentation create() {
		Instrumentation instrumentation = getCurrentInstrumentation();
		if (instrumentation instanceof AppInstrumentation) {
			return (AppInstrumentation) instrumentation;
		}
		return new AppInstrumentation(instrumentation);
	}

	public static Instrumentation getCurrentInstrumentation() {
		return VirtualCore.mainThread().getInstrumentation();
	}

	@Override
	public void inject() throws Throwable {
		mirror.android.app.ActivityThread.mInstrumentation.set(VirtualCore.mainThread(), this);
	}

	@Override
	public boolean isEnvBad() {
		return getCurrentInstrumentation() != this;
	}

	@Override
	public void callActivityOnCreate(Activity activity, Bundle icicle) {
		String pkg = activity.getPackageName();
		boolean isApp = VirtualCore.getCore().isAppInstalled(pkg);
		if (isApp) {
			LocalActivityRecord r = VActivityManager.get().onActivityCreate(activity);
			ContextFixer.fixContext(activity);
			ActivityFixer.fixActivity(activity);
			ActivityInfo info = null;
			if (r != null) {
				info = r.activityInfo;
			}
			if (info != null) {
				if (info.theme != 0) {
					activity.setTheme(info.theme);
				}
				if (activity.getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
						&& info.screenOrientation != ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED) {
					activity.setRequestedOrientation(info.screenOrientation);
				}
			}
		}
		super.callActivityOnCreate(activity, icicle);
	}

	@Override
	public void callActivityOnResume(Activity activity) {
		String pkg = activity.getPackageName();
		boolean isApp = VirtualCore.getCore().isAppInstalled(pkg);
		if (isApp) {
			VActivityManager.get().onActivityResumed(activity);
		}
		super.callActivityOnResume(activity);
		Intent intent = activity.getIntent();
		Bundle bundle = intent.getBundleExtra("_VA_|_sender_");
		if (bundle != null) {
			IBinder loadingPageToken = BundleCompat.getBinder(bundle, "_VA_|_loading_token_");
			ActivityManagerCompat.finishActivity(loadingPageToken, -1, null);
		}
	}

	@Override
	public void callActivityOnDestroy(Activity activity) {
		String pkg = activity.getPackageName();
		boolean isApp = VirtualCore.getCore().isAppInstalled(pkg);
		if (isApp) {
			VActivityManager.get().onActivityDestroy(activity);
		}
		super.callActivityOnDestroy(activity);
	}

	@Override
	public void callApplicationOnCreate(Application app) {
		super.callApplicationOnCreate(app);
	}

	@Override
	public ActivityResult execStartActivity(Context who, IBinder contextThread, IBinder token, Activity target,
			Intent intent, int requestCode, Bundle options) {
		return super.execStartActivity(who, contextThread, token, target, intent, requestCode, options);
	}
}
