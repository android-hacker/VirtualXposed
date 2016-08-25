package com.lody.virtual.client.hook.binders;

import android.os.IBinder;
import android.os.IInterface;

import com.lody.virtual.client.hook.base.HookBinderDelegate;

import mirror.android.app.backup.IBackupManager;
import mirror.android.os.ServiceManager;

/**
 * @author Lody
 *
 */
public class BackupBinderDelegate extends HookBinderDelegate {
	@Override
	protected IInterface createInterface() {
		IBinder binder = ServiceManager.getService.call("backup");
		return IBackupManager.Stub.asInterface.call(binder);
	}
}
