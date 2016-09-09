package mirror.android.content;

import android.os.IBinder;
import android.os.IInterface;

import mirror.RefClass;
import mirror.MethodParams;
import mirror.RefStaticMethod;

public class IRestrictionsManager {
    public static Class<?> TYPE = RefClass.load(IRestrictionsManager.class, "android.content.IRestrictionsManager");

    public static class Stub {
        public static Class<?> TYPE = RefClass.load(Stub.class, "android.content.IRestrictionsManager$Stub");
        @MethodParams({IBinder.class})
        public static RefStaticMethod<IInterface> asInterface;
    }
}