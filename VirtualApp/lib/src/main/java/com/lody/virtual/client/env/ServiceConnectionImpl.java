package com.lody.virtual.client.env;

import android.app.IServiceConnection;
import android.content.ComponentName;
import android.os.IBinder;
import android.os.RemoteException;

import com.lody.virtual.helper.utils.XLog;

/**
 * @author Lody
 */

public class ServiceConnectionImpl extends IServiceConnection.Stub {

    private static final String TAG = ServiceConnectionImpl.class.getSimpleName();

    private IServiceConnection mConnection;

    public ServiceConnectionImpl(IServiceConnection connection) {
        this.mConnection = connection;
    }

    @Override
    public void connected(ComponentName component, IBinder binder) throws RemoteException {
        XLog.d(TAG, "Connect service %s / %s.", component.toShortString(), binder.getInterfaceDescriptor());
        mConnection.connected(component, binder);
    }

    @Override
    public IBinder asBinder() {
        return mConnection.asBinder();
    }
}
