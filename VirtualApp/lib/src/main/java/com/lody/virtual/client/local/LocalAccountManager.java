package com.lody.virtual.client.local;

import android.accounts.Account;
import android.accounts.AuthenticatorDescription;
import android.accounts.IAccountManagerResponse;
import android.os.Bundle;
import android.os.RemoteException;

import com.lody.virtual.client.service.ServiceManagerNative;
import com.lody.virtual.service.IAccountManager;

/**
 * @author Lody
 */

public class LocalAccountManager {

    private static LocalAccountManager sMgr = new LocalAccountManager();

    private IAccountManager mRemote;

    public static LocalAccountManager getInstance() {
        return sMgr;
    }

    public IAccountManager getRemote() {
        if (mRemote == null) {
            mRemote = IAccountManager.Stub
                    .asInterface(ServiceManagerNative.getService(ServiceManagerNative.ACCOUNT_MANAGER));
        }
        return mRemote;
    }


    public void confirmCredentials(IAccountManagerResponse response, Account account, Bundle options, boolean expectActivityLaunch) throws RemoteException {
        getRemote().confirmCredentials(response, account, options, expectActivityLaunch);
    }

    public String getPassword(Account account) throws RemoteException {
        return getRemote().getPassword(account);
    }

    public String getUserData(Account account, String key) throws RemoteException {
        return getRemote().getUserData(account, key);
    }

    public AuthenticatorDescription[] getAuthenticatorTypes() throws RemoteException {
        return getRemote().getAuthenticatorTypes();
    }

    public Account[] getAccounts(String accountType) throws RemoteException {
        return getRemote().getAccounts(accountType);
    }

    public void hasFeatures(IAccountManagerResponse response, Account account, String[] features) throws RemoteException {
        getRemote().hasFeatures(response, account, features);
    }

    public void getAccountsByFeatures(IAccountManagerResponse response, String accountType, String[] features) throws RemoteException {
        getRemote().getAccountsByFeatures(response, accountType, features);
    }

    public boolean addAccount(Account account, String password, Bundle extras) throws RemoteException {
        return getRemote().addAccount(account, password, extras);
    }

    public void removeAccount(IAccountManagerResponse response, Account account) throws RemoteException {
        getRemote().removeAccount(response, account);
    }

    public void invalidateAuthToken(String accountType, String authToken) throws RemoteException {
        getRemote().invalidateAuthToken(accountType, authToken);
    }

    public String peekAuthToken(Account account, String authTokenType) throws RemoteException {
        return getRemote().peekAuthToken(account, authTokenType);
    }

    public void setAuthToken(Account account, String authTokenType, String authToken) throws RemoteException {
        getRemote().setAuthToken(account, authTokenType, authToken);
    }

    public void setPassword(Account account, String password) throws RemoteException {
        getRemote().setPassword(account, password);
    }

    public void clearPassword(Account account) throws RemoteException {
        getRemote().clearPassword(account);
    }

    public void setUserData(Account account, String key, String value) throws RemoteException {
        getRemote().setUserData(account, key, value);
    }

    public void getAuthToken(IAccountManagerResponse response, Account account, String authTokenType, boolean notifyOnAuthFailure, boolean expectActivityLaunch, Bundle options) throws RemoteException {
        getRemote().getAuthToken(response, account, authTokenType, notifyOnAuthFailure, expectActivityLaunch, options);
    }

    public void addAcount(IAccountManagerResponse response, String accountType, String authTokenType, String[] requiredFeatures, boolean expectActivityLaunch, Bundle options) throws RemoteException {
        getRemote().addAcount(response, accountType, authTokenType, requiredFeatures, expectActivityLaunch, options);
    }

    public void updateCredentials(IAccountManagerResponse response, Account account, String authTokenType, boolean expectActivityLaunch, Bundle options) throws RemoteException {
        getRemote().updateCredentials(response, account, authTokenType, expectActivityLaunch, options);
    }

    public void editProperties(IAccountManagerResponse response, String accountType, boolean expectActivityLaunch) throws RemoteException {
        getRemote().editProperties(response, accountType, expectActivityLaunch);
    }
}
