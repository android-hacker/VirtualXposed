package mirror.android.content;

import android.os.IBinder;
import android.os.IInterface;

import mirror.ClassDef;
import mirror.MethodInfo;
import mirror.StaticMethodDef;

public class IRestrictionsManager {
    public static Class<?> Class = ClassDef.init(IRestrictionsManager.class, "android.content.IRestrictionsManager");

    public static class Stub {
        public static Class<?> Class = ClassDef.init(Stub.class, "android.content.IRestrictionsManager$Stub");
        @MethodInfo({IBinder.class})
        public static StaticMethodDef<IInterface> asInterface;
    }
}