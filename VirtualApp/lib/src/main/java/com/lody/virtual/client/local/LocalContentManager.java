package com.lody.virtual.client.local;

import java.util.List;

import com.lody.virtual.client.env.VirtualRuntime;
import com.lody.virtual.client.service.ServiceManagerNative;
import com.lody.virtual.service.IContentManager;

import android.app.IActivityManager;
import android.os.IBinder;
import android.os.RemoteException;

/**
 * @author Lody
 *
 */
public class LocalContentManager {

	private static final LocalContentManager sMgr = new LocalContentManager();

	private IContentManager mRemote;

	public static LocalContentManager getDefault() {
		return sMgr;
	}

	public IContentManager getService() {
		if (mRemote == null) {
			synchronized (this) {
				if (mRemote == null) {
					IBinder binder = ServiceManagerNative.getService(ServiceManagerNative.CONTENT_MANAGER);
					mRemote = IContentManager.Stub.asInterface(binder);
				}
			}
		}
		return mRemote;
	}

	public void publishContentProviders(List<IActivityManager.ContentProviderHolder> holderList) {
		try {
			getService().publishContentProviders(holderList);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public IActivityManager.ContentProviderHolder getContentProvider(String auth) {
		try {
			return getService().getContentProvider(auth);
		} catch (RemoteException e) {
			return VirtualRuntime.crash(e);
		}
	}

}
