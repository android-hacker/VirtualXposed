package mirror.android.media;
import android.os.IBinder;
import android.os.IInterface;

import mirror.ClassDef;
import mirror.MethodInfo;
import mirror.StaticMethodDef;

public class IAudioService {
    public static Class<?> Class = ClassDef.init(IAudioService.class, "android.media.IAudioService");

    public static class Stub {
        public static Class<?> Class = ClassDef.init(Stub.class, "android.media.IAudioService$Stub");
        @MethodInfo({IBinder.class})
        public static StaticMethodDef<IInterface> asInterface;
    }
}