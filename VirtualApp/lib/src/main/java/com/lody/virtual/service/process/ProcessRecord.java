package com.lody.virtual.service.process;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.lody.virtual.client.IVClient;
import com.lody.virtual.service.am.StubInfo;

import android.app.IApplicationThread;
import android.content.pm.ApplicationInfo;
import android.content.pm.ProviderInfo;
import android.os.Binder;
import android.os.ConditionVariable;

public final class ProcessRecord extends Binder {

	public final ConditionVariable lock = new ConditionVariable();
	final public StubInfo stubInfo;
	public final ApplicationInfo info; // all about the first app in the process
	final public String processName; // name of the process
	public final List<String> pendingPackages = new ArrayList<>(2);
	final Set<String> pkgList = new HashSet<>(); // List of packages
													// running in the
													// process
	final List<ProviderInfo> providers;
	final List<String> sharedPackages;
	public IVClient client;
	public IApplicationThread thread;
	public int pid; // The process of this application; 0 if none

	public ProcessRecord(StubInfo stubInfo, ApplicationInfo info, String processName, List<ProviderInfo> providers,
			List<String> sharedPackages) {
		this.stubInfo = stubInfo;
		this.info = info;
		this.processName = processName;
		this.providers = providers;
		this.sharedPackages = sharedPackages;
	}

	public void addPackage(String newPkg) {
		pkgList.add(newPkg);
	}

	public boolean isLaunching(String packageName) {
		return pendingPackages.contains(packageName);
	}
}
