package com.lody.virtual.client.hook.patchs.am;

import android.app.IApplicationThread;
import android.content.IIntentReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;

import com.lody.virtual.client.env.SpecialComponentList;
import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.hook.utils.HookUtils;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.List;
import java.util.ListIterator;
import java.util.WeakHashMap;

import mirror.android.app.LoadedApk;

/**
 * @author Lody
 * @see android.app.IActivityManager#registerReceiver(IApplicationThread,
 *      String, IIntentReceiver, IntentFilter, String, int)
 */
/* package */ class RegisterReceiver extends Hook {

	private static final int IDX_IIntentReceiver = Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1
			? 2
			: 1;

	private static final int IDX_RequiredPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1
			? 4
			: 3;
	private static final int IDX_IntentFilter = Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1
			? 3
			: 2;

	private WeakHashMap<IBinder, IIntentReceiver.Stub> mProxyIIntentReceiver = new WeakHashMap<>();

	@Override
	public String getName() {
		return "registerReceiver";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		HookUtils.replaceFirstAppPkg(args);
		args[IDX_RequiredPermission] = null;
		IntentFilter filter = (IntentFilter) args[IDX_IntentFilter];
		modifyIntentFilter(filter);
		if (args.length > IDX_IIntentReceiver && IIntentReceiver.class.isInstance(args[IDX_IIntentReceiver])) {
			final IIntentReceiver old = (IIntentReceiver) args[IDX_IIntentReceiver];
			// 防止重复代理
			if (!ProxyIIntentReceiver.class.isInstance(old)) {
				final IBinder token = old.asBinder();
				if (token != null) {
					token.linkToDeath(new IBinder.DeathRecipient() {
						@Override
						public void binderDied() {
							token.unlinkToDeath(this, 0);
							mProxyIIntentReceiver.remove(token);
						}
					}, 0);
					IIntentReceiver.Stub proxyIIntentReceiver = mProxyIIntentReceiver.get(token);
					if (proxyIIntentReceiver == null) {
						proxyIIntentReceiver = new ProxyIIntentReceiver(old);
						mProxyIIntentReceiver.put(token, proxyIIntentReceiver);
					}
					WeakReference mDispatcher = LoadedApk.ReceiverDispatcher.InnerReceiver.mDispatcher.get(old);
					LoadedApk.ReceiverDispatcher.mIIntentReceiver.set(mDispatcher.get(), proxyIIntentReceiver);
					args[IDX_IIntentReceiver] = proxyIIntentReceiver;
				}
			}
		}
		return method.invoke(who, args);
	}

	private void modifyIntentFilter(IntentFilter filter) {
		if (filter != null) {
			List<String> actions = mirror.android.content.IntentFilter.mActions.get(filter);
			ListIterator<String> iterator = actions.listIterator();
			while (iterator.hasNext()) {
				String action = iterator.next();
				if (SpecialComponentList.isActionInBlackList(action)) {
					iterator.remove();
					continue;
				}
				String newAction = SpecialComponentList.modifyAction(action);
				if (newAction != null) {
					iterator.set(newAction);
				}
			}
		}
	}

	@Override
	public boolean isEnable() {
		return isAppProcess();
	}

	private static class ProxyIIntentReceiver extends IIntentReceiver.Stub {
		IIntentReceiver old;

		ProxyIIntentReceiver(IIntentReceiver old) {
			this.old = old;
		}

		@Override
		public void performReceive(Intent intent, int resultCode, String data, Bundle extras, boolean ordered,
				boolean sticky, int sendingUser) throws RemoteException {
			try {
				Intent realIntent = intent.getParcelableExtra("_VA_|_intent_");
				if (realIntent != null) {
					intent = realIntent;
				}
				String action = intent.getAction();
				String oldAction = SpecialComponentList.restoreAction(action);
				if (oldAction != null) {
					intent.setAction(oldAction);
				}
				if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
					old.performReceive(intent, resultCode, data, extras, ordered, sticky, sendingUser);
				} else {
					Method performReceive = old.getClass().getDeclaredMethod("performReceive", Intent.class, int.class,
							String.class, Bundle.class, boolean.class, boolean.class);
					performReceive.setAccessible(true);
					performReceive.invoke(old, intent, resultCode, data, extras, ordered, sticky);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		// @Override
		public void performReceive(Intent intent, int resultCode, String data, Bundle extras, boolean ordered,
				boolean sticky) throws android.os.RemoteException {
			this.performReceive(intent, resultCode, data, extras, ordered, sticky, 0);
		}
	}
}