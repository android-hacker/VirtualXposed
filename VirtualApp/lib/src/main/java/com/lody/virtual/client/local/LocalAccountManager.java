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

    public String getPassword(Account account) throws RemoteException {
        return getRemote().getPassword(account);
    }

    public String getUserData(Account account, String key) throws RemoteException {
        return getRemote().getUserData(account, key);
    }

    public AuthenticatorDescription[] getAuthenticatorTypes(int userId) throws RemoteException {
        return getRemote().getAuthenticatorTypes(userId);
    }

    public Account[] getAccounts(String accountType) throws RemoteException {
        return getRemote().getAccounts(accountType);
    }

    public Account[] getAccountsForPackage(String packageName, int uid) throws RemoteException {
        return getRemote().getAccountsForPackage(packageName, uid);
    }

    public Account[] getAccountsByTypeForPackage(String type, String packageName) throws RemoteException {
        return getRemote().getAccountsByTypeForPackage(type, packageName);
    }

    public void hasFeatures(IAccountManagerResponse response, Account account, String[] features) throws RemoteException {
        getRemote().hasFeatures(response, account, features);
    }

    public void getAccountsByFeatures(IAccountManagerResponse response, String accountType, String[] features) throws RemoteException {
        getRemote().getAccountsByFeatures(response, accountType, features);
    }

    public boolean addAccountExplicitly(Account account, String password, Bundle extras) throws RemoteException {
        return getRemote().addAccountExplicitly(account, password, extras);
    }

    public void removeAccount(IAccountManagerResponse response, Account account, boolean expectActivityLaunch) throws RemoteException {
        getRemote().removeAccount(response, account, expectActivityLaunch);
    }

    public boolean removeAccountExplicitly(Account account) throws RemoteException {
        return getRemote().removeAccountExplicitly(account);
    }

    public void copyAccountToUser(IAccountManagerResponse response, Account account, int userFrom, int userTo) throws RemoteException {
        getRemote().copyAccountToUser(response, account, userFrom, userTo);
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

    public void updateAppPermission(Account account, String authTokenType, int uid, boolean value) throws RemoteException {
        getRemote().updateAppPermission(account, authTokenType, uid, value);
    }

    public void getAuthToken(IAccountManagerResponse response, Account account, String authTokenType, boolean notifyOnAuthFailure, boolean expectActivityLaunch, Bundle options) throws RemoteException {
        getRemote().getAuthToken(response, account, authTokenType, notifyOnAuthFailure, expectActivityLaunch, options);
    }

    public void addAccount(IAccountManagerResponse response, String accountType, String authTokenType, String[] requiredFeatures, boolean expectActivityLaunch, Bundle options) throws RemoteException {
        getRemote().addAccount(response, accountType, authTokenType, requiredFeatures, expectActivityLaunch, options);
    }

    public void updateCredentials(IAccountManagerResponse response, Account account, String authTokenType, boolean expectActivityLaunch, Bundle options) throws RemoteException {
        getRemote().updateCredentials(response, account, authTokenType, expectActivityLaunch, options);
    }

    public void editProperties(IAccountManagerResponse response, String accountType, boolean expectActivityLaunch) throws RemoteException {
        getRemote().editProperties(response, accountType, expectActivityLaunch);
    }

    public void confirmCredentialsAsUser(IAccountManagerResponse response, Account account, Bundle options, boolean expectActivityLaunch, int userId) throws RemoteException {
        getRemote().confirmCredentialsAsUser(response, account, options, expectActivityLaunch, userId);
    }

    public boolean accountAuthenticated(Account account) throws RemoteException {
        return getRemote().accountAuthenticated(account);
    }

    public void getAuthTokenLabel(IAccountManagerResponse response, String accountType, String authTokenType) throws RemoteException {
        getRemote().getAuthTokenLabel(response, accountType, authTokenType);
    }

    public boolean addSharedAccountAsUser(Account account, int userId) throws RemoteException {
        return getRemote().addSharedAccountAsUser(account, userId);
    }

    public Account[] getSharedAccountsAsUser(int userId) throws RemoteException {
        return getRemote().getSharedAccountsAsUser(userId);
    }

    public boolean removeSharedAccountAsUser(Account account, int userId) throws RemoteException {
        return getRemote().removeSharedAccountAsUser(account, userId);
    }

    public void renameAccount(IAccountManagerResponse response, Account accountToRename, String newName) throws RemoteException {
        getRemote().renameAccount(response, accountToRename, newName);
    }

    public String getPreviousName(Account account) throws RemoteException {
        return getRemote().getPreviousName(account);
    }

    public boolean renameSharedAccountAsUser(Account accountToRename, String newName, int userId) throws RemoteException {
        return getRemote().renameSharedAccountAsUser(accountToRename, newName, userId);
    }
}
