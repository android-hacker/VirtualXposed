package com.lody.virtual.service.am;

import android.app.IActivityManager;
import android.content.pm.ProviderInfo;
import android.os.ConditionVariable;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;

import com.lody.virtual.helper.utils.VLog;
import com.lody.virtual.service.IContentManager;
import com.lody.virtual.service.pm.VPackageService;
import com.lody.virtual.service.process.ProcessRecord;
import com.lody.virtual.service.process.ProviderList;
import com.lody.virtual.service.process.VProcessService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Lody
 *
 */
public class VContentService extends IContentManager.Stub {

	private static final String TAG = VContentService.class.getSimpleName();

	private static final AtomicReference<VContentService> sService = new AtomicReference<>();

	private final ProviderList mProviderList = new ProviderList();

	private Map<String, ConditionVariable> mLaunchingProviders = new HashMap<>();

	public static void systemReady() {
		sService.set(new VContentService());
	}

	public static VContentService getService() {
		return sService.get();
	}

	@Override
	public synchronized IActivityManager.ContentProviderHolder getContentProvider(String name) {
		if (TextUtils.isEmpty(name)) {
			return null;
		}
		ProviderInfo providerInfo = VPackageService.getService().resolveContentProvider(name, 0);
		if (providerInfo == null) {
			VLog.d(TAG, "Unable to find Provider who named %s.", name);
			return null;
		}
		IActivityManager.ContentProviderHolder holder = mProviderList.getHolder(name);
		if (holder != null) {
			return holder;
		}
		VLog.d(TAG, "Installing %s...", providerInfo.authority);
		ProcessRecord r = VProcessService.getService().startProcessLocked(providerInfo);
		if (r == null) {
			return null;
		}
		ConditionVariable variable = new ConditionVariable();
		mLaunchingProviders.put(name, variable);
		variable.block();
		return mProviderList.getHolder(name);
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
			final String authority = providerInfo.authority;
			ConditionVariable lock = mLaunchingProviders.remove(authority);
			if (lock != null) {
				lock.open();
			}
			IBinder pb = holder.provider.asBinder();
			if (!linkProviderDied(authority, pb)) {
				VLog.e(TAG, "Link Provider(%s) died failed.", authority);
			}

			synchronized (mProviderList) {
				String auths[] = authority.split(";");
				for (String oneAuth : auths) {
					mProviderList.putHolder(oneAuth, holder);
				}
			}
		}
	}

	private boolean linkProviderDied(final String authority, final IBinder binder) {
		if (binder == null) {
			return false;
		}
		try {
			binder.linkToDeath(new IBinder.DeathRecipient() {
				@Override
				public void binderDied() {
					synchronized (mProviderList) {
						mProviderList.removeAuthority(authority);
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
