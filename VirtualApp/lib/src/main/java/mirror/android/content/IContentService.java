package mirror.android.content;

import android.os.IBinder;
import android.os.IInterface;

import mirror.ClassDef;
import mirror.MethodInfo;
import mirror.StaticMethodDef;

/**
 * @author Lody
 */

public class IContentService {
    public static Class<?> Class = ClassDef.init(IContentService.class, "android.content.IContentService");

    public static class Stub {
        public static Class<?> Class = ClassDef.init(Stub.class, "android.content.IContentService$Stub");
        @MethodInfo({IBinder.class})
        public static StaticMethodDef<IInterface> asInterface;
    }
}
