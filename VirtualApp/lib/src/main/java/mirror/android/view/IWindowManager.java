package mirror.android.view;

import android.os.IBinder;
import android.os.IInterface;

import mirror.ClassDef;
import mirror.MethodInfo;
import mirror.StaticMethodDef;

public class IWindowManager {
    public static Class<?> Class = ClassDef.init(IWindowManager.class, "android.view.IWindowManager");

    public static class Stub {
        public static Class<?> Class = ClassDef.init(Stub.class, "android.view.IWindowManager$Stub");
        @MethodInfo({IBinder.class})
        public static StaticMethodDef<IInterface> asInterface;
    }
}
