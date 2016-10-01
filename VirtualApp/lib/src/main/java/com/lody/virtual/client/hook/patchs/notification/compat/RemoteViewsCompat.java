package com.lody.virtual.client.hook.patchs.notification.compat;

import android.app.Notification;
import android.content.Context;
import android.os.Build;
import android.widget.RemoteViews;

/**
 * Created by 247321453 on 2016/7/17.
 */
/*package*/ class RemoteViewsCompat {
	private Context context;
	private RemoteViews mRemoteViews;
	private RemoteViews mBigRemoteViews;
	private RemoteViews mHeadsUpContentView;
	private Notification mNotification;

	RemoteViewsCompat(Context context, Notification notification) {
		this.context = context;
		this.mNotification = checkNotNull(notification, true);
	}

	public Notification getNotification() {
		return mNotification;
	}

	private Notification checkNotNull(Notification notification, boolean first) {
		if (notification.contentView != null) {
			mRemoteViews = notification.contentView;
		}
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			if (notification.bigContentView != null) {
				mBigRemoteViews = notification.bigContentView;
			}
		}
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			mHeadsUpContentView = notification.headsUpContentView;
			if (notification.publicVersion != null) {
				if (mRemoteViews == null && notification.publicVersion.contentView != null) {
					mRemoteViews = notification.publicVersion.contentView;
				}
				if (mBigRemoteViews == null && notification.publicVersion.bigContentView != null) {
					mBigRemoteViews = notification.publicVersion.bigContentView;
				}
				if (mHeadsUpContentView == null) {
					mHeadsUpContentView = notification.publicVersion.headsUpContentView;
				}
			}
		}
		if (first && (mRemoteViews == null && mBigRemoteViews == null)) {
			Notification my = NotificationUtils.clone(context, notification);
			return checkNotNull(my, false);
		} else {
			return notification;
		}
	}

	public RemoteViews getRemoteViews() {
		return mRemoteViews;
	}

	public RemoteViews getBigRemoteViews() {
		return mBigRemoteViews;
	}

	public RemoteViews getHeadsUpContentView() {
		return mHeadsUpContentView;
	}
}
