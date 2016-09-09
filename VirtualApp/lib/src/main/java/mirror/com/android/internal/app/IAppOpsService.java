package mirror.com.android.internal.app;

import android.os.IBinder;
import android.os.IInterface;

import mirror.RefClass;
import mirror.MethodParams;
import mirror.RefStaticMethod;

public class IAppOpsService {
    public static Class<?> TYPE = RefClass.load(IAppOpsService.class, "com.android.internal.app.IAppOpsService");

    public static class Stub {
        public static Class<?> TYPE = RefClass.load(Stub.class, "com.android.internal.app.IAppOpsService$Stub");
        @MethodParams({IBinder.class})
        public static RefStaticMethod<IInterface> asInterface;
    }
}