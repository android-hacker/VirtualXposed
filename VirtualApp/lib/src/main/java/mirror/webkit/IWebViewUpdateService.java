package mirror.webkit;

import android.os.IBinder;
import android.os.IInterface;

import mirror.MethodParams;
import mirror.RefClass;
import mirror.RefMethod;
import mirror.RefStaticMethod;
import mirror.android.net.wifi.IWifiManager;

/**
 * @author CodeHz
 */

public class IWebViewUpdateService {
	public static Class<?> TYPE = RefClass.load(IWebViewUpdateService.class, "android.webkit.IWebViewUpdateService$Stub$Proxy");

	public static RefMethod<String> getCurrentWebViewPackageName;
}
