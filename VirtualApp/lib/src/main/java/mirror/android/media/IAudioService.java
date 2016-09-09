package mirror.android.media;
import android.os.IBinder;
import android.os.IInterface;

import mirror.RefClass;
import mirror.MethodParams;
import mirror.RefStaticMethod;

public class IAudioService {
    public static Class<?> TYPE = RefClass.load(IAudioService.class, "android.media.IAudioService");

    public static class Stub {
        public static Class<?> TYPE = RefClass.load(Stub.class, "android.media.IAudioService$Stub");
        @MethodParams({IBinder.class})
        public static RefStaticMethod<IInterface> asInterface;
    }
}