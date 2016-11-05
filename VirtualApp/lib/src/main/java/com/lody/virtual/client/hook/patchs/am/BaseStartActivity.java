package com.lody.virtual.client.hook.patchs.am;

import java.lang.reflect.Method;

import com.lody.virtual.client.env.Constants;
import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.ipc.VIntentFilterManager;
import com.lody.virtual.helper.utils.ArrayUtils;
import com.lody.virtual.service.interfaces.IIntentFilterObserver;

import android.content.Intent;
import android.os.RemoteException;

public abstract class BaseStartActivity extends Hook {

	@Override
	public Object call(Object who, Method method, Object... args) throws Throwable {
		int indexOfIntent = ArrayUtils.indexOfFirst(args, Intent.class);
		if (indexOfIntent != -1)
			args[indexOfIntent] = filterIntent((Intent) args[indexOfIntent]);
		else {
			indexOfIntent = ArrayUtils.indexOfFirst(args, Intent[].class);
			if (indexOfIntent == -1)
				return null;

			Intent[] intents = (Intent[]) args[indexOfIntent];
			for (int N = 0; N < intents.length; N++) {
				intents[N] = filterIntent(intents[N]);
			}
		}
		return null;
	}

	public Intent filterIntent(Intent intent) {
		if (Intent.ACTION_INSTALL_PACKAGE.equals(intent.getAction())
				|| (Intent.ACTION_VIEW.equals(intent.getAction()) && "file".equals(intent.getScheme()))
				|| "application/vnd.android.package-archive".equals(intent.getType())) {
			intent.setAction(Constants.ACTION_INSTALL_PACKAGE);
		} else if (Intent.ACTION_UNINSTALL_PACKAGE.equals(intent.getAction())
				|| Intent.ACTION_DELETE.equals(intent.getAction())
				|| (Intent.ACTION_VIEW.equals(intent.getAction()) && "package".equals(intent.getScheme()))) {
			intent.setAction(Constants.ACTION_UNINSTALL_PACKAGE);
		}
		IIntentFilterObserver intentFilter = VIntentFilterManager.getInterface();
		if (intentFilter != null) {
			try {
				return new Intent(intentFilter.filter(intent));
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		return intent;
	}
}
