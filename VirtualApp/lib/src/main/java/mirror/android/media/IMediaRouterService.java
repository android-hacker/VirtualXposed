package mirror.android.media;

import android.os.IBinder;
import android.os.IInterface;

import mirror.ClassDef;
import mirror.MethodInfo;
import mirror.StaticMethodDef;

public class IMediaRouterService {
    public static Class<?> Class = ClassDef.init(IMediaRouterService.class, "android.media.IMediaRouterService");

    public static class Stub {
        public static Class<?> Class = ClassDef.init(Stub.class, "android.media.IMediaRouterService$Stub");
        @MethodInfo({IBinder.class})
        public static StaticMethodDef<IInterface> asInterface;
    }
}