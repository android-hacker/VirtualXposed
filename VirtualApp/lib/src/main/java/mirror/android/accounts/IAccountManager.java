package mirror.android.accounts;

import android.os.IBinder;
import android.os.IInterface;

import mirror.RefClass;
import mirror.MethodParams;
import mirror.RefStaticMethod;

public class IAccountManager {
    public static Class<?> TYPE = RefClass.load(IAccountManager.class, "android.accounts.IAccountManager");

    public static class Stub {
        public static Class<?> TYPE = RefClass.load(Stub.class, "android.accounts.IAccountManager$Stub");
        @MethodParams({IBinder.class})
        public static RefStaticMethod<IInterface> asInterface;
    }
}