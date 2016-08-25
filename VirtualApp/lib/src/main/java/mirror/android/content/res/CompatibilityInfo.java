package mirror.android.content.res;

import android.content.pm.ApplicationInfo;
import mirror.ClassDef;
import mirror.CtorDef;
import mirror.MethodInfo;
import mirror.StaticFieldDef;

public class CompatibilityInfo {
    public static Class<?> Class = ClassDef.init(CompatibilityInfo.class, "android.content.res.CompatibilityInfo");
    @MethodInfo({ApplicationInfo.class, int.class, int.class, boolean.class})
    public static CtorDef ctor;
    public static StaticFieldDef<Object> DEFAULT_COMPATIBILITY_INFO;
}
