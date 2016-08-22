package mirror.android.content.pm;

import android.content.pm.*;
import android.content.pm.PackageParser;
import android.util.DisplayMetrics;

import java.io.File;

import mirror.ClassDef;
import mirror.CtorDef;
import mirror.MethodDef;
import mirror.MethodInfo;
import mirror.MethodReflectionInfo;
import mirror.StaticMethodDef;

/**
 * @author Lody
 */

public class PackageParserJellyBean {
    public static Class<?> Class = ClassDef.init(PackageParserJellyBean.class, "android.content.pm.PackageParser");
    @MethodReflectionInfo({"android.content.pm.PackageParser$Package", "int"})
    public static MethodDef<Void> collectCertificates;
    @MethodInfo({String.class})
    public static CtorDef<PackageParser> ctor;
    @MethodReflectionInfo({"android.content.pm.PackageParser$Activity", "int", "boolean", "int", "int"})
    public static StaticMethodDef<ActivityInfo> generateActivityInfo;
    @MethodReflectionInfo({"android.content.pm.PackageParser$Package", "int", "boolean", "int"})
    public static StaticMethodDef<ApplicationInfo> generateApplicationInfo;
    @MethodReflectionInfo({"android.content.pm.PackageParser$Package", "[I", "int", "long", "long", "java.util.HashSet"})
    public static StaticMethodDef<PackageInfo> generatePackageInfo;
    @MethodReflectionInfo({"android.content.pm.PackageParser$Provider", "int", "boolean", "int", "int"})
    public static StaticMethodDef<ProviderInfo> generateProviderInfo;
    @MethodReflectionInfo({"android.content.pm.PackageParser$Service", "int", "boolean", "int", "int"})
    public static StaticMethodDef<ServiceInfo> generateServiceInfo;
    @MethodInfo({File.class, String.class, DisplayMetrics.class, int.class})
    public static MethodDef<PackageParser.Package> parsePackage;
}
