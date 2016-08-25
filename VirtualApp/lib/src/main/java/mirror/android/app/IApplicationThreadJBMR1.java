package mirror.android.app;

import mirror.ClassDef;
import mirror.MethodDef;
import mirror.MethodReflectionInfo;

/**
 * @author Lody
 */

public class IApplicationThreadJBMR1 {
    public static Class<?> Class = ClassDef.init(IApplicationThreadJBMR1.class, "android.app.IApplicationThread");

    @MethodReflectionInfo({"android.content.Intent", "android.content.pm.ActivityInfo", "android.content.res.CompatibilityInfo", "int", "java.lang.String", "android.os.Bundle", "boolean", "int"})
    public static MethodDef<Void> scheduleReceiver;
}
