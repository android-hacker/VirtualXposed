package mirror.android.app;

import android.os.IBinder;
import android.os.IInterface;

import mirror.ClassDef;
import mirror.MethodInfo;
import mirror.StaticMethodDef;

/**
 * @author Lody
 */
public class ApplicationThreadNative {
    public static Class<?> Class = ClassDef.init(ApplicationThreadNative.class, "android.app.ApplicationThreadNative");

    @MethodInfo({IBinder.class})
    public static StaticMethodDef<IInterface> asInterface;
}
