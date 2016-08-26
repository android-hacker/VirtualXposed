package mirror.com.android.internal.telephony;

import android.os.IBinder;
import android.os.IInterface;

import mirror.ClassDef;
import mirror.MethodInfo;
import mirror.StaticMethodDef;

/**
 * @author Lody
 */

public class ISub {
    public static Class<?> Class = ClassDef.init(ISub.class, "com.android.internal.telephony.ISub");

    public static class Stub {
        public static Class<?> Class = ClassDef.init(Stub.class, "com.android.internal.telephony.ISub$Stub");
        @MethodInfo({IBinder.class})
        public static StaticMethodDef<IInterface> asInterface;
    }
}
