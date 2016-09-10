package mirror.android.net;

import android.os.IBinder;
import android.os.IInterface;

import mirror.MethodParams;
import mirror.RefClass;
import mirror.RefMethod;


/**
 * Created by lichen:) on 2016/9/1.
 */
public class IConnectivityManager {
    public static Class<?> Class = RefClass.load(IConnectivityManager.class, "android.net.IConnectivityManager");

    public static class Stub {
        public static Class<?> Class = RefClass.load(Stub.class, "android.net.IConnectivityManager$Stub");
        @MethodParams({IBinder.class})
        public static RefMethod<IInterface> asInterface;
    }
}
