package com.lc.interceptor.client.local.interceptor;

import android.os.IBinder;
import android.os.RemoteException;

import com.lc.interceptor.ICallBody;
import com.lc.interceptor.IInterceptorCallManager;
import com.lc.interceptor.IObjectWrapper;
import com.lc.interceptor.client.hook.base.InterceptorHook;
import com.lody.virtual.client.env.VirtualRuntime;
import com.lody.virtual.client.service.ServiceManagerNative;

/**
 * @author legency
 */
public class VInterceptorCallManager {

    private static final VInterceptorCallManager sMgr = new VInterceptorCallManager();

    public static VInterceptorCallManager get() {
        return sMgr;
    }

    private IInterceptorCallManager mRemote;

    public synchronized IInterceptorCallManager getInterface() {
        if (mRemote == null) {
            synchronized (VInterceptorCallManager.class) {
                if (mRemote == null) {
                    final IBinder pmBinder = ServiceManagerNative.getService(ServiceManagerNative.INTERCEPTOR_SERVICE);
                    mRemote = IInterceptorCallManager.Stub.asInterface(pmBinder);
                }
            }
        }
        return mRemote;
    }

    public Object call(InterceptorHook interceptorHook, Object... objects) {
        try {
            ICallBody callBody = new ICallBody(interceptorHook).arg(objects);
            IObjectWrapper object = getInterface().call(callBody);
            return object != null ? object.get() : null;
        } catch (RemoteException e) {
            return VirtualRuntime.crash(e);
        }
    }
}
