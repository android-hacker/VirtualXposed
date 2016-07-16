package com.lody.virtual.service;

import android.app.IActivityManager;
import android.content.pm.ProviderInfo;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;

import com.lody.virtual.helper.proto.VComponentInfo;
import com.lody.virtual.helper.utils.XLog;
import com.lody.virtual.service.process.ProviderList;
import com.lody.virtual.service.process.VProcessService;

import java.util.List;

/**
 * @author Lody
 *
 */
public class VContentService extends IContentManager.Stub {

	private static final String TAG = VContentService.class.getSimpleName();

	private static final VContentService sService = new VContentService();

	private final ProviderList mProviderList = new ProviderList();

	public static VContentService getService() {
		return sService;
	}

	@Override
	public IActivityManager.ContentProviderHolder getContentProvider(String auth) {
		if (TextUtils.isEmpty(auth)) {
			return null;
		}
		ProviderInfo providerInfo = VPackageService.getService().resolveContentProvider(auth, 0);
		if (providerInfo == null) {
			XLog.d(TAG, "Unable to find Provider who named %s.", auth);
			return null;
		}
		IActivityManager.ContentProviderHolder holder = mProviderList.getHolder(auth);
		if (holder != null) {
			return holder;
		}
		try {
			XLog.d(TAG, "Installing %s...", providerInfo.authority);
			VProcessService.getService().installComponent(VComponentInfo.wrap(providerInfo));
			IActivityManager.ContentProviderHolder getResult = mProviderList.getHolder(auth);
			if (getResult == null) {
				XLog.w(TAG, "Unable to getContentProvider : " + auth);
			}
			return getResult;
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
				XLog.e(TAG, "Link Provider(%s) died failed.", auth);
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
