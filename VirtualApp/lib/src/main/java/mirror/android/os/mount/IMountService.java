package mirror.android.os.mount;

import android.os.IBinder;
import android.os.IInterface;

import mirror.RefClass;
import mirror.MethodParams;
import mirror.RefStaticMethod;

public class IMountService {
    public static Class<?> TYPE = RefClass.load(IMountService.class, "android.os.storage.IMountService");

    public static class Stub {
        public static Class<?> TYPE = RefClass.load(Stub.class, "android.os.storage.IMountService$Stub");
        @MethodParams({IBinder.class})
        public static RefStaticMethod<IInterface> asInterface;
    }
}
