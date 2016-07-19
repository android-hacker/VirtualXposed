package com.lody.virtual.client.hook.patchs.am;

import android.app.IApplicationThread;
import android.content.BroadcastReceiver;
import android.content.IIntentReceiver;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Process;
import android.os.UserHandle;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.hook.utils.HookUtils;
import com.lody.virtual.client.stub.StubReceiver;

import java.lang.reflect.Method;
import java.util.WeakHashMap;

/**
 * @author Lody
 * @see android.app.IActivityManager#registerReceiver(IApplicationThread,
 * String, IIntentReceiver, IntentFilter, String, int)
 */
/* package */ class Hook_RegisterReceiver extends Hook<ActivityManagerPatch> {

    private WeakHashMap<Object, Object> mProxyIIntentReceiver = new WeakHashMap<>();

    /**
     * 这个构造器必须有,用于依赖注入.
     *
     * @param patchObject 注入对象
     */
    public Hook_RegisterReceiver(ActivityManagerPatch patchObject) {
        super(patchObject);
    }

    @Override
    public String getName() {
        return "registerReceiver";
    }

    @Override
    public Object onHook(Object who, Method method, Object... args) throws Throwable {
        if (isServiceProcess()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && args[args.length - 1] instanceof Integer) {
                int userId = (int) args[args.length - 1];
                if (userId == -1) {
                    args[args.length - 1] = UserHandle.getUserId(Process.myUid());
                }
            }
            return method.invoke(who, args);
        }
        String pkg = HookUtils.replaceFirstAppPkg(args);

        final int indexOfRequiredPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1
                ? 4
                : 3;
        if (args != null && args.length > indexOfRequiredPermission
                && args[indexOfRequiredPermission] instanceof String) {
            args[indexOfRequiredPermission] = VirtualCore.getPermissionBroadcast();
        }
        final int indexOfIIntentReceiver = Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1 ? 2 : 1;
        if (args != null && args.length > indexOfIIntentReceiver
                && BroadcastReceiver.class.isInstance(args[indexOfIIntentReceiver])) {
            final BroadcastReceiver old = (BroadcastReceiver) args[indexOfIIntentReceiver];
            if (!StubReceiver.class.isInstance(old)) {
                Object proxyIIntentReceiver = mProxyIIntentReceiver.get(old);
                if (proxyIIntentReceiver == null) {
                    proxyIIntentReceiver = new StubReceiver(old, pkg);
                    mProxyIIntentReceiver.put(old, proxyIIntentReceiver);
                }
                args[indexOfIIntentReceiver] = proxyIIntentReceiver;
            }
        }
        return method.invoke(who, args);
    }

    @Override
    public boolean isEnable() {
        return isAppProcess() || isServiceProcess();
    }
}
