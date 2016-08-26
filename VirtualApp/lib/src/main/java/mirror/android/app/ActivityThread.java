package mirror.android.app;


import android.app.Application;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.ProviderInfo;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.IInterface;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;

import mirror.ClassDef;
import mirror.CtorDef;
import mirror.FieldDef;
import mirror.MethodDef;
import mirror.MethodInfo;
import mirror.MethodReflectionInfo;
import mirror.StaticFieldDef;
import mirror.StaticIntFieldDef;
import mirror.StaticMethodDef;

public class ActivityThread {
    public static Class<?> Class = ClassDef.init(ActivityThread.class, "android.app.ActivityThread");
    public static StaticMethodDef currentActivityThread;
    public static MethodDef<String> getProcessName;
    public static MethodDef<Handler> getHandler;
    public static MethodDef<Object> installProvider;
    public static FieldDef<Object> mBoundApplication;
    public static FieldDef<Handler> mH;
    public static FieldDef<Application> mInitialApplication;
    public static FieldDef<Instrumentation> mInstrumentation;
    public static FieldDef<Map<String, WeakReference<?>>> mPackages;
    public static FieldDef<Map> mProviderMap;
    @MethodInfo({IBinder.class, List.class})
    public static MethodDef<Void> performNewIntents;
    public static StaticFieldDef<IInterface> sPackageManager;
    @MethodInfo({IBinder.class, String.class, int.class, int.class, Intent.class})
    public static MethodDef<Void> sendActivityResult;
    public static MethodDef<IBinder> getApplicationThread;

    public static class ActivityClientRecord {
        public static Class<?> Class = ClassDef.init(ActivityClientRecord.class, "android.app.ActivityThread$ActivityClientRecord");
        public static FieldDef<android.app.Activity> activity;
        public static FieldDef<ActivityInfo> activityInfo;
        public static FieldDef<Intent> intent;
        public static FieldDef token;
    }

    public static class ProviderClientRecord {
        public static Class<?> Class = ClassDef.init(ProviderClientRecord.class, "android.app.ActivityThread$ProviderClientRecord");
        @MethodReflectionInfo({"android.app.ActivityThread", "java.lang.String", "android.content.IContentProvider", "android.content.ContentProvider"})
        public static CtorDef<?> ctor;
        public static FieldDef<String> mName;
        public static FieldDef<IInterface> mProvider;
    }


    public static class AppBindData {
        public static Class<?> Class = ClassDef.init(AppBindData.class, "android.app.ActivityThread$AppBindData");
        public static FieldDef<ApplicationInfo> appInfo;
        public static FieldDef<Object> info;
        public static FieldDef<String> processName;
    }

    public static class H {
        public static Class<?> Class = ClassDef.init(H.class, "android.app.ActivityThread$H");
        public static StaticIntFieldDef LAUNCH_ACTIVITY;
    }


    public static Object installProvider(Object mainThread, Context context, ProviderInfo providerInfo) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
            return installProvider.call(mainThread, context, null, providerInfo, false, true);
        }
        return installProvider.call(mainThread, context, null, providerInfo, false, true, true);
    }
}
