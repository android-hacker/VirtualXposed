package mirror.com.android.internal.os;

import android.os.IBinder;
import android.os.IInterface;

import mirror.RefClass;
import mirror.MethodParams;
import mirror.RefStaticMethod;

public class UserManager {
    public static Class<?> TYPE = RefClass.load(UserManager.class, "android.os.UserManager");

    public static class Stub {
        public static Class<?> TYPE = RefClass.load(Stub.class, "android.os.IUserManager$Stub");
        @MethodParams({IBinder.class})
        public static RefStaticMethod<IInterface> asInterface;
    }
}
