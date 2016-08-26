package mirror.android.app;

import android.os.IBinder;
import android.os.IInterface;

import mirror.ClassDef;
import mirror.MethodInfo;
import mirror.StaticMethodDef;

public class IJobScheduler {
    public static Class<?> Class = ClassDef.init(IJobScheduler.class, "android.app.job.IJobScheduler");

    public static class Stub {
        public static Class<?> Class = ClassDef.init(Stub.class, "android.app.job.IJobScheduler$Stub");
        @MethodInfo({IBinder.class})
        public static StaticMethodDef<IInterface> asInterface;
    }
}
