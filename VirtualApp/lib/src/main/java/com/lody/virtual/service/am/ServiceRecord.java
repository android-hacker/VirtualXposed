package com.lody.virtual.service.am;

import android.app.IServiceConnection;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.IBinder;
import android.os.IInterface;
import android.os.RemoteException;

import java.util.ArrayList;
import java.util.List;

public class ServiceRecord {
	public final List<ServiceBoundRecord> mBoundRecords = new ArrayList<>();
	public long activeSince;
	public long lastActivityTime;
	public int pid;
	public int uid;
	public ServiceInfo serviceInfo;
	public IBinder token;
	public int startId;
	public IBinder binder;
	public IInterface appThread;
	public boolean doRebind = false;

	public boolean hasSomeBound() {
		return mBoundRecords.size() > 0;
	}

	public boolean containConnection(IServiceConnection connection) {
		for (ServiceBoundRecord record : mBoundRecords) {
			if (record.containsConnection(connection)) {
				return true;
			}
		}
		return false;
	}

	public int getClientCount() {
		return mBoundRecords.size();
	}

	List<IServiceConnection> getAllConnections() {
		List<IServiceConnection> list = new ArrayList<>();
		synchronized (mBoundRecords) {
			for (ServiceBoundRecord record : mBoundRecords) {
				if (record.connections.size() > 0) {
					list.addAll(record.connections);
				}
			}
		}
		return list;
	}

	public boolean hasConnection() {
		for (ServiceBoundRecord r : mBoundRecords) {
			if (!r.connections.isEmpty()) {
				return false;
			}
		}
		return true;
	}

	Intent removedConnection(IServiceConnection connection) {
		synchronized (mBoundRecords) {
			Intent intent = null;
			List<ServiceBoundRecord> removed = new ArrayList<>();
			for (ServiceBoundRecord record : mBoundRecords) {
				if (record.containsConnection(connection)) {
					record.removeConnection(connection);
					intent = record.intent;
				}
				if (record.connections.size() <= 0) {
					removed.add(record);
				}
			}
			mBoundRecords.removeAll(removed);
			return intent;
		}
	}

	ServiceBoundRecord findServiceBindRecord(Intent intent) {
		if (intent == null) {
			return null;
		}
		synchronized (mBoundRecords) {
			for (ServiceBoundRecord bindRecord : mBoundRecords) {
				if (bindRecord.intent != null && bindRecord.intent.filterEquals(intent)) {
					return bindRecord;
				}
			}
		}
		return null;
	}

	boolean addToBoundIntent(Intent intent, final IServiceConnection connection) {
		if (intent == null) {
			return false;
		}

		ServiceBoundRecord record = findServiceBindRecord(intent);
		if (record == null) {
			record = new ServiceBoundRecord();
			record.intent = intent;

			if (connection != null && connection.asBinder().isBinderAlive()) {
				record.addConnection(connection);
			}
			synchronized (mBoundRecords) {
				mBoundRecords.add(record);
			}
			return true;
		} else {
			if (connection != null && connection.asBinder().isBinderAlive()) {
				record.addConnection(connection);
			}
		}
		return false;
	}

	boolean hasIntentBound(Intent intent) {
		if (intent == null) {
			return false;
		}
		synchronized (mBoundRecords) {
			for (ServiceBoundRecord bindRecord : mBoundRecords) {
				if (bindRecord.intent != null && bindRecord.intent.filterEquals(intent)) {
					return true;
				}
			}
		}
		return false;
	}

	public static class ServiceBoundRecord {
		public List<IServiceConnection> connections = new ArrayList<>();
		Intent intent;

		public boolean containsConnection(IServiceConnection connection) {
			for (IServiceConnection con : connections) {
				if (con.asBinder() == connection.asBinder()) {
					return true;
				}
			}
			return false;
		}

		public void addConnection(IServiceConnection connection) {
			if (!containsConnection(connection)) {
				connections.add(connection);
				try {
					connection.asBinder().linkToDeath(new DeathRecipient(this, connection), 0);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		}

		public boolean containConnection(IServiceConnection connection) {
			for (IServiceConnection con : connections) {
				if (con.asBinder() == connection.asBinder()) {
					return true;
				}
			}
			return false;
		}

		public void removeConnection(IServiceConnection connection) {
			List<IServiceConnection> removed = new ArrayList<>();
			for (IServiceConnection con : connections) {
				if (con.asBinder() == connection.asBinder()) {
					removed.add(con);
				}
			}
			for (IServiceConnection con : removed) {
				connections.remove(con);
			}
		}
	}

	private static class DeathRecipient implements IBinder.DeathRecipient {

		private final ServiceBoundRecord boundRecord;
		private final IServiceConnection connection;

		private DeathRecipient(ServiceBoundRecord boundRecord, IServiceConnection connection) {
			this.boundRecord = boundRecord;
			this.connection = connection;
		}

		@Override
		public void binderDied() {
			boundRecord.removeConnection(connection);
			connection.asBinder().unlinkToDeath(this, 0);
		}
	}

}