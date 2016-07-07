package com.lody.virtual.client.hook.binders;

import java.io.FileDescriptor;

import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

/**
 * @author Lody
 *
 */
public class ProxyIBinder implements IBinder {

	private IBinder mRemote;

	public ProxyIBinder(IBinder mRemote) {
		this.mRemote = mRemote;
	}

	@Override
	public String getInterfaceDescriptor() throws RemoteException {
		return mRemote.getInterfaceDescriptor();
	}

	@Override
	public boolean pingBinder() {
		return mRemote.pingBinder();
	}

	@Override
	public boolean isBinderAlive() {
		return mRemote.isBinderAlive();
	}

	@Override
	public IInterface queryLocalInterface(String descriptor) {
		return mRemote.queryLocalInterface(descriptor);
	}

	@Override
	public void dump(FileDescriptor fd, String[] args) throws RemoteException {
		mRemote.dump(fd, args);
	}

	@Override
	public void dumpAsync(FileDescriptor fd, String[] args) throws RemoteException {
		mRemote.dumpAsync(fd, args);
	}

	@Override
	public boolean transact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
		return mRemote.transact(code, data, reply, flags);
	}

	@Override
	public void linkToDeath(DeathRecipient recipient, int flags) throws RemoteException {
		mRemote.linkToDeath(recipient, flags);
	}

	@Override
	public boolean unlinkToDeath(DeathRecipient recipient, int flags) {
		return mRemote.unlinkToDeath(recipient, flags);
	}
}
