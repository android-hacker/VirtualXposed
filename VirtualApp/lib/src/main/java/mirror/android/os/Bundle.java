package mirror.android.os;

import android.os.IBinder;

import mirror.RefClass;
import mirror.RefMethod;
import mirror.MethodParams;

/**
 * @author Lody
 */

public class Bundle {
    public static Class<?> TYPE = RefClass.load(Bundle.class, android.os.Bundle.class);

    @MethodParams({String.class, IBinder.class})
    public static RefMethod<Void> putIBinder;

    @MethodParams({String.class})
    public static RefMethod<IBinder> getIBinder;
}
