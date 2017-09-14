package io.virtualapp.home.models;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

import com.lody.virtual.remote.InstalledAppInfo;
import com.lody.virtual.remote.vloc.VLocation;

/**
 * @see android.location.Location
 */
public class LocationData {
    public String packageName;
    public int userId;
    public String name;
    public Drawable icon;
    public int mode;
    public VLocation location;

    public LocationData() {
    }

    public LocationData(Context context, InstalledAppInfo installedAppInfo, int userId) {
        this.packageName = installedAppInfo.packageName;
        this.userId = userId;
        loadData(context, installedAppInfo.getApplicationInfo(installedAppInfo.getInstalledUsers()[0]));
    }

    private void loadData(Context context, ApplicationInfo appInfo) {
        if (appInfo == null) {
            return;
        }
        PackageManager pm = context.getPackageManager();
        try {
            CharSequence sequence = appInfo.loadLabel(pm);
            if (sequence != null) {
                name = sequence.toString();
            }
            icon = appInfo.loadIcon(pm);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return "LocationData{" +
                "packageName='" + packageName + '\'' +
                ", userId=" + userId +
                ", location=" + location +
                '}';
    }
}
