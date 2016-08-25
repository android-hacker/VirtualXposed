package mirror.android.accounts;

import android.os.IBinder;
import android.os.IInterface;

import mirror.ClassDef;
import mirror.MethodInfo;
import mirror.StaticMethodDef;

public class IAccountManager {
    public static Class<?> Class = ClassDef.init(IAccountManager.class, "android.accounts.IAccountManager");

    public static class Stub {
        public static Class<?> Class = ClassDef.init(Stub.class, "android.accounts.IAccountManager$Stub");
        @MethodInfo({IBinder.class})
        public static StaticMethodDef<IInterface> asInterface;
    }
}