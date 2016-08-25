package mirror.android.os;

import android.os.IBinder;
import android.os.IInterface;

import mirror.ClassDef;
import mirror.MethodInfo;
import mirror.StaticMethodDef;

/**
 * @author Lody
 */

public class IPowerManager {
    public static Class<?> Class = ClassDef.init(IPowerManager.class, "android.os.IPowerManager");

    public static class Stub {
        public static Class<?> Class = ClassDef.init(Stub.class, "android.os.IPowerManager$Stub");
        @MethodInfo({IBinder.class})
        public static StaticMethodDef<IInterface> asInterface;
    }
}
