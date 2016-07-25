package com.lody.virtual.service;

import android.app.IActivityManager;
import android.content.Context;
import android.content.pm.ProviderInfo;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;

import com.lody.virtual.helper.proto.VComponentInfo;
import com.lody.virtual.helper.utils.VLog;
import com.lody.virtual.service.pm.VPackageService;
import com.lody.virtual.service.process.ProviderList;
import com.lody.virtual.service.process.VProcessService;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Lody
 *
 */
class VContentService extends IContentManager.Stub {

	private static final String TAG = VContentService.class.getSimpleName();

	private static final AtomicReference<VContentService> sService = new AtomicReference<>();

	private final ProviderList mProviderList = new ProviderList();

	public static void systemReady(Context context) {
		sService.set(new VContentService());
	}

	public static VContentService getService() {
		return sService.get();
	}

	@Override
	public IActivityManager.ContentProviderHolder getContentProvider(String auth) {
		if (TextUtils.isEmpty(auth)) {
			return null;
		}
		ProviderInfo providerInfo = VPackageService.getService().resolveContentProvider(auth, 0);
		if (providerInfo == null) {
			VLog.d(TAG, "Unable to find Provider who named %s.", auth);
			return null;
		}
		IActivityManager.ContentProviderHolder holder = mProviderList.getHolder(auth);
		if (holder != null) {
			return holder;
		}
		try {
			VLog.d(TAG, "Installing %s...", providerInfo.authority);
			VProcessService.getService().installComponent(VComponentInfo.wrap(providerInfo));
			IActivityManager.ContentProviderHolder result = mProviderList.getHolder(auth);
			if (result == null) {
				VLog.w(TAG, "Unable to getContentProvider : " + auth);
			}
			return result;
		} catch (Throwable e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override
	public void publishContentProviders(List<IActivityManager.ContentProviderHolder> holderList) {

		if (holderList == null || holderList.isEmpty()) {
			return;
		}
		for (IActivityManager.ContentProviderHolder holder : holderList) {

			ProviderInfo providerInfo = holder.info;

			if (holder.provider == null || providerInfo == null || providerInfo.authority == null) {
				continue;
			}

			final String auth = providerInfo.authority;
			IBinder pb = holder.provider.asBinder();
			if (!linkProviderDied(auth, pb)) {
				VLog.e(TAG, "Link Provider(%s) died failed.", auth);
			}

			synchronized (mProviderList) {
				String auths[] = auth.split(";");
				for (String oneAuth : auths) {
					mProviderList.putHolder(oneAuth, holder);
				}
			}
		}
	}

	private boolean linkProviderDied(final String auth, final IBinder binder) {
		if (binder == null) {
			return false;
		}
		try {
			binder.linkToDeath(new IBinder.DeathRecipient() {
				@Override
				public void binderDied() {
					synchronized (mProviderList) {
						mProviderList.removeAuth(auth);
						binder.unlinkToDeath(this, 0);
					}
				}
			}, 0);
			return true;
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return false;
	}

}
