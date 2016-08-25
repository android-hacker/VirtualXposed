package mirror.android.app;

import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.IBinder;

import java.util.List;

import mirror.ClassDef;
import mirror.MethodDef;
import mirror.MethodInfo;

/**
 * @author Lody
 */

public class IApplicationThread {
    public static Class<?> Class = ClassDef.init(IApplicationThread.class, "android.app.IApplicationThread");

    @MethodInfo({List.class, IBinder.class})
    public static MethodDef<Void> scheduleNewIntent;

    @MethodInfo({IBinder.class, ServiceInfo.class})
    public static MethodDef<Void> scheduleCreateService;

    @MethodInfo({IBinder.class, Intent.class, boolean.class})
    public static MethodDef<Void> scheduleBindService;

    @MethodInfo({IBinder.class, Intent.class})
    public static MethodDef<Void> scheduleUnbindService;

    @MethodInfo({IBinder.class, int.class, int.class, Intent.class})
    public static MethodDef<Void> scheduleServiceArgs;

    @MethodInfo({IBinder.class})
    public static MethodDef<Void> scheduleStopService;
}
