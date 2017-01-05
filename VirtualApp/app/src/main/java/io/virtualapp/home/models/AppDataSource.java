package io.virtualapp.home.models;

import java.io.File;
import java.util.List;

import org.jdeferred.Promise;

import android.content.Context;

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
	 * @param context
	 *            Context
	 * @return All the Applications we Installed.
	 */
	Promise<List<AppModel>, Throwable, Void> getInstalledApps(Context context);

	Promise<List<AppModel>, Throwable, Void> getStorageApps(Context context, File rootDir);

	void addVirtualApp(AppModel app) throws Throwable;

	void removeVirtualApp(AppModel app) throws Throwable;
}
