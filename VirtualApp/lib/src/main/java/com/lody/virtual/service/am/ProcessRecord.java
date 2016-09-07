package com.lody.virtual.service.am;

import android.content.pm.ApplicationInfo;
import android.content.pm.ProviderInfo;
import android.os.Binder;
import android.os.ConditionVariable;
import android.os.IInterface;

import com.lody.virtual.client.IVClient;
import com.lody.virtual.os.VUserHandle;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class ProcessRecord extends Binder {

	public final ConditionVariable lock = new ConditionVariable();
	final public StubInfo stubInfo;
	public final ApplicationInfo info; // all about the first app in the process
	final public String processName; // name of the process
	final Set<String> pkgList = new HashSet<>(); // List of packages
	final List<ProviderInfo> providers;
	final List<String> sharedPackages;
	final List<String> usesLibraries;
	public IVClient client;
	public IInterface appThread;
	public int pid;
	public int uid;
	public int userId;
													// running in the
													// process
	boolean doneExecuting;

	public ProcessRecord(StubInfo stubInfo, ApplicationInfo info, String processName, List<ProviderInfo> providers,
			List<String> sharedPackages, List<String> usesLibraries, int uid) {
		this.stubInfo = stubInfo;
		this.info = info;
		this.uid = uid;
		this.userId = VUserHandle.getUserId(uid);
		this.processName = processName;
		this.providers = providers;
		this.sharedPackages = sharedPackages;
		this.usesLibraries = usesLibraries;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		ProcessRecord record = (ProcessRecord) o;
		return processName != null ? processName.equals(record.processName) : record.processName == null;
	}

}
