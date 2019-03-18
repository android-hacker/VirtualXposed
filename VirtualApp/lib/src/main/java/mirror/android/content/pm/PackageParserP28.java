package mirror.android.content.pm;

import android.content.pm.PackageManager;

import mirror.MethodParams;
import mirror.MethodReflectParams;
import mirror.RefClass;
import mirror.RefConstructor;
import mirror.RefMethod;
import mirror.RefStaticMethod;

/**
 * @author weishu
 * @date 2018/6/7.
 */

public class PackageParserP28 {
    public static Class<?> TYPE = RefClass.load(PackageParserP28.class, "android.content.pm.PackageParser");
    @MethodReflectParams({"android.content.pm.PackageParser$Package", "boolean"})
    public static RefStaticMethod<Void> collectCertificates;

    @MethodReflectParams({"android.content.pm.PackageParser$Callback"})
    public static RefMethod<Void> setCallback;

    public static class CallbackImpl {
        public static Class<?> CLASS = RefClass.load(PackageParserP28.CallbackImpl.class, "android.content.pm.PackageParser$CallbackImpl");

        @MethodParams({PackageManager.class})
        public static RefConstructor<Object> ctor;
    }
}
