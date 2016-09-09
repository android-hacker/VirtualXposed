package mirror.android.view;

import android.os.IBinder;
import android.os.IInterface;

import mirror.RefClass;
import mirror.MethodParams;
import mirror.RefStaticMethod;

public class IWindowManager {
    public static Class<?> TYPE = RefClass.load(IWindowManager.class, "android.view.IWindowManager");

    public static class Stub {
        public static Class<?> TYPE = RefClass.load(Stub.class, "android.view.IWindowManager$Stub");
        @MethodParams({IBinder.class})
        public static RefStaticMethod<IInterface> asInterface;
    }
}
