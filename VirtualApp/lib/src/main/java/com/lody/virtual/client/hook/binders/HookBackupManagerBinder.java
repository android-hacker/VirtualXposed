package com.lody.virtual.client.hook.binders;

import com.lody.virtual.client.hook.base.HookBinder;

import android.app.backup.IBackupManager;
import android.content.Context;
import android.os.IBinder;
import android.os.ServiceManager;

/**
 * @author Lody
 *
 */
public class HookBackupManagerBinder extends HookBinder<IBackupManager> {
	@Override
	protected IBinder queryBaseBinder() {
		return ServiceManager.getService(Context.BACKUP_SERVICE);
	}

	@Override
	protected IBackupManager createInterface(IBinder baseBinder) {
		return IBackupManager.Stub.asInterface(baseBinder);
	}
}
