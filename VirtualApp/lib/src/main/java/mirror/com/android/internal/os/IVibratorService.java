package mirror.com.android.internal.os;

import android.os.IBinder;
import android.os.IInterface;

import mirror.RefClass;
import mirror.MethodParams;
import mirror.RefStaticMethod;

public class IVibratorService {
    public static Class<?> TYPE = RefClass.load(IVibratorService.class, "android.os.IVibratorService");

    public static class Stub {
        public static Class<?> TYPE = RefClass.load(Stub.class, "android.os.IVibratorService$Stub");
        @MethodParams({IBinder.class})
        public static RefStaticMethod<IInterface> asInterface;
    }
}
