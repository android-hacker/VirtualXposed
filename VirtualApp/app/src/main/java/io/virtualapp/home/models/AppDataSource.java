package io.virtualapp.home.models;

import android.content.Context;

import com.lody.virtual.remote.InstallResult;

import org.jdeferred.Promise;

import java.io.File;
import java.util.List;

/**
 * @author Lody
 * @version 1.0
 */
public interface AppDataSource {

    /**
     * @return All the Applications we Virtual.
     */
    Promise<List<AppData>, Throwable, Void> getVirtualApps();

    /**
     * @param context Context
     * @return All the Applications we Installed.
     */
    Promise<List<AppData>, Throwable, Void> getInstalledApps(Context context);

    Promise<List<AppData>, Throwable, Void> getStorageApps(Context context, File rootDir);

    InstallResult addVirtualApp(PackageAppData app);

    boolean removeVirtualApp(PackageAppData app);
}
