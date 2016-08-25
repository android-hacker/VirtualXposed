package mirror.android.app;

import java.lang.ref.WeakReference;

import android.app.Application;
import android.app.Instrumentation;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IIntentReceiver;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.os.IInterface;

import mirror.ClassDef;
import mirror.FieldDef;
import mirror.MethodDef;
import mirror.MethodInfo;
import mirror.StaticMethodDef;

public class LoadedApk {
    public static Class Class = ClassDef.init(LoadedApk.class, "android.app.LoadedApk");
    public static FieldDef<ApplicationInfo> mApplicationInfo;
    @MethodInfo({boolean.class, Instrumentation.class})
    public static MethodDef<Application> makeApplication;

    public static class ReceiverDispatcher {
        public static Class Class = ClassDef.init(ReceiverDispatcher.class, "android.app.LoadedApk$ReceiverDispatcher");
        public static MethodDef<IInterface> getIIntentReceiver;
        public static FieldDef<BroadcastReceiver> mReceiver;
        public static FieldDef<IIntentReceiver> mIIntentReceiver;

        public static class InnerReceiver {
            public static Class Class = ClassDef.init(InnerReceiver.class, "android.app.LoadedApk$ReceiverDispatcher$InnerReceiver");
            public static FieldDef<WeakReference> mDispatcher;
        }
    }

    public static class ServiceDispatcher {
        public static Class Class = ClassDef.init(ServiceDispatcher.class, "android.app.LoadedApk$ServiceDispatcher");
        public static FieldDef<ServiceConnection> mConnection;
        public static FieldDef<Context> mContext;

        public static class InnerConnection {
            public static Class Class = ClassDef.init(InnerConnection.class, "android.app.LoadedApk$ServiceDispatcher$InnerConnection");
            public static FieldDef<WeakReference> mDispatcher;
        }
    }
}