package com.lody.virtual.client.hook.patchs.notification.compat;

import android.app.Notification;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Looper;
import android.widget.RemoteViews;

import com.lody.virtual.client.core.VirtualCore;

import java.lang.reflect.InvocationTargetException;

/**
 * Created by 247321453 on 2016/7/12.
 */
public class NotificationHandler {

	private static final String TAG = NotificationHandler.class.getSimpleName();

	/** Needn't deal the Notification */
	public static final int RES_NOT_DEAL = 0;
	/** Need to replace the new Notification */
	public static final int RES_REPLACE = 1;
	/** Notification Fixed, needn't replace. */
	public static final int RES_DEAL_OK = 2;
	/** Needn't show the Notification */
	public static final int RES_NOT_SHOW = 3;

	public static boolean DEPEND_SYSTEM = false;

	private static NotificationHandler sInstance = new NotificationHandler();

	private NotificationHandler() {
	}

	public static NotificationHandler getInstance() {
		return sInstance;
	}

	public Result dealNotification(Context context, Notification notification, String packageName) throws Exception {
		Result result = new Result(RES_NOT_DEAL, null);
		if (DEPEND_SYSTEM) {
			if (VirtualCore.get().isOutsideInstalled(packageName)) {
				NotificationUtils.fixNotificationIcon(context, notification);
				result.code = RES_DEAL_OK;
				return result;
			}
		}
		Notification replaceNotification = replaceNotification(context, packageName, notification);
		if (replaceNotification != null) {
			result.code = RES_REPLACE;
			result.notification = replaceNotification;
		} else {
			result.code = RES_NOT_SHOW;
		}
		return result;
	}

	private Notification replaceNotification(Context context, String packageName, Notification notification)
			throws PackageManager.NameNotFoundException, ClassNotFoundException, NoSuchMethodException,
			InvocationTargetException, IllegalAccessException {
		if (Looper.myLooper() == null) {
			Looper.prepare();
		}
		Context appContext = getAppContext(context, packageName);
		if (appContext == null) {
			return null;
		}
		RemoteViewsCompat remoteViewsCompat = new RemoteViewsCompat(appContext, notification);
		Notification cloneNotification = NotificationUtils.clone(appContext, notification);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			RemoteViews oldHeadsUpContentView = remoteViewsCompat.getHeadsUpContentView();
			NotificationUtils.fixIconImage(appContext, oldHeadsUpContentView, notification);
			cloneNotification.headsUpContentView = RemoteViewsUtils.getInstance().createViews(context, appContext,
					oldHeadsUpContentView, false);
		}

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			RemoteViews oldBigContentViews = remoteViewsCompat.getBigRemoteViews();
			NotificationUtils.fixIconImage(appContext, oldBigContentViews, notification);
			cloneNotification.bigContentView = RemoteViewsUtils.getInstance().createViews(context, appContext,
					oldBigContentViews, true);
		}
		RemoteViews oldContentView = remoteViewsCompat.getRemoteViews();
		NotificationUtils.fixIconImage(appContext, oldContentView, notification);
		cloneNotification.contentView = RemoteViewsUtils.getInstance().createViews(context, appContext, oldContentView,
				false);
		NotificationUtils.fixNotificationIcon(context, cloneNotification);
		return cloneNotification;

	}

	private Context getAppContext(Context base, String packageName) {
		try {
			return base.createPackageContext(packageName,
					Context.CONTEXT_IGNORE_SECURITY | Context.CONTEXT_INCLUDE_CODE);
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	public class Result {
		public int code;
		public Notification notification;

		Result(int code, Notification notification) {
			this.code = code;
			this.notification = notification;
		}
	}
}
