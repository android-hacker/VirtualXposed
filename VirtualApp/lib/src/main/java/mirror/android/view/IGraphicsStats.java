package mirror.android.view;

import android.os.IBinder;
import android.os.IInterface;

import mirror.RefClass;
import mirror.MethodParams;
import mirror.RefStaticMethod;

public class IGraphicsStats {
    public static Class<?> TYPE = RefClass.load(IGraphicsStats.class, "android.view.IGraphicsStats");

    public static class Stub {
        public static Class<?> TYPE = RefClass.load(Stub.class, "android.view.IGraphicsStats$Stub");
        @MethodParams({IBinder.class})
        public static RefStaticMethod<IInterface> asInterface;
    }
}
