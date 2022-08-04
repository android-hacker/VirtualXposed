package mirror.android.os.storage;

import android.os.IBinder;
import android.os.IInterface;

import mirror.MethodParams;
import mirror.RefClass;
import mirror.RefStaticMethod;

public class IStorageManager {
    public static Class<?> Class = RefClass.load(IStorageManager.class, "android.os.storage.IStorageManager");

    public static class Stub {
        public static Class<?> Class = RefClass.load(Stub.class, "android.os.storage.IStorageManager$Stub");
        @MethodParams({IBinder.class})
        public static RefStaticMethod<IInterface> asInterface;
    }
}