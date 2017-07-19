package mirror.android.net.wifi;

import android.annotation.TargetApi;
import android.os.Build;


import mirror.RefClass;
import mirror.RefStaticMethod;

/**
 * @author Lody
 */
@TargetApi(Build.VERSION_CODES.KITKAT)
public class WifiSsid {
    public static final Class<?> TYPE = RefClass.load(WifiSsid.class, "android.net.wifi.WifiSsid");
    public static RefStaticMethod<Object> createFromAsciiEncoded;
}
