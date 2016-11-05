package com.lody.virtual.client.hook.patchs.am;

import android.content.pm.ProviderInfo;
import android.os.IInterface;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.hook.providers.ProviderHook;
import com.lody.virtual.client.ipc.VActivityManager;
import com.lody.virtual.client.ipc.VPackageManager;
import com.lody.virtual.client.stub.StubManifest;
import com.lody.virtual.os.VUserHandle;

import java.lang.reflect.Method;

import mirror.android.app.IActivityManager;

/**
 * @author Lody
 */
/* package */ class GetContentProvider extends Hook {
	@Override
	public String getName() {
		return "getContentProvider";
	}

	@Override
	public Object call(Object who, Method method, Object... args) throws Throwable {
		int nameIdx = getProviderNameIndex();
		String name = (String) args[nameIdx];
		int userId = VUserHandle.myUserId();
		ProviderInfo info = VPackageManager.get().resolveContentProvider(name, 0, userId);
		if (info != null && info.enabled && isAppPkg(info.packageName)) {
			int targetVPid = VActivityManager.get().initProcess(info.packageName, info.processName, userId);
			if (targetVPid == -1) {
				return null;
			}
			args[nameIdx] = StubManifest.getStubAuthority(targetVPid);
			Object holder = method.invoke(who, args);
			if (holder == null) {
				return null;
			}
			IInterface provider = IActivityManager.ContentProviderHolder.provider.get(holder);
			if (provider != null) {
				provider = VActivityManager.get().acquireProviderClient(userId, info);
			}
			IActivityManager.ContentProviderHolder.provider.set(holder, provider);
			IActivityManager.ContentProviderHolder.info.set(holder, info);
			return holder;
		}
		Object holder = method.invoke(who, args);
		if (holder != null) {
			IInterface provider = IActivityManager.ContentProviderHolder.provider.get(holder);
			info = IActivityManager.ContentProviderHolder.info.get(holder);
			if (provider != null) {
				provider = ProviderHook.createProxy(true, info.authority, provider);
			}
			IActivityManager.ContentProviderHolder.provider.set(holder, provider);
			return holder;
		}
		return null;
	}


	public int getProviderNameIndex() {
		return 1;
	}

	@Override
	public boolean isEnable() {
		return isAppProcess();
	}
}
