package mirror.android.net.wifi;

import android.os.IBinder;
import android.os.IInterface;

import mirror.RefClass;
import mirror.MethodParams;
import mirror.RefStaticMethod;

public class IWifiManager {
    public static Class<?> TYPE = RefClass.load(IWifiManager.class, "android.net.wifi.IWifiManager");

    public static class Stub {
        public static Class<?> TYPE = RefClass.load(Stub.class, "android.net.wifi.IWifiManager$Stub");
        @MethodParams({IBinder.class})
        public static RefStaticMethod<IInterface> asInterface;
    }
}
