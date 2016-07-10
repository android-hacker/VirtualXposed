package com.lody.virtual.helper.compat;

import java.lang.reflect.Constructor;
import java.util.List;

import com.lody.virtual.helper.utils.Reflect;

import android.app.LoadedApk;
import android.content.ComponentName;
import android.content.pm.ApplicationInfo;
import android.content.pm.ProviderInfo;
import android.content.res.CompatibilityInfo;
import android.content.res.Configuration;

/**
 * @author Lody
 *
 */
public class AppBindDataCompat {
	private Reflect appBindDataRef;

	public AppBindDataCompat() {
		try {
			Class<?> clazz = Class.forName("android.app.ActivityThread$AppBindData");
			Constructor<?> ctor = clazz.getDeclaredConstructor();
			ctor.setAccessible(true);
			appBindDataRef = Reflect.on(ctor.newInstance());
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	public AppBindDataCompat(Object real) {
		appBindDataRef = Reflect.on(real);
	}

	public LoadedApk getInfo() {
		return appBindDataRef.get("info");
	}

	public void setInfo(LoadedApk loadedApk) {
		appBindDataRef.set("info", loadedApk);
	}

	public String getProcessName() {
		return appBindDataRef.get("processName");
	}

	public void setProcessName(String processName) {
		appBindDataRef.set("processName", processName);
	}

	public ApplicationInfo getAppInfo() {
		return appBindDataRef.get("appInfo");
	}

	public void setAppInfo(ApplicationInfo appInfo) {
		appBindDataRef.set("appInfo", appInfo);
	}

	public List<ProviderInfo> getProviders() {
		return appBindDataRef.get("providers");
	}

	public void setProviders(List<ProviderInfo> providers) {
		appBindDataRef.set("providers", providers);
	}

	public ComponentName getInstrumentationName() {
		return appBindDataRef.get("instrumentationName");
	}

	public void setInstrumentationName(ComponentName componentName) {
		appBindDataRef.set("instrumentationName", componentName);
	}

	public Configuration getConfig() {
		return appBindDataRef.get("config");
	}

	public void setConfig(Configuration config) {
		appBindDataRef.set("config", config);
	}

	public CompatibilityInfo getCompatInfo() {
		return appBindDataRef.get("compatInfo");
	}

	public void setCompatInfo(CompatibilityInfo compatInfo) {
		appBindDataRef.set("compatInfo", compatInfo);
	}

	public Object get() {
		return appBindDataRef.get();
	}

}
