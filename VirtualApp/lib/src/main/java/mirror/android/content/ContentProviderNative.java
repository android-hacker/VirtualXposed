package mirror.android.content;

import android.os.IBinder;
import android.os.IInterface;

import mirror.ClassDef;
import mirror.MethodInfo;
import mirror.StaticMethodDef;

public class ContentProviderNative {
    public static Class<?> Class = ClassDef.init(ContentProviderNative.class, "android.content.ContentProviderNative");
    @MethodInfo({IBinder.class})
    public static StaticMethodDef<IInterface> asInterface;
}
