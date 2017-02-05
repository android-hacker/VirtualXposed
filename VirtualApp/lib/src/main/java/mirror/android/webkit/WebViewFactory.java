package mirror.android.webkit;

import mirror.RefClass;
import mirror.RefStaticMethod;

/**
 * @author CodeHz
 */

public class WebViewFactory {
	public static Class<?> TYPE = RefClass.load(WebViewFactory.class, "android.webkit.WebViewFactory");
	public static RefStaticMethod<Object> getUpdateService;
}
