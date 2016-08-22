package mirror.android.content.pm;

import android.content.ComponentName;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PermissionGroupInfo;
import android.content.pm.PermissionInfo;
import android.content.pm.ProviderInfo;
import android.content.pm.ServiceInfo;
import android.content.pm.Signature;
import android.os.Bundle;
import android.util.DisplayMetrics;

import java.io.File;
import java.util.List;

import mirror.ClassDef;
import mirror.CtorDef;
import mirror.FieldDef;
import mirror.MethodDef;
import mirror.MethodInfo;
import mirror.MethodReflectionInfo;
import mirror.StaticMethodDef;

/**
 * @author Lody
 */

public class PackageParser {

    public static Class<?> Class = ClassDef.init(PackageParser.class, "android.content.pm.PackageParser");
    @MethodReflectionInfo({"android.content.pm.PackageParser$Package", "int"})
    public static MethodDef<Void> collectCertificates;
    @MethodInfo({String.class})
    public static CtorDef<android.content.pm.PackageParser> ctor;
    @MethodReflectionInfo({"android.content.pm.PackageParser$Activity", "int"})
    public static StaticMethodDef<ActivityInfo> generateActivityInfo;
    @MethodReflectionInfo({"android.content.pm.PackageParser$Package", "int"})
    public static StaticMethodDef<ApplicationInfo> generateApplicationInfo;
    @MethodReflectionInfo({"android.content.pm.PackageParser$Package", "[I", "int", "long", "long"})
    public static StaticMethodDef<PackageInfo> generatePackageInfo;
    @MethodReflectionInfo({"android.content.pm.PackageParser$Provider", "int"})
    public static StaticMethodDef<ProviderInfo> generateProviderInfo;
    @MethodReflectionInfo({"android.content.pm.PackageParser$Service", "int"})
    public static StaticMethodDef<ServiceInfo> generateServiceInfo;
    @MethodInfo({File.class, String.class, DisplayMetrics.class, int.class})
    public static MethodDef<android.content.pm.PackageParser.Package> parsePackage;
    
    public static class Package {
        public static Class<?> Class = ClassDef.init(Package.class, "android.content.pm.PackageParser$Package");
        public static FieldDef<List> activities;
        public static FieldDef<Bundle> mAppMetaData;
        public static FieldDef<String> mSharedUserId;
        public static FieldDef<Signature[]> mSignatures;
        public static FieldDef<Integer> mVersionCode;
        public static FieldDef<String> packageName;
        public static FieldDef<List> permissionGroups;
        public static FieldDef<List> permissions;
        public static FieldDef<List<String>> protectedBroadcasts;
        public static FieldDef<List> providers;
        public static FieldDef<List> receivers;
        public static FieldDef<List<String>> requestedPermissions;
        public static FieldDef<List> services;
    }

    public static class Activity {
        public static Class<?> Class = ClassDef.init(Activity.class, "android.content.pm.PackageParser$Activity");
        public static FieldDef<ActivityInfo> info;
    }

    public static class Provider {
        public static Class<?> Class = ClassDef.init(Provider.class, "android.content.pm.PackageParser$Provider");
        public static FieldDef<ProviderInfo> info;
    }

    public static class Service {
        public static Class<?> Class = ClassDef.init(Provider.class, "android.content.pm.PackageParser$Service");
        public static FieldDef<ServiceInfo> info;
    }




    public static class Permission {
        public static Class<?> Class = ClassDef.init(Permission.class, "android.content.pm.PackageParser$Permission");
        public static FieldDef<PermissionInfo> info;
    }

    public static class PermissionGroup {
        public static Class<?> Class = ClassDef.init(PermissionGroup.class, "android.content.pm.PackageParser$PermissionGroup");
        public static FieldDef<PermissionGroupInfo> info;
    }

    public static class Component {
        public static Class<?> Class = ClassDef.init(Component.class, "android.content.pm.PackageParser$Component");
        public static FieldDef<String> className;
        public static FieldDef<ComponentName> componentName;
        public static FieldDef<List<IntentFilter>> intents;
    }
}
