package mirror.com.android.internal.os;

import android.os.IBinder;
import android.os.IInterface;

import mirror.RefClass;
import mirror.MethodParams;
import mirror.RefStaticMethod;

public class IDropBoxManagerService {
    public static Class<?> TYPE = RefClass.load(IDropBoxManagerService.class, "com.android.internal.os.IDropBoxManagerService");

    public static class Stub {
        public static Class<?> TYPE = RefClass.load(Stub.class, "com.android.internal.os.IDropBoxManagerService$Stub");
        @MethodParams({IBinder.class})
        public static RefStaticMethod<IInterface> asInterface;
    }
}
