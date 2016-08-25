package mirror.android.os.mount;

import android.os.IBinder;
import android.os.IInterface;

import mirror.ClassDef;
import mirror.MethodInfo;
import mirror.StaticMethodDef;

public class IMountService {
    public static Class<?> Class = ClassDef.init(IMountService.class, "android.os.storage.IMountService");

    public static class Stub {
        public static Class<?> Class = ClassDef.init(Stub.class, "android.os.storage.IMountService$Stub");
        @MethodInfo({IBinder.class})
        public static StaticMethodDef<IInterface> asInterface;
    }
}
