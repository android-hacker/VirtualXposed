package mirror.android.content.pm;

import mirror.MethodReflectParams;
import mirror.RefClass;
import mirror.RefStaticMethod;

/**
 * @author weishu
 * @date 2018/6/7.
 */

public class PackageParserP28 {
    public static Class<?> TYPE = RefClass.load(PackageParserP28.class, "android.content.pm.PackageParser");
    @MethodReflectParams({"android.content.pm.PackageParser$Package", "boolean"})
    public static RefStaticMethod<Void> collectCertificates;
}
