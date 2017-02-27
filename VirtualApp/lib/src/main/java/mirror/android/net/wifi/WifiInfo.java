package mirror.android.net.wifi;

import mirror.RefClass;
import mirror.RefObject;

public class WifiInfo {
    public static Class<?> TYPE = RefClass.load(WifiInfo.class, android.net.wifi.WifiInfo.class);
    public static RefObject<String> mMacAddress;
}