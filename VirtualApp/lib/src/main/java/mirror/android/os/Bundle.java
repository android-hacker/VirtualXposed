package mirror.android.os;

import android.os.IBinder;

import mirror.ClassDef;
import mirror.MethodDef;
import mirror.MethodInfo;

/**
 * @author Lody
 */

public class Bundle {
    public static Class<?> Class = ClassDef.init(Bundle.class, android.os.Bundle.class);

    @MethodInfo({String.class, IBinder.class})
    public static MethodDef<Void> putIBinder;

    @MethodInfo({String.class})
    public static MethodDef<IBinder> getIBinder;
}
