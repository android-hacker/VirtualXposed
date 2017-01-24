package io.virtualapp;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.os.Build;

import com.lody.virtual.client.hook.delegate.PhoneInfoDelegate;
import com.lody.virtual.client.hook.delegate.TaskDescriptionDelegate;
import com.lody.virtual.os.VUserManager;


/**
 * Patch the task description with the (Virtual) user name
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
class MyTaskDescriptionDelegate implements TaskDescriptionDelegate {
    @Override
    public ActivityManager.TaskDescription getTaskDescription(ActivityManager.TaskDescription oldTaskDescription) {
        String laberPrefix = "["+ VUserManager.get().getUserName()+"] ";

        if (!oldTaskDescription.getLabel().startsWith(laberPrefix)) {
            // Is it really necessary?
            return new ActivityManager.TaskDescription(laberPrefix + oldTaskDescription.getLabel(), oldTaskDescription.getIcon(), oldTaskDescription.getPrimaryColor());
        } else {
            return oldTaskDescription;
        }
    }
}
