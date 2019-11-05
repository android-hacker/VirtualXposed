package mirror.android.app;

import android.os.IBinder;
import android.os.IInterface;

import mirror.MethodParams;
import mirror.RefClass;
import mirror.RefStaticMethod;

/**
 * @author weishu
 * @date 2019-11-05.
 */
public class IActivityTaskManager {
    public static Class<?> TYPE = RefClass.load(IActivityTaskManager.class, "android.app.IActivityTaskManager");

    public static class Stub {
        public static Class<?> TYPE = RefClass.load(IActivityTaskManager.Stub.class, "android.app.IActivityTaskManager$Stub");
        @MethodParams({IBinder.class})
        public static RefStaticMethod<IInterface> asInterface;
    }
}
