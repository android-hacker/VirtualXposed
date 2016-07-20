package com.lody.virtual.client.hook.patchs.notification.compat;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;
import android.widget.RemoteViews;

import com.lody.virtual.helper.utils.Reflect;

import java.util.List;

public class NotificationEx extends Notification {

    public NotificationEx() {
        super();
    }

    public NotificationEx(int icon, CharSequence tickerText, long when) {
        super(icon, tickerText, when);
    }

    public NotificationEx(Context context, int icon, CharSequence tickerText, long when, CharSequence contentTitle, CharSequence contentText, Intent contentIntent) {
        super(context, icon, tickerText, when, contentTitle, contentText, contentIntent);
    }

    public NotificationEx(String packageName, Notification notification) {
        super();
        this.packageName = packageName;
        this.orgVersion = notification;
    }

    public NotificationEx(Parcel parcel) {
        orgVersion = new Notification(parcel);
        packageName = parcel.readString();
        actionCount = parcel.readInt();
    }

    public void writeToParcel(Parcel parcel, int flags) {
        if (orgVersion == null) {
            orgVersion = new Notification();
        }
        orgVersion.writeToParcel(parcel, flags);
        parcel.writeString(packageName);
        parcel.writeInt(actionCount);
    }

    public static final Parcelable.Creator<NotificationEx> CREATOR
            = new Parcelable.Creator<NotificationEx>() {
        public NotificationEx createFromParcel(Parcel parcel) {
            return new NotificationEx(parcel);
        }

        public NotificationEx[] newArray(int size) {
            return new NotificationEx[size];
        }
    };

    public Notification getOrgVersion() {
        return orgVersion;
    }

    public String getPackageName() {
        return packageName;
    }

    public int getActionCount() {
        return actionCount;
    }

    public NotificationEx setActionCount(int actionCount) {
        this.actionCount = actionCount;
        return this;
    }

    public NotificationEx setActionCount(RemoteViews remoteViews) {
        try {
            actionCount = Reflect.on(Reflect.on(remoteViews).get("mActions")).call("size").get();
        } catch (Exception e) {

        }
        return this;
    }

    public void appendActions(RemoteViews remoteViews) {
        //orgVersion的contentViews是哪一个？
        //追加mActions
        RemoteViews myRemoteViews = RemoteViewsCompat.findRemoteViews(orgVersion);
        if (myRemoteViews != null) {
            List<Object> mActions = null;
            try {
                mActions = Reflect.on(myRemoteViews).get("mActions");
            } catch (Exception e) {

            }
            if (mActions != null) {
                int count = mActions.size();
                for (int i = actionCount; i < count; i++) {
                    addAction(remoteViews, mActions.get(i));
                }
            }
        }
    }

    private void addAction(RemoteViews remoteViews, Object action) {
        try {
            Reflect.on(remoteViews).call("addAction", action);
        } catch (Exception e) {

        }
    }

    private int actionCount;
    private String packageName;
    private Notification orgVersion;

}
