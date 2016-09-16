package mirror.android.app.job;

import android.os.IBinder;
import android.os.IInterface;

import mirror.RefClass;
import mirror.MethodParams;
import mirror.RefStaticMethod;

public class IJobScheduler {
    public static Class<?> TYPE = RefClass.load(IJobScheduler.class, "android.app.job.IJobScheduler");

    public static class Stub {
        public static Class<?> TYPE = RefClass.load(Stub.class, "android.app.job.IJobScheduler$Stub");
        @MethodParams({IBinder.class})
        public static RefStaticMethod<IInterface> asInterface;
    }
}
