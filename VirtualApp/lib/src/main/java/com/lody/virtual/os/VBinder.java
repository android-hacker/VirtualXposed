package com.lody.virtual.os;

import android.os.Binder;
import android.os.UserHandle;

import com.lody.virtual.IOHook;

/**
 * @author Lody
 */

public class VBinder {

    public static int getCallingUid() {
        return IOHook.onGetCallingUid(Binder.getCallingUid());
    }

    public static int getCallingPid() {
        return Binder.getCallingPid();
    }

    public static VUserHandle getCallingUserHandle() {
        return new VUserHandle(VUserHandle.getUserId(getCallingUid()));
    }
}
