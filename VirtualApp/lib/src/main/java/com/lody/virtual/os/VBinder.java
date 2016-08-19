package com.lody.virtual.os;

import android.os.Binder;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.local.VActivityManager;

/**
 * @author Lody
 */

public class VBinder {

    public static int getCallingUid() {
        VirtualCore core = VirtualCore.getCore();
        return VActivityManager.get().getUidByPid(Binder.getCallingPid());
    }

    public static int getCallingPid() {
        return Binder.getCallingPid();
    }

    public static VUserHandle getCallingUserHandle() {
        return new VUserHandle(VUserHandle.getUserId(getCallingUid()));
    }
}
