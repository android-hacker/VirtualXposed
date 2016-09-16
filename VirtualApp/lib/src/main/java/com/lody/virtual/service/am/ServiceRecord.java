package com.lody.virtual.service.am;

import android.app.IServiceConnection;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;

import java.util.ArrayList;
import java.util.List;

public class ServiceRecord extends Binder {
	public final List<IntentBindRecord> bindings = new ArrayList<>();
	public long activeSince;
	public long lastActivityTime;
	public ServiceInfo serviceInfo;
	public int startId;
	public ProcessRecord process;

	public boolean containConnection(IServiceConnection connection) {
		for (IntentBindRecord record : bindings) {
			if (record.containConnection(connection)) {
				return true;
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
			for (IntentBindRecord record : bindings) {
				count += record.connections.size();
			}
		}
		return count;
	}


	IntentBindRecord peekBinding(Intent service) {
		synchronized (bindings) {
			for (IntentBindRecord bindRecord : bindings) {
				if (bindRecord.intent.filterEquals(service)) {
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
				bindings.add(record);
			}
		}
		record.addConnection(connection);
	}

	public static class IntentBindRecord {
		public  final List<IServiceConnection> connections = new ArrayList<>();
		public IBinder binder;
		Intent intent;
		public boolean doRebind = false;

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
				for (IServiceConnection con : connections) {
					if (con.asBinder() == connection.asBinder()) {
						connections.remove(con);
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