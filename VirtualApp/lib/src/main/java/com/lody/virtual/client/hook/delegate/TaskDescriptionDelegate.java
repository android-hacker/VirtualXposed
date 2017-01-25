package com.lody.virtual.client.hook.delegate;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.os.Build;

public interface TaskDescriptionDelegate {
    public ActivityManager.TaskDescription getTaskDescription(ActivityManager.TaskDescription oldTaskDescription);
}
