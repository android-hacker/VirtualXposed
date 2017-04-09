package com.lody.virtual.client.hook.proxies.search;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Build;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.hook.base.BinderInvocationProxy;
import com.lody.virtual.client.hook.base.MethodProxy;
import com.lody.virtual.client.hook.base.StaticMethodProxy;

import java.lang.reflect.Method;

import mirror.android.app.ISearchManager;

/**
 * @author Lody
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
public class SearchManagerStub extends BinderInvocationProxy {

    public SearchManagerStub() {
        super(ISearchManager.Stub.asInterface, Context.SEARCH_SERVICE);
    }

    @Override
    protected void onBindMethods() {
        super.onBindMethods();
        addMethodProxy(new StaticMethodProxy("launchLegacyAssist"));
        addMethodProxy(new GetSearchableInfo());
    }

     private static class GetSearchableInfo extends MethodProxy {

        @Override
        public String getMethodName() {
            return "getSearchableInfo";
        }

        @Override
        public Object call(Object who, Method method, Object... args) throws Throwable {
            ComponentName component = (ComponentName) args[0];
            if (component != null) {
                ActivityInfo activityInfo = VirtualCore.getPM().getActivityInfo(component, 0);
                if (activityInfo != null) {
                    return null;
                }
            }
            return method.invoke(who, args);
        }
    }
}
