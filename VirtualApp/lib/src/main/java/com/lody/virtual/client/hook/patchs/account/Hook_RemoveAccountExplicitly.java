package com.lody.virtual.client.hook.patchs.account;

import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.local.VAccountManager;

import android.accounts.Account;
import android.accounts.IAccountManagerResponse;
import android.os.Bundle;
import android.os.RemoteException;

/**
 * @author Lody
 *
 * @see android.accounts.IAccountManager#removeAccountExplicitly(Account)
 *
 */

public class Hook_RemoveAccountExplicitly extends Hook {

	@Override
	public String getName() {
		return "removeAccountExplicitly";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		final Account account = (Account) args[0];
		final AtomicBoolean result = new AtomicBoolean(false);
		final CountDownLatch lock = new CountDownLatch(1);
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					VAccountManager.getInstance().removeAccount(new IAccountManagerResponse.Stub() {
						@Override
						public void onResult(Bundle value) throws RemoteException {
							result.set(true);
						}

						@Override
						public void onError(int errorCode, String errorMessage) throws RemoteException {
							// keep default value
						}
					}, account);
				} catch (RemoteException e) {
					e.printStackTrace();
					// keep default value
				}
				lock.countDown();
			}
		}).start();
		lock.await();
		return result.get();
	}
}
