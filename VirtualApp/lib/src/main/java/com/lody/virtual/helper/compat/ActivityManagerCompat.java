package com.lody.virtual.helper.compat;

import android.app.ActivityManagerNative;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;

import com.lody.virtual.helper.utils.Reflect;

/**
 * @author Lody
 */

public class ActivityManagerCompat {

    public static boolean finishActivity(IBinder token, int code, Intent data) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                return ActivityManagerNative.getDefault().finishActivity(token, code, data, false);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        try {
            return Reflect.on(ActivityManagerNative.getDefault()).call("finishActivity", token, code, data).get();
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return false;
    }
}
