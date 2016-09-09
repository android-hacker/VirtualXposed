package mirror.com.android.internal.telephony;

import android.os.IBinder;
import android.os.IInterface;

import mirror.RefClass;
import mirror.MethodParams;
import mirror.RefStaticMethod;

public class IMms {
    public static Class<?> TYPE = RefClass.load(IMms.class, "com.android.internal.telephony.IMms");

    public static class Stub {
        public static Class<?> TYPE = RefClass.load(Stub.class, "com.android.internal.telephony.IMms$Stub");
        @MethodParams({IBinder.class})
        public static RefStaticMethod<IInterface> asInterface;
    }
}
