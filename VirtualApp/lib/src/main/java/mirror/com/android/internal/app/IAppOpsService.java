package mirror.com.android.internal.app;

import android.os.IBinder;
import android.os.IInterface;

import mirror.ClassDef;
import mirror.MethodInfo;
import mirror.StaticMethodDef;

public class IAppOpsService {
    public static Class<?> Class = ClassDef.init(IAppOpsService.class, "com.android.internal.app.IAppOpsService");

    public static class Stub {
        public static Class<?> Class = ClassDef.init(Stub.class, "com.android.internal.app.IAppOpsService$Stub");
        @MethodInfo({IBinder.class})
        public static StaticMethodDef<IInterface> asInterface;
    }
}