package mirror.android.app;

import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.IBinder;

import mirror.RefClass;
import mirror.RefMethod;
import mirror.MethodParams;
import mirror.MethodReflectParams;
import mirror.android.content.res.CompatibilityInfo;

/**
 * @author Lody
 */

public class IApplicationThreadKitkat {
    public static Class<?> TYPE = RefClass.load(IApplicationThreadKitkat.class, "android.app.IApplicationThread");

    @MethodReflectParams({"android.content.Intent", "android.content.pm.ActivityInfo", "android.content.res.CompatibilityInfo", "int", "java.lang.String", "android.os.Bundle", "boolean", "int", "int"})
    public static RefMethod<Void> scheduleReceiver;

    @MethodParams({IBinder.class, ServiceInfo.class, CompatibilityInfo.class, int.class})
    public static RefMethod<Void> scheduleCreateService;

    @MethodParams({IBinder.class, Intent.class, boolean.class, int.class})
    public static RefMethod<Void> scheduleBindService;
}
