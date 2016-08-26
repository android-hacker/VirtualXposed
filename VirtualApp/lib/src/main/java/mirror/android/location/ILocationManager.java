package mirror.android.location;

import android.os.IBinder;
import android.os.IInterface;

import mirror.ClassDef;
import mirror.MethodInfo;
import mirror.StaticMethodDef;

public class ILocationManager {
    public static Class<?> Class = ClassDef.init(ILocationManager.class, "android.location.ILocationManager");

    public static class Stub {
        public static Class<?> Class = ClassDef.init(Stub.class, "android.location.ILocationManager$Stub");
        @MethodInfo({IBinder.class})
        public static StaticMethodDef<IInterface> asInterface;
    }
}
