package mirror.android.app;


import android.content.Context;

import mirror.ClassDef;
import mirror.FieldDef;
import mirror.MethodDef;
import mirror.MethodInfo;

public class ContextImpl {
    public static Class<?> Class = ClassDef.init(ContextImpl.class, "android.app.ContextImpl");
    @MethodInfo({Context.class})
    public static MethodDef getReceiverRestrictedContext;
    public static FieldDef mBasePackageName;
    public static FieldDef mPackageInfo;
    public static FieldDef mPackageManager;
    @MethodInfo({Context.class})
    public static MethodDef setOuterContext;
}
