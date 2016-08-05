package com.lody.virtual.client.hook.patchs.backup;

import com.lody.virtual.client.hook.base.PatchObject;
import com.lody.virtual.client.hook.base.ResultStaticHook;
import com.lody.virtual.client.hook.binders.HookBackupManagerBinder;
import com.lody.virtual.helper.utils.Reflect;

import android.app.backup.BackupManager;
import android.app.backup.IBackupManager;
import android.content.Context;
import android.os.ServiceManager;

/**
 * @author Lody
 *
 *
 * @see IBackupManager
 * @see BackupManager
 */
public class BackupManagerPatch extends PatchObject<IBackupManager, HookBackupManagerBinder> {

	@Override
	protected HookBackupManagerBinder initHookObject() {
		return new HookBackupManagerBinder();
	}

	@Override
	public void inject() throws Throwable {
		getHookObject().injectService(Context.BACKUP_SERVICE);
		Reflect.on(BackupManager.class).set("sService", null);
	}

	@Override
	protected void applyHooks() {
		super.applyHooks();
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

	@Override
	public boolean isEnvBad() {
		return getHookObject() != ServiceManager.getService(Context.BACKUP_SERVICE);
	}
}
