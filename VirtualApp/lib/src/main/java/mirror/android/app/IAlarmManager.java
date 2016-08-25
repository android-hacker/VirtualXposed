package mirror.android.app;

import android.os.IBinder;
import android.os.IInterface;

import mirror.ClassDef;
import mirror.MethodInfo;
import mirror.StaticMethodDef;

/**
 * @author Lody
 */

public class IAlarmManager {
    public static Class<?> Class = ClassDef.init(IAlarmManager.class, "android.app.IAlarmManager");
    public static class Stub {
        public static Class<?> Class = ClassDef.init(Stub.class, "android.app.IAlarmManager$Stub");
        @MethodInfo({IBinder.class})
        public static StaticMethodDef<IInterface> asInterface;
    }
}
