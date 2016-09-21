package mirror.android.net;

import android.os.IBinder;
import android.os.IInterface;

import mirror.MethodParams;
import mirror.RefClass;
import mirror.RefStaticMethod;


/**
 * @author Junelegency
 *
 */
public class IConnectivityManager {
    public static Class<?> TYPE = RefClass.load(IConnectivityManager.class, "android.net.IConnectivityManager");

    public static class Stub {
        public static Class<?> TYPE = RefClass.load(Stub.class, "android.net.IConnectivityManager$Stub");
        @MethodParams({IBinder.class})
        public static RefStaticMethod<IInterface> asInterface;
    }
}
