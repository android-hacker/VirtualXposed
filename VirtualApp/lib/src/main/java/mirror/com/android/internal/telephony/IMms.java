package mirror.com.android.internal.telephony;

import android.os.IBinder;
import android.os.IInterface;

import mirror.ClassDef;
import mirror.MethodInfo;
import mirror.StaticMethodDef;

public class IMms {
    public static Class<?> Class = ClassDef.init(IMms.class, "com.android.internal.telephony.IMms");

    public static class Stub {
        public static Class<?> Class = ClassDef.init(Stub.class, "com.android.internal.telephony.IMms$Stub");
        @MethodInfo({IBinder.class})
        public static StaticMethodDef<IInterface> asInterface;
    }
}
