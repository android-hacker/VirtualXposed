package mirror.android.ddm;

import mirror.ClassDef;
import mirror.MethodInfo;
import mirror.StaticMethodDef;

public class DdmHandleAppNameJBMR1 {
    public static Class Class = ClassDef.init(DdmHandleAppNameJBMR1.class, "android.ddm.DdmHandleAppName");
    @MethodInfo({String.class, int.class})
    public static StaticMethodDef<Void> setAppName;
}