package mirror.android.app;

import android.os.IBinder;
import android.os.IInterface;

import mirror.MethodParams;
import mirror.RefClass;
import mirror.RefStaticMethod;

/**
 * @author Lody
 */

public class IApplicationThreadOreo {

    public static final class Stub {
        public static Class<?> TYPE = RefClass.load(IApplicationThreadOreo.Stub.class, "android.app.IApplicationThread$Stub");
        @MethodParams({IBinder.class})
        public static RefStaticMethod<IInterface> asInterface;
    }


}
