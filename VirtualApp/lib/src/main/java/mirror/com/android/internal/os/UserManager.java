package mirror.com.android.internal.os;

import android.os.IBinder;
import android.os.IInterface;

import mirror.ClassDef;
import mirror.MethodInfo;
import mirror.StaticMethodDef;

public class UserManager {
    public static Class<?> Class = ClassDef.init(UserManager.class, "android.os.UserManager");

    public static class Stub {
        public static Class<?> Class = ClassDef.init(Stub.class, "android.os.IUserManager$Stub");
        @MethodInfo({IBinder.class})
        public static StaticMethodDef<IInterface> asInterface;
    }
}
