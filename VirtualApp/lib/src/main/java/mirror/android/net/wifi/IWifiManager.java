package mirror.android.net.wifi;

import android.os.IBinder;
import android.os.IInterface;

import mirror.ClassDef;
import mirror.MethodInfo;
import mirror.StaticMethodDef;

public class IWifiManager {
    public static Class<?> Class = ClassDef.init(IWifiManager.class, "android.net.wifi.IWifiManager");

    public static class Stub {
        public static Class<?> Class = ClassDef.init(Stub.class, "android.net.wifi.IWifiManager$Stub");
        @MethodInfo({IBinder.class})
        public static StaticMethodDef<IInterface> asInterface;
    }
}
