package com.lody.virtual.client.hook.secondary;

import android.app.IServiceConnection;
import android.content.ComponentName;
import android.os.IBinder;
import android.os.RemoteException;

import com.lody.virtual.client.VClientImpl;
import com.lody.virtual.helper.utils.collection.ArrayMap;
import com.lody.virtual.server.IBinderDelegateService;

/**
 * @author Lody
 */

public class ServiceConnectionDelegate extends IServiceConnection.Stub {

    private final static ArrayMap<IBinder, ServiceConnectionDelegate> DELEGATE_MAP = new ArrayMap<>();
    private IServiceConnection mConn;

    private ServiceConnectionDelegate(IServiceConnection mConn) {
        this.mConn = mConn;
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
