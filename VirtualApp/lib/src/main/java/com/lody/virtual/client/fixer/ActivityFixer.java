package com.lody.virtual.client.fixer;

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

import mirror.com.android.internal.R_Hide;

/**
 * @author Lody
 *
 */
public final class ActivityFixer {

	private ActivityFixer() {
	}

	public static void fixActivity(Activity activity) {
		Context baseContext = activity.getBaseContext();
		try {
			TypedArray typedArray = activity.obtainStyledAttributes((R_Hide.styleable.Window.get()));
			if (typedArray != null) {
				boolean showWallpaper = typedArray.getBoolean(R_Hide.styleable.Window_windowShowWallpaper.get(),
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
				try {
					String label = applicationInfo.loadLabel(pm) + "";
					Bitmap icon = null;
					Drawable drawable = applicationInfo.loadIcon(pm);
					if (drawable instanceof BitmapDrawable) {
						icon = ((BitmapDrawable) drawable).getBitmap();
					}
					activity.setTaskDescription(new ActivityManager.TaskDescription(label, icon));
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
		}
	}
}
