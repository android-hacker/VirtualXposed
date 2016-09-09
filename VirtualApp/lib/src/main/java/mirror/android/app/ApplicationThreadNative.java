package mirror.android.app;

import android.os.IBinder;
import android.os.IInterface;

import mirror.RefClass;
import mirror.MethodParams;
import mirror.RefStaticMethod;

/**
 * @author Lody
 */
public class ApplicationThreadNative {
    public static Class<?> TYPE = RefClass.load(ApplicationThreadNative.class, "android.app.ApplicationThreadNative");

    @MethodParams({IBinder.class})
    public static RefStaticMethod<IInterface> asInterface;
}
