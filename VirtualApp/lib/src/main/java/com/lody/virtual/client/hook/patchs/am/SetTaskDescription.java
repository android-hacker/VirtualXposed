package com.lody.virtual.client.hook.patchs.am;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;

import com.lody.virtual.client.VClientImpl;
import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.hook.delegate.TaskDescriptionDelegate;
import com.lody.virtual.helper.utils.DrawableUtils;

import java.lang.reflect.Method;

/**
 * @author prife
 *
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
/* package */ class SetTaskDescription extends Hook {
	@Override
	public String getName() {
		return "setTaskDescription";
	}

	@Override
	public Object call(Object who, Method method, Object... args) throws Throwable {
		ActivityManager.TaskDescription td = (ActivityManager.TaskDescription)args[1];
		String label = td.getLabel();
		Bitmap icon = td.getIcon();

		// If the activity label/icon isn't specified, the application's label/icon is shown instead
		// Android usually does that for us, but in this case we want info about the contained app, not VIrtualApp itself
		if (label == null || icon == null) {
			Application app = VClientImpl.get().getCurrentApplication();

			if (label == null) {
				label = app.getApplicationInfo().loadLabel(app.getPackageManager()).toString();
			}

			if (icon == null) {
				Drawable drawable = app.getApplicationInfo().loadIcon(app.getPackageManager());
				if(drawable != null) {
					icon = DrawableUtils.drawableToBitMap(drawable);
				}
			}
			td = new ActivityManager.TaskDescription(label, icon, td.getPrimaryColor());
		}

		TaskDescriptionDelegate descriptionDelegate = VirtualCore.get().getTaskDescriptionDelegate();
		if (descriptionDelegate != null) {
			td = descriptionDelegate.getTaskDescription(td);
		}

		args[1] = td;
		return method.invoke(who, args);
	}

	@Override
	public boolean isEnable() {
		return isAppProcess();
	}
}
