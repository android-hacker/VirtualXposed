package com.lody.virtual.service.am;

import android.app.IApplicationThread;
import android.content.IIntentReceiver;
import android.content.IntentFilter;
import android.os.IBinder;

/**
 * Class: Created by andy on 16-8-3. TODO:
 */
public class ReceiverList {
	public IntentFilter filter;
	public IApplicationThread thread;
	public IBinder binder;
	public IIntentReceiver receiver;
	public int userId;
	public int callingUid;
	public int callintPid;

	public ReceiverList() {
	}

	public ReceiverList(IntentFilter filter, IApplicationThread thread, IBinder binder, IIntentReceiver receiver,
			int userId, int callingUid, int callintPid) {
		this.filter = filter;
		this.thread = thread;
		this.binder = binder;
		this.receiver = receiver;
		this.userId = userId;
		this.callingUid = callingUid;
		this.callintPid = callintPid;
	}
}
