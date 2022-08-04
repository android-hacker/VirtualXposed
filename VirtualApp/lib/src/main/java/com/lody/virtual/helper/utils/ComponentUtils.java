package com.lody.virtual.helper.utils;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.ComponentInfo;
import android.os.IBinder;
import android.os.Parcelable;

import com.lody.virtual.GmsSupport;
import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.env.SpecialComponentList;
import com.lody.virtual.client.ipc.VActivityManager;
import com.lody.virtual.client.stub.StubPendingActivity;
import com.lody.virtual.client.stub.StubPendingReceiver;
import com.lody.virtual.client.stub.StubPendingService;
import com.lody.virtual.helper.compat.ObjectsCompat;
import com.lody.virtual.os.VUserHandle;

import static android.content.pm.ActivityInfo.LAUNCH_SINGLE_INSTANCE;

/**
 * @author Lody
 */
public class ComponentUtils {

    public static String getTaskAffinity(ActivityInfo info) {
        if (info.launchMode == LAUNCH_SINGLE_INSTANCE) {
            return "-SingleInstance-" + info.packageName + "/" + info.name;
        } else if (info.taskAffinity == null && info.applicationInfo.taskAffinity == null) {
            return info.packageName;
        } else if (info.taskAffinity != null) {
            return info.taskAffinity;
        }
        return info.applicationInfo.taskAffinity;
    }

    public static boolean isSameIntent(Intent a, Intent b) {
        if (a != null && b != null) {
            if (!ObjectsCompat.equals(a.getAction(), b.getAction())) {
                return false;
            }
            if (!ObjectsCompat.equals(a.getData(), b.getData())) {
                return false;
            }
            if (!ObjectsCompat.equals(a.getType(), b.getType())) {
                return false;
            }
            Object pkgA = a.getPackage();
            if (pkgA == null && a.getComponent() != null) {
                pkgA = a.getComponent().getPackageName();
            }
            String pkgB = b.getPackage();
            if (pkgB == null && b.getComponent() != null) {
                pkgB = b.getComponent().getPackageName();
            }
            if (!ObjectsCompat.equals(pkgA, pkgB)) {
                return false;
            }
            if (!ObjectsCompat.equals(a.getComponent(), b.getComponent())) {
                return false;
            }
            if (!ObjectsCompat.equals(a.getCategories(), b.getCategories())) {
                return false;
            }
        }
        return true;
    }

    public static String getProcessName(ComponentInfo componentInfo) {
        String processName = componentInfo.processName;
        if (processName == null) {
            processName = componentInfo.packageName;
            componentInfo.processName = processName;
        }
        return processName;
    }

    public static boolean isSameComponent(ComponentInfo first, ComponentInfo second) {

        if (first != null && second != null) {
            String pkg1 = first.packageName + "";
            String pkg2 = second.packageName + "";
            String name1 = first.name + "";
            String name2 = second.name + "";
            return pkg1.equals(pkg2) && name1.equals(name2);
        }
        return false;
    }

    public static ComponentName toComponentName(ComponentInfo componentInfo) {
        return new ComponentName(componentInfo.packageName, componentInfo.name);
    }

    public static boolean isSystemApp(ApplicationInfo applicationInfo) {
        return !GmsSupport.isGmsFamilyPackage(applicationInfo.packageName)
                && ((ApplicationInfo.FLAG_SYSTEM & applicationInfo.flags) != 0
                || SpecialComponentList.isSpecSystemPackage(applicationInfo.packageName));
    }

    public static boolean isStubComponent(Intent intent) {
        return intent != null
                && intent.getComponent() != null
                && VirtualCore.get().getHostPkg().equals(intent.getComponent().getPackageName());
    }

    public static Intent redirectBroadcastIntent(Intent intent, int userId) {
        Intent newIntent = intent.cloneFilter();
        newIntent.setComponent(null);
        newIntent.setPackage(null);
        ComponentName component = intent.getComponent();
        String pkg = intent.getPackage();
        if (component != null) {
            newIntent.putExtra("_VA_|_user_id_", userId);
            newIntent.setAction(String.format("_VA_%s_%s", component.getPackageName(), component.getClassName()));
            newIntent.putExtra("_VA_|_component_", component);
            newIntent.putExtra("_VA_|_intent_", new Intent(intent));
        } else if (pkg != null) {
            newIntent.putExtra("_VA_|_user_id_", userId);
            newIntent.putExtra("_VA_|_creator_", pkg);
            newIntent.putExtra("_VA_|_intent_", new Intent(intent));
            String protectedAction = SpecialComponentList.protectAction(intent.getAction());
            if (protectedAction != null) {
                newIntent.setAction(protectedAction);
            }
        } else {
            newIntent.putExtra("_VA_|_user_id_", userId);
            newIntent.putExtra("_VA_|_intent_", new Intent(intent));
            String protectedAction = SpecialComponentList.protectAction(intent.getAction());
            if (protectedAction != null) {
                newIntent.setAction(protectedAction);
            }
        }
        return newIntent;
    }

    public static Intent redirectIntentSender(int type, String creator, Intent intent, IBinder iBinder) {
        Intent cloneFilter = intent.cloneFilter();
        switch (type) {
            case 1:
                cloneFilter.setClass(VirtualCore.get().getContext(), StubPendingReceiver.class);
                break;
            case 2:
                if (VirtualCore.get().resolveActivityInfo(intent, VUserHandle.myUserId()) != null) {
                    cloneFilter.setClass(VirtualCore.get().getContext(), StubPendingActivity.class);
                    cloneFilter.setFlags(intent.getFlags());
                    if (iBinder != null) {
                        try {
                            Parcelable activityForToken = VActivityManager.get().getActivityForToken(iBinder);
                            if (activityForToken != null) {
                                cloneFilter.putExtra("_VA_|_caller_", activityForToken);
                                break;
                            }
                        } catch (Throwable th) {
                            break;
                        }
                    }
                }
                break;
            case 4:
                if (VirtualCore.get().resolveServiceInfo(intent, VUserHandle.myUserId()) != null) {
                    cloneFilter.setClass(VirtualCore.get().getContext(), StubPendingService.class);
                    break;
                }
                break;
            default:
                return null;
        }
        cloneFilter.putExtra("_VA_|_user_id_", VUserHandle.myUserId());
        cloneFilter.putExtra("_VA_|_intent_", intent);
        cloneFilter.putExtra("_VA_|_creator_", creator);
        cloneFilter.putExtra("_VA_|_from_inner_", true);
        return cloneFilter;
    }
}
