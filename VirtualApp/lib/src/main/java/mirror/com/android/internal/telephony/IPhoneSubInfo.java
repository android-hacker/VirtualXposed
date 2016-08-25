package mirror.com.android.internal.telephony;

import android.os.IBinder;
import android.os.IInterface;

import mirror.ClassDef;
import mirror.MethodInfo;
import mirror.StaticMethodDef;

public class IPhoneSubInfo {
    public static Class<?> Class = ClassDef.init(IPhoneSubInfo.class, "com.android.internal.telephony.IPhoneSubInfo");

    public static class Stub {
        public static Class<?> Class = ClassDef.init(Stub.class, "com.android.internal.telephony.IPhoneSubInfo$Stub");
        @MethodInfo({IBinder.class})
        public static StaticMethodDef<IInterface> asInterface;
    }
}
