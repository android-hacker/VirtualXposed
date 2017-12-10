package com.lody.virtual.server.accounts;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorDescription;
import android.accounts.IAccountAuthenticator;
import android.accounts.IAccountAuthenticatorResponse;
import android.accounts.IAccountManagerResponse;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.util.Xml;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.helper.compat.AccountManagerCompat;
import com.lody.virtual.helper.utils.VLog;
import com.lody.virtual.os.VBinder;
import com.lody.virtual.os.VEnvironment;
import com.lody.virtual.os.VUserHandle;
import com.lody.virtual.server.am.VActivityManagerService;
import com.lody.virtual.server.interfaces.IAccountManager;
import com.lody.virtual.server.pm.VPackageManagerService;

import org.xmlpull.v1.XmlPullParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import mirror.com.android.internal.R_Hide;

import static android.accounts.AccountManager.ERROR_CODE_BAD_ARGUMENTS;


/**
 * @author Lody
 */
public class VAccountManagerService implements IAccountManager {

    private static final AtomicReference<VAccountManagerService> sInstance = new AtomicReference<>();
    private static final long CHECK_IN_TIME = 30 * 24 * 60 * 1000L;
    private static final String TAG = VAccountManagerService.class.getSimpleName();
    private final SparseArray<List<VAccount>> accountsByUserId = new SparseArray<>();
    private final LinkedList<AuthTokenRecord> authTokenRecords = new LinkedList<>();
    private final LinkedHashMap<String, Session> mSessions = new LinkedHashMap<>();
    private final AuthenticatorCache cache = new AuthenticatorCache();
    private Context mContext = VirtualCore.get().getContext();
    private long lastAccountChangeTime = 0;


    public static VAccountManagerService get() {
        return sInstance.get();
    }

    public static void systemReady() {
        VAccountManagerService service = new VAccountManagerService();
        service.readAllAccounts();
        sInstance.set(service);
    }


    private static AuthenticatorDescription parseAuthenticatorDescription(Resources resources, String packageName,
                                                                          AttributeSet attributeSet) {
        TypedArray array = resources.obtainAttributes(attributeSet, R_Hide.styleable.AccountAuthenticator.get());
        try {
            String accountType = array.getString(R_Hide.styleable.AccountAuthenticator_accountType.get());
            int label = array.getResourceId(R_Hide.styleable.AccountAuthenticator_label.get(), 0);
            int icon = array.getResourceId(R_Hide.styleable.AccountAuthenticator_icon.get(), 0);
            int smallIcon = array.getResourceId(R_Hide.styleable.AccountAuthenticator_smallIcon.get(), 0);
            int accountPreferences = array.getResourceId(R_Hide.styleable.AccountAuthenticator_accountPreferences.get(), 0);
            boolean customTokens = array.getBoolean(R_Hide.styleable.AccountAuthenticator_customTokens.get(), false);
            if (TextUtils.isEmpty(accountType)) {
                return null;
            }
            return new AuthenticatorDescription(accountType, packageName, label, icon, smallIcon, accountPreferences,
                    customTokens);
        } finally {
            array.recycle();
        }
    }


    @Override
    public AuthenticatorDescription[] getAuthenticatorTypes(int userId) {
        synchronized (cache) {
            AuthenticatorDescription[] descArray = new AuthenticatorDescription[cache.authenticators.size()];
            int i = 0;
            for (AuthenticatorInfo info : cache.authenticators.values()) {
                descArray[i] = info.desc;
                i++;
            }
            return descArray;
        }
    }

    @Override
    public void getAccountsByFeatures(int userId, IAccountManagerResponse response, String type, String[] features) {
        if (response == null) throw new IllegalArgumentException("response is null");
        if (type == null) throw new IllegalArgumentException("accountType is null");
        AuthenticatorInfo info = getAuthenticatorInfo(type);
        if (info == null) {
            Bundle bundle = new Bundle();
            bundle.putParcelableArray(AccountManager.KEY_ACCOUNTS, new Account[0]);
            try {
                response.onResult(bundle);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            return;
        }

        if (features == null || features.length == 0) {
            Bundle bundle = new Bundle();
            bundle.putParcelableArray(AccountManager.KEY_ACCOUNTS, getAccounts(userId, type));
            try {
                response.onResult(bundle);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            new GetAccountsByTypeAndFeatureSession(response, userId, info, features).bind();
        }
    }

    @Override
    public final String getPreviousName(int userId, Account account) {
        if (account == null) throw new IllegalArgumentException("account is null");
        synchronized (accountsByUserId) {
            String previousName = null;
            VAccount vAccount = getAccount(userId, account);
            if (vAccount != null) {
                previousName = vAccount.previousName;
            }
            return previousName;
        }
    }


    @Override
    public Account[] getAccounts(int userId, String type) {
        List<Account> accountList = getAccountList(userId, type);
        return accountList.toArray(new Account[accountList.size()]);
    }


    private List<Account> getAccountList(int userId, String type) {
        synchronized (accountsByUserId) {
            List<Account> accounts = new ArrayList<>();
            List<VAccount> vAccounts = accountsByUserId.get(userId);
            if (vAccounts != null) {
                for (VAccount vAccount : vAccounts) {
                    if (type == null || vAccount.type.equals(type)) {
                        accounts.add(new Account(vAccount.name, vAccount.type));
                    }
                }
            }
            return accounts;
        }
    }

    @Override
    public final void getAuthToken(final int userId, final IAccountManagerResponse response, final Account account, final String authTokenType, final boolean notifyOnAuthFailure, boolean expectActivityLaunch, final Bundle loginOptions) {
        if (response == null) {
            throw new IllegalArgumentException("response is null");
        }
        try {
            if (account == null) {
                VLog.w(TAG, "getAuthToken called with null account");
                response.onError(ERROR_CODE_BAD_ARGUMENTS, "account is null");
                return;
            }
            if (authTokenType == null) {
                VLog.w(TAG, "getAuthToken called with null authTokenType");
                response.onError(ERROR_CODE_BAD_ARGUMENTS, "authTokenType is null");
                return;
            }
        } catch (RemoteException e) {
            e.printStackTrace();
            return;
        }
        AuthenticatorInfo info = getAuthenticatorInfo(account.type);
        if (info == null) {
            try {
                response.onError(ERROR_CODE_BAD_ARGUMENTS, "account.type does not exist");
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            return;
        }
        // Get the calling package. We will use it for the purpose of caching.
        final String callerPkg = loginOptions.getString(AccountManagerCompat.KEY_ANDROID_PACKAGE_NAME);
        final boolean customTokens = info.desc.customTokens;

        loginOptions.putInt(AccountManager.KEY_CALLER_UID, VBinder.getCallingUid());
        loginOptions.putInt(AccountManager.KEY_CALLER_PID, Binder.getCallingPid());
        if (notifyOnAuthFailure) {
            loginOptions.putBoolean(AccountManagerCompat.KEY_NOTIFY_ON_FAILURE, true);
        }
        if (!customTokens) {
            VAccount vAccount;
            synchronized (accountsByUserId) {
                vAccount = getAccount(userId, account);
            }
            String authToken = vAccount != null ? vAccount.authTokens.get(authTokenType) : null;
            if (authToken != null) {
                Bundle result = new Bundle();
                result.putString(AccountManager.KEY_AUTHTOKEN, authToken);
                result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
                result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
                onResult(response, result);
                return;
            }
        }
        if (customTokens) {
            String authToken = getCustomAuthToken(userId, account, authTokenType, callerPkg);
            if (authToken != null) {
                Bundle result = new Bundle();
                result.putString(AccountManager.KEY_AUTHTOKEN, authToken);
                result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
                result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
                onResult(response, result);
                return;
            }
        }
        new Session(response, userId, info, expectActivityLaunch, false, account.name) {

            @Override
            protected String toDebugString(long now) {
                return super.toDebugString(now) + ", getAuthToken"
                        + ", " + account
                        + ", authTokenType " + authTokenType
                        + ", loginOptions " + loginOptions
                        + ", notifyOnAuthFailure " + notifyOnAuthFailure;
            }

            @Override
            public void run() throws RemoteException {
                mAuthenticator.getAuthToken(this, account, authTokenType, loginOptions);
            }

            @Override
            public void onResult(Bundle result) throws RemoteException {
                if (result != null) {
                    String authToken = result.getString(AccountManager.KEY_AUTHTOKEN);
                    if (authToken != null) {
                        String name = result.getString(AccountManager.KEY_ACCOUNT_NAME);
                        String type = result.getString(AccountManager.KEY_ACCOUNT_TYPE);
                        if (TextUtils.isEmpty(type) || TextUtils.isEmpty(name)) {
                            onError(AccountManager.ERROR_CODE_INVALID_RESPONSE,
                                    "the type and name should not be empty");
                            return;
                        }
                        if (!customTokens) {
                            synchronized (accountsByUserId) {
                                VAccount account = getAccount(userId, name, type);
                                if (account == null) {
                                    List<VAccount> accounts = accountsByUserId.get(userId);
                                    if (accounts == null) {
                                        accounts = new ArrayList<>();
                                        accountsByUserId.put(userId, accounts);
                                    }
                                    account = new VAccount(userId, new Account(name, type));
                                    accounts.add(account);
                                    saveAllAccounts();
                                }
                            }
                        }
                        long expiryMillis = result.getLong(
                                AccountManagerCompat.KEY_CUSTOM_TOKEN_EXPIRY, 0L);
                        if (customTokens
                                && expiryMillis > System.currentTimeMillis()) {
                            AuthTokenRecord record = new AuthTokenRecord(userId, account, authTokenType, callerPkg, authToken, expiryMillis);
                            synchronized (authTokenRecords) {
                                authTokenRecords.remove(record);
                                authTokenRecords.add(record);
                            }
                        }
                    }
                    Intent intent = result.getParcelable(AccountManager.KEY_INTENT);
                    if (intent != null && notifyOnAuthFailure && !customTokens) {
                        // TODO: send Signin error Notification
                    }
                }
                super.onResult(result);
            }
        }.bind();
    }


    @Override
    public void setPassword(int userId, Account account, String password) {
        if (account == null) throw new IllegalArgumentException("account is null");
        setPasswordInternal(userId, account, password);
    }

    private void setPasswordInternal(int userId, Account account, String password) {
        synchronized (accountsByUserId) {
            VAccount vAccount = getAccount(userId, account);
            if (vAccount != null) {
                vAccount.password = password;
                vAccount.authTokens.clear();
                saveAllAccounts();
                synchronized (authTokenRecords) {
                    Iterator<AuthTokenRecord> iterator = authTokenRecords.iterator();
                    while (iterator.hasNext()) {
                        AuthTokenRecord record = iterator.next();
                        if (record.userId == userId && record.account.equals(account)) {
                            iterator.remove();
                        }
                    }
                }
                sendAccountsChangedBroadcast(userId);
            }
        }
    }

    @Override
    public void setAuthToken(int userId, Account account, String authTokenType, String authToken) {
        if (account == null) throw new IllegalArgumentException("account is null");
        if (authTokenType == null) throw new IllegalArgumentException("authTokenType is null");
        synchronized (accountsByUserId) {
            VAccount vAccount = getAccount(userId, account);
            if (vAccount != null) {
                // FIXME: cancelNotification
                vAccount.authTokens.put(authTokenType, authToken);
                this.saveAllAccounts();
            }
        }
    }


    @Override
    public void setUserData(int userId, Account account, String key, String value) {
        if (key == null) throw new IllegalArgumentException("key is null");
        if (account == null) throw new IllegalArgumentException("account is null");
        VAccount vAccount = getAccount(userId, account);
        if (vAccount != null) {
            synchronized (accountsByUserId) {
                vAccount.userDatas.put(key, value);
                saveAllAccounts();
            }
        }
    }


    @Override
    public void hasFeatures(int userId, IAccountManagerResponse response,
                            final Account account, final String[] features) {
        if (response == null) throw new IllegalArgumentException("response is null");
        if (account == null) throw new IllegalArgumentException("account is null");
        if (features == null) throw new IllegalArgumentException("features is null");
        AuthenticatorInfo info = this.getAuthenticatorInfo(account.type);
        if (info == null) {
            try {
                response.onError(ERROR_CODE_BAD_ARGUMENTS, "account.type does not exist");
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            return;
        }
        new Session(response, userId, info, false, true, account.name) {

            @Override
            public void run() throws RemoteException {
                try {
                    mAuthenticator.hasFeatures(this, account, features);
                } catch (RemoteException e) {
                    onError(AccountManager.ERROR_CODE_REMOTE_EXCEPTION, "remote exception");
                }
            }

            @Override
            public void onResult(Bundle result) throws RemoteException {
                IAccountManagerResponse response = getResponseAndClose();
                if (response != null) {
                    try {
                        if (result == null) {
                            response.onError(AccountManager.ERROR_CODE_INVALID_RESPONSE, "null bundle");
                            return;
                        }
                        Log.v(TAG, getClass().getSimpleName() + " calling onResult() on response "
                                + response);
                        final Bundle newResult = new Bundle();
                        newResult.putBoolean(AccountManager.KEY_BOOLEAN_RESULT,
                                result.getBoolean(AccountManager.KEY_BOOLEAN_RESULT, false));
                        response.onResult(newResult);
                    } catch (RemoteException e) {
                        // if the caller is dead then there is no one to care about remote exceptions
                        Log.v(TAG, "failure while notifying response", e);
                    }
                }
            }
        }.bind();
    }


    @Override
    public void updateCredentials(int userId, final IAccountManagerResponse response, final Account account,
                                  final String authTokenType, final boolean expectActivityLaunch,
                                  final Bundle loginOptions) {
        if (response == null) throw new IllegalArgumentException("response is null");
        if (account == null) throw new IllegalArgumentException("account is null");
        if (authTokenType == null) throw new IllegalArgumentException("authTokenType is null");
        AuthenticatorInfo info = this.getAuthenticatorInfo(account.type);
        if (info == null) {
            try {
                response.onError(ERROR_CODE_BAD_ARGUMENTS, "account.type does not exist");
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            return;
        }
        new Session(response, userId, info, expectActivityLaunch, false, account.name) {

            @Override
            public void run() throws RemoteException {
                mAuthenticator.updateCredentials(this, account, authTokenType, loginOptions);
            }

            @Override
            protected String toDebugString(long now) {
                if (loginOptions != null) loginOptions.keySet();
                return super.toDebugString(now) + ", updateCredentials"
                        + ", " + account
                        + ", authTokenType " + authTokenType
                        + ", loginOptions " + loginOptions;
            }

        }.bind();
    }

    @Override
    public String getPassword(int userId, Account account) {
        if (account == null) throw new IllegalArgumentException("account is null");
        synchronized (accountsByUserId) {
            VAccount vAccount = getAccount(userId, account);
            if (vAccount != null) {
                return vAccount.password;
            }
            return null;
        }
    }

    @Override
    public String getUserData(int userId, Account account, String key) {
        if (account == null) throw new IllegalArgumentException("account is null");
        if (key == null) throw new IllegalArgumentException("key is null");
        synchronized (accountsByUserId) {
            VAccount vAccount = getAccount(userId, account);
            if (vAccount != null) {
                return vAccount.userDatas.get(key);
            }
            return null;
        }
    }

    @Override
    public void editProperties(int userId, IAccountManagerResponse response, final String accountType,
                               final boolean expectActivityLaunch) {
        if (response == null) throw new IllegalArgumentException("response is null");
        if (accountType == null) throw new IllegalArgumentException("accountType is null");
        AuthenticatorInfo info = this.getAuthenticatorInfo(accountType);
        if (info == null) {
            try {
                response.onError(ERROR_CODE_BAD_ARGUMENTS, "account.type does not exist");
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            return;
        }
        new Session(response, userId, info, expectActivityLaunch, true, null) {

            @Override
            public void run() throws RemoteException {
                mAuthenticator.editProperties(this, mAuthenticatorInfo.desc.type);
            }

            @Override
            protected String toDebugString(long now) {
                return super.toDebugString(now) + ", editProperties"
                        + ", accountType " + accountType;
            }

        }.bind();

    }


    @Override
    public void getAuthTokenLabel(int userId, IAccountManagerResponse response, final String accountType,
                                  final String authTokenType) {
        if (accountType == null) throw new IllegalArgumentException("accountType is null");
        if (authTokenType == null) throw new IllegalArgumentException("authTokenType is null");
        AuthenticatorInfo info = getAuthenticatorInfo(accountType);
        if (info == null) {
            try {
                response.onError(ERROR_CODE_BAD_ARGUMENTS, "account.type does not exist");
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            return;
        }
        new Session(response, userId, info, false, false, null) {

            @Override
            public void run() throws RemoteException {
                mAuthenticator.getAuthTokenLabel(this, authTokenType);
            }

            @Override
            public void onResult(Bundle result) throws RemoteException {
                if (result != null) {
                    String label = result.getString(AccountManager.KEY_AUTH_TOKEN_LABEL);
                    Bundle bundle = new Bundle();
                    bundle.putString(AccountManager.KEY_AUTH_TOKEN_LABEL, label);
                    super.onResult(bundle);
                } else {
                    super.onResult(null);
                }
            }
        }.bind();
    }

    public void confirmCredentials(int userId, IAccountManagerResponse response, final Account account, final Bundle options, final boolean expectActivityLaunch) {
        if (response == null) throw new IllegalArgumentException("response is null");
        if (account == null) throw new IllegalArgumentException("account is null");
        AuthenticatorInfo info = getAuthenticatorInfo(account.type);
        if (info == null) {
            try {
                response.onError(ERROR_CODE_BAD_ARGUMENTS, "account.type does not exist");
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            return;
        }
        new Session(response, userId, info, expectActivityLaunch, true, account.name, true, true) {

            @Override
            public void run() throws RemoteException {
                mAuthenticator.confirmCredentials(this, account, options);
            }

        }.bind();

    }

    @Override
    public void addAccount(int userId, final IAccountManagerResponse response, final String accountType,
                           final String authTokenType, final String[] requiredFeatures,
                           final boolean expectActivityLaunch, final Bundle optionsIn) {
        if (response == null) throw new IllegalArgumentException("response is null");
        if (accountType == null) throw new IllegalArgumentException("accountType is null");
        AuthenticatorInfo info = getAuthenticatorInfo(accountType);
        if (info == null) {
            try {
                response.onError(ERROR_CODE_BAD_ARGUMENTS, "account.type does not exist");
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            return;
        }
        new Session(response, userId, info, expectActivityLaunch, true, null, false, true) {

            @Override
            public void run() throws RemoteException {
                mAuthenticator.addAccount(this, mAuthenticatorInfo.desc.type, authTokenType, requiredFeatures,
                        optionsIn);
            }

            @Override
            protected String toDebugString(long now) {
                return super.toDebugString(now) + ", addAccount"
                        + ", accountType " + accountType
                        + ", requiredFeatures "
                        + (requiredFeatures != null
                        ? TextUtils.join(",", requiredFeatures)
                        : null);
            }

        }.bind();

    }

    @Override
    public boolean addAccountExplicitly(int userId, Account account, String password, Bundle extras) {
        if (account == null) throw new IllegalArgumentException("account is null");
        return insertAccountIntoDatabase(userId, account, password, extras);
    }

    @Override
    public boolean removeAccountExplicitly(int userId, Account account) {
        return account != null && removeAccountInternal(userId, account);
    }

    @Override
    public void renameAccount(int userId, IAccountManagerResponse response, Account accountToRename, String newName) {
        if (accountToRename == null) throw new IllegalArgumentException("account is null");
        Account resultingAccount = renameAccountInternal(userId, accountToRename, newName);
        Bundle result = new Bundle();
        result.putString(AccountManager.KEY_ACCOUNT_NAME, resultingAccount.name);
        result.putString(AccountManager.KEY_ACCOUNT_TYPE, resultingAccount.type);
        try {
            response.onResult(result);
        } catch (RemoteException e) {
            Log.w(TAG, e.getMessage());
        }
    }

    @Override
    public void removeAccount(final int userId, IAccountManagerResponse response, final Account account,
                              boolean expectActivityLaunch) {
        if (response == null) throw new IllegalArgumentException("response is null");
        if (account == null) throw new IllegalArgumentException("account is null");
        AuthenticatorInfo info = this.getAuthenticatorInfo(account.type);
        if (info == null) {
            try {
                response.onError(ERROR_CODE_BAD_ARGUMENTS, "account.type does not exist");
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            return;
        }
        // FIXME: Cancel Notification

        new Session(response, userId, info, expectActivityLaunch, true, account.name) {
            @Override
            protected String toDebugString(long now) {
                return super.toDebugString(now) + ", removeAccount"
                        + ", account " + account;
            }

            @Override
            public void run() throws RemoteException {
                mAuthenticator.getAccountRemovalAllowed(this, account);
            }

            @Override
            public void onResult(Bundle result) throws RemoteException {
                if (result != null && result.containsKey(AccountManager.KEY_BOOLEAN_RESULT)
                        && !result.containsKey(AccountManager.KEY_INTENT)) {
                    final boolean removalAllowed = result.getBoolean(AccountManager.KEY_BOOLEAN_RESULT);
                    if (removalAllowed) {
                        removeAccountInternal(userId, account);
                    }
                    IAccountManagerResponse response = getResponseAndClose();
                    if (response != null) {
                        Log.v(TAG, getClass().getSimpleName() + " calling onResult() on response "
                                + response);
                        Bundle result2 = new Bundle();
                        result2.putBoolean(AccountManager.KEY_BOOLEAN_RESULT, removalAllowed);
                        try {
                            response.onResult(result2);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                }
                super.onResult(result);
            }

        }.bind();

    }

    @Override
    public void clearPassword(int userId, Account account) {
        if (account == null) throw new IllegalArgumentException("account is null");
        setPasswordInternal(userId, account, null);
    }

    private boolean removeAccountInternal(int userId, Account account) {
        List<VAccount> accounts = accountsByUserId.get(userId);
        if (accounts != null) {
            Iterator<VAccount> iterator = accounts.iterator();
            while (iterator.hasNext()) {
                VAccount vAccount = iterator.next();
                if (userId == vAccount.userId
                        && TextUtils.equals(vAccount.name, account.name)
                        && TextUtils.equals(account.type, vAccount.type)) {
                    iterator.remove();
                    saveAllAccounts();
                    sendAccountsChangedBroadcast(userId);
                    return true;
                }
            }
        }
        return false;
    }


    @Override
    public boolean accountAuthenticated(int userId, final Account account) {
        if (account == null) {
            throw new IllegalArgumentException("account is null");
        }
        synchronized (accountsByUserId) {
            VAccount vAccount = getAccount(userId, account);
            if (vAccount != null) {
                vAccount.lastAuthenticatedTime = System.currentTimeMillis();
                saveAllAccounts();
                return true;
            }
            return false;
        }
    }

    @Override
    public void invalidateAuthToken(int userId, String accountType, String authToken) {
        if (accountType == null) throw new IllegalArgumentException("accountType is null");
        if (authToken == null) throw new IllegalArgumentException("authToken is null");
        synchronized (accountsByUserId) {
            List<VAccount> accounts = accountsByUserId.get(userId);
            if (accounts != null) {
                boolean changed = false;
                for (VAccount account : accounts) {
                    if (account.type.equals(accountType)) {
                        account.authTokens.values().remove(authToken);
                        changed = true;
                    }
                }
                if (changed) {
                    saveAllAccounts();
                }
            }
            synchronized (authTokenRecords) {
                Iterator<AuthTokenRecord> iterator = authTokenRecords.iterator();
                while (iterator.hasNext()) {
                    AuthTokenRecord record = iterator.next();
                    if (record.userId == userId && record.authTokenType.equals(accountType)
                            && record.authToken.equals(authToken)) {
                        iterator.remove();
                    }
                }
            }
        }
    }


    private Account renameAccountInternal(int userId, Account accountToRename, String newName) {
        // TODO: Cancel Notification
        synchronized (accountsByUserId) {
            VAccount vAccount = getAccount(userId, accountToRename);
            if (vAccount != null) {
                vAccount.previousName = vAccount.name;
                vAccount.name = newName;
                saveAllAccounts();
                Account newAccount = new Account(vAccount.name, vAccount.type);
                synchronized (authTokenRecords) {
                    for (AuthTokenRecord record : authTokenRecords) {
                        if (record.userId == userId && record.account.equals(accountToRename)) {
                            record.account = newAccount;
                        }
                    }
                }
                sendAccountsChangedBroadcast(userId);
                return newAccount;
            }
        }
        return accountToRename;
    }

    @Override
    public String peekAuthToken(int userId, Account account, String authTokenType) {
        if (account == null) throw new IllegalArgumentException("account is null");
        if (authTokenType == null) throw new IllegalArgumentException("authTokenType is null");
        synchronized (accountsByUserId) {
            VAccount vAccount = getAccount(userId, account);
            if (vAccount != null) {
                return vAccount.authTokens.get(authTokenType);
            }
            return null;
        }
    }


    private String getCustomAuthToken(int userId, Account account, String authTokenType, String packageName) {
        AuthTokenRecord record = new AuthTokenRecord(userId, account, authTokenType, packageName);
        String authToken = null;
        long now = System.currentTimeMillis();
        synchronized (authTokenRecords) {
            Iterator<AuthTokenRecord> iterator = authTokenRecords.iterator();
            while (iterator.hasNext()) {
                AuthTokenRecord one = iterator.next();
                if (one.expiryEpochMillis > 0 && one.expiryEpochMillis < now) {
                    iterator.remove();
                } else if (record.equals(one)) {
                    authToken = record.authToken;
                }
            }
        }
        return authToken;
    }

    private void onResult(IAccountManagerResponse response, Bundle result) {
        try {
            response.onResult(result);
        } catch (RemoteException e) {
            // if the caller is dead then there is no one to care about remote
            // exceptions
            e.printStackTrace();
        }
    }

    private AuthenticatorInfo getAuthenticatorInfo(String type) {
        synchronized (cache) {
            return type == null ? null : cache.authenticators.get(type);
        }
    }


    private VAccount getAccount(int userId, Account account) {
        return this.getAccount(userId, account.name, account.type);
    }

    private boolean insertAccountIntoDatabase(int userId, Account account, String password, Bundle extras) {
        if (account == null) {
            return false;
        }
        synchronized (accountsByUserId) {
            VAccount vAccount = new VAccount(userId, account);
            vAccount.password = password;
            // convert the [Bundle] to [Map<String, String>]
            if (extras != null) {
                for (String key : extras.keySet()) {
                    Object value = extras.get(key);
                    if (value instanceof String) {
                        vAccount.userDatas.put(key, (String) value);
                    }
                }
            }
            List<VAccount> accounts = accountsByUserId.get(userId);
            if (accounts == null) {
                accounts = new ArrayList<>();
                accountsByUserId.put(userId, accounts);
            }
            accounts.add(vAccount);
            saveAllAccounts();
            sendAccountsChangedBroadcast(vAccount.userId);
            return true;
        }
    }

    private void sendAccountsChangedBroadcast(int userId) {
        Intent intent = new Intent(AccountManager.LOGIN_ACCOUNTS_CHANGED_ACTION);
        VActivityManagerService.get().sendBroadcastAsUser(intent, new VUserHandle(userId));
        broadcastCheckInNowIfNeed(userId);
    }

    private void broadcastCheckInNowIfNeed(int userId) {
        long time = System.currentTimeMillis();
        if (Math.abs(time - lastAccountChangeTime) > CHECK_IN_TIME) {
            lastAccountChangeTime = time;
            saveAllAccounts();
            Intent intent = new Intent("android.server.checkin.CHECKIN_NOW");
            VActivityManagerService.get().sendBroadcastAsUser(intent, new VUserHandle(userId));
        }
    }

    /**
     * Serializing all accounts
     */
    private void saveAllAccounts() {
        File accountFile = VEnvironment.getAccountConfigFile();
        Parcel dest = Parcel.obtain();
        try {
            dest.writeInt(1);
            List<VAccount> accounts = new ArrayList<>();
            for (int i = 0; i < this.accountsByUserId.size(); i++) {
                List<VAccount> list = this.accountsByUserId.valueAt(i);
                if (list != null) {
                    accounts.addAll(list);
                }
            }
            dest.writeInt(accounts.size());
            for (VAccount account : accounts) {
                account.writeToParcel(dest, 0);
            }
            dest.writeLong(lastAccountChangeTime);
            FileOutputStream fileOutputStream = new FileOutputStream(accountFile);
            fileOutputStream.write(dest.marshall());
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        dest.recycle();
    }

    /**
     * Read all accounts from file.
     */
    private void readAllAccounts() {
        File accountFile = VEnvironment.getAccountConfigFile();
        refreshAuthenticatorCache(null);
        if (accountFile.exists()) {
            accountsByUserId.clear();
            Parcel dest = Parcel.obtain();
            try {
                FileInputStream is = new FileInputStream(accountFile);
                byte[] bytes = new byte[(int) accountFile.length()];
                int readLength = is.read(bytes);
                is.close();
                if (readLength != bytes.length) {
                    throw new IOException(String.format(Locale.ENGLISH, "Expect length %d, but got %d.", bytes.length, readLength));
                }
                dest.unmarshall(bytes, 0, bytes.length);
                dest.setDataPosition(0);
                dest.readInt(); // skip the magic
                int size = dest.readInt(); // the VAccount's size we need to read
                boolean invalid = false;
                while (size-- > 0) {
                    VAccount account = new VAccount(dest);
                    VLog.d(TAG, "Reading account : " + account.type);
                    AuthenticatorInfo info = cache.authenticators.get(account.type);
                    if (info != null) {
                        List<VAccount> accounts = accountsByUserId.get(account.userId);
                        if (accounts == null) {
                            accounts = new ArrayList<>();
                            accountsByUserId.put(account.userId, accounts);
                        }
                        accounts.add(account);
                    } else {
                        invalid = true;
                    }
                }
                lastAccountChangeTime = dest.readLong();
                if (invalid) {
                    saveAllAccounts();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                dest.recycle();
            }
        }
    }


    private VAccount getAccount(int userId, String accountName, String accountType) {
        List<VAccount> accounts = accountsByUserId.get(userId);
        if (accounts != null) {
            for (VAccount account : accounts) {
                if (TextUtils.equals(account.name, accountName) && TextUtils.equals(account.type, accountType)) {
                    return account;
                }
            }
        }
        return null;
    }


    public void refreshAuthenticatorCache(String packageName) {
        cache.authenticators.clear();
        Intent intent = new Intent(AccountManager.ACTION_AUTHENTICATOR_INTENT);
        if (packageName != null) {
            intent.setPackage(packageName);
        }
        generateServicesMap(
                VPackageManagerService.get().queryIntentServices(intent, null, PackageManager.GET_META_DATA, 0),
                cache.authenticators, new RegisteredServicesParser());
    }

    private void generateServicesMap(List<ResolveInfo> services, Map<String, AuthenticatorInfo> map,
                                     RegisteredServicesParser accountParser) {
        for (ResolveInfo info : services) {
            XmlResourceParser parser = accountParser.getParser(mContext, info.serviceInfo,
                    AccountManager.AUTHENTICATOR_META_DATA_NAME);
            if (parser != null) {
                try {
                    AttributeSet attributeSet = Xml.asAttributeSet(parser);
                    int type;
                    while ((type = parser.next()) != XmlPullParser.END_DOCUMENT && type != XmlPullParser.START_TAG) {
                        // Nothing to do
                    }
                    if (AccountManager.AUTHENTICATOR_ATTRIBUTES_NAME.equals(parser.getName())) {
                        AuthenticatorDescription desc = parseAuthenticatorDescription(
                                accountParser.getResources(mContext, info.serviceInfo.applicationInfo),
                                info.serviceInfo.packageName, attributeSet);
                        if (desc != null) {
                            map.put(desc.type, new AuthenticatorInfo(desc, info.serviceInfo));
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    final static class AuthTokenRecord {
        public int userId;
        public Account account;
        public long expiryEpochMillis;
        public String authToken;
        private String authTokenType;
        private String packageName;

        AuthTokenRecord(int userId, Account account, String authTokenType, String packageName, String authToken,
                        long expiryEpochMillis) {
            this.userId = userId;
            this.account = account;
            this.authTokenType = authTokenType;
            this.packageName = packageName;
            this.authToken = authToken;
            this.expiryEpochMillis = expiryEpochMillis;
        }

        AuthTokenRecord(int userId, Account account, String authTokenType, String packageName) {
            this.userId = userId;
            this.account = account;
            this.authTokenType = authTokenType;
            this.packageName = packageName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            AuthTokenRecord that = (AuthTokenRecord) o;
            return userId == that.userId
                    && account.equals(that.account)
                    && authTokenType.equals(that.authTokenType)
                    && packageName.equals(that.packageName);
        }

        @Override
        public int hashCode() {
            return ((this.userId * 31 + this.account.hashCode()) * 31
                    + this.authTokenType.hashCode()) * 31
                    + this.packageName.hashCode();
        }
    }

    private final class AuthenticatorInfo {
        final AuthenticatorDescription desc;
        final ServiceInfo serviceInfo;

        AuthenticatorInfo(AuthenticatorDescription desc, ServiceInfo info) {
            this.desc = desc;
            this.serviceInfo = info;
        }
    }

    private final class AuthenticatorCache {
        final Map<String, AuthenticatorInfo> authenticators = new HashMap<>();
    }

    private abstract class Session extends IAccountAuthenticatorResponse.Stub
            implements IBinder.DeathRecipient, ServiceConnection {
        final int mUserId;
        final AuthenticatorInfo mAuthenticatorInfo;
        private final boolean mStripAuthTokenFromResult;
        public int mNumResults;
        IAccountAuthenticator mAuthenticator;
        private IAccountManagerResponse mResponse;
        private boolean mExpectActivityLaunch;
        private long mCreationTime;
        private String mAccountName;
        private boolean mAuthDetailsRequired;
        private boolean mUpdateLastAuthenticatedTime;
        private int mNumRequestContinued;
        private int mNumErrors;


        Session(IAccountManagerResponse response, int userId, AuthenticatorInfo info, boolean expectActivityLaunch, boolean stripAuthTokenFromResult, String accountName, boolean authDetailsRequired, boolean updateLastAuthenticatedTime) {
            if (info == null) throw new IllegalArgumentException("accountType is null");
            this.mStripAuthTokenFromResult = stripAuthTokenFromResult;
            this.mResponse = response;
            this.mUserId = userId;
            this.mAuthenticatorInfo = info;
            this.mExpectActivityLaunch = expectActivityLaunch;
            this.mCreationTime = SystemClock.elapsedRealtime();
            this.mAccountName = accountName;
            this.mAuthDetailsRequired = authDetailsRequired;
            this.mUpdateLastAuthenticatedTime = updateLastAuthenticatedTime;
            synchronized (mSessions) {
                mSessions.put(toString(), this);
            }
            if (response != null) {
                try {
                    response.asBinder().linkToDeath(this, 0 /* flags */);
                } catch (RemoteException e) {
                    mResponse = null;
                    binderDied();
                }
            }
        }

        Session(IAccountManagerResponse response, int userId, AuthenticatorInfo info, boolean expectActivityLaunch, boolean stripAuthTokenFromResult, String accountName) {
            this(response, userId, info, expectActivityLaunch, stripAuthTokenFromResult, accountName, false, false);
        }

        IAccountManagerResponse getResponseAndClose() {
            if (mResponse == null) {
                // this session has already been closed
                return null;
            }
            IAccountManagerResponse response = mResponse;
            close(); // this clears mResponse so we need to save the response before this call
            return response;
        }

        private void close() {
            synchronized (mSessions) {
                if (mSessions.remove(toString()) == null) {
                    // the session was already closed, so bail out now
                    return;
                }
            }
            if (mResponse != null) {
                // stop listening for response deaths
                mResponse.asBinder().unlinkToDeath(this, 0 /* flags */);

                // clear this so that we don't accidentally send any further results
                mResponse = null;
            }
            unbind();
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mAuthenticator = IAccountAuthenticator.Stub.asInterface(service);
            try {
                run();
            } catch (RemoteException e) {
                onError(AccountManager.ERROR_CODE_REMOTE_EXCEPTION,
                        "remote exception");
            }
        }

        @Override
        public void onRequestContinued() {
            mNumRequestContinued++;
        }

        @Override
        public void onError(int errorCode, String errorMessage) {
            mNumErrors++;
            IAccountManagerResponse response = getResponseAndClose();
            if (response != null) {
                Log.v(TAG, getClass().getSimpleName()
                        + " calling onError() on response " + response);
                try {
                    response.onError(errorCode, errorMessage);
                } catch (RemoteException e) {
                    Log.v(TAG, "Session.onError: caught RemoteException while responding", e);
                }
            } else {
                Log.v(TAG, "Session.onError: already closed");
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mAuthenticator = null;
            IAccountManagerResponse response = getResponseAndClose();
            if (response != null) {
                try {
                    response.onError(AccountManager.ERROR_CODE_REMOTE_EXCEPTION,
                            "disconnected");
                } catch (RemoteException e) {
                    Log.v(TAG, "Session.onServiceDisconnected: "
                            + "caught RemoteException while responding", e);
                }
            }
        }

        @Override
        public void onResult(Bundle result) throws RemoteException {
            mNumResults++;
            if (result != null) {
                boolean isSuccessfulConfirmCreds = result.getBoolean(
                        AccountManager.KEY_BOOLEAN_RESULT, false);
                boolean isSuccessfulUpdateCredsOrAddAccount =
                        result.containsKey(AccountManager.KEY_ACCOUNT_NAME)
                                && result.containsKey(AccountManager.KEY_ACCOUNT_TYPE);
                // We should only update lastAuthenticated time, if
                // mUpdateLastAuthenticatedTime is true and the confirmRequest
                // or updateRequest was successful
                boolean needUpdate = mUpdateLastAuthenticatedTime
                        && (isSuccessfulConfirmCreds || isSuccessfulUpdateCredsOrAddAccount);
                if (needUpdate || mAuthDetailsRequired) {
                    synchronized (accountsByUserId) {
                        VAccount account = getAccount(mUserId, mAccountName, mAuthenticatorInfo.desc.type);
                        if (needUpdate && account != null) {
                            account.lastAuthenticatedTime = System.currentTimeMillis();
                            saveAllAccounts();
                        }
                        if (mAuthDetailsRequired) {
                            long lastAuthenticatedTime = -1;
                            if (account != null) {
                                lastAuthenticatedTime = account.lastAuthenticatedTime;
                            }
                            result.putLong(AccountManagerCompat.KEY_LAST_AUTHENTICATED_TIME, lastAuthenticatedTime);
                        }
                    }
                }
            }
            if (result != null
                    && !TextUtils.isEmpty(result.getString(AccountManager.KEY_AUTHTOKEN))) {
//				String accountName = result.getString(AccountManager.KEY_ACCOUNT_NAME);
//				String accountType = result.getString(AccountManager.KEY_ACCOUNT_TYPE);
//				if (!TextUtils.isEmpty(accountName) && !TextUtils.isEmpty(accountType)) {
//					Account account = new Account(accountName, accountType);
//					FIXME: Cancel Notification
//				}
            }
            Intent intent = null;
            if (result != null) {
                intent = result.getParcelable(AccountManager.KEY_INTENT);
            }
            IAccountManagerResponse response;
            if (mExpectActivityLaunch && result != null
                    && result.containsKey(AccountManager.KEY_INTENT)) {
                response = mResponse;
            } else {
                response = getResponseAndClose();
            }
            if (response != null) {
                try {
                    if (result == null) {
                        Log.v(TAG, getClass().getSimpleName()
                                + " calling onError() on response " + response);
                        response.onError(AccountManager.ERROR_CODE_INVALID_RESPONSE,
                                "null bundle returned");
                    } else {
                        if (mStripAuthTokenFromResult) {
                            result.remove(AccountManager.KEY_AUTHTOKEN);
                        }
                        Log.v(TAG, getClass().getSimpleName()
                                + " calling onResult() on response " + response);
                        if ((result.getInt(AccountManager.KEY_ERROR_CODE, -1) > 0) &&
                                (intent == null)) {
                            // All AccountManager error codes are greater than 0
                            response.onError(result.getInt(AccountManager.KEY_ERROR_CODE),
                                    result.getString(AccountManager.KEY_ERROR_MESSAGE));
                        } else {
                            response.onResult(result);
                        }
                    }
                } catch (RemoteException e) {
                    // if the caller is dead then there is no one to care about remote exceptions
                    Log.v(TAG, "failure while notifying response", e);
                }
            }
        }

        public abstract void run() throws RemoteException;

        void bind() {
            Log.v(TAG, "initiating bind to authenticator type " + mAuthenticatorInfo.desc.type);
            Intent intent = new Intent();
            intent.setAction(AccountManager.ACTION_AUTHENTICATOR_INTENT);
            intent.setClassName(mAuthenticatorInfo.serviceInfo.packageName, mAuthenticatorInfo.serviceInfo.name);
            intent.putExtra("_VA_|_user_id_", mUserId);

            if (!mContext.bindService(intent, this, Context.BIND_AUTO_CREATE)) {
                Log.d(TAG, "bind attempt failed for " + toDebugString());
                onError(AccountManager.ERROR_CODE_REMOTE_EXCEPTION, "bind failure");
            }
        }

        protected String toDebugString() {
            return toDebugString(SystemClock.elapsedRealtime());
        }

        protected String toDebugString(long now) {
            return "Session: expectLaunch " + mExpectActivityLaunch
                    + ", connected " + (mAuthenticator != null)
                    + ", stats (" + mNumResults + "/" + mNumRequestContinued
                    + "/" + mNumErrors + ")"
                    + ", lifetime " + ((now - mCreationTime) / 1000.0);
        }

        private void unbind() {
            if (mAuthenticator != null) {
                mAuthenticator = null;
                mContext.unbindService(this);
            }
        }

        @Override
        public void binderDied() {
            mResponse = null;
            close();
        }
    }

    private class GetAccountsByTypeAndFeatureSession extends Session {
        private final String[] mFeatures;
        private volatile Account[] mAccountsOfType = null;
        private volatile ArrayList<Account> mAccountsWithFeatures = null;
        private volatile int mCurrentAccount = 0;

        public GetAccountsByTypeAndFeatureSession(IAccountManagerResponse response, int userId, AuthenticatorInfo info, String[] features) {
            super(response, userId, info, false /* expectActivityLaunch */,
                    true /* stripAuthTokenFromResult */, null /* accountName */);
            mFeatures = features;
        }

        @Override
        public void run() throws RemoteException {
            mAccountsOfType = getAccounts(mUserId, mAuthenticatorInfo.desc.type);
            // check whether each account matches the requested features
            mAccountsWithFeatures = new ArrayList<Account>(mAccountsOfType.length);
            mCurrentAccount = 0;

            checkAccount();
        }

        public void checkAccount() {
            if (mCurrentAccount >= mAccountsOfType.length) {
                sendResult();
                return;
            }

            final IAccountAuthenticator accountAuthenticator = mAuthenticator;
            if (accountAuthenticator == null) {
                // It is possible that the authenticator has died, which is indicated by
                // mAuthenticator being set to null. If this happens then just abort.
                // There is no need to send back a result or error in this case since
                // that already happened when mAuthenticator was cleared.
                Log.v(TAG, "checkAccount: aborting session since we are no longer"
                        + " connected to the authenticator, " + toDebugString());
                return;
            }
            try {
                accountAuthenticator.hasFeatures(this, mAccountsOfType[mCurrentAccount], mFeatures);
            } catch (RemoteException e) {
                onError(AccountManager.ERROR_CODE_REMOTE_EXCEPTION, "remote exception");
            }
        }

        @Override
        public void onResult(Bundle result) {
            mNumResults++;
            if (result == null) {
                onError(AccountManager.ERROR_CODE_INVALID_RESPONSE, "null bundle");
                return;
            }
            if (result.getBoolean(AccountManager.KEY_BOOLEAN_RESULT, false)) {
                mAccountsWithFeatures.add(mAccountsOfType[mCurrentAccount]);
            }
            mCurrentAccount++;
            checkAccount();
        }

        public void sendResult() {
            IAccountManagerResponse response = getResponseAndClose();
            if (response != null) {
                try {
                    Account[] accounts = new Account[mAccountsWithFeatures.size()];
                    for (int i = 0; i < accounts.length; i++) {
                        accounts[i] = mAccountsWithFeatures.get(i);
                    }
                    if (Log.isLoggable(TAG, Log.VERBOSE)) {
                        Log.v(TAG, getClass().getSimpleName() + " calling onResult() on response "
                                + response);
                    }
                    Bundle result = new Bundle();
                    result.putParcelableArray(AccountManager.KEY_ACCOUNTS, accounts);
                    response.onResult(result);
                } catch (RemoteException e) {
                    // if the caller is dead then there is no one to care about remote exceptions
                    Log.v(TAG, "failure while notifying response", e);
                }
            }
        }


        @Override
        protected String toDebugString(long now) {
            return super.toDebugString(now) + ", getAccountsByTypeAndFeatures"
                    + ", " + (mFeatures != null ? TextUtils.join(",", mFeatures) : null);
        }
    }

}
