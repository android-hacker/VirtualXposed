package com.lody.virtual.service.am;

import android.app.IApplicationThread;
import android.content.pm.ApplicationInfo;
import android.content.pm.ProviderInfo;
import android.os.Binder;
import android.os.ConditionVariable;

import com.lody.virtual.client.IVClient;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class ProcessRecord extends Binder {

	public final ConditionVariable lock = new ConditionVariable();
	public final ConditionVariable attachLock = new ConditionVariable();
	final public StubInfo stubInfo;
	public final ApplicationInfo info; // all about the first app in the process
	final public String processName; // name of the process
	final Set<String> pkgList = new HashSet<>(); // List of packages
													// running in the
													// process
	boolean doneExecuting;
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

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ProcessRecord record = (ProcessRecord) o;
		return processName != null ? processName.equals(record.processName) : record.processName == null;

	}

}
