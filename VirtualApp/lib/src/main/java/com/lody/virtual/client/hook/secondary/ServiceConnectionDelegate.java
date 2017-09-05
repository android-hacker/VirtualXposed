package com.lody.virtual.client.hook.secondary;

import android.app.IServiceConnection;
import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.lody.virtual.client.VClientImpl;
import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.helper.collection.ArrayMap;
import com.lody.virtual.server.IBinderDelegateService;

import mirror.android.app.ActivityThread;
import mirror.android.app.ContextImpl;
import mirror.android.app.LoadedApk;

/**
 * @author Lody
 */

public class ServiceConnectionDelegate extends IServiceConnection.Stub {
    private final static ArrayMap<ServiceConnection, IServiceConnection> CONNECTION_ARRAY_MAP = new ArrayMap<>();
    private final static ArrayMap<IBinder, ServiceConnectionDelegate> DELEGATE_MAP = new ArrayMap<>();
    private IServiceConnection mConn;

    private ServiceConnectionDelegate(IServiceConnection mConn) {
        this.mConn = mConn;
    }

    public static IServiceConnection getDelegate(Context context, ServiceConnection connection,int flags) {
        IServiceConnection sd = CONNECTION_ARRAY_MAP.get(connection);
        if(sd != null){
            Log.d("ConnectionDelegate", "bindService:use old:"+sd);
            return getDelegate(sd);
        }
        if (connection == null) {
            throw new IllegalArgumentException("connection is null");
        }
        try {
            Object activityThread = ActivityThread.currentActivityThread.call();
            Object loadApk = ContextImpl.mPackageInfo.get(VirtualCore.get().getContext());
            Handler handler = ActivityThread.getHandler.call(activityThread);
            sd = LoadedApk.getServiceDispatcher.call(loadApk, connection, context, handler, flags);
        } catch (Exception e) {
            Log.e("ConnectionDelegate", "bindService", e);
        }
        if (sd == null) {
            throw new RuntimeException("Not supported in system context");
        }
        CONNECTION_ARRAY_MAP.put(connection, sd);
        return getDelegate(sd);
    }

    public static IServiceConnection removeDelegate(ServiceConnection conn) {
        ServiceConnectionDelegate serviceConnectionDelegate = ServiceConnectionDelegate.removeDelegate(CONNECTION_ARRAY_MAP.get(conn));
        Log.d("ConnectionDelegate", "removeDelegate:" + serviceConnectionDelegate);
        return serviceConnectionDelegate;
    }

    public static ServiceConnectionDelegate getDelegate(IServiceConnection conn) {
        if(conn instanceof ServiceConnectionDelegate){
            return (ServiceConnectionDelegate)conn;
        }
        IBinder binder = conn.asBinder();
        ServiceConnectionDelegate delegate = DELEGATE_MAP.get(binder);
        if (delegate == null) {
            delegate = new ServiceConnectionDelegate(conn);
            DELEGATE_MAP.put(binder, delegate);
        }
        return delegate;
    }

    public static ServiceConnectionDelegate removeDelegate(IServiceConnection conn) {
        return DELEGATE_MAP.remove(conn.asBinder());
    }

    @Override
    public void connected(ComponentName name, IBinder service) throws RemoteException {
        IBinderDelegateService delegateService = IBinderDelegateService.Stub.asInterface(service);
        if (delegateService != null) {
            name = delegateService.getComponent();
            service = delegateService.getService();
            IBinder proxy = ProxyServiceFactory.getProxyService(VClientImpl.get().getCurrentApplication(), name, service);
            if (proxy != null) {
                service = proxy;
            }
        }
        mConn.connected(name, service);
    }
}
