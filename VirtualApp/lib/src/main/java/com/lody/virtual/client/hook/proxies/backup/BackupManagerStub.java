package com.lody.virtual.client.hook.proxies.backup;

import android.app.backup.BackupManager;

import com.lody.virtual.client.hook.base.BinderInvocationProxy;
import com.lody.virtual.client.hook.base.ResultStaticMethodProxy;

import mirror.android.app.backup.IBackupManager;

/**
 * @author Lody
 *
 * @see BackupManager
 */
public class BackupManagerStub extends BinderInvocationProxy {
	public BackupManagerStub() {
		super(IBackupManager.Stub.asInterface, "backup");
	}

	@Override
	protected void onBindMethods() {
		super.onBindMethods();
		addMethodProxy(new ResultStaticMethodProxy("dataChanged", null));
		addMethodProxy(new ResultStaticMethodProxy("clearBackupData", null));
		addMethodProxy(new ResultStaticMethodProxy("agentConnected", null));
		addMethodProxy(new ResultStaticMethodProxy("agentDisconnected", null));
		addMethodProxy(new ResultStaticMethodProxy("restoreAtInstall", null));
		addMethodProxy(new ResultStaticMethodProxy("setBackupEnabled", null));
		addMethodProxy(new ResultStaticMethodProxy("setBackupProvisioned", null));
		addMethodProxy(new ResultStaticMethodProxy("backupNow", null));
		addMethodProxy(new ResultStaticMethodProxy("fullBackup", null));
		addMethodProxy(new ResultStaticMethodProxy("fullTransportBackup", null));
		addMethodProxy(new ResultStaticMethodProxy("fullRestore", null));
		addMethodProxy(new ResultStaticMethodProxy("acknowledgeFullBackupOrRestore", null));
		addMethodProxy(new ResultStaticMethodProxy("getCurrentTransport", null));
		addMethodProxy(new ResultStaticMethodProxy("listAllTransports", new String[0]));
		addMethodProxy(new ResultStaticMethodProxy("selectBackupTransport", null));
		addMethodProxy(new ResultStaticMethodProxy("isBackupEnabled", false));
		addMethodProxy(new ResultStaticMethodProxy("setBackupPassword", true));
		addMethodProxy(new ResultStaticMethodProxy("hasBackupPassword", false));
		addMethodProxy(new ResultStaticMethodProxy("beginRestoreSession", null));
	}
}
