package mirror.com.android.internal.telephony;

import android.os.IBinder;
import android.os.IInterface;

import mirror.ClassDef;
import mirror.MethodInfo;
import mirror.StaticMethodDef;

public class ITelephony {
    public static Class<?> Class = ClassDef.init(ITelephony.class, "com.android.internal.telephony.ITelephony");

    public static class Stub {
        public static Class<?> Class = ClassDef.init(Stub.class, "com.android.internal.telephony.ITelephony$Stub");
        @MethodInfo({IBinder.class})
        public static StaticMethodDef<IInterface> asInterface;
    }
}
