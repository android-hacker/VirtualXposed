package mirror.android.app;


import android.app.IActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ProviderInfo;
import android.os.Build;
import android.os.IBinder;

import java.util.List;

import mirror.ClassDef;
import mirror.FieldDef;
import mirror.MethodDef;
import mirror.MethodInfo;
import mirror.StaticFieldDef;
import mirror.StaticIntFieldDef;
import mirror.StaticMethodDef;

public class ActivityThread {
    public static Class<?> Class = ClassDef.init(ActivityThread.class, "android.app.ActivityThread");
    public static StaticMethodDef currentActivityThread;
    public static MethodDef getHandler;
    public static MethodDef installProvider;
    public static FieldDef mBoundApplication;
    public static FieldDef mH;
    public static FieldDef mInitialApplication;
    public static FieldDef mInstrumentation;
    public static FieldDef mPackages;
    public static FieldDef mProviderMap;
    @MethodInfo({IBinder.class, List.class})
    public static MethodDef performNewIntents;
    public static StaticFieldDef sPackageManager;
    @MethodInfo({IBinder.class, String.class, int.class, int.class, Intent.class})
    public static MethodDef sendActivityResult;

    public static class ActivityClientRecord {
        public static Class<?> Class = ClassDef.init(ActivityClientRecord.class, "android.app.ActivityThread$ActivityClientRecord");
        public static FieldDef activity;
        public static FieldDef activityInfo;
        public static FieldDef intent;
        public static FieldDef token;
    }

    public static class AppBindData {
        public static Class<?> Class = ClassDef.init(AppBindData.class, "android.app.ActivityThread$AppBindData");
        public static FieldDef appInfo;
        public static FieldDef info;
        public static FieldDef processName;
    }

    public static class H {
        public static Class<?> Class = ClassDef.init(H.class, "android.app.ActivityThread$H");
        public static StaticIntFieldDef LAUNCH_ACTIVITY;
    }


    public static IActivityManager.ContentProviderHolder installProvider(Object mainThread, Context context, ProviderInfo providerInfo) {
        if (Build.VERSION.SDK_INT <= 15) {
            return installProvider.call(mainThread, context, null, providerInfo, false, true);
        }
        return installProvider.call(mainThread, context, null, providerInfo, false, true, true);
    }
}
