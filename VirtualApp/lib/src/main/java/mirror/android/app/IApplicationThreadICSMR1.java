package mirror.android.app;

import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.IBinder;

import mirror.ClassDef;
import mirror.MethodDef;
import mirror.MethodInfo;
import mirror.MethodReflectionInfo;
import mirror.android.content.res.CompatibilityInfo;

/**
 * @author Lody
 */

public class IApplicationThreadICSMR1 {
    public static Class<?> Class = ClassDef.init(IApplicationThreadICSMR1.class, "android.app.IApplicationThread");

    @MethodReflectionInfo({"android.content.Intent", "android.content.pm.ActivityInfo", "android.content.res.CompatibilityInfo", "int", "java.lang.String", "android.os.Bundle", "boolean"})
    public static MethodDef<Void> scheduleReceiver;

    @MethodInfo({IBinder.class, ServiceInfo.class, CompatibilityInfo.class})
    public static MethodDef<Void> scheduleCreateService;

    @MethodInfo({IBinder.class, boolean.class, int.class, int.class, Intent.class})
    public static MethodDef<Void> scheduleServiceArgs;
}
