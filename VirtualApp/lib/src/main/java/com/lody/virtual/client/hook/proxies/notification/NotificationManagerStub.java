package com.lody.virtual.client.hook.proxies.notification;

import android.os.Build;
import android.os.IInterface;

import com.lody.virtual.client.hook.base.Inject;
import com.lody.virtual.client.hook.base.MethodInvocationProxy;
import com.lody.virtual.client.hook.base.MethodInvocationStub;
import com.lody.virtual.client.hook.base.ReplaceCallingPkgMethodProxy;
import com.lody.virtual.client.hook.base.StaticMethodProxy;
import com.lody.virtual.client.hook.utils.MethodParameterUtils;
import com.lody.virtual.helper.compat.BuildCompat;
import com.lody.virtual.helper.utils.DeviceUtil;

import java.lang.reflect.Method;

import mirror.android.app.NotificationManager;
import mirror.android.widget.Toast;

/**
 * @author Lody
 * @see android.app.NotificationManager
 * @see android.widget.Toast
 */
@Inject(MethodProxies.class)
public class NotificationManagerStub extends MethodInvocationProxy<MethodInvocationStub<IInterface>> {

    public NotificationManagerStub() {
        super(new MethodInvocationStub<IInterface>(NotificationManager.getService.call()));
    }

    @Override
    protected void onBindMethods() {
        super.onBindMethods();
        addMethodProxy(new ReplaceCallingPkgMethodProxy("enqueueToast"));
        addMethodProxy(new ReplaceCallingPkgMethodProxy("cancelToast"));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            addMethodProxy(new ReplaceCallingPkgMethodProxy("removeAutomaticZenRules"));
            addMethodProxy(new ReplaceCallingPkgMethodProxy("getImportance"));
            addMethodProxy(new ReplaceCallingPkgMethodProxy("areNotificationsEnabled"));
            addMethodProxy(new ReplaceCallingPkgMethodProxy("setNotificationPolicy"));
            addMethodProxy(new ReplaceCallingPkgMethodProxy("getNotificationPolicy"));
            addMethodProxy(new ReplaceCallingPkgMethodProxy("isNotificationPolicyAccessGrantedForPackage"));
        }

        // http://androidxref.com/8.0.0_r4/xref/frameworks/base/core/java/android/app/INotificationManager.aidl
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            addMethodProxy(new ReplaceCallingPkgMethodProxy("createNotificationChannelGroups"));
            addMethodProxy(new ReplaceCallingPkgMethodProxy("getNotificationChannelGroups"));
            addMethodProxy(new ReplaceCallingPkgMethodProxy("deleteNotificationChannelGroup"));
            addMethodProxy(new ReplaceCallingPkgMethodProxy("createNotificationChannels"));
            addMethodProxy(new ReplaceCallingPkgMethodProxy("getNotificationChannels") {
                @Override
                public boolean beforeCall(Object who, Method method, Object... args) {
                    MethodParameterUtils.replaceLastUid(args);
                    return super.beforeCall(who, method, args);
                }
            });
            addMethodProxy(new StaticMethodProxy("getNotificationChannel") {
                @Override
                public boolean beforeCall(Object who, Method method, Object... args) {
                    MethodParameterUtils.replaceLastUid(args);
                    int sequence = BuildCompat.isQ() ? 2 : 1;
                    MethodParameterUtils.replaceSequenceAppPkg(args, sequence);
                    return super.beforeCall(who, method, args);
                }
            });
            addMethodProxy(new ReplaceCallingPkgMethodProxy("deleteNotificationChannel"));
        }
        if (DeviceUtil.isSamsung()) {
            addMethodProxy(new ReplaceCallingPkgMethodProxy("removeEdgeNotification"));
        }
    }

    @Override
    public void inject() throws Throwable {
        NotificationManager.sService.set(getInvocationStub().getProxyInterface());
        Toast.sService.set(getInvocationStub().getProxyInterface());
    }

    @Override
    public boolean isEnvBad() {
        return NotificationManager.getService.call() != getInvocationStub().getProxyInterface();
    }
}
