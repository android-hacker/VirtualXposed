package mirror.android.app;

import mirror.RefClass;
import mirror.RefMethod;
import mirror.MethodReflectParams;

/**
 * @author Lody
 */

public class IApplicationThreadJBMR1 {
    public static Class<?> TYPE = RefClass.load(IApplicationThreadJBMR1.class, "android.app.IApplicationThread");

    @MethodReflectParams({"android.content.Intent", "android.content.pm.ActivityInfo", "android.content.res.CompatibilityInfo", "int", "java.lang.String", "android.os.Bundle", "boolean", "int"})
    public static RefMethod<Void> scheduleReceiver;
}
