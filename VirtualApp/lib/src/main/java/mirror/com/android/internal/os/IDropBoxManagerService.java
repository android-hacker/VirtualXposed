package mirror.com.android.internal.os;

import android.os.IBinder;
import android.os.IInterface;

import mirror.ClassDef;
import mirror.MethodInfo;
import mirror.StaticMethodDef;

public class IDropBoxManagerService {
    public static Class<?> Class = ClassDef.init(IDropBoxManagerService.class, "com.android.internal.os.IDropBoxManagerService");

    public static class Stub {
        public static Class<?> Class = ClassDef.init(Stub.class, "com.android.internal.os.IDropBoxManagerService$Stub");
        @MethodInfo({IBinder.class})
        public static StaticMethodDef<IInterface> asInterface;
    }
}
