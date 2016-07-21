package com.lody.virtual.client.hook.patchs.am;

import android.app.IApplicationThread;
import android.content.ComponentName;
import android.content.IIntentReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.hook.utils.HookUtils;

import java.lang.reflect.Method;
import java.util.WeakHashMap;

/**
 * @author Lody
 *
 * @see android.app.IActivityManager#registerReceiver(IApplicationThread,
 *      String, IIntentReceiver, IntentFilter, String, int)
 */
/* package */ class Hook_RegisterReceiver extends Hook {

    private WeakHashMap<IBinder, IIntentReceiver.Stub> mProxyIIntentReceiver = new WeakHashMap<>();

    @Override
    public String getName() {
        return "registerReceiver";
    }

    @Override
    public Object onHook(Object who, Method method, Object... args) throws Throwable {

        HookUtils.replaceFirstAppPkg(args);

        final int indexOfRequiredPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1
                ? 4
                : 3;
        if (args != null && args.length > indexOfRequiredPermission
                && args[indexOfRequiredPermission] instanceof String) {
            args[indexOfRequiredPermission] = VirtualCore.getPermissionBroadcast();
        }
        final int indexOfIIntentReceiver = Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1 ? 2 : 1;
        if (args != null && args.length > indexOfIIntentReceiver
                && IIntentReceiver.class.isInstance(args[indexOfIIntentReceiver])) {
            final IIntentReceiver old = (IIntentReceiver) args[indexOfIIntentReceiver];
            IBinder token = old.asBinder();
            IIntentReceiver.Stub proxyIIntentReceiver = mProxyIIntentReceiver.get(token);
            if (proxyIIntentReceiver == null) {
                proxyIIntentReceiver = new IIntentReceiver.Stub() {
                    @Override
                    public void performReceive(Intent intent, int resultCode, String data, Bundle extras,
                                               boolean ordered, boolean sticky, int sendingUser) throws RemoteException {
                        try {
                            String action = intent.getAction();
                            ComponentName oldComponent = VirtualCore.getOriginComponentName(action);
                            if (oldComponent != null) {
                                        intent.setComponent(oldComponent);
                                intent.setAction(null);
                            }

                            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
                                old.performReceive(intent, resultCode, data, extras, ordered, sticky, sendingUser);
                            } else {
                                Method performReceive = old.getClass().getDeclaredMethod("performReceive", Intent.class,
                                        int.class, String.class, Bundle.class, boolean.class, boolean.class);
                                performReceive.setAccessible(true);
                                performReceive.invoke(old, intent, resultCode, data, extras, ordered, sticky);
                            }
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }

                    // @Override
                    public void performReceive(Intent intent, int resultCode, String data, Bundle extras,
                                               boolean ordered, boolean sticky) throws android.os.RemoteException {
                        this.performReceive(intent, resultCode, data, extras, ordered, sticky, 0);
                    }
                };
                mProxyIIntentReceiver.put(token, proxyIIntentReceiver);
            }
            args[indexOfIIntentReceiver] = proxyIIntentReceiver;
        }

        return method.invoke(who, args);
    }

    @Override
    public boolean isEnable() {
        return isAppProcess();
    }
}