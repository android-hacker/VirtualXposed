package com.lody.virtual.server;

import android.accounts.IAccountManagerResponse;
import android.accounts.Account;
import android.accounts.AuthenticatorDescription;
import android.os.Bundle;


/**
 * Central application service that provides account management.
 * @hide
 */
interface IAccountManager {
    AuthenticatorDescription[] getAuthenticatorTypes(int userId);
    void getAccountsByFeatures(int userId, in IAccountManagerResponse response, in String type, in String[] features);
    String getPreviousName(int userId, in Account account);
    Account[] getAccounts(int userId, in String type);
    void getAuthToken(int userId, in IAccountManagerResponse response, in Account account, in String authTokenType, in boolean notifyOnAuthFailure, in boolean expectActivityLaunch, in Bundle loginOptions);
    void setPassword(int userId, in Account account, in String password);
    void setAuthToken(int userId, in Account account, in String authTokenType, in String authToken);
    void setUserData(int userId, in Account account, in String key, in String value);
    void hasFeatures(int userId, in IAccountManagerResponse response,
    							in Account account, in String[] features);
    void updateCredentials(int userId, in IAccountManagerResponse response, in Account account,
    								  in String authTokenType, in boolean expectActivityLaunch,
    								  in Bundle loginOptions);
    void editProperties(int userId, in IAccountManagerResponse response, in String accountType,
    							   in boolean expectActivityLaunch);
    void getAuthTokenLabel(int userId, in IAccountManagerResponse response, in String accountType,
    								  in String authTokenType);
    String getUserData(int userId, in Account account, in String key);
    String getPassword(int userId, in Account account);
    void confirmCredentials(int userId, in IAccountManagerResponse response, in Account account, in Bundle options, in boolean expectActivityLaunch);
    void addAccount(int userId, in IAccountManagerResponse response, in String accountType,
    						   in String authTokenType, in String[] requiredFeatures,
    						   in boolean expectActivityLaunch, in Bundle optionsIn);
    boolean addAccountExplicitly(int userId, in Account account, in String password, in Bundle extras);
    boolean removeAccountExplicitly(int userId, in Account account);
    void renameAccount(int userId, in IAccountManagerResponse response, in Account accountToRename, in String newName);
    void removeAccount(in int userId, in IAccountManagerResponse response, in Account account,
    							  in boolean expectActivityLaunch);
    void clearPassword(int userId, in Account account);
    boolean accountAuthenticated(int userId, in Account account);
    void invalidateAuthToken(int userId, in String accountType, in String authToken);
    String peekAuthToken(int userId, in Account account, in String authTokenType);

}