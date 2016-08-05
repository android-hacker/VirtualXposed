package com.lody.virtual.service.am;

import android.app.Activity;
import android.app.ApplicationThreadNative;
import android.app.IApplicationThread;
import android.content.IIntentReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;

import com.lody.virtual.client.service.ServiceManagerNative;
import com.lody.virtual.helper.utils.VLog;
import com.lody.virtual.service.IReceiverManager;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * By : qiang.sheng qiang.sheng@godinsec.com Created by andy on 16-8-3.
 * TODO:
 * 管理Receiver的服务, 用于模拟系统广播的发送. 例如:应用安装, 卸载.
 */
public class VReceiverService extends IReceiverManager.Stub {

	private static  final VReceiverService mService = new VReceiverService();

	final Map<IBinder, ReceiverList> mRegisteredReceivers = (Map<IBinder, ReceiverList>) Collections
			.synchronizedMap(new HashMap<IBinder, ReceiverList>());
	public Map<String, Set<IBinder>> receivers = (Map<String, Set<IBinder>>) Collections
			.synchronizedMap(new HashMap<String, Set<IBinder>>());

	public static VReceiverService getService() {
		return mService;
	}

	public static IReceiverManager getReceiverManager() {
		IBinder receiverServiceBinder = ServiceManagerNative.getService(ServiceManagerNative.RECEIVER_MANAGER);
		if (receiverServiceBinder == null)
			return null;
		return Stub.asInterface(receiverServiceBinder);
	}


	@Override
	public void unregisterReceiver(IBinder receiver) {
		ReceiverList receiverList = mRegisteredReceivers.remove(receiver);
		for (Set<IBinder> intentReceivers : receivers.values()) {
			if (intentReceivers.contains(receiverList.binder)) {
				intentReceivers.remove(receiverList.binder);
				VLog.i("Andy", "Receiver -> unregisterReceiver %s",
						Arrays.toString(new Object[]{receiver, receiverList.receiver}));
			}
		}
	}

	@Override
	public Intent registerReceiver(IBinder caller, final IBinder receiver, IntentFilter filter, String permission,
			int userId) throws RemoteException {
		final IIntentReceiver intentReceiver = IIntentReceiver.Stub.asInterface(receiver);
		IApplicationThread iApplicationThread = ApplicationThreadNative.asInterface(caller);
		ReceiverList receiverList = mRegisteredReceivers.get(receiver);
		if (receiverList == null) {
			receiverList = new ReceiverList(filter, iApplicationThread, receiver, intentReceiver,
					userId != -1 ? userId : Binder.getCallingUid(), Binder.getCallingUid(), Binder.getCallingPid());
			mRegisteredReceivers.put(receiver, receiverList);
			Iterator<String> iterator = receiverList.filter.actionsIterator();
			while (iterator.hasNext()) {
				final String action = iterator.next();
				if (action != null) {
					Set<IBinder> intentReceivers = receivers.get(action);
					if (intentReceivers == null) {
						intentReceivers = Collections.synchronizedSet(new HashSet<IBinder>());
						receivers.put(action, intentReceivers);
					}

					if (!intentReceivers.contains(receiver)) {
						intentReceivers.add(receiver);
						VLog.i("Andy", "Receiver -> registerReceiver %s", Arrays.toString(new Object[]{action, receiver,
								intentReceiver, intentReceivers.size(), intentReceivers.contains(intentReceiver.asBinder())}));
					}
				}
			}

			receiver.linkToDeath(new DeathRecipient() {
				@Override
				public void binderDied() {
					ReceiverList receiverList = mRegisteredReceivers.remove(receiver);
					VLog.i("Andy", "Receiver -> removeReceivers %s",
							Arrays.toString(new Object[]{receiver, receiverList.receiver}));
					for (Set<IBinder> intentReceivers : receivers.values()) {
						if (intentReceivers.contains(receiver)) {
							intentReceivers.remove(receiver);
							VLog.i("Andy", "Receiver -> removeReceiver %s",
									Arrays.toString(new Object[]{receiver, receiverList.receiver}));
						}
					}
				}
			}, 0);
		}

		return null;
	}

	@Override
	public int broadcastIntent(IBinder caller, Intent intent, String resolvedType, IBinder resultTo, int resultCode,
			String resultData, Bundle map, String requiredPermission, boolean serialized, boolean sticky)
			throws RemoteException {
		String action = intent.getAction();
		if (action == null) {
			return 0;
		}

		Set<IBinder> intentReceivers = receivers.get(action);
		if (intentReceivers == null)
			return 0;

		for (IBinder receiver : intentReceivers) {
			if (receiver == null)
				continue;

			ReceiverList list = mRegisteredReceivers.get(receiver);
			if (list != null) {
				VLog.i("Andy", "Receiver -> broadcastIntent %s",
						Arrays.toString(new Object[]{action, intent, receiver, list.receiver}));
				if (list.receiver != null)
					list.receiver.performReceive(new Intent(intent), Activity.RESULT_OK, null, null, false, sticky,
							list.userId);
			}
		}
		return 0;
	}
}
