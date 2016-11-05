package com.lody.virtual.client.hook.patchs.am;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.IInterface;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.ipc.VActivityManager;
import com.lody.virtual.helper.proto.PendingIntentData;

import java.lang.reflect.Method;

import mirror.android.app.PendingIntentJBMR2;

/**
 * @author Lody
 *
 */

public class GetPackageForIntentSender extends Hook {
    @Override
    public String getName() {
        return "getPackageForIntentSender";
    }

    @Override
    public Object call(Object who, Method method, Object... args) throws Throwable {
        IInterface sender = (IInterface) args[0];
        if (sender != null) {
            IBinder binder = sender.asBinder();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                PendingIntent pendingIntent = PendingIntentData.readPendingIntent(binder);
                Intent intent = PendingIntentJBMR2.getIntent.call(pendingIntent);
                if (intent != null) {
                    String creator = intent.getStringExtra("_VA_|_creator_");
                    if (creator != null) {
                        return creator;
                    }
                }
            } else {
                PendingIntentData data = VActivityManager.get().getPendingIntent(binder);
                if (data != null) {
                    return data.creator;
                }
            }
        }
        return super.call(who, method, args);
    }

    @Override
    public boolean isEnable() {
        return isAppProcess();
    }
}
