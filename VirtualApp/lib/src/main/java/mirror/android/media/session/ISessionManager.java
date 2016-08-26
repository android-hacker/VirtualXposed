package mirror.android.media.session;

import android.os.IBinder;
import android.os.IInterface;

import mirror.ClassDef;
import mirror.MethodInfo;
import mirror.StaticMethodDef;

public class ISessionManager {
    public static Class<?> Class = ClassDef.init(ISessionManager.class, "android.media.session.ISessionManager");

    public static class Stub {
        public static Class<?> Class = ClassDef.init(Stub.class, "android.media.session.ISessionManager$Stub");
        @MethodInfo({IBinder.class})
        public static StaticMethodDef<IInterface> asInterface;
    }
}
