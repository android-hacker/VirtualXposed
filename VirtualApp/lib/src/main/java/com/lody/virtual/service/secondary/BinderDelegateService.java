package com.lody.virtual.service.secondary;

import android.content.ComponentName;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;

import com.lody.virtual.service.IBinderDelegateService;
import com.lody.virtual.service.am.ServiceRecord;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Lody
 */

public class BinderDelegateService extends IBinderDelegateService.Stub {

    private ComponentName name;
    private ServiceRecord record;

    private interface ProxyBinderFactory {
        IBinder create(Binder binder);
    }
    private static final Map<String, ProxyBinderFactory> mFactories = new HashMap<>();
    static {
        mFactories.put("android.accounts.IAccountAuthenticator", new ProxyBinderFactory() {
            @Override
            public IBinder create(Binder binder) {
                return new FakeIdentityBinder(binder);
            }
        });
    }

    public BinderDelegateService(ComponentName name, ServiceRecord record) {
        this.name = name;
        this.record = record;
    }

    @Override
    public ComponentName getComponent() throws RemoteException {
        return name;
    }

    @Override
    public IBinder getService() throws RemoteException {
        IBinder service = record.binder;
        if (service instanceof Binder) {
            Binder localService = (Binder) service;
            ProxyBinderFactory factory = mFactories.get(localService.getInterfaceDescriptor());
            if (factory != null) {
                service = factory.create(localService);
            }
        }
        return service;
    }
}
