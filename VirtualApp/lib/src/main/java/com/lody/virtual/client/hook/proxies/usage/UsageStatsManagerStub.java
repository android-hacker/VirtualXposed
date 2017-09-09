package com.lody.virtual.client.hook.proxies.usage;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;

import com.lody.virtual.client.hook.base.BinderInvocationProxy;
import com.lody.virtual.client.hook.base.ReplaceLastPkgMethodProxy;

import mirror.android.app.IUsageStatsManager;

/**
 * Created by caokai on 2017/9/8.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP_MR1)
public class UsageStatsManagerStub extends BinderInvocationProxy {

    public UsageStatsManagerStub() {
        super(IUsageStatsManager.Stub.asInterface, Context.USAGE_STATS_SERVICE);
    }
    @Override
    protected void onBindMethods() {
        super.onBindMethods();
        addMethodProxy(new ReplaceLastPkgMethodProxy("queryUsageStats"));
        addMethodProxy(new ReplaceLastPkgMethodProxy("queryConfigurations"));
        addMethodProxy(new ReplaceLastPkgMethodProxy("queryEvents"));
    }

}
