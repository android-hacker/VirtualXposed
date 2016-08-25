package mirror.android.view;

import android.os.IBinder;
import android.os.IInterface;

import mirror.ClassDef;
import mirror.MethodInfo;
import mirror.StaticMethodDef;

public class IGraphicsStats {
    public static Class<?> Class = ClassDef.init(IGraphicsStats.class, "android.view.IGraphicsStats");

    public static class Stub {
        public static Class<?> Class = ClassDef.init(Stub.class, "android.view.IGraphicsStats$Stub");
        @MethodInfo({IBinder.class})
        public static StaticMethodDef<IInterface> asInterface;
    }
}
