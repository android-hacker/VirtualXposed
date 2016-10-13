package com.lody.virtual.client.hook.patchs.am;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;

import com.lody.virtual.client.VClientImpl;
import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.helper.utils.DrawableUtils;
import com.lody.virtual.os.VUserManager;

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
		String VACLIENT_SUFFIX = " ["+ VUserManager.get().getUserName()+"]";
		if ((label == null || !label.startsWith(VACLIENT_SUFFIX) || icon == null)){
			Application app = VClientImpl.getClient().getCurrentApplication();
			if (label == null) {
				label = VACLIENT_SUFFIX+app.getApplicationInfo().loadLabel(app.getPackageManager());
			} else {
				label = VACLIENT_SUFFIX+label;
			}

			if (icon == null) {
				Drawable drawable = app.getApplicationInfo().loadIcon(app.getPackageManager());
				if(drawable != null) {
					icon = DrawableUtils.drawableToBitMap(drawable);
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