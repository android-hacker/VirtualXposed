package io.virtualapp.delegate;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.os.Build;

import com.lody.virtual.client.hook.delegate.TaskDescriptionDelegate;
import com.lody.virtual.os.VUserHandle;


/**
 * Patch the task description with the (Virtual) user name
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class MyTaskDescriptionDelegate implements TaskDescriptionDelegate {
    @Override
    public ActivityManager.TaskDescription getTaskDescription(ActivityManager.TaskDescription oldTaskDescription) {
        String labelPrefix = "[Space " + VUserHandle.myUserId() + 1 + "] ";

        if (!oldTaskDescription.getLabel().startsWith(labelPrefix)) {
            // Is it really necessary?
            return new ActivityManager.TaskDescription(labelPrefix + oldTaskDescription.getLabel(), oldTaskDescription.getIcon(), oldTaskDescription.getPrimaryColor());
        } else {
            return oldTaskDescription;
        }
    }
}
