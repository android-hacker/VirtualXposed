package com.lody.virtual.client.hook.proxies.pm;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageDataObserver;
import android.content.pm.IPackageDeleteObserver2;
import android.content.pm.IPackageInstallerCallback;
import android.content.pm.PackageInfo;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;
import android.content.pm.PermissionGroupInfo;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.pm.Signature;
import android.graphics.Bitmap;
import android.os.Binder;
import android.os.Build;
import android.os.IInterface;
import android.os.Process;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.hook.base.MethodProxy;
import com.lody.virtual.client.hook.utils.MethodParameterUtils;
import com.lody.virtual.client.ipc.VPackageManager;
import com.lody.virtual.helper.collection.ArraySet;
import com.lody.virtual.helper.compat.ParceledListSliceCompat;
import com.lody.virtual.helper.utils.ArrayUtils;
import com.lody.virtual.os.VUserHandle;
import com.lody.virtual.server.IPackageInstaller;
import com.lody.virtual.server.pm.installer.SessionInfo;
import com.lody.virtual.server.pm.installer.SessionParams;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import mirror.android.content.pm.ParceledListSlice;

/**
 * @author Lody
 */
@SuppressWarnings("unused")
class MethodProxies {

    static class IsPackageAvailable extends MethodProxy {

        @Override
        public String getMethodName() {
            return "isPackageAvailable";
        }

        @Override
        public Object call(Object who, Method method, Object... args) throws Throwable {
            String pkgName = (String) args[0];
            if (isAppPkg(pkgName)) {
                return true;
            }
            return method.invoke(who, args);
        }

        @Override
        public boolean isEnable() {
            return isAppProcess();
        }
    }


    static class GetInstallerPackageName extends MethodProxy {

        @Override
        public String getMethodName() {
            return "getInstallerPackageName";
        }

        @Override
        public Object call(Object who, Method method, Object... args) throws Throwable {
            return "com.android.vending";
        }

        @Override
        public boolean isEnable() {
            return isAppProcess();
        }
    }

    static class GetPreferredActivities extends MethodProxy {

        @Override
        public String getMethodName() {
            return "getPreferredActivities";
        }

        @Override
        public Object call(Object who, Method method, Object... args) throws Throwable {
            MethodParameterUtils.replaceLastAppPkg(args);
            return method.invoke(who, args);
        }
    }


    static class GetComponentEnabledSetting extends MethodProxy {

        @Override
        public String getMethodName() {
            return "getComponentEnabledSetting";
        }

        @Override
        public Object call(Object who, Method method, Object... args) throws Throwable {
            // NOTE: 有4个状态: 0默认 1可用 2禁止 3User Disable
            ComponentName component = (ComponentName) args[0];
            if (component != null) {
                return 1;
            }
            return method.invoke(who, args);
        }
    }


    static class RemovePackageFromPreferred extends MethodProxy {

        @Override
        public String getMethodName() {
            return "removePackageFromPreferred";
        }

        @Override
        public Object call(Object who, Method method, Object... args) throws Throwable {
            MethodParameterUtils.replaceFirstAppPkg(args);
            return method.invoke(who, args);
        }
    }

    /**
     * @author Lody
     *         <p>
     *         public ActivityInfo getServiceInfo(ComponentName className, int
     *         flags, int userId)
     */
    static class GetServiceInfo extends MethodProxy {

        @Override
        public String getMethodName() {
            return "getServiceInfo";
        }

        @Override
        public Object call(Object who, Method method, Object... args) throws Throwable {
            ComponentName componentName = (ComponentName) args[0];
            int flags = (int) args[1];
            int userId = VUserHandle.myUserId();
            ServiceInfo info = VPackageManager.get().getServiceInfo(componentName, flags, userId);
            if (info != null) {
                return info;
            }
            info = (ServiceInfo) method.invoke(who, args);
            if (info == null || !isVisiblePackage(info.applicationInfo)) {
                return null;
            }
            return info;
        }

        @Override
        public boolean isEnable() {
            return isAppProcess();
        }
    }


    static class GetPackageUid extends MethodProxy {

        @Override
        public String getMethodName() {
            return "getPackageUid";
        }

        @Override
        public Object call(Object who, Method method, Object... args) throws Throwable {
            String pkgName = (String) args[0];
            if (pkgName.equals(getHostPkg())) {
                return method.invoke(who, args);
            }
            int uid = VPackageManager.get().getPackageUid(pkgName, 0);
            return VUserHandle.getAppId(uid);
        }

        @Override
        public boolean isEnable() {
            return isAppProcess();
        }

    }

    /**
     * @author Lody
     *         <p>
     *         <p>
     *         public ActivityInfo getActivityInfo(ComponentName className, int
     *         flags, int userId)
     */
    static class GetActivityInfo extends MethodProxy {

        @Override
        public String getMethodName() {
            return "getActivityInfo";
        }

        @Override
        public Object call(Object who, Method method, Object... args) throws Throwable {
            ComponentName componentName = (ComponentName) args[0];
            if (getHostPkg().equals(componentName.getPackageName())) {
                return method.invoke(who, args);
            }
            int userId = VUserHandle.myUserId();
            int flags = (int) args[1];
            ActivityInfo info = VPackageManager.get().getActivityInfo(componentName, flags, userId);
            if (info == null) {
                info = (ActivityInfo) method.invoke(who, args);
                if (info == null || !isVisiblePackage(info.applicationInfo)) {
                    return null;
                }
            }
            return info;
        }

        @Override
        public boolean isEnable() {
            return isAppProcess();
        }
    }

    static class GetPackageUidEtc extends GetPackageUid {
        @Override
        public String getMethodName() {
            return super.getMethodName() + "Etc";
        }
    }

    static class GetPackageInstaller extends MethodProxy {

        @Override
        public boolean isEnable() {
            return isAppProcess();
        }

        @Override
        public String getMethodName() {
            return "getPackageInstaller";
        }

        @Override
        public Object call(final Object who, Method method, Object... args) throws Throwable {
            final IInterface installer = (IInterface) method.invoke(who, args);
            final IPackageInstaller vInstaller = VPackageManager.get().getPackageInstaller();
            return Proxy.newProxyInstance(installer.getClass().getClassLoader(), installer.getClass().getInterfaces(),
                    new InvocationHandler() {
                        @Override
                        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                            switch (method.getName()) {
                                case "createSession": {
                                    SessionParams params = SessionParams.create((PackageInstaller.SessionParams) args[0]);
                                    String installerPackageName = (String) args[1];
                                    return vInstaller.createSession(params, installerPackageName, VUserHandle.myUserId());
                                }
                                case "updateSessionAppIcon": {
                                    int sessionId = (int) args[0];
                                    Bitmap appIcon = (Bitmap) args[1];
                                    vInstaller.updateSessionAppIcon(sessionId, appIcon);
                                    return 0;
                                }
                                case "updateSessionAppLabel": {
                                    int sessionId = (int) args[0];
                                    String appLabel = (String) args[1];
                                    vInstaller.updateSessionAppLabel(sessionId, appLabel);
                                    return 0;
                                }
                                case "abandonSession": {
                                    vInstaller.abandonSession((Integer) args[0]);
                                    return 0;
                                }
                                case "openSession": {
                                    return vInstaller.openSession((Integer) args[0]);
                                }
                                case "getSessionInfo": {
                                    SessionInfo info = vInstaller.getSessionInfo((Integer) args[0]);
                                    if (info != null) {
                                        return info.alloc();
                                    }
                                    return null;
                                }
                                case "getAllSessions": {
                                    return ParceledListSliceCompat.create(
                                            vInstaller.getAllSessions((Integer) args[0]).getList()
                                    );
                                }
                                case "getMySessions": {
                                    String installerPackageName = (String) args[0];
                                    int userId = (int) args[1];
                                    return ParceledListSliceCompat.create(
                                            vInstaller.getMySessions(installerPackageName, userId).getList()
                                    );
                                }
                                case "registerCallback": {
                                    IPackageInstallerCallback callback = (IPackageInstallerCallback) args[0];
                                    vInstaller.registerCallback(callback, VUserHandle.myUserId());
                                    return 0;
                                }
                                case "unregisterCallback": {
                                    IPackageInstallerCallback callback = (IPackageInstallerCallback) args[0];
                                    vInstaller.unregisterCallback(callback);
                                    return 0;
                                }
                                case "setPermissionsResult": {
                                    int sessionId = (int) args[0];
                                    boolean accepted = (boolean) args[1];
                                    vInstaller.setPermissionsResult(sessionId, accepted);
                                    return 0;
                                }
                            }
                            throw new RuntimeException("Not support PackageInstaller method : " + method.getName());
                        }
                    });
        }
    }


    static class FreeStorageAndNotify extends MethodProxy {
        @Override
        public String getMethodName() {
            return "freeStorageAndNotify";
        }

        @Override
        public Object call(Object who, Method method, Object... args) throws Throwable {
            if (args[args.length - 1] instanceof IPackageDataObserver) {
                IPackageDataObserver observer = (IPackageDataObserver) args[args.length - 1];
                observer.onRemoveCompleted(null, true);
            }
            return 0;
        }
    }


    static class GetPackageGids extends MethodProxy {

        @Override
        public String getMethodName() {
            return "getPackageGids";
        }

        @Override
        public Object call(Object who, Method method, Object... args) throws Throwable {
            MethodParameterUtils.replaceFirstAppPkg(args);
            return method.invoke(who, args);
        }

        @Override
        public boolean isEnable() {
            return isAppProcess();
        }

    }


    static class RevokeRuntimePermission extends MethodProxy {

        @Override
        public String getMethodName() {
            return "revokeRuntimePermission";
        }

        @Override
        public Object call(Object who, Method method, Object... args) throws Throwable {
            MethodParameterUtils.replaceFirstAppPkg(args);
            return method.invoke(who, args);
        }

        @Override
        public boolean isEnable() {
            return isAppProcess();
        }
    }


    static class ClearPackagePreferredActivities extends MethodProxy {

        @Override
        public String getMethodName() {
            return "clearPackagePreferredActivities";
        }

        @Override
        public Object call(Object who, Method method, Object... args) throws Throwable {
            MethodParameterUtils.replaceFirstAppPkg(args);
            return method.invoke(who, args);
        }
    }


    static class ResolveContentProvider extends MethodProxy {

        @Override
        public String getMethodName() {
            return "resolveContentProvider";
        }

        @Override
        public Object call(Object who, Method method, Object... args) throws Throwable {
            String name = (String) args[0];
            int flags = (int) args[1];
            int userId = VUserHandle.myUserId();
            ProviderInfo info = VPackageManager.get().resolveContentProvider(name, flags, userId);
            if (info == null) {
                info = (ProviderInfo) method.invoke(who, args);
                if (info != null && isVisiblePackage(info.applicationInfo)) {
                    return info;
                }
            }
            return info;
        }
    }


    @SuppressWarnings("unchecked")
    static class QueryIntentServices extends MethodProxy {

        @Override
        public String getMethodName() {
            return "queryIntentServices";
        }

        @Override
        public Object call(Object who, Method method, Object... args) throws Throwable {
            boolean slice = ParceledListSliceCompat.isReturnParceledListSlice(method);
            int userId = VUserHandle.myUserId();
            List<ResolveInfo> appResult = VPackageManager.get().queryIntentServices((Intent) args[0],
                    (String) args[1], (Integer) args[2], userId);
            Object _hostResult = method.invoke(who, args);
            if (_hostResult != null) {
                List<ResolveInfo> hostResult = slice ? ParceledListSlice.getList.call(_hostResult)
                        : (List) _hostResult;
                if (hostResult != null) {
                    Iterator<ResolveInfo> iterator = hostResult.iterator();
                    while (iterator.hasNext()) {
                        ResolveInfo info = iterator.next();
                        if (info == null || info.serviceInfo == null || !isVisiblePackage(info.serviceInfo.applicationInfo)) {
                            iterator.remove();
                        }
                    }
                    appResult.addAll(hostResult);
                }
            }
            if (slice) {
                return ParceledListSliceCompat.create(appResult);
            }
            return appResult;
        }

        @Override
        public boolean isEnable() {
            return isAppProcess();
        }
    }


    static class GetPermissions extends MethodProxy {

        @Override
        public String getMethodName() {
            return "getPermissions";
        }

        @Override
        public Object call(Object who, Method method, Object... args) throws Throwable {
            return method.invoke(who, args);
        }
    }

    static class IsPackageForzen extends MethodProxy {

        @Override
        public String getMethodName() {
            return "isPackageForzen";
        }

        @Override
        public Object call(Object who, Method method, Object... args) throws Throwable {
            return false;
        }

        @Override
        public boolean isEnable() {
            return isAppProcess();
        }
    }


    static class GetPackageGidsEtc extends GetPackageGids {

        @Override
        public String getMethodName() {
            return super.getMethodName() + "Etc";
        }

    }

    @SuppressWarnings("unchecked")
    static class QueryIntentActivities extends MethodProxy {

        @Override
        public String getMethodName() {
            return "queryIntentActivities";
        }

        @Override
        public Object call(Object who, Method method, Object... args) throws Throwable {
            boolean slice = ParceledListSliceCompat.isReturnParceledListSlice(method);
            int userId = VUserHandle.myUserId();
            List<ResolveInfo> appResult = VPackageManager.get().queryIntentActivities((Intent) args[0],
                    (String) args[1], (Integer) args[2], userId);
            Object _hostResult = method.invoke(who, args);
            if (_hostResult != null) {
                List<ResolveInfo> hostResult = slice ? ParceledListSlice.getList.call(_hostResult)
                        : (List) _hostResult;
                if (hostResult != null) {
                    Iterator<ResolveInfo> iterator = hostResult.iterator();
                    while (iterator.hasNext()) {
                        ResolveInfo info = iterator.next();
                        if (info == null || info.activityInfo == null || !isVisiblePackage(info.activityInfo.applicationInfo)) {
                            iterator.remove();
                        }
                    }
                    appResult.addAll(hostResult);
                }
            }
            if (slice) {
                return ParceledListSliceCompat.create(appResult);
            }
            return appResult;
        }

        @Override
        public boolean isEnable() {
            return isAppProcess();
        }
    }

    static class ResolveService extends MethodProxy {

        @Override
        public String getMethodName() {
            return "resolveService";
        }

        @Override
        public Object call(Object who, Method method, Object... args) throws Throwable {
            Intent intent = (Intent) args[0];
            String resolvedType = (String) args[1];
            int flags = (int) args[2];
            int userId = VUserHandle.myUserId();
            ResolveInfo resolveInfo = VPackageManager.get().resolveService(intent, resolvedType, flags, userId);
            if (resolveInfo == null) {
                resolveInfo = (ResolveInfo) method.invoke(who, args);
            }
            return resolveInfo;
        }
    }


    static class ClearPackagePersistentPreferredActivities extends MethodProxy {

        @Override
        public String getMethodName() {
            return "clearPackagePersistentPreferredActivities";
        }

        @Override
        public Object call(Object who, Method method, Object... args) throws Throwable {
            MethodParameterUtils.replaceFirstAppPkg(args);
            return method.invoke(who, args);
        }
    }

    static class GetPermissionGroupInfo extends MethodProxy {

        @Override
        public String getMethodName() {
            return "getPermissionGroupInfo";
        }

        @Override
        public Object call(Object who, Method method, Object... args) throws Throwable {
            String name = (String) args[0];
            int flags = (int) args[1];
            PermissionGroupInfo info = VPackageManager.get().getPermissionGroupInfo(name, flags);
            if (info != null) {
                return info;
            }
            return super.call(who, method, args);
        }

        @Override
        public boolean isEnable() {
            return isAppProcess();
        }
    }


    static final class GetPackageInfo extends MethodProxy {

        @Override
        public String getMethodName() {
            return "getPackageInfo";
        }

        @Override
        public boolean beforeCall(Object who, Method method, Object... args) {
            return args != null && args[0] != null;
        }

        @Override
        public Object call(Object who, Method method, Object... args) throws Throwable {
            String pkg = (String) args[0];
            int flags = (int) args[1];
            int userId = VUserHandle.myUserId();
            PackageInfo packageInfo = VPackageManager.get().getPackageInfo(pkg, flags, userId);
            if (packageInfo != null) {
                return packageInfo;
            }
            packageInfo = (PackageInfo) method.invoke(who, args);
            if (packageInfo != null) {
                if (isVisiblePackage(packageInfo.applicationInfo)) {
                    return packageInfo;
                }
            }
            return null;
        }

    }


    static class DeleteApplicationCacheFiles extends MethodProxy {

        @Override
        public String getMethodName() {
            return "deleteApplicationCacheFiles";
        }

        @Override
        public Object call(Object who, Method method, Object... args) throws Throwable {
            // TODO
            return method.invoke(who, args);
        }
    }


    static class SetApplicationBlockedSettingAsUser extends MethodProxy {

        @Override
        public String getMethodName() {
            return "setApplicationBlockedSettingAsUser";
        }

        @Override
        public Object call(Object who, Method method, Object... args) throws Throwable {
            MethodParameterUtils.replaceFirstAppPkg(args);
            return method.invoke(who, args);
        }

        @Override
        public boolean isEnable() {
            return isAppProcess();
        }
    }


    static class GetApplicationEnabledSetting extends MethodProxy {

        @Override
        public String getMethodName() {
            return "getApplicationEnabledSetting";
        }

        @Override
        public Object call(Object who, Method method, Object... args) throws Throwable {
            MethodParameterUtils.replaceFirstAppPkg(args);
            return method.invoke(who, args);
        }
    }

    static class AddPackageToPreferred extends MethodProxy {

        @Override
        public String getMethodName() {
            return "addPackageToPreferred";
        }

        @Override
        public Object call(Object who, Method method, Object... args) throws Throwable {
            return 0;
        }
    }

    static class CheckPermission extends MethodProxy {

        @Override
        public String getMethodName() {
            return "checkPermission";
        }

        @Override
        public Object call(Object who, Method method, Object... args) throws Throwable {
            String permName = (String) args[0];
            String pkgName = (String) args[1];
            int userId = VUserHandle.myUserId();
            return VPackageManager.get().checkPermission(permName, pkgName, userId);
        }

        @Override
        public Object afterCall(Object who, Method method, Object[] args, Object result) throws Throwable {
            return super.afterCall(who, method, args, result);
        }

        @Override
        public boolean isEnable() {
            return isAppProcess();
        }
    }


    static class GetPackagesForUid extends MethodProxy {


        @Override
        public String getMethodName() {
            return "getPackagesForUid";
        }

        @Override
        public Object call(Object who, Method method, Object... args) throws Throwable {
            int uid = (int) args[0];
            int callingUid = Binder.getCallingUid();
            if (uid == VirtualCore.get().myUid()) {
                uid = getBaseVUid();
            }
            String[] callingPkgs = VPackageManager.get().getPackagesForUid(callingUid);
            String[] targetPkgs = VPackageManager.get().getPackagesForUid(uid);
            String[] selfPkgs = VPackageManager.get().getPackagesForUid(Process.myUid());

            Set<String> pkgList = new ArraySet<>(2);
            if (callingPkgs != null && callingPkgs.length > 0) {
                pkgList.addAll(Arrays.asList(callingPkgs));
            }
            if (targetPkgs != null && targetPkgs.length > 0) {
                pkgList.addAll(Arrays.asList(targetPkgs));
            }
            if (selfPkgs != null && selfPkgs.length > 0) {
                pkgList.addAll(Arrays.asList(selfPkgs));
            }
            return pkgList.toArray(new String[pkgList.size()]);
        }

        @Override
        public boolean isEnable() {
            return isAppProcess();
        }
    }


    @SuppressWarnings("unchecked")
    static class QueryContentProviders extends MethodProxy {

        @Override
        public String getMethodName() {
            return "queryContentProviders";
        }

        @Override
        public Object call(Object who, Method method, Object... args) throws Throwable {
            String processName = (String) args[0];
            int flags = (int) args[2];
            List<ProviderInfo> infos = VPackageManager.get().queryContentProviders(processName, flags, 0);
            if (ParceledListSliceCompat.isReturnParceledListSlice(method)) {
                return ParceledListSliceCompat.create(infos);
            }
            return infos;
        }

    }

    static class SetApplicationEnabledSetting extends MethodProxy {

        @Override
        public String getMethodName() {
            return "setApplicationEnabledSetting";
        }

        @Override
        public Object call(Object who, Method method, Object... args) throws Throwable {
            MethodParameterUtils.replaceFirstAppPkg(args);
            return method.invoke(who, args);
        }

        @Override
        public boolean isEnable() {
            return isAppProcess();
        }
    }

    @SuppressLint("PackageManagerGetSignatures")
    static class CheckSignatures extends MethodProxy {

        @Override
        public String getMethodName() {
            return "checkSignatures";
        }

        @Override
        public Object call(Object who, Method method, Object... args) throws Throwable {

            if (args.length == 2 && args[0] instanceof String && args[1] instanceof String) {

                PackageManager pm = VirtualCore.getPM();

                String pkgNameOne = (String) args[0], pkgNameTwo = (String) args[1];
                try {
                    PackageInfo pkgOne = pm.getPackageInfo(pkgNameOne, PackageManager.GET_SIGNATURES);
                    PackageInfo pkgTwo = pm.getPackageInfo(pkgNameTwo, PackageManager.GET_SIGNATURES);

                    Signature[] one = pkgOne.signatures;
                    Signature[] two = pkgTwo.signatures;

                    if (ArrayUtils.isEmpty(one)) {
                        if (!ArrayUtils.isEmpty(two)) {
                            return PackageManager.SIGNATURE_FIRST_NOT_SIGNED;
                        } else {
                            return PackageManager.SIGNATURE_NEITHER_SIGNED;
                        }
                    } else {
                        if (ArrayUtils.isEmpty(two)) {
                            return PackageManager.SIGNATURE_SECOND_NOT_SIGNED;
                        } else {
                            if (Arrays.equals(one, two)) {
                                return PackageManager.SIGNATURE_MATCH;
                            } else {
                                return PackageManager.SIGNATURE_NO_MATCH;
                            }
                        }
                    }
                } catch (Throwable e) {
                    // Ignore
                }
            }

            return method.invoke(who, args);
        }
    }

    static class checkUidSignatures extends MethodProxy {

        @Override
        public String getMethodName() {
            return "checkUidSignatures";
        }

        @Override
        public Object call(Object who, Method method, Object... args) throws Throwable {
            int uid1 = (int) args[0];
            int uid2 = (int) args[1];
            // TODO: verify the signatures by uid.
            return PackageManager.SIGNATURE_MATCH;
        }
    }

    static class getNameForUid extends MethodProxy {

        @Override
        public String getMethodName() {
            return "getNameForUid";
        }

        @Override
        public Object call(Object who, Method method, Object... args) throws Throwable {
            int uid = (int) args[0];
            return VPackageManager.get().getNameForUid(uid);
        }
    }


    static class DeletePackage extends MethodProxy {

        @Override
        public String getMethodName() {
            return "deletePackage";
        }

        @Override
        public Object call(Object who, Method method, Object... args) throws Throwable {
            String pkgName = (String) args[0];
            try {
                VirtualCore.get().uninstallPackage(pkgName);
                IPackageDeleteObserver2 observer = (IPackageDeleteObserver2) args[1];
                if (observer != null) {
                    observer.onPackageDeleted(pkgName, 0, "done.");
                }
            } catch (Throwable e) {
                // Ignore
            }
            return 0;
        }

    }


    static class ActivitySupportsIntent extends MethodProxy {
        @Override
        public String getMethodName() {
            return "activitySupportsIntent";
        }

        @Override
        public Object call(Object who, Method method, Object... args) throws Throwable {
            ComponentName component = (ComponentName) args[0];
            Intent intent = (Intent) args[1];
            String resolvedType = (String) args[2];
            return VPackageManager.get().activitySupportsIntent(component, intent, resolvedType);
        }
    }


    static class ResolveIntent extends MethodProxy {

        @Override
        public String getMethodName() {
            return "resolveIntent";
        }

        @Override
        public Object call(Object who, Method method, Object... args) throws Throwable {
            Intent intent = (Intent) args[0];
            String resolvedType = (String) args[1];
            int flags = (int) args[2];
            int userId = VUserHandle.myUserId();
            ResolveInfo resolveInfo = VPackageManager.get().resolveIntent(intent, resolvedType, flags, userId);
            if (resolveInfo == null) {
                resolveInfo = (ResolveInfo) method.invoke(who, args);
            }
            return resolveInfo;
        }
    }


    static class GetApplicationInfo extends MethodProxy {

        @Override
        public String getMethodName() {
            return "getApplicationInfo";
        }

        @Override
        public Object call(Object who, Method method, Object... args) throws Throwable {
            String pkg = (String) args[0];
            int flags = (int) args[1];
            if (getHostPkg().equals(pkg)) {
                return method.invoke(who, args);
            }
            int userId = VUserHandle.myUserId();
            ApplicationInfo info = VPackageManager.get().getApplicationInfo(pkg, flags, userId);
            if (info != null) {
                return info;
            }
            info = (ApplicationInfo) method.invoke(who, args);
            if (info == null || !isVisiblePackage(info)) {
                return null;
            }
            return info;
        }

        @Override
        public boolean isEnable() {
            return isAppProcess();
        }
    }


    static class GetProviderInfo extends MethodProxy {

        @Override
        public String getMethodName() {
            return "getProviderInfo";
        }

        @Override
        public Object call(Object who, Method method, Object... args) throws Throwable {
            ComponentName componentName = (ComponentName) args[0];
            int flags = (int) args[1];
            if (getHostPkg().equals(componentName.getPackageName())) {
                return method.invoke(who, args);
            }
            int userId = VUserHandle.myUserId();
            ProviderInfo info = VPackageManager.get().getProviderInfo(componentName, flags, userId);
            if (info == null) {
                info = (ProviderInfo) method.invoke(who, args);
                if (info == null || !isVisiblePackage(info.applicationInfo)) {
                    return null;
                }
            }
            return info;
        }

    }

    static class SetComponentEnabledSetting extends MethodProxy {

        @Override
        public String getMethodName() {
            return "setComponentEnabledSetting";
        }

        @Override
        public Object call(Object who, Method method, Object... args) throws Throwable {
            return 0;
        }

        @Override
        public boolean isEnable() {
            return isAppProcess();
        }
    }

    static class GetInstalledApplications extends MethodProxy {

        @Override
        public String getMethodName() {
            return "getInstalledApplications";
        }

        @Override
        public Object call(Object who, Method method, Object... args) throws Throwable {

            int flags = (Integer) args[0];
            int userId = VUserHandle.myUserId();
            List<ApplicationInfo> appInfos = VPackageManager.get().getInstalledApplications(flags, userId);
            if (ParceledListSliceCompat.isReturnParceledListSlice(method)) {
                return ParceledListSliceCompat.create(appInfos);
            }
            return appInfos;
        }
    }

    @SuppressWarnings({"unchecked", "WrongConstant"})
    static class GetInstalledPackages extends MethodProxy {

        @Override
        public String getMethodName() {
            return "getInstalledPackages";
        }

        @Override
        public Object call(Object who, Method method, Object... args) throws Throwable {
            int flags = (int) args[0];
            int userId = VUserHandle.myUserId();
            List<PackageInfo> packageInfos;
            if (isAppProcess()) {
                packageInfos = new ArrayList<>(VirtualCore.get().getInstalledAppCount());
            } else {
                packageInfos = VirtualCore.get().getUnHookPackageManager().getInstalledPackages(flags);
            }
            packageInfos.addAll(VPackageManager.get().getInstalledPackages(flags, userId));
            if (ParceledListSliceCompat.isReturnParceledListSlice(method)) {
                return ParceledListSliceCompat.create(packageInfos);
            } else {
                return packageInfos;
            }
        }
    }

    @SuppressWarnings("unchecked")
    static class QueryIntentReceivers extends MethodProxy {

        @Override
        public String getMethodName() {
            return "queryIntentReceivers";
        }

        @Override
        public Object call(Object who, Method method, Object... args) throws Throwable {
            boolean slice = ParceledListSliceCompat.isReturnParceledListSlice(method);
            int userId = VUserHandle.myUserId();
            List<ResolveInfo> appResult = VPackageManager.get().queryIntentReceivers((Intent) args[0], (String) args[1],
                    (Integer) args[2], userId);
            Object _hostResult = method.invoke(who, args);
            List<ResolveInfo> hostResult = slice ? ParceledListSlice.getList.call(_hostResult)
                    : (List) _hostResult;
            if (hostResult != null) {
                Iterator<ResolveInfo> iterator = hostResult.iterator();
                while (iterator.hasNext()) {
                    ResolveInfo info = iterator.next();
                    if (info == null || info.activityInfo == null || !isVisiblePackage(info.activityInfo.applicationInfo)) {
                        iterator.remove();
                    }
                }
                appResult.addAll(hostResult);
            }
            if (slice) {
                return ParceledListSliceCompat.create(appResult);
            }
            return appResult;
        }
    }


    static class GetReceiverInfo extends MethodProxy {

        @Override
        public String getMethodName() {
            return "getReceiverInfo";
        }

        @Override
        public Object call(Object who, Method method, Object... args) throws Throwable {
            ComponentName componentName = (ComponentName) args[0];
            if (getHostPkg().equals(componentName.getPackageName())) {
                return method.invoke(who, args);
            }
            int flags = (int) args[1];
            ActivityInfo info = VPackageManager.get().getReceiverInfo(componentName, flags, 0);
            if (info == null) {
                info = (ActivityInfo) method.invoke(who, args);
                if (info == null || !isVisiblePackage(info.applicationInfo)) {
                    return null;
                }
            }
            return info;
        }

        @Override
        public boolean isEnable() {
            return isAppProcess();
        }
    }


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    static class GetPermissionFlags extends MethodProxy {

        @Override
        public String getMethodName() {
            return "getPermissionFlags";
        }

        @Override
        public Object call(Object who, Method method, Object... args) throws Throwable {
            // TODO
            return method.invoke(who, args);
        }

    }


    static class SetPackageStoppedState extends MethodProxy {

        @Override
        public String getMethodName() {
            return "setPackageStoppedState";
        }

        @Override
        public Object call(Object who, Method method, Object... args) throws Throwable {
            MethodParameterUtils.replaceFirstAppPkg(args);
            return method.invoke(who, args);
        }

        @Override
        public boolean isEnable() {
            return isAppProcess();
        }
    }


    @SuppressWarnings("unchecked")
    @TargetApi(Build.VERSION_CODES.KITKAT)
    static class QueryIntentContentProviders extends MethodProxy {

        @Override
        public String getMethodName() {
            return "queryIntentContentProviders";
        }

        @Override
        public Object call(Object who, Method method, Object... args) throws Throwable {
            boolean slice = ParceledListSliceCompat.isReturnParceledListSlice(method);
            int userId = VUserHandle.myUserId();
            List<ResolveInfo> appResult = VPackageManager.get().queryIntentContentProviders((Intent) args[0], (String) args[1],
                    (Integer) args[2], userId);
            Object _hostResult = method.invoke(who, args);
            List<ResolveInfo> hostResult = slice ? ParceledListSlice.getList.call(_hostResult)
                    : (List) _hostResult;
            if (hostResult != null) {
                Iterator<ResolveInfo> iterator = hostResult.iterator();
                while (iterator.hasNext()) {
                    ResolveInfo info = iterator.next();
                    if (info == null || info.providerInfo == null || !isVisiblePackage(info.providerInfo.applicationInfo)) {
                        iterator.remove();
                    }
                }
                appResult.addAll(hostResult);
            }
            if (ParceledListSliceCompat.isReturnParceledListSlice(method)) {
                return ParceledListSliceCompat.create(appResult);
            }
            return appResult;
        }

        @Override
        public boolean isEnable() {
            return isAppProcess();
        }
    }


    static class GetApplicationBlockedSettingAsUser extends MethodProxy {

        @Override
        public String getMethodName() {
            return "getApplicationBlockedSettingAsUser";
        }

        @Override
        public Object call(Object who, Method method, Object... args) throws Throwable {
            MethodParameterUtils.replaceFirstAppPkg(args);
            return method.invoke(who, args);
        }
    }

}
