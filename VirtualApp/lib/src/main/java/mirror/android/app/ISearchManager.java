package mirror.android.app;

import android.os.IBinder;
import android.os.IInterface;

import mirror.ClassDef;
import mirror.MethodInfo;
import mirror.StaticMethodDef;

public class ISearchManager {
    public static Class<?> Class = ClassDef.init(ISearchManager.class, "android.app.ISearchManager");

    public static class Stub {
        public static Class<?> Class = ClassDef.init(Stub.class, "android.app.ISearchManager$Stub");
        @MethodInfo({IBinder.class})
        public static StaticMethodDef<IInterface> asInterface;
    }
}
