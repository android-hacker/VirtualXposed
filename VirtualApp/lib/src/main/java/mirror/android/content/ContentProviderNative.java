package mirror.android.content;

import android.os.IBinder;
import android.os.IInterface;

import mirror.RefClass;
import mirror.MethodParams;
import mirror.RefStaticMethod;

public class ContentProviderNative {
    public static Class<?> TYPE = RefClass.load(ContentProviderNative.class, "android.content.ContentProviderNative");
    @MethodParams({IBinder.class})
    public static RefStaticMethod<IInterface> asInterface;
}
