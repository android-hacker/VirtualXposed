package mirror.android.content;

import android.os.IBinder;
import android.os.IInterface;

import mirror.RefClass;
import mirror.MethodParams;
import mirror.RefStaticMethod;

/**
 * @author Lody
 */

public class IContentService {
    public static Class<?> TYPE = RefClass.load(IContentService.class, "android.content.IContentService");

    public static class Stub {
        public static Class<?> TYPE = RefClass.load(Stub.class, "android.content.IContentService$Stub");
        @MethodParams({IBinder.class})
        public static RefStaticMethod<IInterface> asInterface;
    }
}
