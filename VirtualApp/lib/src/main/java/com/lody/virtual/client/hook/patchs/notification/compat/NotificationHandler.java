package com.lody.virtual.client.hook.patchs.notification.compat;

import java.lang.reflect.InvocationTargetException;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.helper.utils.VLog;

import android.app.Notification;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Looper;
import android.widget.RemoteViews;

/**
 * Created by 247321453 on 2016/7/12.
 */
public class NotificationHandler {
	/** 没有处理 */
	public static final int RESULT_CODE_DONT_DEAL = 0;
	/** 需要替换通知栏 */
	public static final int RESULT_CODE_REPLACE = 1;
	/** 替换了资源 */
	public static final int RESULT_CODE_DEAL_OK = 2;
	/** 不显示通知栏 */
	public static final int RESULT_CODE_DONT_SHOW = 3;
	private static final String TAG = NotificationHandler.class.getSimpleName();
	/** 双开不处理 */
	public static boolean DOPEN_NOT_DEAL = false;
	/** 系统样式的通知栏不处理 */
	public static boolean SYSTEM_NOTIFICATION_NOT_DEAL = false;
	static NotificationHandler sNotificationHandler;

	private NotificationHandler() {
	}

	public static NotificationHandler getInstance() {
		if (sNotificationHandler == null) {
			synchronized (NotificationHandler.class) {
				if (sNotificationHandler == null) {
					sNotificationHandler = new NotificationHandler();
				}
			}
		}
		return sNotificationHandler;
	}

	private Context getContext() {
		return VirtualCore.get().getContext();
	}

	public Result dealNotification(Context context, Notification notification, String packageName) throws Exception {
		Result result = new Result(RESULT_CODE_DONT_DEAL, null);
		// if (!NotificaitionUtils.isPluginNotification(notification)) {
		// //不是插件的通知栏
		// return result;
		// }
		if (DOPEN_NOT_DEAL) {
			if (VirtualCore.get().isOutsideInstalled(packageName)) {
				// 双开模式，直接替换icon
				NotificaitionUtils.fixNotificationIcon(context, notification);
				result.code = RESULT_CODE_DEAL_OK;
				return result;
			}
		}
		// 自定义样式
		Notification notification1 = replaceNotification(context, packageName, notification);
		if (notification1 != null) {
			result.code = RESULT_CODE_REPLACE;
			result.notification = notification1;
		} else {
			result.code = RESULT_CODE_DONT_SHOW;
			VLog.w(TAG, "dont show notification:" + notification);
		}
		return result;
	}

	/***
	 * @param packageName
	 *            通知栏包名
	 * @param notification
	 *            通知栏
	 * @return
	 * @throws PackageManager.NameNotFoundException
	 */
	private Notification replaceNotification(Context context, String packageName, Notification notification)
			throws PackageManager.NameNotFoundException, ClassNotFoundException, NoSuchMethodException,
			InvocationTargetException, IllegalAccessException {
		// notification Animation run need looper
		// check before running.
		if (Looper.myLooper() == null) {
			Looper.prepare();
		}
		Context pluginContext = getContext(context, packageName);
		if (pluginContext == null) {
			return null;
		}
		// 获取需要绘制的remoteviews
		RemoteViewsCompat remoteViewsCompat = new RemoteViewsCompat(pluginContext, notification);
		/// clone and set
		Notification notification1 = NotificaitionUtils.clone(pluginContext, notification);
		//
		if (Build.VERSION.SDK_INT >= 21) {
			RemoteViews oldHeadsUpContentView = remoteViewsCompat.getHeadsUpContentView();
			NotificaitionUtils.fixIconImage(pluginContext, oldHeadsUpContentView, notification);
			notification1.headsUpContentView = RemoteViewsUtils.getInstance().createViews(context, pluginContext,
					oldHeadsUpContentView, false);
		}
		//
		if (Build.VERSION.SDK_INT >= 16) {
			RemoteViews oldBigContentViews = remoteViewsCompat.getBigRemoteViews();
			NotificaitionUtils.fixIconImage(pluginContext, oldBigContentViews, notification);
			notification1.bigContentView = RemoteViewsUtils.getInstance().createViews(context, pluginContext,
					oldBigContentViews, true);
		}
		//
		RemoteViews oldContentView = remoteViewsCompat.getRemoteViews();
		NotificaitionUtils.fixIconImage(pluginContext, oldContentView, notification);
		notification1.contentView = RemoteViewsUtils.getInstance().createViews(context, pluginContext, oldContentView,
				false);

		NotificaitionUtils.fixNotificationIcon(context, notification1);
		return notification1;

	}

	private Context getContext(Context base, String packageName) {
		Context pluginContext = null;
		try {
			pluginContext = base.createPackageContext(packageName,
					Context.CONTEXT_IGNORE_SECURITY | Context.CONTEXT_INCLUDE_CODE);
		} catch (PackageManager.NameNotFoundException e) {
		}
		return pluginContext;
	}

	public class Result {
		public int code;
		public Notification notification;

		public Result(int code, Notification notification) {
			this.code = code;
			this.notification = notification;
		}
	}
}
