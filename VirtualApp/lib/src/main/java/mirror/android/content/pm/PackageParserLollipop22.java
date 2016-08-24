package mirror.android.content.pm;

import android.content.pm.*;
import android.content.pm.PackageParser;

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

public class PackageParserLollipop22 {
    public static Class<?> Class = ClassDef.init(PackageParserLollipop22.class, "android.content.pm.PackageParser");
    @MethodReflectionInfo({"android.content.pm.PackageParser$Package", "int"})
    public static MethodDef<Void> collectCertificates;
    public static CtorDef<PackageParser> ctor;
    @MethodReflectionInfo({"android.content.pm.PackageParser$Activity", "int", "android.content.pm.PackageUserState", "int"})
    public static StaticMethodDef<ActivityInfo> generateActivityInfo;
    @MethodReflectionInfo({"android.content.pm.PackageParser$Package", "int", "android.content.pm.PackageUserState"})
    public static StaticMethodDef<ApplicationInfo> generateApplicationInfo;
    @MethodReflectionInfo({"android.content.pm.PackageParser$Package", "[I", "int", "long", "long", "android.util.ArraySet", "android.content.pm.PackageUserState"})
    public static StaticMethodDef<PackageInfo> generatePackageInfo;
    @MethodReflectionInfo({"android.content.pm.PackageParser$Provider", "int", "android.content.pm.PackageUserState", "int"})
    public static StaticMethodDef<ProviderInfo> generateProviderInfo;
    @MethodReflectionInfo({"android.content.pm.PackageParser$Service", "int", "android.content.pm.PackageUserState", "int"})
    public static StaticMethodDef<ServiceInfo> generateServiceInfo;
    @MethodInfo({File.class, int.class})
    public static MethodDef<PackageParser.Package> parsePackage;
}
