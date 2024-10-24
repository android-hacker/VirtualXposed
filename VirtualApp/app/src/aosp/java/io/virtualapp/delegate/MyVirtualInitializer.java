package io.virtualapp.delegate;

import android.app.Application;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.hook.proxies.view.AutoFillManagerStub;
import com.lody.virtual.helper.compat.BuildCompat;
import com.lody.virtual.helper.utils.VLog;
import com.microsoft.appcenter.AppCenter;
import com.microsoft.appcenter.analytics.Analytics;
import com.microsoft.appcenter.crashes.Crashes;

/**
 * @author weishu
 * @date 2019/2/25.
 */
public class MyVirtualInitializer extends BaseVirtualInitializer {

    static final String TAG = "MyVirtualInitializer";

    public MyVirtualInitializer(Application application, VirtualCore core) {
        super(application, core);
    }

    @Override
    public void onMainProcess() {
        AppCenter.start(application, "bf5e74bd-3795-49bd-95c8-327db494dd11",
                Analytics.class, Crashes.class);
        super.onMainProcess();
    }

    @Override
    public void onVirtualProcess() {

        // For Crash statics
        AppCenter.start(application, "bf5e74bd-3795-49bd-95c8-327db494dd11",
                Analytics.class, Crashes.class);

        super.onVirtualProcess();

        // Override
        virtualCore.setCrashHandler(new MyCrashHandler());


        if (BuildCompat.isOreo()) {
            // Android 13以上的版本在attachBaseContext里注入这个对象会报错，所以挪到这里来
            try {
                new AutoFillManagerStub().inject();
            } catch (Throwable e) {
                VLog.w(TAG, "AutoFillManagerStub inject error",e );
            }
        }

    }

    @Override
    public void onChildProcess() {
        super.onChildProcess();
    }
}
