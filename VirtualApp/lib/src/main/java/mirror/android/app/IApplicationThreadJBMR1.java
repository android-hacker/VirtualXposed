package mirror.android.app;

import android.app.IApplicationThread;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.CompatibilityInfo;
import android.os.Bundle;

import mirror.ClassDef;
import mirror.MethodDef;
import mirror.MethodInfo;

/**
 * @author Lody
 */

public class IApplicationThreadJBMR1 {
    public static Class<?> Class = ClassDef.init(IApplicationThreadJBMR1.class, IApplicationThread.class);

    @MethodInfo({Intent.class, ActivityInfo.class, CompatibilityInfo.class, int.class, String.class, Bundle.class, boolean.class, int.class})
    public static MethodDef<Void> scheduleReceiver;
}
