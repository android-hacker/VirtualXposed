package mirror.android.net.wifi;

import mirror.RefClass;
import mirror.RefStaticObject;

public final class WifiScanner {
    public static Class<?> Class;
    public static RefStaticObject<String> GET_AVAILABLE_CHANNELS_EXTRA;

    static {
        RefClass.load(WifiScanner.class, "android.net.wifi.WifiScanner");
    }
}