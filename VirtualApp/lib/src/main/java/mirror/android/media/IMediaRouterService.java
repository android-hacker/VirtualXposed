package mirror.android.media;

import android.os.IBinder;
import android.os.IInterface;

import mirror.RefClass;
import mirror.MethodParams;
import mirror.RefStaticMethod;

public class IMediaRouterService {
    public static Class<?> TYPE = RefClass.load(IMediaRouterService.class, "android.media.IMediaRouterService");

    public static class Stub {
        public static Class<?> TYPE = RefClass.load(Stub.class, "android.media.IMediaRouterService$Stub");
        @MethodParams({IBinder.class})
        public static RefStaticMethod<IInterface> asInterface;
    }
}