package com.lody.virtual.client.hook.patchs.am;

import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.IBinder;
import android.util.TypedValue;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.ipc.ActivityClientRecord;
import com.lody.virtual.client.ipc.VActivityManager;

import java.lang.reflect.Method;

/**
 * @author Lody
 */

/*package*/ class FinishActivity extends Hook {
	@Override
	public String getName() {
		return "finishActivity";
	}

	@Override
	public Object afterCall(Object who, Method method, Object[] args, Object result) throws Throwable {
		IBinder token = (IBinder) args[0];
		ActivityClientRecord r = VActivityManager.get().getActivityRecord(token);
		boolean taskRemoved = VActivityManager.get().onActivityDestroy(token);
		if (!taskRemoved && r != null && r.activity != null && r.info.getThemeResource() != 0) {
			try {
                TypedValue out = new TypedValue();
                Resources.Theme theme = r.activity.getResources().newTheme();
                theme.applyStyle(r.info.getThemeResource(), true);
                if (theme.resolveAttribute(android.R.attr.windowAnimationStyle, out, true)) {

                    TypedArray array = theme.obtainStyledAttributes(out.data,
                            new int[]{
                                    android.R.attr.activityCloseEnterAnimation,
                                    android.R.attr.activityCloseExitAnimation
                            });
                    r.activity.overridePendingTransition(array.getResourceId(0, 0), array.getResourceId(1, 0));
                    array.recycle();
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
		}
		return super.afterCall(who, method, args, result);
	}

	@Override
	public boolean isEnable() {
		return isAppProcess();
	}
}
