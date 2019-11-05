package com.lody.virtual.client.hook.proxies.am;

import android.os.IBinder;

import com.lody.virtual.client.hook.base.BinderInvocationProxy;
import com.lody.virtual.client.hook.base.Inject;
import com.lody.virtual.client.hook.base.StaticMethodProxy;
import com.lody.virtual.client.ipc.VActivityManager;

import java.lang.reflect.Method;

import mirror.android.app.IActivityTaskManager;

/**
 * @author weishu
 * @date 2019-11-05.
 */
@Inject(MethodProxies.class)
public class ActivityTaskManagerStub extends BinderInvocationProxy {
    public ActivityTaskManagerStub() {
        super(IActivityTaskManager.Stub.TYPE, "activity_task");
    }

    @Override
    protected void onBindMethods() {
        super.onBindMethods();

        addMethodProxy(new StaticMethodProxy("activityDestroyed") {
            @Override
            public Object call(Object who, Method method, Object... args) throws Throwable {
                IBinder token = (IBinder) args[0];
                VActivityManager.get().onActivityDestroy(token);
                return super.call(who, method, args);
            }
        });
        addMethodProxy(new StaticMethodProxy("activityResumed") {
            @Override
            public Object call(Object who, Method method, Object... args) throws Throwable {
                IBinder token = (IBinder) args[0];
                VActivityManager.get().onActivityResumed(token);
                return super.call(who, method, args);
            }
        });
        addMethodProxy(new StaticMethodProxy("finishActivity") {
            @Override
            public Object call(Object who, Method method, Object... args) throws Throwable {
                IBinder token = (IBinder) args[0];
                VActivityManager.get().finishActivity(token);
                return super.call(who, method, args);
            }
        });
    }
}
