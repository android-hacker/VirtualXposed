package com.lody.virtual.client.hook.patchs.backup;

import com.lody.virtual.client.hook.base.Patch;
import com.lody.virtual.client.hook.base.PatchObject;
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
@Patch({Hook_AgentConnected.class, Hook_AgentDisconnected.class, Hook_BeginRestoreSession.class,
		Hook_ClearBackupData.class, Hook_DataChanged.class, Hook_GetAvailableRestoreToken.class,
		Hook_RestoreAtInstall.class})
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
	public boolean isEnvBad() {
		return getHookObject() != ServiceManager.getService(Context.BACKUP_SERVICE);
	}
}
