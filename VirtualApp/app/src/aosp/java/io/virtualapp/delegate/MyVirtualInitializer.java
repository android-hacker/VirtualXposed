package io.virtualapp.delegate;

import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.lody.virtual.client.core.VirtualCore;

import io.fabric.sdk.android.Fabric;

/**
 * @author weishu
 * @date 2019/2/25.
 */
public class MyVirtualInitializer extends BaseVirtualInitializer {
    public MyVirtualInitializer(Application application, VirtualCore core) {
        super(application, core);
    }

    @Override
    public void onMainProcess() {
        Fabric.with(this.application, new Crashlytics());
        super.onMainProcess();
    }

    @Override
    public void onVirtualProcess() {

        // For Crash statics
        Fabric.with(application, new Crashlytics());

        super.onVirtualProcess();

        // Override
        virtualCore.setCrashHandler(new MyCrashHandler());
    }
}
