package mirror.android.service.persistentdata;

import android.os.IBinder;
import android.os.IInterface;

import mirror.RefClass;
import mirror.MethodParams;
import mirror.RefStaticMethod;

public class IPersistentDataBlockService {
    public static Class<?> TYPE = RefClass.load(IPersistentDataBlockService.class, "android.service.persistentdata.IPersistentDataBlockService");

    public static class Stub {
        public static Class<?> TYPE = RefClass.load(Stub.class, "android.service.persistentdata.IPersistentDataBlockService$Stub");
        @MethodParams({IBinder.class})
        public static RefStaticMethod<IInterface> asInterface;
    }
}
