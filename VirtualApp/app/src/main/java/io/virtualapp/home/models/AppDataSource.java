package io.virtualapp.home.models;

import android.content.Context;

import org.jdeferred.Promise;

import java.util.List;

/**
 * @author Lody
 * @version 1.0
 */
public interface AppDataSource {

    /**
     * @return All the Applications we Virtual.
     */
    Promise<List<AppModel>, Throwable, Void> getVirtualApps();

    /**
     * @param context Context
     * @return All the Applications we Installed.
     */
    Promise<List<AppModel>, Throwable, Void> getInstalledApps(Context context);

    Promise<List<AppModel>, Throwable, Void> getSdCardApps(Context context);

    void addVirtualApp(AppModel app) throws Throwable;

    void removeVirtualApp(AppModel app) throws Throwable;
}
