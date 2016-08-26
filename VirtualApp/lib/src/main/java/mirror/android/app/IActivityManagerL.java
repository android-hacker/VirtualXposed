package mirror.android.app;

import android.content.Intent;
import android.os.IBinder;

import mirror.ClassDef;
import mirror.MethodDef;
import mirror.MethodInfo;

/**
 * @author Lody
 */

public class IActivityManagerL {
    public static Class<?> Class = ClassDef.init(IActivityManagerL.class, "android.app.IActivityManager");
    @MethodInfo({IBinder.class, int.class, Intent.class, boolean.class})
    public static MethodDef<Boolean> finishActivity;
}
