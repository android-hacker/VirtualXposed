package mirror.com.android.internal.os;

import android.os.IBinder;
import android.os.IInterface;

import mirror.ClassDef;
import mirror.MethodInfo;
import mirror.StaticMethodDef;

public class IVibratorService {
    public static Class<?> Class = ClassDef.init(IVibratorService.class, "android.os.IVibratorService");

    public static class Stub {
        public static Class<?> Class = ClassDef.init(Stub.class, "android.os.IVibratorService$Stub");
        @MethodInfo({IBinder.class})
        public static StaticMethodDef<IInterface> asInterface;
    }
}
