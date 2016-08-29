package com.lody.virtual.client.hook.patchs.am;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.IBinder;

import com.lody.virtual.client.VClientImpl;
import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.os.VUserManager;

import java.lang.reflect.Method;

/**
 * @author prife
 *
 * @see android.app.IActivityManager#setTaskDescription(IBinder token,
 * 				ActivityManager.TaskDescription values)
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
/* package */ class SetTaskDescription extends Hook {
	//static final String VACLIENT_SUFFIX = "[VA]";
	@Override
	public String getName() {
		return "setTaskDescription";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		ActivityManager.TaskDescription td = (ActivityManager.TaskDescription)args[1];

		String label = td.getLabel();
		Bitmap icon = td.getIcon();
		if ((label == null || !label.contains("[VA")) || icon == null) {
			String suffix = String.format("[VA:%s]", VUserManager.get().getUserName());
			Application app = VClientImpl.getClient().getCurrentApplication();
			if (label == null) {
				label = app.getApplicationInfo().loadLabel(app.getPackageManager()) + suffix;
			} else {
				label += suffix;
			}

			if (icon == null) {
				Drawable drawable = app.getApplicationInfo().loadIcon(app.getPackageManager());
				if (drawable instanceof BitmapDrawable) {
					icon = ((BitmapDrawable) drawable).getBitmap();
				}
			}
			args[1] = new ActivityManager.TaskDescription(label, icon, td.getPrimaryColor());
		}

		return method.invoke(who, args);
	}

	@Override
	public boolean isEnable() {
		return isAppProcess();
	}
}