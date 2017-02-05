package mirror.android.webkit;

import mirror.RefClass;
import mirror.RefMethod;

/**
 * @author CodeHz
 */

public class IWebViewUpdateService {
    public static Class<?> TYPE = RefClass.load(IWebViewUpdateService.class, "android.webkit.IWebViewUpdateService$Stub$Proxy");

    public static RefMethod<String> getCurrentWebViewPackageName;
}
