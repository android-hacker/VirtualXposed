package mirror.android.app;

import android.os.IBinder;
import android.os.IInterface;

import mirror.MethodParams;
import mirror.RefClass;
import mirror.RefStaticMethod;

/**
 * Created by caokai on 2017/9/8.
 */

public class IUsageStatsManager {
    public static Class<?> TYPE = RefClass.load(IUsageStatsManager.class, "android.app.usage.IUsageStatsManager");

    public static class Stub {
        public static Class<?> TYPE = RefClass.load(IUsageStatsManager.Stub.class, "android.app.usage.IUsageStatsManager$Stub");
        @MethodParams({IBinder.class})
        public static RefStaticMethod<IInterface> asInterface;
    }
}
