package com.lody.virtual.client.hook.proxies.battery;

import android.content.Context;
import android.os.IInterface;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.hook.base.BinderInvocationProxy;
import com.lody.virtual.client.hook.base.ReplaceLastUidMethodProxy;

import mirror.com.android.internal.os.health.SystemHealthManager;

/**
 * @author weishu
 * @date 2020/11/24.
 */
public class BatteryStatsStub extends BinderInvocationProxy {

    public BatteryStatsStub() {
        super(getInterface(), "batterystats");
    }

    private static IInterface getInterface() {
        Object manager = VirtualCore.get().getContext().getSystemService(Context.SYSTEM_HEALTH_SERVICE);
        return SystemHealthManager.mBatteryStats.get(manager);
    }

    @Override
    protected void onBindMethods() {
        super.onBindMethods();

        addMethodProxy(new ReplaceLastUidMethodProxy("takeUidSnapshot"));
    }
}
