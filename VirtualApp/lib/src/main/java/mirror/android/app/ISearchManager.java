package mirror.android.app;

import android.os.IBinder;
import android.os.IInterface;

import mirror.RefClass;
import mirror.MethodParams;
import mirror.RefStaticMethod;

public class ISearchManager {
    public static Class<?> TYPE = RefClass.load(ISearchManager.class, "android.app.ISearchManager");

    public static class Stub {
        public static Class<?> TYPE = RefClass.load(Stub.class, "android.app.ISearchManager$Stub");
        @MethodParams({IBinder.class})
        public static RefStaticMethod<IInterface> asInterface;
    }
}
