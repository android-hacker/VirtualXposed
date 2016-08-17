package com.lody.virtual.client.hook.patchs.mount;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.hook.utils.HookUtils;

import java.lang.reflect.Method;

/**
 * @author Lody
 *
 *
 *         原型: public StorageVolume[] getVolumeList(int uid, String packageName,
 *         int flags)
 */
/* package */ class Hook_GetVolumeList extends Hook {

	@Override
	public String getName() {
		return "getVolumeList";
	}

	@Override
	public boolean beforeHook(Object who, Method method, Object... args) {
		if (args.length >= 1) {
			if (args[0] instanceof Integer) {
				args[0] = VirtualCore.getCore().myUid();
			}
		}
		HookUtils.replaceFirstAppPkg(args);
		return super.beforeHook(who, method, args);
	}

	@Override
	public Object afterHook(Object who, Method method, Object[] args, Object result) throws Throwable {
//		if (result instanceof StorageVolume[]) {
//			StorageVolume[] volumes = (StorageVolume[]) result;
//			for (StorageVolume volume : volumes) {
//				try {
//					Field mPathField = StorageVolume.class.getDeclaredField("mPath");
//					mPathField.setAccessible(true);
//					Object object = mPathField.get(volume);
//					if (mPathField.getType() == String.class) {
//						String path = (String) object;
//						String newPath = VEnvironment.redirectSDCard(VUserHandle.myUserId(), path).getPath();
//						mPathField.set(volume, newPath);
//					} else if (mPathField.getType() == File.class) {
//						File path = (File) object;
//						File newPath = VEnvironment.redirectSDCard(VUserHandle.myUserId(), path.getPath());
//						mPathField.set(volume, newPath);
//					}
//				} catch (Throwable e) {
//					e.printStackTrace();
//				}
//			}
//		}
		return result;
	}
}
