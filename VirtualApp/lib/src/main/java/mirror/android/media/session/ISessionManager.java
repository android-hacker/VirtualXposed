package mirror.android.media.session;

import android.os.IBinder;
import android.os.IInterface;

import mirror.RefClass;
import mirror.MethodParams;
import mirror.RefStaticMethod;

public class ISessionManager {
    public static Class<?> TYPE = RefClass.load(ISessionManager.class, "android.media.session.ISessionManager");

    public static class Stub {
        public static Class<?> TYPE = RefClass.load(Stub.class, "android.media.session.ISessionManager$Stub");
        @MethodParams({IBinder.class})
        public static RefStaticMethod<IInterface> asInterface;
    }
}
