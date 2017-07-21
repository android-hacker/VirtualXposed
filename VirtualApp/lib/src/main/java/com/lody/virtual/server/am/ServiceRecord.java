package com.lody.virtual.server.am;

import android.app.IServiceConnection;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.app.Notification;

public class ServiceRecord extends Binder {
	final HashMap<Intent.FilterComparison, IntentBindRecord> bindings
			= new HashMap<Intent.FilterComparison, IntentBindRecord>();
	public long activeSince;
	public long lastActivityTime;
	public ComponentName name;
	public ServiceInfo serviceInfo;
	public int startId;
	public ProcessRecord process;
	final HashMap<IBinder, ArrayList<ConnectionRecord>> connections
			= new HashMap<IBinder, ArrayList<ConnectionRecord>>();
	public Notification foregroundNoti;
	public int foregroundId;

	public boolean containConnection(IServiceConnection connection) {
		for (IntentBindRecord record : bindings.values()) {
			if (record.containConnection(connection)) {
				return true;
			}
		}
		return false;
	}

	public IntentBindRecord retrieveIntentBindRecord(Intent intent) {
		Intent.FilterComparison filter = new Intent.FilterComparison(intent);
		IntentBindRecord i = bindings.get(filter);
		if (i == null) {
			i = new IntentBindRecord();
			i.intent = intent;
			bindings.put(filter, i);
		}

		return i;
	}

	public AppBindRecord retrieveAppBindingLocked(Intent intent, ProcessRecord app) {
		IntentBindRecord i = retrieveIntentBindRecord(intent);
		AppBindRecord a = i.apps.get(app);
		if (a != null) {
			return a;
		}
		a = new AppBindRecord(this, i, app);
		i.apps.put(app, a);
		return a;
	}

	public boolean hasAutoCreateConnections() {
		Collection<ArrayList<ConnectionRecord>> connectionRecords = connections.values();
		if (connectionRecords == null) {
			return false;
		}

		Iterator<ArrayList<ConnectionRecord>> connectionRecordIterator
				= connectionRecords.iterator();
		while (connectionRecordIterator.hasNext()) {
			ArrayList<ConnectionRecord> cr = connectionRecordIterator.next();
			for (int i=0; i<cr.size(); i++) {
				if ((cr.get(i).flags & Context.BIND_AUTO_CREATE) != 0) {
					return true;
				}
			}
		}

		return false;
	}

	public int getClientCount() {
		return bindings.size();
	}


	int getConnectionCount() {
		int count = 0;
		synchronized (bindings) {
			for (IntentBindRecord record : bindings.values()) {
				count += record.connections.size();
			}
		}
		return count;
	}


	IntentBindRecord peekBinding(Intent service) {
		synchronized (bindings) {
			for (IntentBindRecord bindRecord : bindings.values()) {
				if (bindRecord.intent != null && bindRecord.intent.filterEquals(service)) {
					return bindRecord;
				}
			}
		}
		return null;
	}

	void addToBoundIntent(Intent intent, IServiceConnection connection) {
		IntentBindRecord record = peekBinding(intent);
		if (record == null) {
			record = new IntentBindRecord();
			record.intent = intent;
			synchronized (bindings) {
				Intent.FilterComparison filter = new Intent.FilterComparison(intent);
				bindings.put(filter, record);
			}
		}
		record.addConnection(connection);
	}

	public static class IntentBindRecord {
		final HashMap<ProcessRecord, AppBindRecord> apps
				= new HashMap<ProcessRecord, AppBindRecord>();
		public  final List<IServiceConnection> connections = Collections.synchronizedList(new ArrayList<IServiceConnection>());
		public IBinder binder;
		/** Set when we have initiated a request for this binder. */
		boolean requested;
		/** Set when we still need to tell the service all clients are unbound. */
		boolean hasBound;
		Intent intent;
		public boolean doRebind = false;

		int collectFlags() {
			int flags = 0;
			Set<Map.Entry<ProcessRecord,AppBindRecord>> entrySet = apps.entrySet();
			for (Map.Entry<ProcessRecord,AppBindRecord> app : entrySet) {
				if (app.getValue().connections.size() > 0) {
					for (ConnectionRecord conn : app.getValue().connections) {
						flags |= conn.flags;
					}
				}
			}
			return flags;
		}

		public boolean containConnection(IServiceConnection connection) {
			for (IServiceConnection con : connections) {
				if (con.asBinder() == connection.asBinder()) {
					return true;
				}
			}
			return false;
		}

		public void addConnection(IServiceConnection connection) {
			if (!containConnection(connection)) {
				connections.add(connection);
				try {
					connection.asBinder().linkToDeath(new DeathRecipient(this, connection), 0);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		}

		public void removeConnection(IServiceConnection connection) {
			synchronized (connections) {
				Iterator<IServiceConnection> iterator = connections.iterator();
				while (iterator.hasNext()) {
					IServiceConnection conn = iterator.next();
					if (conn.asBinder() == connection.asBinder()) {
						iterator.remove();
					}
				}
			}
		}
	}

	private static class DeathRecipient implements IBinder.DeathRecipient {

		private final IntentBindRecord bindRecord;
		private final IServiceConnection connection;

		private DeathRecipient(IntentBindRecord bindRecord, IServiceConnection connection) {
			this.bindRecord = bindRecord;
			this.connection = connection;
		}

		@Override
		public void binderDied() {
			bindRecord.removeConnection(connection);
			connection.asBinder().unlinkToDeath(this, 0);
		}
	}

}