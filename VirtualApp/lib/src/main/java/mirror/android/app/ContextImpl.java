package mirror.android.app;


import android.content.Context;
import android.content.pm.PackageManager;

import mirror.RefClass;
import mirror.RefMethod;
import mirror.RefObject;
import mirror.MethodParams;

public class ContextImpl {
    public static Class<?> TYPE = RefClass.load(ContextImpl.class, "android.app.ContextImpl");
    @MethodParams({Context.class})
    public static RefObject<String> mBasePackageName;
    public static RefObject<Object> mPackageInfo;
    public static RefObject<PackageManager> mPackageManager;

    public static RefMethod<Context> getReceiverRestrictedContext;
}
