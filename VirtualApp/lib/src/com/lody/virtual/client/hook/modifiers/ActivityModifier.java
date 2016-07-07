package com.lody.virtual.client.hook.modifiers;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;

import com.lody.virtual.helper.utils.Reflect;

/**
 * @author Lody
 *
 */
public final class ActivityModifier {

	private ActivityModifier() {
	}

	public static void fixActivity(Activity activity) {
		Context baseContext = activity.getBaseContext();
		// 这里不能直接调用 R 资源,要用反射,因为直接调用的话编译之后会被优化成直接赋值.

		try {
			Reflect styleableRef = Reflect.on(com.android.internal.R.styleable.class);
			TypedArray typedArray = activity.obtainStyledAttributes((int[]) styleableRef.get("Window"));
			if (typedArray != null) {
				boolean showWallpaper = typedArray.getBoolean((Integer) styleableRef.get("Window_windowShowWallpaper"),
						false);
				if (showWallpaper) {
					activity.getWindow().setBackgroundDrawable(WallpaperManager.getInstance(activity).getDrawable());
				}
				typedArray.recycle();
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			Intent intent = activity.getIntent();
			ApplicationInfo applicationInfo = baseContext.getApplicationInfo();
			PackageManager pm = activity.getPackageManager();
			if (intent != null && activity.isTaskRoot()) {
				String label = "" + applicationInfo.loadLabel(pm);
				Bitmap icon = null;
				Drawable drawable = applicationInfo.loadIcon(pm);
				if (drawable instanceof BitmapDrawable) {
					icon = ((BitmapDrawable) drawable).getBitmap();
				}
				activity.setTaskDescription(new ActivityManager.TaskDescription(label, icon));
			}

		}
	}
}
