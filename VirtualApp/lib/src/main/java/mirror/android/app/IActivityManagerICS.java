package mirror.android.app;

import android.content.Intent;
import android.os.IBinder;
import mirror.ClassDef;
import mirror.MethodDef;
import mirror.MethodInfo;

public class IActivityManagerICS {
    public static Class<?> Class = ClassDef.init(IActivityManagerICS.class, "android.app.IActivityManager");
    @MethodInfo({IBinder.class, int.class, Intent.class})
    public static MethodDef<Boolean> finishActivity;
}
