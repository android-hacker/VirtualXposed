package mirror.android.app;

import android.os.IBinder;
import android.os.IInterface;

import mirror.MethodParams;
import mirror.MethodReflectParams;
import mirror.RefClass;
import mirror.RefMethod;
import mirror.RefStaticMethod;

/**
 * @author Lody
 */

public class IApplicationThreadOreo {

    public static Class<?> TYPE = RefClass.load(IApplicationThreadOreo.class, "android.app.IApplicationThread");

    public static final class Stub {
        public static Class<?> TYPE = RefClass.load(IApplicationThreadOreo.Stub.class, "android.app.IApplicationThread$Stub");
        @MethodParams({IBinder.class})
        public static RefStaticMethod<IInterface> asInterface;
    }

    @MethodReflectParams({"android.os.IBinder", "android.content.pm.ParceledListSlice"})
    public static RefMethod<Void> scheduleServiceArgs;
}
