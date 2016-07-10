package com.lody.virtual.client.hook.delegate;

import android.app.Activity;
import android.app.ActivityThread;
import android.app.Application;
import android.app.Instrumentation;
import android.content.pm.ActivityInfo;
import android.os.Bundle;

import com.lody.virtual.client.core.PatchManager;
import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.hook.modifiers.ActivityModifier;
import com.lody.virtual.client.hook.modifiers.ContextModifier;
import com.lody.virtual.client.interfaces.Injectable;
import com.lody.virtual.client.local.LocalActivityManager;
import com.lody.virtual.client.local.LocalActivityRecord;

import java.lang.reflect.Field;

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
		Field f_mInstrumentation = ActivityThread.class.getDeclaredField("mInstrumentation");
		if (!f_mInstrumentation.isAccessible()) {
			f_mInstrumentation.setAccessible(true);
		}
		f_mInstrumentation.set(VirtualCore.mainThread(), this);
	}

	@Override
	public boolean isEnvBad() {
		return getCurrentInstrumentation() != this;
	}

	@Override
	public void callActivityOnCreate(Activity activity, Bundle icicle) {
		PatchManager.getInstance().fixContext(activity);
		String pkg = activity.getPackageName();
		boolean isApp = VirtualCore.getCore().isAppInstalled(pkg);
		if (isApp) {
            LocalActivityRecord r = LocalActivityManager.getInstance().onActivityCreate(activity);
			ContextModifier.modifyContext(activity);
			ActivityModifier.fixActivity(activity);
            ActivityInfo info = null;
            if (r != null) {
                info = r.activityInfo;
            }
            if (info != null) {
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
			LocalActivityManager.getInstance().onActivityResumed(activity);
		}
		super.callActivityOnResume(activity);
	}

	@Override
    public void callActivityOnDestroy(Activity activity) {
        String pkg = activity.getPackageName();
        boolean isApp = VirtualCore.getCore().isAppInstalled(pkg);
        if (isApp) {
            LocalActivityManager.getInstance().onActivityDestroy(activity);
        }
        super.callActivityOnDestroy(activity);
    }

    @Override
	public void callApplicationOnCreate(Application app) {
		super.callApplicationOnCreate(app);
	}

}
