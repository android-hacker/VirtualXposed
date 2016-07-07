package com.lody.virtual.service;

import java.util.ArrayList;
import java.util.List;

import android.app.IServiceConnection;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.IBinder;
import android.os.RemoteException;

class ServiceRecord {
	public final List<ServiceBoundRecord> mBoundRecords = new ArrayList<>();
	ServiceInfo serviceInfo;
	IBinder token;
	int startId;
	IBinder binder;
	boolean doRebind = false;

	public boolean hasSomeBound() {
		return mBoundRecords.size() > 0;
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

	Intent removedConnection(IServiceConnection con) {
		synchronized (mBoundRecords) {
			Intent intent = null;
			List<ServiceBoundRecord> removed = new ArrayList<>();
			for (ServiceBoundRecord record : mBoundRecords) {
				if (record.containsConnection(con)) {
					record.removeConnection(con);
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

	static class ServiceBoundRecord {
		public List<IServiceConnection> connections = new ArrayList<>();
		Intent intent;

		public boolean containsConnection(IServiceConnection connection) {
			for (IServiceConnection con : connections) {
				if (con != null && connection != null && con.asBinder() == connection.asBinder()) {
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

		public void removeConnection(IServiceConnection connection) {
			List<IServiceConnection> removed = new ArrayList<>();
			for (IServiceConnection con : connections) {
				if (con != null && connection != null && con.asBinder() == connection.asBinder()) {
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