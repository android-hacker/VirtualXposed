package com.lody.virtual.server.pm;

import android.content.Intent;

import com.lody.virtual.client.env.Constants;
import com.lody.virtual.client.stub.VASettings;
import com.lody.virtual.os.VUserHandle;
import com.lody.virtual.server.am.VActivityManagerService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Lody
 */

public class PrivilegeAppOptimizer {

    private static final PrivilegeAppOptimizer sInstance = new PrivilegeAppOptimizer();
    private final List<String> privilegeApps = new ArrayList<>();

    private PrivilegeAppOptimizer() {
        Collections.addAll(privilegeApps, VASettings.PRIVILEGE_APPS);
    }

    public static PrivilegeAppOptimizer get() {
        return sInstance;
    }

    public List<String> getPrivilegeApps() {
        return Collections.unmodifiableList(privilegeApps);
    }

    public void addPrivilegeApp(String packageName) {
        privilegeApps.add(packageName);
    }

    public void removePrivilegeApp(String packageName) {
        privilegeApps.remove(packageName);
    }

    public boolean isPrivilegeApp(String packageName) {
        return privilegeApps.contains(packageName);
    }

    public void performOptimizeAllApps() {
        for (String pkg : privilegeApps) {
            performOptimize(pkg, VUserHandle.USER_ALL);
        }
    }

    public boolean performOptimize(String packageName, int userId) {
        VActivityManagerService.get().sendBroadcastAsUser(
                specifyApp(new Intent(Intent.ACTION_BOOT_COMPLETED), packageName, userId)
                , new VUserHandle(userId));
        return true;
    }

    public static void notifyBootFinish() {
        for (String pkg : Constants.PRIVILEGE_APP) {
            try {
                PrivilegeAppOptimizer.get().performOptimize(pkg, 0);
            } catch (Throwable ignored) {
            }
        }
    }

    private Intent specifyApp(Intent intent, String packageName, int userId) {
        intent.putExtra("_VA_|_privilege_pkg_", packageName);
        intent.putExtra("_VA_|_user_id_", userId);
        intent.putExtra("_VA_|_intent_", new Intent(Intent.ACTION_BOOT_COMPLETED));
        return intent;
    }

}
