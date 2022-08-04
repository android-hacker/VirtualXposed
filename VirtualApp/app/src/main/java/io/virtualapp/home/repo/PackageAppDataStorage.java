package io.virtualapp.home.repo;

import android.content.pm.ApplicationInfo;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.remote.InstalledAppInfo;

import java.util.HashMap;
import java.util.Map;

import io.virtualapp.XApp;
import io.virtualapp.abs.Callback;
import io.virtualapp.abs.ui.VUiKit;
import io.virtualapp.home.models.PackageAppData;

/**
 * @author Lody
 *         <p>
 *         Cache the loaded PackageAppData.
 */
public class PackageAppDataStorage {

    private static final PackageAppDataStorage STORAGE = new PackageAppDataStorage();
    private final Map<String, PackageAppData> packageDataMap = new HashMap<>();

    public static PackageAppDataStorage get() {
        return STORAGE;
    }

    public PackageAppData acquire(String packageName) {
        PackageAppData data;
        synchronized (packageDataMap) {
            data = packageDataMap.get(packageName);
            if (data == null) {
                data = loadAppData(packageName);
            }
        }
        return data;
    }

    public void acquire(String packageName, Callback<PackageAppData> callback) {
        VUiKit.defer()
                .when(() -> acquire(packageName))
                .done(callback::callback);
    }

    private PackageAppData loadAppData(String packageName) {
        InstalledAppInfo setting = VirtualCore.get().getInstalledAppInfo(packageName, 0);
        if (setting != null) {
            PackageAppData data = new PackageAppData(XApp.getApp(), setting);
            synchronized (packageDataMap) {
                packageDataMap.put(packageName, data);
            }
            return data;
        }
        return null;
    }

    public PackageAppData acquire(ApplicationInfo appInfo) {
        PackageAppData data;
        synchronized (packageDataMap) {
            data = packageDataMap.get(appInfo.packageName);
            if (data == null) {
                data = loadAppData(appInfo);
            }
        }
        return data;
    }

    public void acquire(ApplicationInfo appInfo, Callback<PackageAppData> callback) {
        VUiKit.defer()
                .when(() -> acquire(appInfo))
                .done(callback::callback);
    }

    private PackageAppData loadAppData(ApplicationInfo appInfo) {
        PackageAppData data = new PackageAppData(XApp.getApp(), appInfo);
        synchronized (packageDataMap) {
            packageDataMap.put(appInfo.packageName, data);
        }
        return data;
    }

}
