package mirror.android.app;


import android.content.Context;
import android.content.pm.PackageManager;

import mirror.ClassDef;
import mirror.FieldDef;
import mirror.MethodInfo;

public class ContextImpl {
    public static Class<?> Class = ClassDef.init(ContextImpl.class, "android.app.ContextImpl");
    @MethodInfo({Context.class})
    public static FieldDef<String> mBasePackageName;
    public static FieldDef<android.app.LoadedApk> mPackageInfo;
    public static FieldDef<PackageManager> mPackageManager;
}
