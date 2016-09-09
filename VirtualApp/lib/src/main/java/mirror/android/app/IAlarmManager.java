package mirror.android.app;

import android.os.IBinder;
import android.os.IInterface;

import mirror.RefClass;
import mirror.MethodParams;
import mirror.RefStaticMethod;

/**
 * @author Lody
 */

public class IAlarmManager {
    public static Class<?> TYPE = RefClass.load(IAlarmManager.class, "android.app.IAlarmManager");
    public static class Stub {
        public static Class<?> TYPE = RefClass.load(Stub.class, "android.app.IAlarmManager$Stub");
        @MethodParams({IBinder.class})
        public static RefStaticMethod<IInterface> asInterface;
    }
}
