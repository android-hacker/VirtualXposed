package com.lody.virtual.service;

import android.accounts.IAccountManagerResponse;
import android.accounts.Account;
import android.accounts.AuthenticatorDescription;
import android.os.Bundle;


/**
 * Central application service that provides account management.
 */
interface IAccountManager {
    String getPassword(in Account account);
    String getUserData(in Account account, String key);
    AuthenticatorDescription[] getAuthenticatorTypes();
    Account[] getAccounts(String accountType);
    void hasFeatures(in IAccountManagerResponse response, in Account account, in String[] features);
    void getAccountsByFeatures(in IAccountManagerResponse response, String accountType, in String[] features);
    boolean addAccount(in Account account, String password, in Bundle extras);
    void removeAccount(in IAccountManagerResponse response, in Account account);
    void invalidateAuthToken(String accountType, String authToken);
    String peekAuthToken(in Account account, String authTokenType);
    void setAuthToken(in Account account, String authTokenType, String authToken);
    void setPassword(in Account account, String password);
    void clearPassword(in Account account);
    void setUserData(in Account account, String key, String value);

    void getAuthToken(in IAccountManagerResponse response, in Account account,
        String authTokenType, boolean notifyOnAuthFailure, boolean expectActivityLaunch,
        in Bundle options);
    void addAcount(in IAccountManagerResponse response, String accountType,
        String authTokenType, in String[] requiredFeatures, boolean expectActivityLaunch,
        in Bundle options);
    void updateCredentials(in IAccountManagerResponse response, in Account account,
        String authTokenType, boolean expectActivityLaunch, in Bundle options);
    void editProperties(in IAccountManagerResponse response, String accountType,
        boolean expectActivityLaunch);
    void confirmCredentials(in IAccountManagerResponse response, in Account account,
        in Bundle options, boolean expectActivityLaunch);
}
