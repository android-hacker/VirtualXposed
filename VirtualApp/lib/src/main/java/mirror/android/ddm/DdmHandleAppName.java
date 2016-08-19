package mirror.android.ddm;

import mirror.ClassDef;
import mirror.MethodInfo;
import mirror.StaticMethodDef;

public class DdmHandleAppName {
    public static Class Class = ClassDef.init(DdmHandleAppName.class, "android.ddm.DdmHandleAppName");
    @MethodInfo({String.class})
    public static StaticMethodDef setAppName;
}