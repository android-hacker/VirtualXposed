package mirror.android.service.persistentdata;

import android.os.IBinder;
import android.os.IInterface;

import mirror.ClassDef;
import mirror.MethodInfo;
import mirror.StaticMethodDef;

public class IPersistentDataBlockService {
    public static Class<?> Class = ClassDef.init(IPersistentDataBlockService.class, "android.service.persistentdata.IPersistentDataBlockService");

    public static class Stub {
        public static Class<?> Class = ClassDef.init(Stub.class, "android.service.persistentdata.IPersistentDataBlockService$Stub");
        @MethodInfo({IBinder.class})
        public static StaticMethodDef<IInterface> asInterface;
    }
}
