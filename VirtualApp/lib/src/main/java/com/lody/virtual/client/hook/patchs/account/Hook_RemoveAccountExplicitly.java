package com.lody.virtual.client.hook.patchs.account;

import android.accounts.Account;
import android.accounts.IAccountManagerResponse;
import android.os.Bundle;
import android.os.RemoteException;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.local.LocalAccountManager;

import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Lody
 *
 * @see android.accounts.IAccountManager#removeAccountExplicitly(Account)
 *
 */

public class Hook_RemoveAccountExplicitly extends Hook<AccountManagerPatch> {

    /**
     * 这个构造器必须有,用于依赖注入.
     *
     * @param patchObject 注入对象
     */
    public Hook_RemoveAccountExplicitly(AccountManagerPatch patchObject) {
        super(patchObject);
    }

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
                    LocalAccountManager.getInstance().removeAccount(new IAccountManagerResponse.Stub() {
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
