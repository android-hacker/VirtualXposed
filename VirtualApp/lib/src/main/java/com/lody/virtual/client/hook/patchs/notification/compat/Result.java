package com.lody.virtual.client.hook.patchs.notification.compat;

import android.app.Notification;

public class Result {
    public int code;
    public Notification notification;

    Result(int code, Notification notification) {
        this.code = code;
        this.notification = notification;
    }
}
