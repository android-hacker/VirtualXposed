package com.lody.virtual.client.hook.patchs.backup;

import android.app.backup.BackupManager;

import com.lody.virtual.client.hook.base.PatchBinderDelegate;
import com.lody.virtual.client.hook.base.ResultStaticHook;

import mirror.android.app.backup.IBackupManager;

/**
 * @author Lody
 *
 * @see BackupManager
 */
public class BackupManagerPatch extends PatchBinderDelegate {
	public BackupManagerPatch() {
		super(IBackupManager.Stub.TYPE, "backup");
	}

	@Override
	protected void onBindHooks() {
		super.onBindHooks();
		addHook(new ResultStaticHook("dataChanged", null));
		addHook(new ResultStaticHook("clearBackupData", null));
		addHook(new ResultStaticHook("agentConnected", null));
		addHook(new ResultStaticHook("agentDisconnected", null));
		addHook(new ResultStaticHook("restoreAtInstall", null));
		addHook(new ResultStaticHook("setBackupEnabled", null));
		addHook(new ResultStaticHook("setBackupProvisioned", null));
		addHook(new ResultStaticHook("backupNow", null));
		addHook(new ResultStaticHook("fullBackup", null));
		addHook(new ResultStaticHook("fullTransportBackup", null));
		addHook(new ResultStaticHook("fullRestore", null));
		addHook(new ResultStaticHook("acknowledgeFullBackupOrRestore", null));
		addHook(new ResultStaticHook("getCurrentTransport", null));
		addHook(new ResultStaticHook("listAllTransports", new String[0]));
		addHook(new ResultStaticHook("selectBackupTransport", null));
		addHook(new ResultStaticHook("isBackupEnabled", false));
		addHook(new ResultStaticHook("setBackupPassword", true));
		addHook(new ResultStaticHook("hasBackupPassword", false));
		addHook(new ResultStaticHook("beginRestoreSession", null));
	}
}
