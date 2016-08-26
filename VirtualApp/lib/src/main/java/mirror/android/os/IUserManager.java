package mirror.android.os;

import android.os.IBinder;
import android.os.IInterface;

import mirror.ClassDef;
import mirror.MethodInfo;
import mirror.StaticMethodDef;

public class IUserManager {
    public static Class<?> Class = ClassDef.init(IUserManager.class, "android.os.IUserManager");

    public static class Stub {
        public static Class<?> Class = ClassDef.init(Stub.class, "android.os.IUserManager$Stub");
        @MethodInfo({IBinder.class})
        public static StaticMethodDef<IInterface> asInterface;
    }
}
