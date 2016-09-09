package mirror.android.location;

import android.os.IBinder;
import android.os.IInterface;

import mirror.RefClass;
import mirror.MethodParams;
import mirror.RefStaticMethod;

public class ILocationManager {
    public static Class<?> TYPE = RefClass.load(ILocationManager.class, "android.location.ILocationManager");

    public static class Stub {
        public static Class<?> TYPE = RefClass.load(Stub.class, "android.location.ILocationManager$Stub");
        @MethodParams({IBinder.class})
        public static RefStaticMethod<IInterface> asInterface;
    }
}
