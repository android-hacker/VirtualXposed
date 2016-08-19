package com.lody.virtual.service.accounts;

import android.Manifest;
import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAndUser;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorDescription;
import android.accounts.CantAddAccountActivity;
import android.accounts.GrantCredentialsPermissionActivity;
import android.accounts.IAccountAuthenticator;
import android.accounts.IAccountAuthenticatorResponse;
import android.accounts.IAccountManagerResponse;
import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.Notification;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.pm.Signature;
import android.content.pm.UserInfo;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.Process;
import android.os.RemoteException;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.util.Slog;
import android.util.SparseArray;

import com.android.internal.util.ArrayUtils;
import com.android.internal.util.IndentingPrintWriter;
import com.google.android.collect.Lists;
import com.google.android.collect.Sets;
import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.env.Constants;
import com.lody.virtual.helper.ExtraConstants;
import com.lody.virtual.os.VBinder;
import com.lody.virtual.os.VEnvironment;
import com.lody.virtual.os.VUserHandle;
import com.lody.virtual.os.VUserManager;
import com.lody.virtual.service.IAccountManager;
import com.lody.virtual.service.am.VActivityManagerService;
import com.lody.virtual.service.pm.VPackageManagerService;

import java.io.File;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A system service that provides  account, password, and authtoken management for all
 * accounts on the device. Some of these calls are implemented with the help of the corresponding
 * {@link IAccountAuthenticator} services. This service is not accessed by users directly,
 * instead one uses an instance of {@link AccountManager}, which can be accessed as follows:
 *    AccountManager accountManager = AccountManager.get(context);
 * @hide
 */
public class VAccountManagerService
        extends IAccountManager.Stub
        implements RegisteredServicesCacheListener<AuthenticatorDescription> {
    private static final String TAG = "VAccountManagerService";

    private static final String DATABASE_NAME = "accounts.db";
    private static final int DATABASE_VERSION = 8;

    private static final int MAX_DEBUG_DB_SIZE = 64;

    private final Context mContext;

    private VUserManager mUserManager;

    private final MessageHandler mMessageHandler;

    // Messages that can be sent on mHandler
    private static final int MESSAGE_TIMED_OUT = 3;
    private static final int MESSAGE_COPY_SHARED_ACCOUNT = 4;

    private final IAccountAuthenticatorCache mAuthenticatorCache;

    private static final String TABLE_ACCOUNTS = "accounts";
    private static final String ACCOUNTS_ID = "_id";
    private static final String ACCOUNTS_NAME = "name";
    private static final String ACCOUNTS_TYPE = "type";
    private static final String ACCOUNTS_TYPE_COUNT = "count(type)";
    private static final String ACCOUNTS_PASSWORD = "password";
    private static final String ACCOUNTS_PREVIOUS_NAME = "previous_name";
    private static final String ACCOUNTS_LAST_AUTHENTICATE_TIME_EPOCH_MILLIS =
            "last_password_entry_time_millis_epoch";

    private static final String TABLE_AUTHTOKENS = "authtokens";
    private static final String AUTHTOKENS_ID = "_id";
    private static final String AUTHTOKENS_ACCOUNTS_ID = "accounts_id";
    private static final String AUTHTOKENS_TYPE = "type";
    private static final String AUTHTOKENS_AUTHTOKEN = "authtoken";

    private static final String TABLE_GRANTS = "grants";
    private static final String GRANTS_ACCOUNTS_ID = "accounts_id";
    private static final String GRANTS_AUTH_TOKEN_TYPE = "auth_token_type";
    private static final String GRANTS_GRANTEE_UID = "uid";

    private static final String TABLE_EXTRAS = "extras";
    private static final String EXTRAS_ID = "_id";
    private static final String EXTRAS_ACCOUNTS_ID = "accounts_id";
    private static final String EXTRAS_KEY = "key";
    private static final String EXTRAS_VALUE = "value";

    private static final String TABLE_META = "meta";
    private static final String META_KEY = "key";
    private static final String META_VALUE = "value";

    private static final String TABLE_SHARED_ACCOUNTS = "shared_accounts";

    private static final String[] ACCOUNT_TYPE_COUNT_PROJECTION =
            new String[] { ACCOUNTS_TYPE, ACCOUNTS_TYPE_COUNT};
    private static final Intent ACCOUNTS_CHANGED_INTENT;
    static {
        ACCOUNTS_CHANGED_INTENT = new Intent(AccountManager.LOGIN_ACCOUNTS_CHANGED_ACTION);
        ACCOUNTS_CHANGED_INTENT.setFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY_BEFORE_BOOT);
    }

    private static final String COUNT_OF_MATCHING_GRANTS = ""
            + "SELECT COUNT(*) FROM " + TABLE_GRANTS + ", " + TABLE_ACCOUNTS
            + " WHERE " + GRANTS_ACCOUNTS_ID + "=" + ACCOUNTS_ID
            + " AND " + GRANTS_GRANTEE_UID + "=?"
            + " AND " + GRANTS_AUTH_TOKEN_TYPE + "=?"
            + " AND " + ACCOUNTS_NAME + "=?"
            + " AND " + ACCOUNTS_TYPE + "=?";

    private static final String SELECTION_AUTHTOKENS_BY_ACCOUNT =
            AUTHTOKENS_ACCOUNTS_ID + "=(select _id FROM accounts WHERE name=? AND type=?)";

    private static final String[] COLUMNS_AUTHTOKENS_TYPE_AND_AUTHTOKEN = {AUTHTOKENS_TYPE,
            AUTHTOKENS_AUTHTOKEN};

    private static final String SELECTION_USERDATA_BY_ACCOUNT =
            EXTRAS_ACCOUNTS_ID + "=(select _id FROM accounts WHERE name=? AND type=?)";
    private static final String[] COLUMNS_EXTRAS_KEY_AND_VALUE = {EXTRAS_KEY, EXTRAS_VALUE};

    private final LinkedHashMap<String, Session> mSessions = new LinkedHashMap<String, Session>();
    private final AtomicInteger mNotificationIds = new AtomicInteger(1);

    static class UserAccounts {
        private final int userId;
        private final DatabaseHelper openHelper;
        private final HashMap<Pair<Pair<Account, String>, Integer>, Integer>
                credentialsPermissionNotificationIds =
                new HashMap<Pair<Pair<Account, String>, Integer>, Integer>();
        private final HashMap<Account, Integer> signinRequiredNotificationIds =
                new HashMap<Account, Integer>();
        private final Object cacheLock = new Object();
        /** protected by the {@link #cacheLock} */
        private final HashMap<String, Account[]> accountCache =
                new LinkedHashMap<String, Account[]>();
        /** protected by the {@link #cacheLock} */
        private final HashMap<Account, HashMap<String, String>> userDataCache =
                new HashMap<Account, HashMap<String, String>>();
        /** protected by the {@link #cacheLock} */
        private final HashMap<Account, HashMap<String, String>> authTokenCache =
                new HashMap<Account, HashMap<String, String>>();

        /** protected by the {@link #cacheLock} */
        private final TokenCache accountTokenCaches = new TokenCache();

        /**
         * protected by the {@link #cacheLock}
         *
         * Caches the previous names associated with an account. Previous names
         * should be cached because we expect that when an Account is renamed,
         * many clients will receive a LOGIN_ACCOUNTS_CHANGED broadcast and
         * want to know if the accounts they care about have been renamed.
         *
         * The previous names are wrapped in an {@link AtomicReference} so that
         * we can distinguish between those accounts with no previous names and
         * those whose previous names haven't been cached (yet).
         */
        private final HashMap<Account, AtomicReference<String>> previousNameCache =
                new HashMap<Account, AtomicReference<String>>();

        private int debugDbInsertionPoint = -1;
        private SQLiteStatement statementForLogging;

        UserAccounts(Context context, int userId) {
            this.userId = userId;
            synchronized (cacheLock) {
                openHelper = new DatabaseHelper(context, userId);
            }
        }
    }

    private final SparseArray<UserAccounts> mUsers = new SparseArray<UserAccounts>();

    private static AtomicReference<VAccountManagerService> sThis =
            new AtomicReference<VAccountManagerService>();
    private static final Account[] EMPTY_ACCOUNT_ARRAY = new Account[]{};

    /**
     * This should only be called by system code. One should only call this after the service
     * has started.
     * @return a reference to the VAccountManagerService instance
     * @hide
     */
    public static VAccountManagerService getSingleton() {
        return sThis.get();
    }

    public VAccountManagerService(Context context) {
        this(context, new AccountAuthenticatorCache(context));
    }

    public VAccountManagerService(Context context,
                                  IAccountAuthenticatorCache authenticatorCache) {
        mContext = context;
        mMessageHandler = new MessageHandler(Looper.getMainLooper());

        mAuthenticatorCache = authenticatorCache;
        mAuthenticatorCache.setListener(this, null /* Handler */);

        sThis.set(this);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.ACTION_PACKAGE_REMOVED);
        intentFilter.addDataScheme("package");
        mContext.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context1, Intent intent) {
                // Don't delete accounts when updating a authenticator's
                // package.
                if (!intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)) {
                    /* Purging data requires file io, don't block the main thread. This is probably
                     * less than ideal because we are introducing a race condition where old grants
                     * could be exercised until they are purged. But that race condition existed
                     * anyway with the broadcast receiver.
                     *
                     * Ideally, we would completely clear the cache, purge data from the database,
                     * and then rebuild the cache. All under the cache lock. But that change is too
                     * large at this point.
                     */
                    Runnable r = new Runnable() {
                        @Override
                        public void run() {
                            purgeOldGrantsAll();
                        }
                    };
                    new Thread(r).start();
                }
            }
        }, intentFilter);

        IntentFilter userFilter = new IntentFilter();
        userFilter.addAction(Intent.ACTION_USER_REMOVED);
        userFilter.addAction(Intent.ACTION_USER_STARTED);
        mContext.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (Constants.ACTION_USER_REMOVED.equals(action)) {
                    onUserRemoved(intent);
                } else if (Constants.ACTION_USER_STARTED.equals(action)) {
                    onUserStarted(intent);
                }
            }
        },userFilter);
    }

    @Override
    public boolean onTransact(int code, Parcel data, Parcel reply, int flags)
            throws RemoteException {
        try {
            return super.onTransact(code, data, reply, flags);
        } catch (RuntimeException e) {
            // The account manager only throws security exceptions, so let's
            // log all others.
            if (!(e instanceof SecurityException)) {
                Slog.wtf(TAG, "Account Manager Crash", e);
            }
            throw e;
        }
    }

    public static void systemReady(Context context) {
        new VAccountManagerService(context);
    }

    private VUserManager getUserManager() {
        if (mUserManager == null) {
            mUserManager = VUserManager.get();
        }
        return mUserManager;
    }

    /**
     * Validate internal set of accounts against installed authenticators for
     * given user. Clears cached authenticators before validating.
     */
    public void validateAccounts(int userId) {
        final UserAccounts accounts = getUserAccounts(userId);

        // Invalidate user-specific cache to make sure we catch any
        // removed authenticators.
        validateAccountsInternal(accounts, true /* invalidateAuthenticatorCache */);
    }

    /**
     * Validate internal set of accounts against installed authenticators for
     * given user. Clear cached authenticators before validating when requested.
     */
    private void validateAccountsInternal(
            UserAccounts accounts, boolean invalidateAuthenticatorCache) {
        if (invalidateAuthenticatorCache) {
            mAuthenticatorCache.invalidateCache(accounts.userId);
        }

        final HashSet<AuthenticatorDescription> knownAuth = Sets.newHashSet();
        for (RegisteredServicesCache.ServiceInfo<AuthenticatorDescription> service :
                mAuthenticatorCache.getAllServices(accounts.userId)) {
            knownAuth.add(service.type);
        }

        synchronized (accounts.cacheLock) {
            final SQLiteDatabase db = accounts.openHelper.getWritableDatabase();
            boolean accountDeleted = false;
            Cursor cursor = db.query(TABLE_ACCOUNTS,
                    new String[]{ACCOUNTS_ID, ACCOUNTS_TYPE, ACCOUNTS_NAME},
                    null, null, null, null, ACCOUNTS_ID);
            try {
                accounts.accountCache.clear();
                final HashMap<String, ArrayList<String>> accountNamesByType =
                        new LinkedHashMap<String, ArrayList<String>>();
                while (cursor.moveToNext()) {
                    final long accountId = cursor.getLong(0);
                    final String accountType = cursor.getString(1);
                    final String accountName = cursor.getString(2);

                    if (!knownAuth.contains(AuthenticatorDescription.newKey(accountType))) {
                        Slog.w(TAG, "deleting account " + accountName + " because type "
                                + accountType + " no longer has a registered authenticator");
                        db.delete(TABLE_ACCOUNTS, ACCOUNTS_ID + "=" + accountId, null);
                        accountDeleted = true;

                        logRecord(db, DebugDbHelper.ACTION_AUTHENTICATOR_REMOVE, TABLE_ACCOUNTS,
                                accountId, accounts);

                        final Account account = new Account(accountName, accountType);
                        accounts.userDataCache.remove(account);
                        accounts.authTokenCache.remove(account);
                        accounts.accountTokenCaches.remove(account);
                    } else {
                        ArrayList<String> accountNames = accountNamesByType.get(accountType);
                        if (accountNames == null) {
                            accountNames = new ArrayList<String>();
                            accountNamesByType.put(accountType, accountNames);
                        }
                        accountNames.add(accountName);
                    }
                }
                for (Map.Entry<String, ArrayList<String>> cur
                        : accountNamesByType.entrySet()) {
                    final String accountType = cur.getKey();
                    final ArrayList<String> accountNames = cur.getValue();
                    final Account[] accountsForType = new Account[accountNames.size()];
                    int i = 0;
                    for (String accountName : accountNames) {
                        accountsForType[i] = new Account(accountName, accountType);
                        ++i;
                    }
                    accounts.accountCache.put(accountType, accountsForType);
                }
            } finally {
                cursor.close();
                if (accountDeleted) {
                    sendAccountsChangedBroadcast(accounts.userId);
                }
            }
        }
    }

    private UserAccounts getUserAccountsForCaller() {
        return getUserAccounts(VUserHandle.getCallingUserId());
    }

    protected UserAccounts getUserAccounts(int userId) {
        synchronized (mUsers) {
            UserAccounts accounts = mUsers.get(userId);
            if (accounts == null) {
                accounts = new UserAccounts(mContext, userId);
                initializeDebugDbSizeAndCompileSqlStatementForLogging(
                        accounts.openHelper.getWritableDatabase(), accounts);
                mUsers.append(userId, accounts);
                purgeOldGrants(accounts);
                validateAccountsInternal(accounts, true /* invalidateAuthenticatorCache */);
            }
            return accounts;
        }
    }

    private void purgeOldGrantsAll() {
        synchronized (mUsers) {
            for (int i = 0; i < mUsers.size(); i++) {
                purgeOldGrants(mUsers.valueAt(i));
            }
        }
    }

    private void purgeOldGrants(UserAccounts accounts) {
        synchronized (accounts.cacheLock) {
            final SQLiteDatabase db = accounts.openHelper.getWritableDatabase();
            final Cursor cursor = db.query(TABLE_GRANTS,
                    new String[]{GRANTS_GRANTEE_UID},
                    null, null, GRANTS_GRANTEE_UID, null, null);
            try {
                while (cursor.moveToNext()) {
                    final int uid = cursor.getInt(0);
                    final boolean packageExists = VPackageManagerService.getService().getPackagesForUid(uid) != null;
                    if (packageExists) {
                        continue;
                    }
                    Log.d(TAG, "deleting grants for UID " + uid
                            + " because its package is no longer installed");
                    db.delete(TABLE_GRANTS, GRANTS_GRANTEE_UID + "=?",
                            new String[]{Integer.toString(uid)});
                }
            } finally {
                cursor.close();
            }
        }
    }

    private void onUserRemoved(Intent intent) {
        int userId = intent.getIntExtra(Intent.EXTRA_USER_HANDLE, -1);
        if (userId < 1) return;

        UserAccounts accounts;
        synchronized (mUsers) {
            accounts = mUsers.get(userId);
            mUsers.remove(userId);
        }
        if (accounts == null) {
            File dbFile = new File(getDatabaseName(userId));
            dbFile.delete();
            return;
        }

        synchronized (accounts.cacheLock) {
            accounts.openHelper.close();
            File dbFile = new File(getDatabaseName(userId));
            dbFile.delete();
        }
    }

    private void onUserStarted(Intent intent) {
        int userId = intent.getIntExtra(Intent.EXTRA_USER_HANDLE, -1);
        if (userId < 1) return;

        // Check if there's a shared account that needs to be created as an account
        Account[] sharedAccounts = getSharedAccountsAsUser(userId);
        if (sharedAccounts == null || sharedAccounts.length == 0) return;
        Account[] accounts = getAccountsAsUser(null, userId);
        for (Account sa : sharedAccounts) {
            if (ArrayUtils.contains(accounts, sa)) continue;
            // Account doesn't exist. Copy it now.
            copyAccountToUser(null /*no response*/, sa, VUserHandle.USER_OWNER, userId);
        }
    }

    @Override
    public void onServiceChanged(AuthenticatorDescription desc, int userId, boolean removed) {
        validateAccountsInternal(getUserAccounts(userId), false /* invalidateAuthenticatorCache */);
    }

    @Override
    public String getPassword(Account account) {
        int callingUid = VBinder.getCallingUid();
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, "getPassword: " + account
                    + ", caller's uid " + VBinder.getCallingUid()
                    + ", pid " + VBinder.getCallingPid());
        }
        if (account == null) throw new IllegalArgumentException("account is null");
        int userId = VUserHandle.getCallingUserId();
        if (!isAccountManagedByCaller(account.type, callingUid, userId)) {
            String msg = String.format(
                    "uid %s cannot get secrets for accounts of type: %s",
                    callingUid,
                    account.type);
            throw new SecurityException(msg);
        }
        long identityToken = clearCallingIdentity();
        try {
            UserAccounts accounts = getUserAccounts(userId);
            return readPasswordInternal(accounts, account);
        } finally {
            restoreCallingIdentity(identityToken);
        }
    }

    private String readPasswordInternal(UserAccounts accounts, Account account) {
        if (account == null) {
            return null;
        }

        synchronized (accounts.cacheLock) {
            final SQLiteDatabase db = accounts.openHelper.getReadableDatabase();
            Cursor cursor = db.query(TABLE_ACCOUNTS, new String[]{ACCOUNTS_PASSWORD},
                    ACCOUNTS_NAME + "=? AND " + ACCOUNTS_TYPE+ "=?",
                    new String[]{account.name, account.type}, null, null, null);
            try {
                if (cursor.moveToNext()) {
                    return cursor.getString(0);
                }
                return null;
            } finally {
                cursor.close();
            }
        }
    }

    @Override
    public String getPreviousName(Account account) {
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, "getPreviousName: " + account
                    + ", caller's uid " + VBinder.getCallingUid()
                    + ", pid " + VBinder.getCallingPid());
        }
        if (account == null) throw new IllegalArgumentException("account is null");
        int userId = VUserHandle.getCallingUserId();
        long identityToken = clearCallingIdentity();
        try {
            UserAccounts accounts = getUserAccounts(userId);
            return readPreviousNameInternal(accounts, account);
        } finally {
            restoreCallingIdentity(identityToken);
        }
    }

    private String readPreviousNameInternal(UserAccounts accounts, Account account) {
        if  (account == null) {
            return null;
        }
        synchronized (accounts.cacheLock) {
            AtomicReference<String> previousNameRef = accounts.previousNameCache.get(account);
            if (previousNameRef == null) {
                final SQLiteDatabase db = accounts.openHelper.getReadableDatabase();
                Cursor cursor = db.query(
                        TABLE_ACCOUNTS,
                        new String[]{ ACCOUNTS_PREVIOUS_NAME },
                        ACCOUNTS_NAME + "=? AND " + ACCOUNTS_TYPE+ "=?",
                        new String[] { account.name, account.type },
                        null,
                        null,
                        null);
                try {
                    if (cursor.moveToNext()) {
                        String previousName = cursor.getString(0);
                        previousNameRef = new AtomicReference<String>(previousName);
                        accounts.previousNameCache.put(account, previousNameRef);
                        return previousName;
                    } else {
                        return null;
                    }
                } finally {
                    cursor.close();
                }
            } else {
                return previousNameRef.get();
            }
        }
    }

    @Override
    public String getUserData(Account account, String key) {
        final int callingUid = VBinder.getCallingUid();
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            String msg = String.format("getUserData( account: %s, key: %s, callerUid: %s, pid: %s",
                    account, key, callingUid, VBinder.getCallingPid());
            Log.v(TAG, msg);
        }
        if (account == null) throw new IllegalArgumentException("account is null");
        if (key == null) throw new IllegalArgumentException("key is null");
        int userId = VUserHandle.getCallingUserId();
        if (!isAccountManagedByCaller(account.type, callingUid, userId)) {
            String msg = String.format(
                    "uid %s cannot get user data for accounts of type: %s",
                    callingUid,
                    account.type);
            throw new SecurityException(msg);
        }
        long identityToken = clearCallingIdentity();
        try {
            UserAccounts accounts = getUserAccounts(userId);
            return readUserDataInternal(accounts, account, key);
        } finally {
            restoreCallingIdentity(identityToken);
        }
    }

    @Override
    public AuthenticatorDescription[] getAuthenticatorTypes(int userId) {
        int callingUid = VBinder.getCallingUid();
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, "getAuthenticatorTypes: "
                    + "for user id " + userId
                    + "caller's uid " + callingUid
                    + ", pid " + VBinder.getCallingPid());
        }
        // Only allow the system process to read accounts of other users
        if (isCrossUser(callingUid, userId)) {
            throw new SecurityException(
                    String.format(
                            "User %s tying to get authenticator types for %s" ,
                            VUserHandle.getCallingUserId(),
                            userId));
        }

        final long identityToken = clearCallingIdentity();
        try {
            return getAuthenticatorTypesInternal(userId);

        } finally {
            restoreCallingIdentity(identityToken);
        }
    }

    /**
     * Should only be called inside of a clearCallingIdentity block.
     */
    private AuthenticatorDescription[] getAuthenticatorTypesInternal(int userId) {
        Collection<AccountAuthenticatorCache.ServiceInfo<AuthenticatorDescription>>
                authenticatorCollection = mAuthenticatorCache.getAllServices(userId);
        AuthenticatorDescription[] types =
                new AuthenticatorDescription[authenticatorCollection.size()];
        int i = 0;
        for (AccountAuthenticatorCache.ServiceInfo<AuthenticatorDescription> authenticator
                : authenticatorCollection) {
            types[i] = authenticator.type;
            i++;
        }
        return types;
    }



    private boolean isCrossUser(int callingUid, int userId) {
        return (userId != VUserHandle.getCallingUserId()
                && callingUid != Process.myUid()
                && mContext.checkCallingOrSelfPermission(
                        Manifest.permission.INTERACT_ACROSS_USERS_FULL)
                                != PackageManager.PERMISSION_GRANTED);
    }

    @Override
    public boolean addAccountExplicitly(Account account, String password, Bundle extras) {
        final int callingUid = VBinder.getCallingUid();
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, "addAccountExplicitly: " + account
                    + ", caller's uid " + callingUid
                    + ", pid " + VBinder.getCallingPid());
        }
        if (account == null) throw new IllegalArgumentException("account is null");
        int userId = VUserHandle.getCallingUserId();
        if (!isAccountManagedByCaller(account.type, callingUid, userId)) {
            String msg = String.format(
                    "uid %s cannot explicitly add accounts of type: %s",
                    callingUid,
                    account.type);
            throw new SecurityException(msg);
        }
        /*
         * Child users are not allowed to add accounts. Only the accounts that are
         * shared by the parent profile can be added to child profile.
         *
         * TODO: Only allow accounts that were shared to be added by
         *     a limited user.
         */

        // fails if the account already exists
        long identityToken = clearCallingIdentity();
        try {
            UserAccounts accounts = getUserAccounts(userId);
            return addAccountInternal(accounts, account, password, extras, false, callingUid);
        } finally {
            restoreCallingIdentity(identityToken);
        }
    }

    @Override
    public void copyAccountToUser(final IAccountManagerResponse response, final Account account,
            int userFrom, int userTo) {
        int callingUid = VBinder.getCallingUid();
        if (isCrossUser(callingUid, VUserHandle.USER_ALL)) {
            throw new SecurityException("Calling copyAccountToUser requires "
                    + Manifest.permission.INTERACT_ACROSS_USERS_FULL);
        }
        final UserAccounts fromAccounts = getUserAccounts(userFrom);
        final UserAccounts toAccounts = getUserAccounts(userTo);
        if (fromAccounts == null || toAccounts == null) {
            if (response != null) {
                Bundle result = new Bundle();
                result.putBoolean(AccountManager.KEY_BOOLEAN_RESULT, false);
                try {
                    response.onResult(result);
                } catch (RemoteException e) {
                    Slog.w(TAG, "Failed to report error back to the client." + e);
                }
            }
            return;
        }

        Slog.d(TAG, "Copying account " + account.name
                + " from user " + userFrom + " to user " + userTo);
        long identityToken = clearCallingIdentity();
        try {
            new Session(fromAccounts, response, account.type, false,
                    false /* stripAuthTokenFromResult */, account.name,
                    false /* authDetailsRequired */) {
                @Override
                protected String toDebugString(long now) {
                    return super.toDebugString(now) + ", getAccountCredentialsForClone"
                            + ", " + account.type;
                }

                @Override
                public void run() throws RemoteException {
                    mAuthenticator.getAccountCredentialsForCloning(this, account);
                }

                @Override
                public void onResult(Bundle result) {
                    if (result != null
                            && result.getBoolean(AccountManager.KEY_BOOLEAN_RESULT, false)) {
                        // Create a Session for the target user and pass in the bundle
                        completeCloningAccount(response, result, account, toAccounts);
                    } else {
                        super.onResult(result);
                    }
                }
            }.bind();
        } finally {
            restoreCallingIdentity(identityToken);
        }
    }

    @Override
    public boolean accountAuthenticated(final Account account) {
        final int callingUid = VBinder.getCallingUid();
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            String msg = String.format(
                    "accountAuthenticated( account: %s, callerUid: %s)",
                    account,
                    callingUid);
            Log.v(TAG, msg);
        }
        if (account == null) {
            throw new IllegalArgumentException("account is null");
        }
        int userId = VUserHandle.getCallingUserId();
        if (!isAccountManagedByCaller(account.type, callingUid, userId)) {
            String msg = String.format(
                    "uid %s cannot notify authentication for accounts of type: %s",
                    callingUid,
                    account.type);
            throw new SecurityException(msg);
        }

        if (!canUserModifyAccounts(userId) || !canUserModifyAccountsForType(userId, account.type)) {
            return false;
        }

        long identityToken = clearCallingIdentity();
        try {
            UserAccounts accounts = getUserAccounts(userId);
            return updateLastAuthenticatedTime(account);
        } finally {
            restoreCallingIdentity(identityToken);
        }
    }

    private boolean updateLastAuthenticatedTime(Account account) {
        final UserAccounts accounts = getUserAccountsForCaller();
        synchronized (accounts.cacheLock) {
            final ContentValues values = new ContentValues();
            values.put(ACCOUNTS_LAST_AUTHENTICATE_TIME_EPOCH_MILLIS, System.currentTimeMillis());
            final SQLiteDatabase db = accounts.openHelper.getWritableDatabase();
            int i = db.update(
                    TABLE_ACCOUNTS,
                    values,
                    ACCOUNTS_NAME + "=? AND " + ACCOUNTS_TYPE + "=?",
                    new String[] {
                            account.name, account.type
                    });
            if (i > 0) {
                return true;
            }
        }
        return false;
    }

    private void completeCloningAccount(IAccountManagerResponse response,
            final Bundle accountCredentials, final Account account, final UserAccounts targetUser) {
        long id = clearCallingIdentity();
        try {
            new Session(targetUser, response, account.type, false,
                    false /* stripAuthTokenFromResult */, account.name,
                    false /* authDetailsRequired */) {
                @Override
                protected String toDebugString(long now) {
                    return super.toDebugString(now) + ", getAccountCredentialsForClone"
                            + ", " + account.type;
                }

                @Override
                public void run() throws RemoteException {
                    // Confirm that the owner's account still exists before this step.
                    UserAccounts owner = getUserAccounts(VUserHandle.USER_OWNER);
                    synchronized (owner.cacheLock) {
                        for (Account acc : getAccounts(VUserHandle.USER_OWNER)) {
                            if (acc.equals(account)) {
                                mAuthenticator.addAccountFromCredentials(
                                        this, account, accountCredentials);
                                break;
                            }
                        }
                    }
                }

                @Override
                public void onResult(Bundle result) {
                    // TODO: Anything to do if if succedded?
                    // TODO: If it failed: Show error notification? Should we remove the shadow
                    // account to avoid retries?
                    super.onResult(result);
                }

                @Override
                public void onError(int errorCode, String errorMessage) {
                    super.onError(errorCode,  errorMessage);
                    // TODO: Show error notification to user
                    // TODO: Should we remove the shadow account so that it doesn't keep trying?
                }

            }.bind();
        } finally {
            restoreCallingIdentity(id);
        }
    }

    private boolean addAccountInternal(UserAccounts accounts, Account account, String password,
            Bundle extras, boolean restricted, int callingUid) {
        if (account == null) {
            return false;
        }
        synchronized (accounts.cacheLock) {
            final SQLiteDatabase db = accounts.openHelper.getWritableDatabase();
            db.beginTransaction();
            try {
                long numMatches = DatabaseUtils.longForQuery(db,
                        "select count(*) from " + TABLE_ACCOUNTS
                                + " WHERE " + ACCOUNTS_NAME + "=? AND " + ACCOUNTS_TYPE+ "=?",
                        new String[]{account.name, account.type});
                if (numMatches > 0) {
                    Log.w(TAG, "insertAccountIntoDatabase: " + account
                            + ", skipping since the account already exists");
                    return false;
                }
                ContentValues values = new ContentValues();
                values.put(ACCOUNTS_NAME, account.name);
                values.put(ACCOUNTS_TYPE, account.type);
                values.put(ACCOUNTS_PASSWORD, password);
                values.put(ACCOUNTS_LAST_AUTHENTICATE_TIME_EPOCH_MILLIS, System.currentTimeMillis());
                long accountId = db.insert(TABLE_ACCOUNTS, ACCOUNTS_NAME, values);
                if (accountId < 0) {
                    Log.w(TAG, "insertAccountIntoDatabase: " + account
                            + ", skipping the DB insert failed");
                    return false;
                }
                if (extras != null) {
                    for (String key : extras.keySet()) {
                        final String value = extras.getString(key);
                        if (insertExtraLocked(db, accountId, key, value) < 0) {
                            Log.w(TAG, "insertAccountIntoDatabase: " + account
                                    + ", skipping since insertExtra failed for key " + key);
                            return false;
                        }
                    }
                }
                db.setTransactionSuccessful();

                logRecord(db, DebugDbHelper.ACTION_ACCOUNT_ADD, TABLE_ACCOUNTS, accountId,
                        accounts, callingUid);

                insertAccountIntoCacheLocked(accounts, account);
            } finally {
                db.endTransaction();
            }
            sendAccountsChangedBroadcast(accounts.userId);
        }
        if (accounts.userId == VUserHandle.USER_OWNER) {
            addAccountToLimitedUsers(account);
        }
        return true;
    }

    /**
     * Adds the account to all limited users as shared accounts. If the user is currently
     * running, then clone the account too.
     * @param account the account to share with limited users
     */
    private void addAccountToLimitedUsers(Account account) {
        List<UserInfo> users = getUserManager().getUsers();
        for (UserInfo user : users) {
            if (user.isRestricted()) {
                addSharedAccountAsUser(account, user.id);
                try {
                    if (ActivityManagerNative.getDefault().isUserRunning(user.id, false)) {
                        mMessageHandler.sendMessage(mMessageHandler.obtainMessage(
                                MESSAGE_COPY_SHARED_ACCOUNT, VUserHandle.USER_OWNER, user.id,
                                account));
                    }
                } catch (RemoteException re) {
                    // Shouldn't happen
                }
            }
        }
    }

    private long insertExtraLocked(SQLiteDatabase db, long accountId, String key, String value) {
        ContentValues values = new ContentValues();
        values.put(EXTRAS_KEY, key);
        values.put(EXTRAS_ACCOUNTS_ID, accountId);
        values.put(EXTRAS_VALUE, value);
        return db.insert(TABLE_EXTRAS, EXTRAS_KEY, values);
    }

    public void hasFeatures(IAccountManagerResponse response,
            Account account, String[] features) {
        int callingUid = VBinder.getCallingUid();
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, "hasFeatures: " + account
                    + ", response " + response
                    + ", features " + stringArrayToString(features)
                    + ", caller's uid " + callingUid
                    + ", pid " + VBinder.getCallingPid());
        }
        if (response == null) throw new IllegalArgumentException("response is null");
        if (account == null) throw new IllegalArgumentException("account is null");
        if (features == null) throw new IllegalArgumentException("features is null");
        int userId = VUserHandle.getCallingUserId();
        checkReadAccountsPermitted(callingUid, account.type, userId);

        long identityToken = clearCallingIdentity();
        try {
            UserAccounts accounts = getUserAccounts(userId);
            new TestFeaturesSession(accounts, response, account, features).bind();
        } finally {
            restoreCallingIdentity(identityToken);
        }
    }

    private class TestFeaturesSession extends Session {
        private final String[] mFeatures;
        private final Account mAccount;

        public TestFeaturesSession(UserAccounts accounts, IAccountManagerResponse response,
                Account account, String[] features) {
            super(accounts, response, account.type, false /* expectActivityLaunch */,
                    true /* stripAuthTokenFromResult */, account.name,
                    false /* authDetailsRequired */);
            mFeatures = features;
            mAccount = account;
        }

        @Override
        public void run() throws RemoteException {
            try {
                mAuthenticator.hasFeatures(this, mAccount, mFeatures);
            } catch (RemoteException e) {
                onError(AccountManager.ERROR_CODE_REMOTE_EXCEPTION, "remote exception");
            }
        }

        @Override
        public void onResult(Bundle result) {
            IAccountManagerResponse response = getResponseAndClose();
            if (response != null) {
                try {
                    if (result == null) {
                        response.onError(AccountManager.ERROR_CODE_INVALID_RESPONSE, "null bundle");
                        return;
                    }
                    if (Log.isLoggable(TAG, Log.VERBOSE)) {
                        Log.v(TAG, getClass().getSimpleName() + " calling onResult() on response "
                                + response);
                    }
                    final Bundle newResult = new Bundle();
                    newResult.putBoolean(AccountManager.KEY_BOOLEAN_RESULT,
                            result.getBoolean(AccountManager.KEY_BOOLEAN_RESULT, false));
                    response.onResult(newResult);
                } catch (RemoteException e) {
                    // if the caller is dead then there is no one to care about remote exceptions
                    if (Log.isLoggable(TAG, Log.VERBOSE)) {
                        Log.v(TAG, "failure while notifying response", e);
                    }
                }
            }
        }

        @Override
        protected String toDebugString(long now) {
            return super.toDebugString(now) + ", hasFeatures"
                    + ", " + mAccount
                    + ", " + (mFeatures != null ? TextUtils.join(",", mFeatures) : null);
        }
    }

    @Override
    public void renameAccount(
            IAccountManagerResponse response, Account accountToRename, String newName) {
        final int callingUid = VBinder.getCallingUid();
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, "renameAccount: " + accountToRename + " -> " + newName
                + ", caller's uid " + callingUid
                + ", pid " + VBinder.getCallingPid());
        }
        if (accountToRename == null) throw new IllegalArgumentException("account is null");
        int userId = VUserHandle.getCallingUserId();
        if (!isAccountManagedByCaller(accountToRename.type, callingUid, userId)) {
            String msg = String.format(
                    "uid %s cannot rename accounts of type: %s",
                    callingUid,
                    accountToRename.type);
            throw new SecurityException(msg);
        }
        long identityToken = clearCallingIdentity();
        try {
            UserAccounts accounts = getUserAccounts(userId);
            Account resultingAccount = renameAccountInternal(accounts, accountToRename, newName);
            Bundle result = new Bundle();
            result.putString(AccountManager.KEY_ACCOUNT_NAME, resultingAccount.name);
            result.putString(AccountManager.KEY_ACCOUNT_TYPE, resultingAccount.type);
            try {
                response.onResult(result);
            } catch (RemoteException e) {
                Log.w(TAG, e.getMessage());
            }
        } finally {
            restoreCallingIdentity(identityToken);
        }
    }

    private Account renameAccountInternal(
            UserAccounts accounts, Account accountToRename, String newName) {
        Account resultAccount = null;
        /*
         * Cancel existing notifications. Let authenticators
         * re-post notifications as required. But we don't know if
         * the authenticators have bound their notifications to
         * now stale account name data.
         *
         * With a rename api, we might not need to do this anymore but it
         * shouldn't hurt.
         */
        cancelNotification(
                getSigninRequiredNotificationId(accounts, accountToRename),
                 new VUserHandle(accounts.userId));
        synchronized(accounts.credentialsPermissionNotificationIds) {
            for (Pair<Pair<Account, String>, Integer> pair:
                    accounts.credentialsPermissionNotificationIds.keySet()) {
                if (accountToRename.equals(pair.first.first)) {
                    int id = accounts.credentialsPermissionNotificationIds.get(pair);
                    cancelNotification(id, new VUserHandle(accounts.userId));
                }
            }
        }
        synchronized (accounts.cacheLock) {
            final SQLiteDatabase db = accounts.openHelper.getWritableDatabase();
            db.beginTransaction();
            boolean isSuccessful = false;
            Account renamedAccount = new Account(newName, accountToRename.type);
            try {
                final ContentValues values = new ContentValues();
                values.put(ACCOUNTS_NAME, newName);
                values.put(ACCOUNTS_PREVIOUS_NAME, accountToRename.name);
                final long accountId = getAccountIdLocked(db, accountToRename);
                if (accountId >= 0) {
                    final String[] argsAccountId = { String.valueOf(accountId) };
                    db.update(TABLE_ACCOUNTS, values, ACCOUNTS_ID + "=?", argsAccountId);
                    db.setTransactionSuccessful();
                    isSuccessful = true;
                    logRecord(db, DebugDbHelper.ACTION_ACCOUNT_RENAME, TABLE_ACCOUNTS, accountId,
                            accounts);
                }
            } finally {
                db.endTransaction();
                if (isSuccessful) {
                    /*
                     * Database transaction was successful. Clean up cached
                     * data associated with the account in the user profile.
                     */
                    insertAccountIntoCacheLocked(accounts, renamedAccount);
                    /*
                     * Extract the data and token caches before removing the
                     * old account to preserve the user data associated with
                     * the account.
                     */
                    HashMap<String, String> tmpData = accounts.userDataCache.get(accountToRename);
                    HashMap<String, String> tmpTokens = accounts.authTokenCache.get(accountToRename);
                    removeAccountFromCacheLocked(accounts, accountToRename);
                    /*
                     * Update the cached data associated with the renamed
                     * account.
                     */
                    accounts.userDataCache.put(renamedAccount, tmpData);
                    accounts.authTokenCache.put(renamedAccount, tmpTokens);
                    accounts.previousNameCache.put(
                          renamedAccount,
                          new AtomicReference<String>(accountToRename.name));
                    resultAccount = renamedAccount;

                    if (accounts.userId == VUserHandle.USER_OWNER) {
                        /*
                         * Owner's account was renamed, rename the account for
                         * those users with which the account was shared.
                         */
                        List<UserInfo> users = mUserManager.getUsers(true);
                        for (UserInfo user : users) {
                            if (!user.isPrimary() && user.isRestricted()) {
                                renameSharedAccountAsUser(accountToRename, newName, user.id);
                            }
                        }
                    }
                    sendAccountsChangedBroadcast(accounts.userId);
                }
            }
        }
        return resultAccount;
    }

    @Override
    public void removeAccount(IAccountManagerResponse response, Account account,
            boolean expectActivityLaunch) {
        removeAccountAsUser(
                response,
                account,
                expectActivityLaunch,
                VUserHandle.getCallingUserId());
    }

    @Override
    public void removeAccountAsUser(IAccountManagerResponse response, Account account,
            boolean expectActivityLaunch, int userId) {
        final int callingUid = VBinder.getCallingUid();
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, "removeAccount: " + account
                    + ", response " + response
                    + ", caller's uid " + callingUid
                    + ", pid " + VBinder.getCallingPid()
                    + ", for user id " + userId);
        }
        if (response == null) throw new IllegalArgumentException("response is null");
        if (account == null) throw new IllegalArgumentException("account is null");
        // Only allow the system process to modify accounts of other users
        if (isCrossUser(callingUid, userId)) {
            throw new SecurityException(
                    String.format(
                            "User %s tying remove account for %s" ,
                            VUserHandle.getCallingUserId(),
                            userId));
        }
        /*
         * Only the system or authenticator should be allowed to remove accounts for that
         * authenticator.  This will let users remove accounts (via Settings in the system) but not
         * arbitrary applications (like competing authenticators).
         */
        VUserHandle user = new VUserHandle(userId);
        if (!isAccountManagedByCaller(account.type, callingUid, user.getIdentifier())
                && !isSystemUid(callingUid)) {
            String msg = String.format(
                    "uid %s cannot remove accounts of type: %s",
                    callingUid,
                    account.type);
            throw new SecurityException(msg);
        }
        if (!canUserModifyAccounts(userId)) {
            try {
                response.onError(AccountManager.ERROR_CODE_USER_RESTRICTED,
                        "User cannot modify accounts");
            } catch (RemoteException re) {
            }
            return;
        }
        if (!canUserModifyAccountsForType(userId, account.type)) {
            try {
                response.onError(AccountManager.ERROR_CODE_MANAGEMENT_DISABLED_FOR_ACCOUNT_TYPE,
                        "User cannot modify accounts of this type (policy).");
            } catch (RemoteException re) {
            }
            return;
        }
        long identityToken = clearCallingIdentity();
        UserAccounts accounts = getUserAccounts(userId);
        cancelNotification(getSigninRequiredNotificationId(accounts, account), user);
        synchronized(accounts.credentialsPermissionNotificationIds) {
            for (Pair<Pair<Account, String>, Integer> pair:
                accounts.credentialsPermissionNotificationIds.keySet()) {
                if (account.equals(pair.first.first)) {
                    int id = accounts.credentialsPermissionNotificationIds.get(pair);
                    cancelNotification(id, user);
                }
            }
        }

        logRecord(accounts, DebugDbHelper.ACTION_CALLED_ACCOUNT_REMOVE, TABLE_ACCOUNTS);

        try {
            new RemoveAccountSession(accounts, response, account, expectActivityLaunch).bind();
        } finally {
            restoreCallingIdentity(identityToken);
        }
    }

    @Override
    public boolean removeAccountExplicitly(Account account) {
        final int callingUid = VBinder.getCallingUid();
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, "removeAccountExplicitly: " + account
                    + ", caller's uid " + callingUid
                    + ", pid " + VBinder.getCallingPid());
        }
        int userId = VBinder.getCallingUserHandle().getIdentifier();
        if (account == null) {
            /*
             * Null accounts should result in returning false, as per
             * AccountManage.addAccountExplicitly(...) java doc.
             */
            Log.e(TAG, "account is null");
            return false;
        } else if (!isAccountManagedByCaller(account.type, callingUid, userId)) {
            String msg = String.format(
                    "uid %s cannot explicitly add accounts of type: %s",
                    callingUid,
                    account.type);
            throw new SecurityException(msg);
        }
        UserAccounts accounts = getUserAccountsForCaller();
        if (!canUserModifyAccounts(userId) || !canUserModifyAccountsForType(userId, account.type)) {
            return false;
        }
        logRecord(accounts, DebugDbHelper.ACTION_CALLED_ACCOUNT_REMOVE, TABLE_ACCOUNTS);
        long identityToken = clearCallingIdentity();
        try {
            return removeAccountInternal(accounts, account);
        } finally {
            restoreCallingIdentity(identityToken);
        }
    }

    private class RemoveAccountSession extends Session {
        final Account mAccount;
        public RemoveAccountSession(UserAccounts accounts, IAccountManagerResponse response,
                Account account, boolean expectActivityLaunch) {
            super(accounts, response, account.type, expectActivityLaunch,
                    true /* stripAuthTokenFromResult */, account.name,
                    false /* authDetailsRequired */);
            mAccount = account;
        }

        @Override
        protected String toDebugString(long now) {
            return super.toDebugString(now) + ", removeAccount"
                    + ", account " + mAccount;
        }

        @Override
        public void run() throws RemoteException {
            mAuthenticator.getAccountRemovalAllowed(this, mAccount);
        }

        @Override
        public void onResult(Bundle result) {
            if (result != null && result.containsKey(AccountManager.KEY_BOOLEAN_RESULT)
                    && !result.containsKey(AccountManager.KEY_INTENT)) {
                final boolean removalAllowed = result.getBoolean(AccountManager.KEY_BOOLEAN_RESULT);
                if (removalAllowed) {
                    removeAccountInternal(mAccounts, mAccount);
                }
                IAccountManagerResponse response = getResponseAndClose();
                if (response != null) {
                    if (Log.isLoggable(TAG, Log.VERBOSE)) {
                        Log.v(TAG, getClass().getSimpleName() + " calling onResult() on response "
                                + response);
                    }
                    Bundle result2 = new Bundle();
                    result2.putBoolean(AccountManager.KEY_BOOLEAN_RESULT, removalAllowed);
                    try {
                        response.onResult(result2);
                    } catch (RemoteException e) {
                        // ignore
                    }
                }
            }
            super.onResult(result);
        }
    }

    /* For testing */
    protected void removeAccountInternal(Account account) {
        removeAccountInternal(getUserAccountsForCaller(), account);
    }

    private boolean removeAccountInternal(UserAccounts accounts, Account account) {
        int deleted;
        synchronized (accounts.cacheLock) {
            final SQLiteDatabase db = accounts.openHelper.getWritableDatabase();
            final long accountId = getAccountIdLocked(db, account);
            deleted = db.delete(TABLE_ACCOUNTS, ACCOUNTS_NAME + "=? AND " + ACCOUNTS_TYPE
                    + "=?",
                    new String[]{account.name, account.type});
            removeAccountFromCacheLocked(accounts, account);
            sendAccountsChangedBroadcast(accounts.userId);

            logRecord(db, DebugDbHelper.ACTION_ACCOUNT_REMOVE, TABLE_ACCOUNTS, accountId, accounts);
        }
        if (accounts.userId == VUserHandle.USER_OWNER) {
            // Owner's account was removed, remove from any users that are sharing
            // this account.
            int callingUid = getCallingUid();
            long id = Binder.clearCallingIdentity();
            try {
                List<UserInfo> users = mUserManager.getUsers(true);
                for (UserInfo user : users) {
                    if (!user.isPrimary() && user.isRestricted()) {
                        removeSharedAccountAsUser(account, user.id, callingUid);
                    }
                }
            } finally {
                Binder.restoreCallingIdentity(id);
            }
        }
        return (deleted > 0);
    }

    @Override
    public void invalidateAuthToken(String accountType, String authToken) {
        int callerUid = VBinder.getCallingUid();
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, "invalidateAuthToken: accountType " + accountType
                    + ", caller's uid " + callerUid
                    + ", pid " + VBinder.getCallingPid());
        }
        if (accountType == null) throw new IllegalArgumentException("accountType is null");
        if (authToken == null) throw new IllegalArgumentException("authToken is null");
        int userId = VUserHandle.getCallingUserId();
        long identityToken = clearCallingIdentity();
        try {
            UserAccounts accounts = getUserAccounts(userId);
            synchronized (accounts.cacheLock) {
                final SQLiteDatabase db = accounts.openHelper.getWritableDatabase();
                db.beginTransaction();
                try {
                    invalidateAuthTokenLocked(accounts, db, accountType, authToken);
                    invalidateCustomTokenLocked(accounts, accountType, authToken);
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
            }
        } finally {
            restoreCallingIdentity(identityToken);
        }
    }

    private void invalidateCustomTokenLocked(
            UserAccounts accounts,
            String accountType,
            String authToken) {
        if (authToken == null || accountType == null) {
            return;
        }
        // Also wipe out cached token in memory.
        accounts.accountTokenCaches.remove(accountType, authToken);
    }

    private void invalidateAuthTokenLocked(UserAccounts accounts, SQLiteDatabase db,
            String accountType, String authToken) {
        if (authToken == null || accountType == null) {
            return;
        }
        Cursor cursor = db.rawQuery(
                "SELECT " + TABLE_AUTHTOKENS + "." + AUTHTOKENS_ID
                        + ", " + TABLE_ACCOUNTS + "." + ACCOUNTS_NAME
                        + ", " + TABLE_AUTHTOKENS + "." + AUTHTOKENS_TYPE
                        + " FROM " + TABLE_ACCOUNTS
                        + " JOIN " + TABLE_AUTHTOKENS
                        + " ON " + TABLE_ACCOUNTS + "." + ACCOUNTS_ID
                        + " = " + AUTHTOKENS_ACCOUNTS_ID
                        + " WHERE " + AUTHTOKENS_AUTHTOKEN + " = ? AND "
                        + TABLE_ACCOUNTS + "." + ACCOUNTS_TYPE + " = ?",
                new String[]{authToken, accountType});
        try {
            while (cursor.moveToNext()) {
                long authTokenId = cursor.getLong(0);
                String accountName = cursor.getString(1);
                String authTokenType = cursor.getString(2);
                db.delete(TABLE_AUTHTOKENS, AUTHTOKENS_ID + "=" + authTokenId, null);
                writeAuthTokenIntoCacheLocked(
                        accounts,
                        db,
                        new Account(accountName, accountType),
                        authTokenType,
                        null);
            }
        } finally {
            cursor.close();
        }
    }

    private void saveCachedToken(
            UserAccounts accounts,
            Account account,
            String callerPkg,
            byte[] callerSigDigest,
            String tokenType,
            String token,
            long expiryMillis) {

        if (account == null || tokenType == null || callerPkg == null || callerSigDigest == null) {
            return;
        }
        cancelNotification(getSigninRequiredNotificationId(accounts, account),
                new VUserHandle(accounts.userId));
        synchronized (accounts.cacheLock) {
            accounts.accountTokenCaches.put(
                    account, token, tokenType, callerPkg, callerSigDigest, expiryMillis);
        }
    }

    private boolean saveAuthTokenToDatabase(UserAccounts accounts, Account account, String type,
            String authToken) {
        if (account == null || type == null) {
            return false;
        }
        cancelNotification(getSigninRequiredNotificationId(accounts, account),
                new VUserHandle(accounts.userId));
        synchronized (accounts.cacheLock) {
            final SQLiteDatabase db = accounts.openHelper.getWritableDatabase();
            db.beginTransaction();
            try {
                long accountId = getAccountIdLocked(db, account);
                if (accountId < 0) {
                    return false;
                }
                db.delete(TABLE_AUTHTOKENS,
                        AUTHTOKENS_ACCOUNTS_ID + "=" + accountId + " AND " + AUTHTOKENS_TYPE + "=?",
                        new String[]{type});
                ContentValues values = new ContentValues();
                values.put(AUTHTOKENS_ACCOUNTS_ID, accountId);
                values.put(AUTHTOKENS_TYPE, type);
                values.put(AUTHTOKENS_AUTHTOKEN, authToken);
                if (db.insert(TABLE_AUTHTOKENS, AUTHTOKENS_AUTHTOKEN, values) >= 0) {
                    db.setTransactionSuccessful();
                    writeAuthTokenIntoCacheLocked(accounts, db, account, type, authToken);
                    return true;
                }
                return false;
            } finally {
                db.endTransaction();
            }
        }
    }

    @Override
    public String peekAuthToken(Account account, String authTokenType) {
        final int callingUid = VBinder.getCallingUid();
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, "peekAuthToken: " + account
                    + ", authTokenType " + authTokenType
                    + ", caller's uid " + callingUid
                    + ", pid " + VBinder.getCallingPid());
        }
        if (account == null) throw new IllegalArgumentException("account is null");
        if (authTokenType == null) throw new IllegalArgumentException("authTokenType is null");
        int userId = VUserHandle.getCallingUserId();
        if (!isAccountManagedByCaller(account.type, callingUid, userId)) {
            String msg = String.format(
                    "uid %s cannot peek the authtokens associated with accounts of type: %s",
                    callingUid,
                    account.type);
            throw new SecurityException(msg);
        }
        long identityToken = clearCallingIdentity();
        try {
            UserAccounts accounts = getUserAccounts(userId);
            return readAuthTokenInternal(accounts, account, authTokenType);
        } finally {
            restoreCallingIdentity(identityToken);
        }
    }

    @Override
    public void setAuthToken(Account account, String authTokenType, String authToken) {
        final int callingUid = VBinder.getCallingUid();
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, "setAuthToken: " + account
                    + ", authTokenType " + authTokenType
                    + ", caller's uid " + callingUid
                    + ", pid " + VBinder.getCallingPid());
        }
        if (account == null) throw new IllegalArgumentException("account is null");
        if (authTokenType == null) throw new IllegalArgumentException("authTokenType is null");
        int userId = VUserHandle.getCallingUserId();
        if (!isAccountManagedByCaller(account.type, callingUid, userId)) {
            String msg = String.format(
                    "uid %s cannot set auth tokens associated with accounts of type: %s",
                    callingUid,
                    account.type);
            throw new SecurityException(msg);
        }
        long identityToken = clearCallingIdentity();
        try {
            UserAccounts accounts = getUserAccounts(userId);
            saveAuthTokenToDatabase(accounts, account, authTokenType, authToken);
        } finally {
            restoreCallingIdentity(identityToken);
        }
    }

    @Override
    public void setPassword(Account account, String password) {
        final int callingUid = VBinder.getCallingUid();
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, "setAuthToken: " + account
                    + ", caller's uid " + callingUid
                    + ", pid " + VBinder.getCallingPid());
        }
        if (account == null) throw new IllegalArgumentException("account is null");
        int userId = VUserHandle.getCallingUserId();
        if (!isAccountManagedByCaller(account.type, callingUid, userId)) {
            String msg = String.format(
                    "uid %s cannot set secrets for accounts of type: %s",
                    callingUid,
                    account.type);
            throw new SecurityException(msg);
        }
        long identityToken = clearCallingIdentity();
        try {
            UserAccounts accounts = getUserAccounts(userId);
            setPasswordInternal(accounts, account, password, callingUid);
        } finally {
            restoreCallingIdentity(identityToken);
        }
    }

    private void setPasswordInternal(UserAccounts accounts, Account account, String password,
            int callingUid) {
        if (account == null) {
            return;
        }
        synchronized (accounts.cacheLock) {
            final SQLiteDatabase db = accounts.openHelper.getWritableDatabase();
            db.beginTransaction();
            try {
                final ContentValues values = new ContentValues();
                values.put(ACCOUNTS_PASSWORD, password);
                final long accountId = getAccountIdLocked(db, account);
                if (accountId >= 0) {
                    final String[] argsAccountId = {String.valueOf(accountId)};
                    db.update(TABLE_ACCOUNTS, values, ACCOUNTS_ID + "=?", argsAccountId);
                    db.delete(TABLE_AUTHTOKENS, AUTHTOKENS_ACCOUNTS_ID + "=?", argsAccountId);
                    accounts.authTokenCache.remove(account);
                    accounts.accountTokenCaches.remove(account);
                    db.setTransactionSuccessful();

                    String action = (password == null || password.length() == 0) ?
                            DebugDbHelper.ACTION_CLEAR_PASSWORD
                            : DebugDbHelper.ACTION_SET_PASSWORD;
                    logRecord(db, action, TABLE_ACCOUNTS, accountId, accounts, callingUid);
                }
            } finally {
                db.endTransaction();
            }
            sendAccountsChangedBroadcast(accounts.userId);
        }
    }

    private void sendAccountsChangedBroadcast(int userId) {
        Log.i(TAG, "the accounts changed, sending broadcast of "
                + ACCOUNTS_CHANGED_INTENT.getAction());
        VActivityManagerService.getService().sendBroadcastAsUser(ACCOUNTS_CHANGED_INTENT, new VUserHandle(userId));
    }

    @Override
    public void clearPassword(Account account) {
        final int callingUid = VBinder.getCallingUid();
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, "clearPassword: " + account
                    + ", caller's uid " + callingUid
                    + ", pid " + VBinder.getCallingPid());
        }
        if (account == null) throw new IllegalArgumentException("account is null");
        int userId = VUserHandle.getCallingUserId();
        if (!isAccountManagedByCaller(account.type, callingUid, userId)) {
            String msg = String.format(
                    "uid %s cannot clear passwords for accounts of type: %s",
                    callingUid,
                    account.type);
            throw new SecurityException(msg);
        }
        long identityToken = clearCallingIdentity();
        try {
            UserAccounts accounts = getUserAccounts(userId);
            setPasswordInternal(accounts, account, null, callingUid);
        } finally {
            restoreCallingIdentity(identityToken);
        }
    }

    @Override
    public void setUserData(Account account, String key, String value) {
        final int callingUid = VBinder.getCallingUid();
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, "setUserData: " + account
                    + ", key " + key
                    + ", caller's uid " + callingUid
                    + ", pid " + VBinder.getCallingPid());
        }
        if (key == null) throw new IllegalArgumentException("key is null");
        if (account == null) throw new IllegalArgumentException("account is null");
        int userId = VUserHandle.getCallingUserId();
        if (!isAccountManagedByCaller(account.type, callingUid, userId)) {
            String msg = String.format(
                    "uid %s cannot set user data for accounts of type: %s",
                    callingUid,
                    account.type);
            throw new SecurityException(msg);
        }
        long identityToken = clearCallingIdentity();
        try {
            UserAccounts accounts = getUserAccounts(userId);
            setUserdataInternal(accounts, account, key, value);
        } finally {
            restoreCallingIdentity(identityToken);
        }
    }

    private void setUserdataInternal(UserAccounts accounts, Account account, String key,
            String value) {
        if (account == null || key == null) {
            return;
        }
        synchronized (accounts.cacheLock) {
            final SQLiteDatabase db = accounts.openHelper.getWritableDatabase();
            db.beginTransaction();
            try {
                long accountId = getAccountIdLocked(db, account);
                if (accountId < 0) {
                    return;
                }
                long extrasId = getExtrasIdLocked(db, accountId, key);
                if (extrasId < 0 ) {
                    extrasId = insertExtraLocked(db, accountId, key, value);
                    if (extrasId < 0) {
                        return;
                    }
                } else {
                    ContentValues values = new ContentValues();
                    values.put(EXTRAS_VALUE, value);
                    if (1 != db.update(TABLE_EXTRAS, values, EXTRAS_ID + "=" + extrasId, null)) {
                        return;
                    }

                }
                writeUserDataIntoCacheLocked(accounts, db, account, key, value);
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        }
    }

    private void onResult(IAccountManagerResponse response, Bundle result) {
        if (result == null) {
            Log.e(TAG, "the result is unexpectedly null", new Exception());
        }
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, getClass().getSimpleName() + " calling onResult() on response "
                    + response);
        }
        try {
            response.onResult(result);
        } catch (RemoteException e) {
            // if the caller is dead then there is no one to care about remote
            // exceptions
            if (Log.isLoggable(TAG, Log.VERBOSE)) {
                Log.v(TAG, "failure while notifying response", e);
            }
        }
    }

    @Override
    public void getAuthTokenLabel(IAccountManagerResponse response, final String accountType,
                                  final String authTokenType)
            throws RemoteException {
        if (accountType == null) throw new IllegalArgumentException("accountType is null");
        if (authTokenType == null) throw new IllegalArgumentException("authTokenType is null");

        final int callingUid = getCallingUid();
        clearCallingIdentity();
        if (callingUid != Process.SYSTEM_UID) {
            throw new SecurityException("can only call from system");
        }
        int userId = VUserHandle.getUserId(callingUid);
        long identityToken = clearCallingIdentity();
        try {
            UserAccounts accounts = getUserAccounts(userId);
            new Session(accounts, response, accountType, false /* expectActivityLaunch */,
                    false /* stripAuthTokenFromResult */,  null /* accountName */,
                    false /* authDetailsRequired */) {
                @Override
                protected String toDebugString(long now) {
                    return super.toDebugString(now) + ", getAuthTokenLabel"
                            + ", " + accountType
                            + ", authTokenType " + authTokenType;
                }

                @Override
                public void run() throws RemoteException {
                    mAuthenticator.getAuthTokenLabel(this, authTokenType);
                }

                @Override
                public void onResult(Bundle result) {
                    if (result != null) {
                        String label = result.getString(AccountManager.KEY_AUTH_TOKEN_LABEL);
                        Bundle bundle = new Bundle();
                        bundle.putString(AccountManager.KEY_AUTH_TOKEN_LABEL, label);
                        super.onResult(bundle);
                        return;
                    } else {
                        super.onResult(result);
                    }
                }
            }.bind();
        } finally {
            restoreCallingIdentity(identityToken);
        }
    }

    @Override
    public void getAuthToken(
            IAccountManagerResponse response,
            final Account account,
            final String authTokenType,
            final boolean notifyOnAuthFailure,
            final boolean expectActivityLaunch,
            final Bundle loginOptions) {
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, "getAuthToken: " + account
                    + ", response " + response
                    + ", authTokenType " + authTokenType
                    + ", notifyOnAuthFailure " + notifyOnAuthFailure
                    + ", expectActivityLaunch " + expectActivityLaunch
                    + ", caller's uid " + VBinder.getCallingUid()
                    + ", pid " + VBinder.getCallingPid());
        }
        if (response == null) throw new IllegalArgumentException("response is null");
        try {
            if (account == null) {
                Slog.w(TAG, "getAuthToken called with null account");
                response.onError(AccountManager.ERROR_CODE_BAD_ARGUMENTS, "account is null");
                return;
            }
            if (authTokenType == null) {
                Slog.w(TAG, "getAuthToken called with null authTokenType");
                response.onError(AccountManager.ERROR_CODE_BAD_ARGUMENTS, "authTokenType is null");
                return;
            }
        } catch (RemoteException e) {
            Slog.w(TAG, "Failed to report error back to the client." + e);
            return;
        }
        int userId = VUserHandle.getCallingUserId();
        long ident = Binder.clearCallingIdentity();
        final UserAccounts accounts;
        final RegisteredServicesCache.ServiceInfo<AuthenticatorDescription> authenticatorInfo;
        try {
            accounts = getUserAccounts(userId);
            authenticatorInfo = mAuthenticatorCache.getServiceInfo(
                    AuthenticatorDescription.newKey(account.type), accounts.userId);
        } finally {
            Binder.restoreCallingIdentity(ident);
        }

        final boolean customTokens =
                authenticatorInfo != null && authenticatorInfo.type.customTokens;

        // skip the check if customTokens
        final int callerUid = VBinder.getCallingUid();
        final boolean permissionGranted =
                customTokens || permissionIsGranted(account, authTokenType, callerUid, userId);

        // Get the calling package. We will use it for the purpose of caching.
        final String callerPkg = loginOptions.getString(AccountManager.KEY_ANDROID_PACKAGE_NAME);
        List<String> callerOwnedPackageNames;
        ident = Binder.clearCallingIdentity();
        try {
            callerOwnedPackageNames = Arrays.asList(VPackageManagerService.getService().getPackagesForUid(callerUid));
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
        if (callerPkg == null || !callerOwnedPackageNames.contains(callerPkg)) {
            String msg = String.format(
                    "Uid %s is attempting to illegally masquerade as package %s!",
                    callerUid,
                    callerPkg);
            throw new SecurityException(msg);
        }

        // let authenticator know the identity of the caller
        loginOptions.putInt(AccountManager.KEY_CALLER_UID, callerUid);
        loginOptions.putInt(AccountManager.KEY_CALLER_PID, VBinder.getCallingPid());

        if (notifyOnAuthFailure) {
            loginOptions.putBoolean(AccountManager.KEY_NOTIFY_ON_FAILURE, true);
        }

        long identityToken = clearCallingIdentity();
        try {
            // Distill the caller's package signatures into a single digest.
            final byte[] callerPkgSigDigest = calculatePackageSignatureDigest(callerPkg, callerUid);

            // if the caller has permission, do the peek. otherwise go the more expensive
            // route of starting a Session
            if (!customTokens && permissionGranted) {
                String authToken = readAuthTokenInternal(accounts, account, authTokenType);
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
                /*
                 * Look up tokens in the new cache only if the loginOptions don't have parameters
                 * outside of those expected to be injected by the AccountManager, e.g.
                 * ANDORID_PACKAGE_NAME.
                 */
                String token = readCachedTokenInternal(
                        accounts,
                        account,
                        authTokenType,
                        callerPkg,
                        callerPkgSigDigest);
                if (token != null) {
                    if (Log.isLoggable(TAG, Log.VERBOSE)) {
                        Log.v(TAG, "getAuthToken: cache hit ofr custom token authenticator.");
                    }
                    Bundle result = new Bundle();
                    result.putString(AccountManager.KEY_AUTHTOKEN, token);
                    result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
                    result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
                    onResult(response, result);
                    return;
                }
            }

            new Session(accounts, response, account.type, expectActivityLaunch,
                    false /* stripAuthTokenFromResult */, account.name,
                    false /* authDetailsRequired */) {
                @Override
                protected String toDebugString(long now) {
                    if (loginOptions != null) loginOptions.keySet();
                    return super.toDebugString(now) + ", getAuthToken"
                            + ", " + account
                            + ", authTokenType " + authTokenType
                            + ", loginOptions " + loginOptions
                            + ", notifyOnAuthFailure " + notifyOnAuthFailure;
                }

                @Override
                public void run() throws RemoteException {
                    // If the caller doesn't have permission then create and return the
                    // "grant permission" intent instead of the "getAuthToken" intent.
                    if (!permissionGranted) {
                        mAuthenticator.getAuthTokenLabel(this, authTokenType);
                    } else {
                        mAuthenticator.getAuthToken(this, account, authTokenType, loginOptions);
                    }
                }

                @Override
                public void onResult(Bundle result) {
                    if (result != null) {
                        if (result.containsKey(AccountManager.KEY_AUTH_TOKEN_LABEL)) {
                            Intent intent = newGrantCredentialsPermissionIntent(
                                    account,
                                    callerUid,
                                    new AccountAuthenticatorResponse(this),
                                    authTokenType);
                            Bundle bundle = new Bundle();
                            bundle.putParcelable(AccountManager.KEY_INTENT, intent);
                            onResult(bundle);
                            return;
                        }
                        String authToken = result.getString(AccountManager.KEY_AUTHTOKEN);
                        if (authToken != null) {
                            String name = result.getString(AccountManager.KEY_ACCOUNT_NAME);
                            String type = result.getString(AccountManager.KEY_ACCOUNT_TYPE);
                            if (TextUtils.isEmpty(type) || TextUtils.isEmpty(name)) {
                                onError(AccountManager.ERROR_CODE_INVALID_RESPONSE,
                                        "the type and name should not be empty");
                                return;
                            }
                            Account resultAccount = new Account(name, type);
                            if (!customTokens) {
                                saveAuthTokenToDatabase(
                                        mAccounts,
                                        resultAccount,
                                        authTokenType,
                                        authToken);
                            }
                            long expiryMillis = result.getLong(
                                    AbstractAccountAuthenticator.KEY_CUSTOM_TOKEN_EXPIRY, 0L);
                            if (customTokens
                                    && expiryMillis > System.currentTimeMillis()) {
                                saveCachedToken(
                                        mAccounts,
                                        account,
                                        callerPkg,
                                        callerPkgSigDigest,
                                        authTokenType,
                                        authToken,
                                        expiryMillis);
                            }
                        }

                        Intent intent = result.getParcelable(AccountManager.KEY_INTENT);
                        if (intent != null && notifyOnAuthFailure && !customTokens) {
                            doNotification(mAccounts,
                                    account, result.getString(AccountManager.KEY_AUTH_FAILED_MESSAGE),
                                    intent, accounts.userId);
                        }
                    }
                    super.onResult(result);
                }
            }.bind();
        } finally {
            restoreCallingIdentity(identityToken);
        }
    }

    private byte[] calculatePackageSignatureDigest(String callerPkg, int callingUid) {
        MessageDigest digester;
        try {
            digester = MessageDigest.getInstance("SHA-256");
            PackageInfo pkgInfo = VPackageManagerService.getService().getPackageInfo(
                    callerPkg, PackageManager.GET_SIGNATURES, VUserHandle.getUserId(callingUid));
            for (Signature sig : pkgInfo.signatures) {
                digester.update(sig.toByteArray());
            }
        } catch (NoSuchAlgorithmException x) {
            Log.wtf(TAG, "SHA-256 should be available", x);
            digester = null;
        }
        return (digester == null) ? null : digester.digest();
    }

    private void createNoCredentialsPermissionNotification(Account account, Intent intent,
            int userId) {
//        int uid = intent.getIntExtra(
//                GrantCredentialsPermissionActivity.EXTRAS_REQUESTING_UID, -1);
//        String authTokenType = intent.getStringExtra(
//                GrantCredentialsPermissionActivity.EXTRAS_AUTH_TOKEN_TYPE);
//        final String titleAndSubtitle =
//                mContext.getString(R.string.permission_request_notification_with_subtitle,
//                account.name);
//        final int index = titleAndSubtitle.indexOf('\n');
//        String title = titleAndSubtitle;
//        String subtitle = "";
//        if (index > 0) {
//            title = titleAndSubtitle.substring(0, index);
//            subtitle = titleAndSubtitle.substring(index + 1);
//        }
//        VUserHandle user = new VUserHandle(userId);
//        Context contextForUser = getContextForUser(user);
//        Notification n = new Notification.Builder(contextForUser)
//                .setSmallIcon(android.R.drawable.stat_sys_warning)
//                .setWhen(0)
//                .setColor(contextForUser.getColor(
//                        com.android.internal.R.color.system_notification_accent_color))
//                .setContentTitle(title)
//                .setContentText(subtitle)
//                .setContentIntent(PendingIntent.getActivityAsUser(mContext, 0, intent,
//                        PendingIntent.FLAG_CANCEL_CURRENT, null, user))
//                .build();
//        installNotification(getCredentialPermissionNotificationId(
//                account, authTokenType, uid), n, user);
    }

    private Intent newGrantCredentialsPermissionIntent(Account account, int uid,
            AccountAuthenticatorResponse response, String authTokenType) {

        Intent intent = new Intent(mContext, GrantCredentialsPermissionActivity.class);
        // See FLAG_ACTIVITY_NEW_TASK docs for limitations and benefits of the flag.
        // Since it was set in Eclair+ we can't change it without breaking apps using
        // the intent from a non-Activity context.
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addCategory(
                String.valueOf(getCredentialPermissionNotificationId(account, authTokenType, uid)));

        intent.putExtra(GrantCredentialsPermissionActivity.EXTRAS_ACCOUNT, account);
        intent.putExtra(GrantCredentialsPermissionActivity.EXTRAS_AUTH_TOKEN_TYPE, authTokenType);
        intent.putExtra(GrantCredentialsPermissionActivity.EXTRAS_RESPONSE, response);
        // FIXME: uid
        intent.putExtra(GrantCredentialsPermissionActivity.EXTRAS_REQUESTING_UID, Process.myUid());

        return intent;
    }

    private Integer getCredentialPermissionNotificationId(Account account, String authTokenType,
            int uid) {
        Integer id;
        UserAccounts accounts = getUserAccounts(VUserHandle.getUserId(uid));
        synchronized (accounts.credentialsPermissionNotificationIds) {
            final Pair<Pair<Account, String>, Integer> key =
                    new Pair<Pair<Account, String>, Integer>(
                            new Pair<Account, String>(account, authTokenType), uid);
            id = accounts.credentialsPermissionNotificationIds.get(key);
            if (id == null) {
                id = mNotificationIds.incrementAndGet();
                accounts.credentialsPermissionNotificationIds.put(key, id);
            }
        }
        return id;
    }

    private Integer getSigninRequiredNotificationId(UserAccounts accounts, Account account) {
        Integer id;
        synchronized (accounts.signinRequiredNotificationIds) {
            id = accounts.signinRequiredNotificationIds.get(account);
            if (id == null) {
                id = mNotificationIds.incrementAndGet();
                accounts.signinRequiredNotificationIds.put(account, id);
            }
        }
        return id;
    }

    @Override
    public void addAccount(final IAccountManagerResponse response, final String accountType,
            final String authTokenType, final String[] requiredFeatures,
            final boolean expectActivityLaunch, final Bundle optionsIn) {
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, "addAccount: accountType " + accountType
                    + ", response " + response
                    + ", authTokenType " + authTokenType
                    + ", requiredFeatures " + stringArrayToString(requiredFeatures)
                    + ", expectActivityLaunch " + expectActivityLaunch
                    + ", caller's uid " + VBinder.getCallingUid()
                    + ", pid " + VBinder.getCallingPid());
        }
        if (response == null) throw new IllegalArgumentException("response is null");
        if (accountType == null) throw new IllegalArgumentException("accountType is null");

        // Is user disallowed from modifying accounts?
        int userId = VBinder.getCallingUserHandle().getIdentifier();
        if (!canUserModifyAccounts(userId)) {
            try {
                response.onError(AccountManager.ERROR_CODE_USER_RESTRICTED,
                        "User is not allowed to add an account!");
            } catch (RemoteException re) {
            }
            showCantAddAccount(AccountManager.ERROR_CODE_USER_RESTRICTED, userId);
            return;
        }
        if (!canUserModifyAccountsForType(userId, accountType)) {
            try {
                response.onError(AccountManager.ERROR_CODE_MANAGEMENT_DISABLED_FOR_ACCOUNT_TYPE,
                        "User cannot modify accounts of this type (policy).");
            } catch (RemoteException re) {
            }
            showCantAddAccount(AccountManager.ERROR_CODE_MANAGEMENT_DISABLED_FOR_ACCOUNT_TYPE,
                    userId);
            return;
        }

        final int pid = VBinder.getCallingPid();
        final int uid = VBinder.getCallingUid();
        final Bundle options = (optionsIn == null) ? new Bundle() : optionsIn;
        options.putInt(AccountManager.KEY_CALLER_UID, uid);
        options.putInt(AccountManager.KEY_CALLER_PID, pid);

        int usrId = VUserHandle.getCallingUserId();
        long identityToken = clearCallingIdentity();
        try {
            UserAccounts accounts = getUserAccounts(usrId);
            logRecordWithUid(
                    accounts, DebugDbHelper.ACTION_CALLED_ACCOUNT_ADD, TABLE_ACCOUNTS, uid);
            new Session(accounts, response, accountType, expectActivityLaunch,
                    true /* stripAuthTokenFromResult */, null /* accountName */,
                    false /* authDetailsRequired */, true /* updateLastAuthenticationTime */) {
                @Override
                public void run() throws RemoteException {
                    mAuthenticator.addAccount(this, mAccountType, authTokenType, requiredFeatures,
                            options);
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
        } finally {
            restoreCallingIdentity(identityToken);
        }
    }

    @Override
    public void addAccountAsUser(final IAccountManagerResponse response, final String accountType,
            final String authTokenType, final String[] requiredFeatures,
            final boolean expectActivityLaunch, final Bundle optionsIn, int userId) {
        int callingUid = VBinder.getCallingUid();
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, "addAccount: accountType " + accountType
                    + ", response " + response
                    + ", authTokenType " + authTokenType
                    + ", requiredFeatures " + stringArrayToString(requiredFeatures)
                    + ", expectActivityLaunch " + expectActivityLaunch
                    + ", caller's uid " + VBinder.getCallingUid()
                    + ", pid " + VBinder.getCallingPid()
                    + ", for user id " + userId);
        }
        if (response == null) throw new IllegalArgumentException("response is null");
        if (accountType == null) throw new IllegalArgumentException("accountType is null");
        // Only allow the system process to add accounts of other users
        if (isCrossUser(callingUid, userId)) {
            throw new SecurityException(
                    String.format(
                            "User %s trying to add account for %s" ,
                            VUserHandle.getCallingUserId(),
                            userId));
        }

        // Is user disallowed from modifying accounts?
        if (!canUserModifyAccounts(userId)) {
            try {
                response.onError(AccountManager.ERROR_CODE_USER_RESTRICTED,
                        "User is not allowed to add an account!");
            } catch (RemoteException re) {
            }
            showCantAddAccount(AccountManager.ERROR_CODE_USER_RESTRICTED, userId);
            return;
        }
        if (!canUserModifyAccountsForType(userId, accountType)) {
            try {
                response.onError(AccountManager.ERROR_CODE_MANAGEMENT_DISABLED_FOR_ACCOUNT_TYPE,
                        "User cannot modify accounts of this type (policy).");
            } catch (RemoteException re) {
            }
            showCantAddAccount(AccountManager.ERROR_CODE_MANAGEMENT_DISABLED_FOR_ACCOUNT_TYPE,
                    userId);
            return;
        }

        final int pid = VBinder.getCallingPid();
        final int uid = VBinder.getCallingUid();
        final Bundle options = (optionsIn == null) ? new Bundle() : optionsIn;
        options.putInt(AccountManager.KEY_CALLER_UID, uid);
        options.putInt(AccountManager.KEY_CALLER_PID, pid);

        long identityToken = clearCallingIdentity();
        try {
            UserAccounts accounts = getUserAccounts(userId);
            logRecordWithUid(
                    accounts, DebugDbHelper.ACTION_CALLED_ACCOUNT_ADD, TABLE_ACCOUNTS, userId);
            new Session(accounts, response, accountType, expectActivityLaunch,
                    true /* stripAuthTokenFromResult */, null /* accountName */,
                    false /* authDetailsRequired */, true /* updateLastAuthenticationTime */) {
                @Override
                public void run() throws RemoteException {
                    mAuthenticator.addAccount(this, mAccountType, authTokenType, requiredFeatures,
                            options);
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
        } finally {
            restoreCallingIdentity(identityToken);
        }
    }

    private void showCantAddAccount(int errorCode, int userId) {
        Intent cantAddAccount = new Intent(mContext, CantAddAccountActivity.class);
        cantAddAccount.putExtra(CantAddAccountActivity.EXTRA_ERROR_CODE, errorCode);
        cantAddAccount.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        long identityToken = clearCallingIdentity();
        try {
            cantAddAccount.putExtra(ExtraConstants.EXTRA_TARGET_USER, userId);
            mContext.startActivity(cantAddAccount);
        } finally {
            restoreCallingIdentity(identityToken);
        }
    }

    @Override
    public void confirmCredentialsAsUser(
            IAccountManagerResponse response,
            final Account account,
            final Bundle options,
            final boolean expectActivityLaunch,
            int userId) {
        int callingUid = VBinder.getCallingUid();
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, "confirmCredentials: " + account
                    + ", response " + response
                    + ", expectActivityLaunch " + expectActivityLaunch
                    + ", caller's uid " + callingUid
                    + ", pid " + VBinder.getCallingPid());
        }
        // Only allow the system process to read accounts of other users
        if (isCrossUser(callingUid, userId)) {
            throw new SecurityException(
                    String.format(
                            "User %s trying to confirm account credentials for %s" ,
                            VUserHandle.getCallingUserId(),
                            userId));
        }
        if (response == null) throw new IllegalArgumentException("response is null");
        if (account == null) throw new IllegalArgumentException("account is null");
        long identityToken = clearCallingIdentity();
        try {
            UserAccounts accounts = getUserAccounts(userId);
            new Session(accounts, response, account.type, expectActivityLaunch,
                    true /* stripAuthTokenFromResult */, account.name,
                    true /* authDetailsRequired */, true /* updateLastAuthenticatedTime */) {
                @Override
                public void run() throws RemoteException {
                    mAuthenticator.confirmCredentials(this, account, options);
                }
                @Override
                protected String toDebugString(long now) {
                    return super.toDebugString(now) + ", confirmCredentials"
                            + ", " + account;
                }
            }.bind();
        } finally {
            restoreCallingIdentity(identityToken);
        }
    }

    @Override
    public void updateCredentials(IAccountManagerResponse response, final Account account,
            final String authTokenType, final boolean expectActivityLaunch,
            final Bundle loginOptions) {
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, "updateCredentials: " + account
                    + ", response " + response
                    + ", authTokenType " + authTokenType
                    + ", expectActivityLaunch " + expectActivityLaunch
                    + ", caller's uid " + VBinder.getCallingUid()
                    + ", pid " + VBinder.getCallingPid());
        }
        if (response == null) throw new IllegalArgumentException("response is null");
        if (account == null) throw new IllegalArgumentException("account is null");
        if (authTokenType == null) throw new IllegalArgumentException("authTokenType is null");
        int userId = VUserHandle.getCallingUserId();
        long identityToken = clearCallingIdentity();
        try {
            UserAccounts accounts = getUserAccounts(userId);
            new Session(accounts, response, account.type, expectActivityLaunch,
                    true /* stripAuthTokenFromResult */, account.name,
                    false /* authDetailsRequired */, true /* updateLastCredentialTime */) {
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
        } finally {
            restoreCallingIdentity(identityToken);
        }
    }

    @Override
    public void editProperties(IAccountManagerResponse response, final String accountType,
            final boolean expectActivityLaunch) {
        final int callingUid = VBinder.getCallingUid();
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, "editProperties: accountType " + accountType
                    + ", response " + response
                    + ", expectActivityLaunch " + expectActivityLaunch
                    + ", caller's uid " + callingUid
                    + ", pid " + VBinder.getCallingPid());
        }
        if (response == null) throw new IllegalArgumentException("response is null");
        if (accountType == null) throw new IllegalArgumentException("accountType is null");
        int userId = VUserHandle.getCallingUserId();
        if (!isAccountManagedByCaller(accountType, callingUid, userId) && !isSystemUid(callingUid)) {
            String msg = String.format(
                    "uid %s cannot edit authenticator properites for account type: %s",
                    callingUid,
                    accountType);
            throw new SecurityException(msg);
        }
        long identityToken = clearCallingIdentity();
        try {
            UserAccounts accounts = getUserAccounts(userId);
            new Session(accounts, response, accountType, expectActivityLaunch,
                    true /* stripAuthTokenFromResult */, null /* accountName */,
                    false /* authDetailsRequired */) {
                @Override
                public void run() throws RemoteException {
                    mAuthenticator.editProperties(this, mAccountType);
                }
                @Override
                protected String toDebugString(long now) {
                    return super.toDebugString(now) + ", editProperties"
                            + ", accountType " + accountType;
                }
            }.bind();
        } finally {
            restoreCallingIdentity(identityToken);
        }
    }

    private class GetAccountsByTypeAndFeatureSession extends Session {
        private final String[] mFeatures;
        private volatile Account[] mAccountsOfType = null;
        private volatile ArrayList<Account> mAccountsWithFeatures = null;
        private volatile int mCurrentAccount = 0;
        private final int mCallingUid;

        public GetAccountsByTypeAndFeatureSession(UserAccounts accounts,
                IAccountManagerResponse response, String type, String[] features, int callingUid) {
            super(accounts, response, type, false /* expectActivityLaunch */,
                    true /* stripAuthTokenFromResult */, null /* accountName */,
                    false /* authDetailsRequired */);
            mCallingUid = callingUid;
            mFeatures = features;
        }

        @Override
        public void run() throws RemoteException {
            synchronized (mAccounts.cacheLock) {
                mAccountsOfType = getAccountsFromCacheLocked(mAccounts, mAccountType, mCallingUid,
                        null);
            }
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
                if (Log.isLoggable(TAG, Log.VERBOSE)) {
                    Log.v(TAG, "checkAccount: aborting session since we are no longer"
                            + " connected to the authenticator, " + toDebugString());
                }
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
                    if (Log.isLoggable(TAG, Log.VERBOSE)) {
                        Log.v(TAG, "failure while notifying response", e);
                    }
                }
            }
        }


        @Override
        protected String toDebugString(long now) {
            return super.toDebugString(now) + ", getAccountsByTypeAndFeatures"
                    + ", " + (mFeatures != null ? TextUtils.join(",", mFeatures) : null);
        }
    }

    /**
     * Returns the accounts visible to the client within the context of a specific user
     * @hide
     */
    public Account[] getAccounts(int userId) {
        int callingUid = VBinder.getCallingUid();
        List<String> visibleAccountTypes = getTypesVisibleToCaller(callingUid, userId);
        if (visibleAccountTypes.isEmpty()) {
            return new Account[0];
        }
        long identityToken = clearCallingIdentity();
        try {
            UserAccounts accounts = getUserAccounts(userId);
            return getAccountsInternal(
                    accounts,
                    callingUid,
                    null,  // packageName
                    visibleAccountTypes);
        } finally {
            restoreCallingIdentity(identityToken);
        }
    }

    /**
     * Returns accounts for all running users.
     *
     * @hide
     */
    public AccountAndUser[] getRunningAccounts() {
        final int[] runningUserIds;
        try {
            runningUserIds = ActivityManagerNative.getDefault().getRunningUserIds();
        } catch (RemoteException e) {
            // Running in system_server; should never happen
            throw new RuntimeException(e);
        }
        return getAccounts(runningUserIds);
    }

    /** {@hide} */
    public AccountAndUser[] getAllAccounts() {
        final List<UserInfo> users = getUserManager().getUsers();
        final int[] userIds = new int[users.size()];
        for (int i = 0; i < userIds.length; i++) {
            userIds[i] = users.get(i).id;
        }
        return getAccounts(userIds);
    }

    private AccountAndUser[] getAccounts(int[] userIds) {
        final ArrayList<AccountAndUser> runningAccounts = Lists.newArrayList();
        for (int userId : userIds) {
            UserAccounts userAccounts = getUserAccounts(userId);
            if (userAccounts == null) continue;
            synchronized (userAccounts.cacheLock) {
                Account[] accounts = getAccountsFromCacheLocked(userAccounts, null,
                        VBinder.getCallingUid(), null);
                for (int a = 0; a < accounts.length; a++) {
                    runningAccounts.add(new AccountAndUser(accounts[a], userId));
                }
            }
        }

        AccountAndUser[] accountsArray = new AccountAndUser[runningAccounts.size()];
        return runningAccounts.toArray(accountsArray);
    }

    public Account[] getAccountsAsUser(String type, int userId) {
        return getAccountsAsUser(type, userId, null, -1);
    }

    private Account[] getAccountsAsUser(
            String type,
            int userId,
            String callingPackage,
            int packageUid) {
        int callingUid = VBinder.getCallingUid();
        // Only allow the system process to read accounts of other users
        if (userId != VUserHandle.getCallingUserId()
                && callingUid != Process.myUid()
                && mContext.checkCallingOrSelfPermission(
                    Manifest.permission.INTERACT_ACROSS_USERS_FULL)
                    != PackageManager.PERMISSION_GRANTED) {
            throw new SecurityException("User " + VUserHandle.getCallingUserId()
                    + " trying to get account for " + userId);
        }

        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, "getAccounts: accountType " + type
                    + ", caller's uid " + VBinder.getCallingUid()
                    + ", pid " + VBinder.getCallingPid());
        }
        // If the original calling app was using the framework account chooser activity, we'll
        // be passed in the original caller's uid here, which is what should be used for filtering.
        if (packageUid != -1 && VUserHandle.isSameApp(callingUid, Process.myUid())) {
            callingUid = packageUid;
        }

        List<String> visibleAccountTypes = getTypesVisibleToCaller(callingUid, userId);
        if (visibleAccountTypes.isEmpty()
                || (type != null && !visibleAccountTypes.contains(type))) {
            return new Account[0];
        } else if (visibleAccountTypes.contains(type)) {
            // Prune the list down to just the requested type.
            visibleAccountTypes = new ArrayList<>();
            visibleAccountTypes.add(type);
        } // else aggregate all the visible accounts (it won't matter if the
          // list is empty).

        long identityToken = clearCallingIdentity();
        try {
            UserAccounts accounts = getUserAccounts(userId);
            return getAccountsInternal(
                    accounts,
                    callingUid,
                    callingPackage,
                    visibleAccountTypes);
        } finally {
            restoreCallingIdentity(identityToken);
        }
    }

    private Account[] getAccountsInternal(
            UserAccounts userAccounts,
            int callingUid,
            String callingPackage,
            List<String> visibleAccountTypes) {
        synchronized (userAccounts.cacheLock) {
            ArrayList<Account> visibleAccounts = new ArrayList<>();
            for (String visibleType : visibleAccountTypes) {
                Account[] accountsForType = getAccountsFromCacheLocked(
                        userAccounts, visibleType, callingUid, callingPackage);
                if (accountsForType != null) {
                    visibleAccounts.addAll(Arrays.asList(accountsForType));
                }
            }
            Account[] result = new Account[visibleAccounts.size()];
            for (int i = 0; i < visibleAccounts.size(); i++) {
                result[i] = visibleAccounts.get(i);
            }
            return result;
        }
    }

    @Override
    public boolean addSharedAccountAsUser(Account account, int userId) {
        userId = handleIncomingUser(userId);
        UserAccounts accounts = getUserAccounts(userId);
        SQLiteDatabase db = accounts.openHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ACCOUNTS_NAME, account.name);
        values.put(ACCOUNTS_TYPE, account.type);
        db.delete(TABLE_SHARED_ACCOUNTS, ACCOUNTS_NAME + "=? AND " + ACCOUNTS_TYPE+ "=?",
                new String[] {account.name, account.type});
        long accountId = db.insert(TABLE_SHARED_ACCOUNTS, ACCOUNTS_NAME, values);
        if (accountId < 0) {
            Log.w(TAG, "insertAccountIntoDatabase: " + account
                    + ", skipping the DB insert failed");
            return false;
        }
        logRecord(db, DebugDbHelper.ACTION_ACCOUNT_ADD, TABLE_SHARED_ACCOUNTS, accountId, accounts);
        return true;
    }

    @Override
    public boolean renameSharedAccountAsUser(Account account, String newName, int userId) {
        userId = handleIncomingUser(userId);
        UserAccounts accounts = getUserAccounts(userId);
        SQLiteDatabase db = accounts.openHelper.getWritableDatabase();
        long sharedTableAccountId = getAccountIdFromSharedTable(db, account);
        final ContentValues values = new ContentValues();
        values.put(ACCOUNTS_NAME, newName);
        values.put(ACCOUNTS_PREVIOUS_NAME, account.name);
        int r = db.update(
                TABLE_SHARED_ACCOUNTS,
                values,
                ACCOUNTS_NAME + "=? AND " + ACCOUNTS_TYPE+ "=?",
                new String[] { account.name, account.type });
        if (r > 0) {
            int callingUid = getCallingUid();
            logRecord(db, DebugDbHelper.ACTION_ACCOUNT_RENAME, TABLE_SHARED_ACCOUNTS,
                    sharedTableAccountId, accounts, callingUid);
            // Recursively rename the account.
            renameAccountInternal(accounts, account, newName);
        }
        return r > 0;
    }

    @Override
    public boolean removeSharedAccountAsUser(Account account, int userId) {
        return removeSharedAccountAsUser(account, userId, getCallingUid());
    }

    private boolean removeSharedAccountAsUser(Account account, int userId, int callingUid) {
        userId = handleIncomingUser(userId);
        UserAccounts accounts = getUserAccounts(userId);
        SQLiteDatabase db = accounts.openHelper.getWritableDatabase();
        long sharedTableAccountId = getAccountIdFromSharedTable(db, account);
        int r = db.delete(TABLE_SHARED_ACCOUNTS, ACCOUNTS_NAME + "=? AND " + ACCOUNTS_TYPE+ "=?",
                new String[] {account.name, account.type});
        if (r > 0) {
            logRecord(db, DebugDbHelper.ACTION_ACCOUNT_REMOVE, TABLE_SHARED_ACCOUNTS,
                    sharedTableAccountId, accounts, callingUid);
            removeAccountInternal(accounts, account);
        }
        return r > 0;
    }

    @Override
    public Account[] getSharedAccountsAsUser(int userId) {
        userId = handleIncomingUser(userId);
        UserAccounts accounts = getUserAccounts(userId);
        ArrayList<Account> accountList = new ArrayList<Account>();
        Cursor cursor = null;
        try {
            cursor = accounts.openHelper.getReadableDatabase()
                    .query(TABLE_SHARED_ACCOUNTS, new String[]{ACCOUNTS_NAME, ACCOUNTS_TYPE},
                    null, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(ACCOUNTS_NAME);
                int typeIndex = cursor.getColumnIndex(ACCOUNTS_TYPE);
                do {
                    accountList.add(new Account(cursor.getString(nameIndex),
                            cursor.getString(typeIndex)));
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        Account[] accountArray = new Account[accountList.size()];
        accountList.toArray(accountArray);
        return accountArray;
    }

    public Account[] getAccounts(String type) {
        return getAccountsAsUser(type, VUserHandle.getCallingUserId());
    }

    public Account[] getAccountsForPackage(String packageName, int uid) {
        int callingUid = VBinder.getCallingUid();
        if (!VUserHandle.isSameApp(callingUid, Process.myUid())) {
            throw new SecurityException("getAccountsForPackage() called from unauthorized uid "
                    + callingUid + " with uid=" + uid);
        }
        return getAccountsAsUser(null, VUserHandle.getCallingUserId(), packageName, uid);
    }

    public Account[] getAccountsByTypeForPackage(String type, String packageName) {
        int packageUid = VPackageManagerService.getService().getPackageUid(
                    packageName, VUserHandle.getCallingUserId());
        return getAccountsAsUser(type, VUserHandle.getCallingUserId(), packageName, packageUid);
    }

    @Override
    public void getAccountsByFeatures(
            IAccountManagerResponse response,
            String type,
            String[] features) {
        int callingUid = VBinder.getCallingUid();
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, "getAccounts: accountType " + type
                    + ", response " + response
                    + ", features " + stringArrayToString(features)
                    + ", caller's uid " + callingUid
                    + ", pid " + VBinder.getCallingPid());
        }
        if (response == null) throw new IllegalArgumentException("response is null");
        if (type == null) throw new IllegalArgumentException("accountType is null");
        int userId = VUserHandle.getCallingUserId();

        List<String> visibleAccountTypes = getTypesVisibleToCaller(callingUid, userId);
        if (!visibleAccountTypes.contains(type)) {
            Bundle result = new Bundle();
            // Need to return just the accounts that are from matching signatures.
            result.putParcelableArray(AccountManager.KEY_ACCOUNTS, new Account[0]);
            try {
                response.onResult(result);
            } catch (RemoteException e) {
                Log.e(TAG, "Cannot respond to caller do to exception." , e);
            }
            return;
        }
        long identityToken = clearCallingIdentity();
        try {
            UserAccounts userAccounts = getUserAccounts(userId);
            if (features == null || features.length == 0) {
                Account[] accounts;
                synchronized (userAccounts.cacheLock) {
                    accounts = getAccountsFromCacheLocked(userAccounts, type, callingUid, null);
                }
                Bundle result = new Bundle();
                result.putParcelableArray(AccountManager.KEY_ACCOUNTS, accounts);
                onResult(response, result);
                return;
            }
            new GetAccountsByTypeAndFeatureSession(
                    userAccounts,
                    response,
                    type,
                    features,
                    callingUid).bind();
        } finally {
            restoreCallingIdentity(identityToken);
        }
    }

    private long getAccountIdFromSharedTable(SQLiteDatabase db, Account account) {
        Cursor cursor = db.query(TABLE_SHARED_ACCOUNTS, new String[]{ACCOUNTS_ID},
                "name=? AND type=?", new String[]{account.name, account.type}, null, null, null);
        try {
            if (cursor.moveToNext()) {
                return cursor.getLong(0);
            }
            return -1;
        } finally {
            cursor.close();
        }
    }

    private long getAccountIdLocked(SQLiteDatabase db, Account account) {
        Cursor cursor = db.query(TABLE_ACCOUNTS, new String[]{ACCOUNTS_ID},
                "name=? AND type=?", new String[]{account.name, account.type}, null, null, null);
        try {
            if (cursor.moveToNext()) {
                return cursor.getLong(0);
            }
            return -1;
        } finally {
            cursor.close();
        }
    }

    private long getExtrasIdLocked(SQLiteDatabase db, long accountId, String key) {
        Cursor cursor = db.query(TABLE_EXTRAS, new String[]{EXTRAS_ID},
                EXTRAS_ACCOUNTS_ID + "=" + accountId + " AND " + EXTRAS_KEY + "=?",
                new String[]{key}, null, null, null);
        try {
            if (cursor.moveToNext()) {
                return cursor.getLong(0);
            }
            return -1;
        } finally {
            cursor.close();
        }
    }

    private abstract class Session extends IAccountAuthenticatorResponse.Stub
            implements DeathRecipient, ServiceConnection {
        IAccountManagerResponse mResponse;
        final String mAccountType;
        final boolean mExpectActivityLaunch;
        final long mCreationTime;
        final String mAccountName;
        // Indicates if we need to add auth details(like last credential time)
        final boolean mAuthDetailsRequired;
        // If set, we need to update the last authenticated time. This is
        // currently
        // used on
        // successful confirming credentials.
        final boolean mUpdateLastAuthenticatedTime;

        public int mNumResults = 0;
        private int mNumRequestContinued = 0;
        private int mNumErrors = 0;

        IAccountAuthenticator mAuthenticator = null;

        private final boolean mStripAuthTokenFromResult;
        protected final UserAccounts mAccounts;

        public Session(UserAccounts accounts, IAccountManagerResponse response, String accountType,
                boolean expectActivityLaunch, boolean stripAuthTokenFromResult, String accountName,
                boolean authDetailsRequired) {
            this(accounts, response, accountType, expectActivityLaunch, stripAuthTokenFromResult,
                    accountName, authDetailsRequired, false /* updateLastAuthenticatedTime */);
        }

        public Session(UserAccounts accounts, IAccountManagerResponse response, String accountType,
                boolean expectActivityLaunch, boolean stripAuthTokenFromResult, String accountName,
                boolean authDetailsRequired, boolean updateLastAuthenticatedTime) {
            super();
            //if (response == null) throw new IllegalArgumentException("response is null");
            if (accountType == null) throw new IllegalArgumentException("accountType is null");
            mAccounts = accounts;
            mStripAuthTokenFromResult = stripAuthTokenFromResult;
            mResponse = response;
            mAccountType = accountType;
            mExpectActivityLaunch = expectActivityLaunch;
            mCreationTime = SystemClock.elapsedRealtime();
            mAccountName = accountName;
            mAuthDetailsRequired = authDetailsRequired;
            mUpdateLastAuthenticatedTime = updateLastAuthenticatedTime;

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
            cancelTimeout();
            unbind();
        }

        @Override
        public void binderDied() {
            mResponse = null;
            close();
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

        void bind() {
            if (Log.isLoggable(TAG, Log.VERBOSE)) {
                Log.v(TAG, "initiating bind to authenticator type " + mAccountType);
            }
            if (!bindToAuthenticator(mAccountType)) {
                Log.d(TAG, "bind attempt failed for " + toDebugString());
                onError(AccountManager.ERROR_CODE_REMOTE_EXCEPTION, "bind failure");
            }
        }

        private void unbind() {
            if (mAuthenticator != null) {
                mAuthenticator = null;
                mContext.unbindService(this);
            }
        }

        public void cancelTimeout() {
            mMessageHandler.removeMessages(MESSAGE_TIMED_OUT, this);
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
        public void onServiceDisconnected(ComponentName name) {
            mAuthenticator = null;
            IAccountManagerResponse response = getResponseAndClose();
            if (response != null) {
                try {
                    response.onError(AccountManager.ERROR_CODE_REMOTE_EXCEPTION,
                            "disconnected");
                } catch (RemoteException e) {
                    if (Log.isLoggable(TAG, Log.VERBOSE)) {
                        Log.v(TAG, "Session.onServiceDisconnected: "
                                + "caught RemoteException while responding", e);
                    }
                }
            }
        }

        public abstract void run() throws RemoteException;

        public void onTimedOut() {
            IAccountManagerResponse response = getResponseAndClose();
            if (response != null) {
                try {
                    response.onError(AccountManager.ERROR_CODE_REMOTE_EXCEPTION,
                            "timeout");
                } catch (RemoteException e) {
                    if (Log.isLoggable(TAG, Log.VERBOSE)) {
                        Log.v(TAG, "Session.onTimedOut: caught RemoteException while responding",
                                e);
                    }
                }
            }
        }

        @Override
        public void onResult(Bundle result) {
            mNumResults++;
            Intent intent = null;
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
                    boolean accountPresent = isAccountPresentForCaller(mAccountName, mAccountType);
                    if (needUpdate && accountPresent) {
                        updateLastAuthenticatedTime(new Account(mAccountName, mAccountType));
                    }
                    if (mAuthDetailsRequired) {
                        long lastAuthenticatedTime = -1;
                        if (accountPresent) {
                            lastAuthenticatedTime = DatabaseUtils.longForQuery(
                                    mAccounts.openHelper.getReadableDatabase(),
                                    "select " + ACCOUNTS_LAST_AUTHENTICATE_TIME_EPOCH_MILLIS
                                            + " from " +
                                            TABLE_ACCOUNTS + " WHERE " + ACCOUNTS_NAME + "=? AND "
                                            + ACCOUNTS_TYPE + "=?",
                                    new String[] {
                                            mAccountName, mAccountType
                                    });
                        }
                        result.putLong(AccountManager.KEY_LAST_AUTHENTICATED_TIME,
                                lastAuthenticatedTime);
                    }
                }
            }
            if (result != null
                    && (intent = result.getParcelable(AccountManager.KEY_INTENT)) != null) {
                /*
                 * The Authenticator API allows third party authenticators to
                 * supply arbitrary intents to other apps that they can run,
                 * this can be very bad when those apps are in the system like
                 * the System Settings.
                 */
                int authenticatorUid = VBinder.getCallingUid();
                long bid = Binder.clearCallingIdentity();
                try {
                    ResolveInfo resolveInfo = VPackageManagerService.getService().resolveIntent(intent, intent.resolveType(mContext), 0, mAccounts.userId);
                    int targetUid = resolveInfo.activityInfo.applicationInfo.uid;
                    //TODO: checkSig
                    if (false/*PackageManager.SIGNATURE_MATCH !=
                            pm.checkSignatures(authenticatorUid, targetUid)*/) {
                        throw new SecurityException(
                                "Activity to be started with KEY_INTENT must " +
                               "share Authenticator's signatures");
                    }
                } finally {
                    Binder.restoreCallingIdentity(bid);
                }
            }
            if (result != null
                    && !TextUtils.isEmpty(result.getString(AccountManager.KEY_AUTHTOKEN))) {
                String accountName = result.getString(AccountManager.KEY_ACCOUNT_NAME);
                String accountType = result.getString(AccountManager.KEY_ACCOUNT_TYPE);
                if (!TextUtils.isEmpty(accountName) && !TextUtils.isEmpty(accountType)) {
                    Account account = new Account(accountName, accountType);
                    cancelNotification(getSigninRequiredNotificationId(mAccounts, account),
                            new VUserHandle(mAccounts.userId));
                }
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
                        if (Log.isLoggable(TAG, Log.VERBOSE)) {
                            Log.v(TAG, getClass().getSimpleName()
                                    + " calling onError() on response " + response);
                        }
                        response.onError(AccountManager.ERROR_CODE_INVALID_RESPONSE,
                                "null bundle returned");
                    } else {
                        if (mStripAuthTokenFromResult) {
                            result.remove(AccountManager.KEY_AUTHTOKEN);
                        }
                        if (Log.isLoggable(TAG, Log.VERBOSE)) {
                            Log.v(TAG, getClass().getSimpleName()
                                    + " calling onResult() on response " + response);
                        }
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
                    if (Log.isLoggable(TAG, Log.VERBOSE)) {
                        Log.v(TAG, "failure while notifying response", e);
                    }
                }
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
                if (Log.isLoggable(TAG, Log.VERBOSE)) {
                    Log.v(TAG, getClass().getSimpleName()
                            + " calling onError() on response " + response);
                }
                try {
                    response.onError(errorCode, errorMessage);
                } catch (RemoteException e) {
                    if (Log.isLoggable(TAG, Log.VERBOSE)) {
                        Log.v(TAG, "Session.onError: caught RemoteException while responding", e);
                    }
                }
            } else {
                if (Log.isLoggable(TAG, Log.VERBOSE)) {
                    Log.v(TAG, "Session.onError: already closed");
                }
            }
        }

        /**
         * find the component name for the authenticator and initiate a bind
         * if no authenticator or the bind fails then return false, otherwise return true
         */
        private boolean bindToAuthenticator(String authenticatorType) {
            final AccountAuthenticatorCache.ServiceInfo<AuthenticatorDescription> authenticatorInfo;
            authenticatorInfo = mAuthenticatorCache.getServiceInfo(
                    AuthenticatorDescription.newKey(authenticatorType), mAccounts.userId);
            if (authenticatorInfo == null) {
                if (Log.isLoggable(TAG, Log.VERBOSE)) {
                    Log.v(TAG, "there is no authenticator for " + authenticatorType
                            + ", bailing out");
                }
                return false;
            }

            Intent intent = new Intent();
            intent.setAction(AccountManager.ACTION_AUTHENTICATOR_INTENT);
            intent.setComponent(authenticatorInfo.componentName);
            intent.putExtra(ExtraConstants.EXTRA_TARGET_USER, mAccounts.userId);
            if (Log.isLoggable(TAG, Log.VERBOSE)) {
                Log.v(TAG, "performing bindService to " + authenticatorInfo.componentName);
            }
            if (!mContext.bindService(intent,this, Context.BIND_AUTO_CREATE)) {
                if (Log.isLoggable(TAG, Log.VERBOSE)) {
                    Log.v(TAG, "bindService to " + authenticatorInfo.componentName + " failed");
                }
                return false;
            }
            return true;
        }
    }

    private class MessageHandler extends Handler {
        MessageHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_TIMED_OUT:
                    Session session = (Session)msg.obj;
                    session.onTimedOut();
                    break;

                case MESSAGE_COPY_SHARED_ACCOUNT:
                    copyAccountToUser(/*no response*/ null, (Account) msg.obj, msg.arg1, msg.arg2);
                    break;

                default:
                    throw new IllegalStateException("unhandled message: " + msg.what);
            }
        }
    }

    private static String getDatabaseName(int userId) {
        File systemDir = VEnvironment.getSystemSecureDirectory();
        File databaseFile = new File(VEnvironment.getUserSystemDirectory(userId), DATABASE_NAME);
        if (userId == 0) {
            // Migrate old file, if it exists, to the new location.
            // Make sure the new file doesn't already exist. A dummy file could have been
            // accidentally created in the old location, causing the new one to become corrupted
            // as well.
            File oldFile = new File(systemDir, DATABASE_NAME);
            if (oldFile.exists() && !databaseFile.exists()) {
                // Check for use directory; create if it doesn't exist, else renameTo will fail
                File userDir = VEnvironment.getUserSystemDirectory(userId);
                if (!userDir.exists()) {
                    if (!userDir.mkdirs()) {
                        throw new IllegalStateException("User dir cannot be created: " + userDir);
                    }
                }
                if (!oldFile.renameTo(databaseFile)) {
                    throw new IllegalStateException("User dir cannot be migrated: " + databaseFile);
                }
            }
        }
        return databaseFile.getPath();
    }

    private static class DebugDbHelper{
        private DebugDbHelper() {
        }

        private static String TABLE_DEBUG = "debug_table";

        // Columns for the table
        private static String ACTION_TYPE = "action_type";
        private static String TIMESTAMP = "time";
        private static String CALLER_UID = "caller_uid";
        private static String TABLE_NAME = "table_name";
        private static String KEY = "primary_key";

        // These actions correspond to the occurrence of real actions. Since
        // these are called by the authenticators, the uid associated will be
        // of the authenticator.
        private static String ACTION_SET_PASSWORD = "action_set_password";
        private static String ACTION_CLEAR_PASSWORD = "action_clear_password";
        private static String ACTION_ACCOUNT_ADD = "action_account_add";
        private static String ACTION_ACCOUNT_REMOVE = "action_account_remove";
        private static String ACTION_AUTHENTICATOR_REMOVE = "action_authenticator_remove";
        private static String ACTION_ACCOUNT_RENAME = "action_account_rename";

        // These actions don't necessarily correspond to any action on
        // accountDb taking place. As an example, there might be a request for
        // addingAccount, which might not lead to addition of account on grounds
        // of bad authentication. We will still be logging it to keep track of
        // who called.
        private static String ACTION_CALLED_ACCOUNT_ADD = "action_called_account_add";
        private static String ACTION_CALLED_ACCOUNT_REMOVE = "action_called_account_remove";

        private static SimpleDateFormat dateFromat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        private static void createDebugTable(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + TABLE_DEBUG + " ( "
                    + ACCOUNTS_ID + " INTEGER,"
                    + ACTION_TYPE + " TEXT NOT NULL, "
                    + TIMESTAMP + " DATETIME,"
                    + CALLER_UID + " INTEGER NOT NULL,"
                    + TABLE_NAME + " TEXT NOT NULL,"
                    + KEY + " INTEGER PRIMARY KEY)");
            db.execSQL("CREATE INDEX timestamp_index ON " + TABLE_DEBUG + " (" + TIMESTAMP + ")");
        }
    }

    private void logRecord(UserAccounts accounts, String action, String tableName) {
        SQLiteDatabase db = accounts.openHelper.getWritableDatabase();
        logRecord(db, action, tableName, -1, accounts);
    }

    private void logRecordWithUid(UserAccounts accounts, String action, String tableName, int uid) {
        SQLiteDatabase db = accounts.openHelper.getWritableDatabase();
        logRecord(db, action, tableName, -1, accounts, uid);
    }

    /*
     * This function receives an opened writable database.
     */
    private void logRecord(SQLiteDatabase db, String action, String tableName, long accountId,
            UserAccounts userAccount) {
        logRecord(db, action, tableName, accountId, userAccount, getCallingUid());
    }

    /*
     * This function receives an opened writable database.
     */
    private void logRecord(SQLiteDatabase db, String action, String tableName, long accountId,
            UserAccounts userAccount, int callingUid) {
        SQLiteStatement logStatement = userAccount.statementForLogging;
        logStatement.bindLong(1, accountId);
        logStatement.bindString(2, action);
        logStatement.bindString(3, DebugDbHelper.dateFromat.format(new Date()));
        logStatement.bindLong(4, callingUid);
        logStatement.bindString(5, tableName);
        logStatement.bindLong(6, userAccount.debugDbInsertionPoint);
        logStatement.execute();
        logStatement.clearBindings();
        userAccount.debugDbInsertionPoint = (userAccount.debugDbInsertionPoint + 1)
                % MAX_DEBUG_DB_SIZE;
    }

    /*
     * This should only be called once to compile the sql statement for logging
     * and to find the insertion point.
     */
    private void initializeDebugDbSizeAndCompileSqlStatementForLogging(SQLiteDatabase db,
            UserAccounts userAccount) {
        // Initialize the count if not done earlier.
        int size = (int) getDebugTableRowCount(db);
        if (size >= MAX_DEBUG_DB_SIZE) {
            // Table is full, and we need to find the point where to insert.
            userAccount.debugDbInsertionPoint = (int) getDebugTableInsertionPoint(db);
        } else {
            userAccount.debugDbInsertionPoint = size;
        }
        compileSqlStatementForLogging(db, userAccount);
    }

    private void compileSqlStatementForLogging(SQLiteDatabase db, UserAccounts userAccount) {
        String sql = "INSERT OR REPLACE INTO " + DebugDbHelper.TABLE_DEBUG
                + " VALUES (?,?,?,?,?,?)";
        userAccount.statementForLogging = db.compileStatement(sql);
    }

    private long getDebugTableRowCount(SQLiteDatabase db) {
        String queryCountDebugDbRows = "SELECT COUNT(*) FROM " + DebugDbHelper.TABLE_DEBUG;
        return DatabaseUtils.longForQuery(db, queryCountDebugDbRows, null);
    }

    /*
     * Finds the row key where the next insertion should take place. This should
     * be invoked only if the table has reached its full capacity.
     */
    private long getDebugTableInsertionPoint(SQLiteDatabase db) {
        // This query finds the smallest timestamp value (and if 2 records have
        // same timestamp, the choose the lower id).
        String queryCountDebugDbRows = new StringBuilder()
                .append("SELECT ").append(DebugDbHelper.KEY)
                .append(" FROM ").append(DebugDbHelper.TABLE_DEBUG)
                .append(" ORDER BY ")
                .append(DebugDbHelper.TIMESTAMP).append(",").append(DebugDbHelper.KEY)
                .append(" LIMIT 1")
                .toString();
        return DatabaseUtils.longForQuery(db, queryCountDebugDbRows, null);
    }

    static class DatabaseHelper extends SQLiteOpenHelper {

        public DatabaseHelper(Context context, int userId) {
            super(context, VAccountManagerService.getDatabaseName(userId), null, DATABASE_VERSION);
        }

        /**
         * This call needs to be made while the mCacheLock is held. The way to
         * ensure this is to get the lock any time a method is called ont the DatabaseHelper
         * @param db The database.
         */
        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + TABLE_ACCOUNTS + " ( "
                    + ACCOUNTS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + ACCOUNTS_NAME + " TEXT NOT NULL, "
                    + ACCOUNTS_TYPE + " TEXT NOT NULL, "
                    + ACCOUNTS_PASSWORD + " TEXT, "
                    + ACCOUNTS_PREVIOUS_NAME + " TEXT, "
                    + ACCOUNTS_LAST_AUTHENTICATE_TIME_EPOCH_MILLIS + " INTEGER DEFAULT 0, "
                    + "UNIQUE(" + ACCOUNTS_NAME + "," + ACCOUNTS_TYPE + "))");

            db.execSQL("CREATE TABLE " + TABLE_AUTHTOKENS + " (  "
                    + AUTHTOKENS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,  "
                    + AUTHTOKENS_ACCOUNTS_ID + " INTEGER NOT NULL, "
                    + AUTHTOKENS_TYPE + " TEXT NOT NULL,  "
                    + AUTHTOKENS_AUTHTOKEN + " TEXT,  "
                    + "UNIQUE (" + AUTHTOKENS_ACCOUNTS_ID + "," + AUTHTOKENS_TYPE + "))");

            createGrantsTable(db);

            db.execSQL("CREATE TABLE " + TABLE_EXTRAS + " ( "
                    + EXTRAS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + EXTRAS_ACCOUNTS_ID + " INTEGER, "
                    + EXTRAS_KEY + " TEXT NOT NULL, "
                    + EXTRAS_VALUE + " TEXT, "
                    + "UNIQUE(" + EXTRAS_ACCOUNTS_ID + "," + EXTRAS_KEY + "))");

            db.execSQL("CREATE TABLE " + TABLE_META + " ( "
                    + META_KEY + " TEXT PRIMARY KEY NOT NULL, "
                    + META_VALUE + " TEXT)");

            createSharedAccountsTable(db);

            createAccountsDeletionTrigger(db);

            DebugDbHelper.createDebugTable(db);
        }

        private void createSharedAccountsTable(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + TABLE_SHARED_ACCOUNTS + " ( "
                    + ACCOUNTS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + ACCOUNTS_NAME + " TEXT NOT NULL, "
                    + ACCOUNTS_TYPE + " TEXT NOT NULL, "
                    + "UNIQUE(" + ACCOUNTS_NAME + "," + ACCOUNTS_TYPE + "))");
        }

        private void addLastSuccessfullAuthenticatedTimeColumn(SQLiteDatabase db) {
            db.execSQL("ALTER TABLE " + TABLE_ACCOUNTS + " ADD COLUMN "
                    + ACCOUNTS_LAST_AUTHENTICATE_TIME_EPOCH_MILLIS + " DEFAULT 0");
        }

        private void addOldAccountNameColumn(SQLiteDatabase db) {
            db.execSQL("ALTER TABLE " + TABLE_ACCOUNTS + " ADD COLUMN " + ACCOUNTS_PREVIOUS_NAME);
        }

        private void addDebugTable(SQLiteDatabase db) {
            DebugDbHelper.createDebugTable(db);
        }

        private void createAccountsDeletionTrigger(SQLiteDatabase db) {
            db.execSQL(""
                    + " CREATE TRIGGER " + TABLE_ACCOUNTS + "Delete DELETE ON " + TABLE_ACCOUNTS
                    + " BEGIN"
                    + "   DELETE FROM " + TABLE_AUTHTOKENS
                    + "     WHERE " + AUTHTOKENS_ACCOUNTS_ID + "=OLD." + ACCOUNTS_ID + " ;"
                    + "   DELETE FROM " + TABLE_EXTRAS
                    + "     WHERE " + EXTRAS_ACCOUNTS_ID + "=OLD." + ACCOUNTS_ID + " ;"
                    + "   DELETE FROM " + TABLE_GRANTS
                    + "     WHERE " + GRANTS_ACCOUNTS_ID + "=OLD." + ACCOUNTS_ID + " ;"
                    + " END");
        }

        private void createGrantsTable(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + TABLE_GRANTS + " (  "
                    + GRANTS_ACCOUNTS_ID + " INTEGER NOT NULL, "
                    + GRANTS_AUTH_TOKEN_TYPE + " STRING NOT NULL,  "
                    + GRANTS_GRANTEE_UID + " INTEGER NOT NULL,  "
                    + "UNIQUE (" + GRANTS_ACCOUNTS_ID + "," + GRANTS_AUTH_TOKEN_TYPE
                    +   "," + GRANTS_GRANTEE_UID + "))");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.e(TAG, "upgrade from version " + oldVersion + " to version " + newVersion);

            if (oldVersion == 1) {
                // no longer need to do anything since the work is done
                // when upgrading from version 2
                oldVersion++;
            }

            if (oldVersion == 2) {
                createGrantsTable(db);
                db.execSQL("DROP TRIGGER " + TABLE_ACCOUNTS + "Delete");
                createAccountsDeletionTrigger(db);
                oldVersion++;
            }

            if (oldVersion == 3) {
                db.execSQL("UPDATE " + TABLE_ACCOUNTS + " SET " + ACCOUNTS_TYPE +
                        " = 'com.google' WHERE " + ACCOUNTS_TYPE + " == 'com.google.GAIA'");
                oldVersion++;
            }

            if (oldVersion == 4) {
                createSharedAccountsTable(db);
                oldVersion++;
            }

            if (oldVersion == 5) {
                addOldAccountNameColumn(db);
                oldVersion++;
            }

            if (oldVersion == 6) {
                addLastSuccessfullAuthenticatedTimeColumn(db);
                oldVersion++;
            }

            if (oldVersion == 7) {
                addDebugTable(db);
                oldVersion++;
            }

            if (oldVersion != newVersion) {
                Log.e(TAG, "failed to upgrade version " + oldVersion + " to version " + newVersion);
            }
        }

        @Override
        public void onOpen(SQLiteDatabase db) {
            if (Log.isLoggable(TAG, Log.VERBOSE)) Log.v(TAG, "opened database " + DATABASE_NAME);
        }
    }

    public IBinder onBind(@SuppressWarnings("unused") Intent intent) {
        return asBinder();
    }

    /**
     * Searches array of arguments for the specified string
     * @param args array of argument strings
     * @param value value to search for
     * @return true if the value is contained in the array
     */
    private static boolean scanArgs(String[] args, String value) {
        if (args != null) {
            for (String arg : args) {
                if (value.equals(arg)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    protected void dump(FileDescriptor fd, PrintWriter fout, String[] args) {
        if (mContext.checkCallingOrSelfPermission(Manifest.permission.DUMP)
                != PackageManager.PERMISSION_GRANTED) {
            fout.println("Permission Denial: can't dump AccountsManager from from pid="
                    + VBinder.getCallingPid() + ", uid=" + VBinder.getCallingUid()
                    + " without permission " + Manifest.permission.DUMP);
            return;
        }
        final boolean isCheckinRequest = scanArgs(args, "--checkin") || scanArgs(args, "-c");
        final IndentingPrintWriter ipw = new IndentingPrintWriter(fout, "  ");

        final List<UserInfo> users = getUserManager().getUsers();
        for (UserInfo user : users) {
            ipw.println("User " + user + ":");
            ipw.increaseIndent();
            dumpUser(getUserAccounts(user.id), fd, ipw, args, isCheckinRequest);
            ipw.println();
            ipw.decreaseIndent();
        }
    }

    private void dumpUser(UserAccounts userAccounts, FileDescriptor fd, PrintWriter fout,
            String[] args, boolean isCheckinRequest) {
        synchronized (userAccounts.cacheLock) {
            final SQLiteDatabase db = userAccounts.openHelper.getReadableDatabase();

            if (isCheckinRequest) {
                // This is a checkin request. *Only* upload the account types and the count of each.
                Cursor cursor = db.query(TABLE_ACCOUNTS, ACCOUNT_TYPE_COUNT_PROJECTION,
                        null, null, ACCOUNTS_TYPE, null, null);
                try {
                    while (cursor.moveToNext()) {
                        // print type,count
                        fout.println(cursor.getString(0) + "," + cursor.getString(1));
                    }
                } finally {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            } else {
                Account[] accounts = getAccountsFromCacheLocked(userAccounts, null /* type */,
                        Process.myUid(), null);
                fout.println("Accounts: " + accounts.length);
                for (Account account : accounts) {
                    fout.println("  " + account);
                }

                // Add debug information.
                fout.println();
                Cursor cursor = db.query(DebugDbHelper.TABLE_DEBUG, null,
                        null, null, null, null, DebugDbHelper.TIMESTAMP);
                fout.println("AccountId, Action_Type, timestamp, UID, TableName, Key");
                fout.println("Accounts History");
                try {
                    while (cursor.moveToNext()) {
                        // print type,count
                        fout.println(cursor.getString(0) + "," + cursor.getString(1) + "," +
                                cursor.getString(2) + "," + cursor.getString(3) + ","
                                + cursor.getString(4) + "," + cursor.getString(5));
                    }
                } finally {
                    cursor.close();
                }

                fout.println();
                synchronized (mSessions) {
                    final long now = SystemClock.elapsedRealtime();
                    fout.println("Active Sessions: " + mSessions.size());
                    for (Session session : mSessions.values()) {
                        fout.println("  " + session.toDebugString(now));
                    }
                }

                fout.println();
                mAuthenticatorCache.dump(fd, fout, args, userAccounts.userId);
            }
        }
    }

    private void doNotification(UserAccounts accounts, Account account, CharSequence message,
            Intent intent, int userId) {
        long identityToken = clearCallingIdentity();
        try {
            if (Log.isLoggable(TAG, Log.VERBOSE)) {
                Log.v(TAG, "doNotification: " + message + " intent:" + intent);
            }

            if (intent.getComponent() != null &&
                    GrantCredentialsPermissionActivity.class.getName().equals(
                            intent.getComponent().getClassName())) {
                createNoCredentialsPermissionNotification(account, intent, userId);
            } else {
//                final Integer notificationId = getSigninRequiredNotificationId(accounts, account);
//                intent.addCategory(String.valueOf(notificationId));
//                VUserHandle user = new VUserHandle(userId);
//                Context contextForUser = getContextForUser(user);
//                final String notificationTitleFormat =
//                        contextForUser.getText(R.string.notification_title).toString();
//                Notification n = new Notification.Builder(contextForUser)
//                        .setWhen(0)
//                        .setSmallIcon(android.R.drawable.stat_sys_warning)
//                        .setColor(contextForUser.getColor(
//                                com.android.internal.R.color.system_notification_accent_color))
//                        .setContentTitle(String.format(notificationTitleFormat, account.name))
//                        .setContentText(message)
//                        .setContentIntent(PendingIntent.getActivityAsUser(
//                                mContext, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT,
//                                null, user))
//                        .build();
//                installNotification(notificationId, n, user);
            }
        } finally {
            restoreCallingIdentity(identityToken);
        }
    }

    protected void installNotification(final int notificationId, final Notification n,
            VUserHandle user) {
        // TODO
    }

    protected void cancelNotification(int id, VUserHandle user) {
//        long identityToken = clearCallingIdentity();
//        try {
//            ((NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE))
//                .cancelAsUser(null, id, user);
//        } finally {
//            restoreCallingIdentity(identityToken);
//        }
    }

    private boolean isPermitted(int callingUid, String... permissions) {
        for (String perm : permissions) {
            if (mContext.checkCallingOrSelfPermission(perm) == PackageManager.PERMISSION_GRANTED) {
                if (Log.isLoggable(TAG, Log.VERBOSE)) {
                    Log.v(TAG, "  caller uid " + callingUid + " has " + perm);
                }
                return true;
            }
        }
        return false;
    }

    /** Succeeds if any of the specified permissions are granted. */
    private void checkBinderPermission(String... permissions) {
        final int callingUid = VBinder.getCallingUid();
        if (isPermitted(callingUid, permissions)) {
            String msg = String.format(
                    "caller uid %s  lacks any of %s",
                    callingUid,
                    TextUtils.join(",", permissions));
            Log.w(TAG, "  " + msg);
            throw new SecurityException(msg);
        }
    }

    private int handleIncomingUser(int userId) {
        try {
            return ActivityManagerNative.getDefault().handleIncomingUser(
                    VBinder.getCallingPid(), VBinder.getCallingUid(), userId, true, true, "", null);
        } catch (RemoteException re) {
            // Shouldn't happen, local.
        }
        return userId;
    }

    private boolean isPrivileged(int callingUid) {
        final PackageManager userPackageManager = VirtualCore.getPM();
        String[] packages = userPackageManager.getPackagesForUid(callingUid);
        for (String name : packages) {
            try {
                PackageInfo packageInfo = userPackageManager.getPackageInfo(name, 0 /* flags */);
                if (packageInfo != null
                        && (packageInfo.applicationInfo.privateFlags
                                & ApplicationInfo.PRIVATE_FLAG_PRIVILEGED) != 0) {
                    return true;
                }
            } catch (NameNotFoundException e) {
                return false;
            }
        }
        return false;
    }

    private boolean permissionIsGranted(
            Account account, String authTokenType, int callerUid, int userId) {
        final boolean isPrivileged = isPrivileged(callerUid);
        final boolean fromAuthenticator = account != null
                && isAccountManagedByCaller(account.type, callerUid, userId);
        final boolean hasExplicitGrants = account != null
                && hasExplicitlyGrantedPermission(account, authTokenType, callerUid);
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, "checkGrantsOrCallingUidAgainstAuthenticator: caller uid "
                    + callerUid + ", " + account
                    + ": is authenticator? " + fromAuthenticator
                    + ", has explicit permission? " + hasExplicitGrants);
        }
        return fromAuthenticator || hasExplicitGrants || isPrivileged;
    }

    private boolean isAccountVisibleToCaller(String accountType, int callingUid, int userId) {
        if (accountType == null) {
            return false;
        } else {
            return getTypesVisibleToCaller(callingUid, userId).contains(accountType);
        }
    }

    private boolean isAccountManagedByCaller(String accountType, int callingUid, int userId) {
        if (accountType == null) {
            return false;
        } else {
            return getTypesManagedByCaller(callingUid, userId).contains(accountType);
        }
    }

    private List<String> getTypesVisibleToCaller(int callingUid, int userId) {
        boolean isPermitted =
                isPermitted(callingUid, Manifest.permission.GET_ACCOUNTS,
                        Manifest.permission.GET_ACCOUNTS_PRIVILEGED);
        Log.i(TAG, String.format("getTypesVisibleToCaller: isPermitted? %s", isPermitted));
        return getTypesForCaller(callingUid, userId, isPermitted);
    }

    private List<String> getTypesManagedByCaller(int callingUid, int userId) {
        return getTypesForCaller(callingUid, userId, false);
    }

    private List<String> getTypesForCaller(
            int callingUid, int userId, boolean isOtherwisePermitted) {
        List<String> managedAccountTypes = new ArrayList<>();
        long identityToken = Binder.clearCallingIdentity();
        Collection<RegisteredServicesCache.ServiceInfo<AuthenticatorDescription>> serviceInfos;
        try {
            serviceInfos = mAuthenticatorCache.getAllServices(userId);
        } finally {
            Binder.restoreCallingIdentity(identityToken);
        }
        for (RegisteredServicesCache.ServiceInfo<AuthenticatorDescription> serviceInfo :
                serviceInfos) {
            // TODO: checkSig
            final int sigChk = PackageManager.SIGNATURE_MATCH;//mPackageManager.checkSignatures(serviceInfo.uid, callingUid);
            if (isOtherwisePermitted || sigChk == PackageManager.SIGNATURE_MATCH) {
                managedAccountTypes.add(serviceInfo.type.type);
            }
        }
        return managedAccountTypes;
    }

    private boolean isAccountPresentForCaller(String accountName, String accountType) {
        if (getUserAccountsForCaller().accountCache.containsKey(accountType)) {
            for (Account account : getUserAccountsForCaller().accountCache.get(accountType)) {
                if (account.name.equals(accountName)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean hasExplicitlyGrantedPermission(Account account, String authTokenType,
            int callerUid) {
        if (callerUid == Process.SYSTEM_UID) {
            return true;
        }
        UserAccounts accounts = getUserAccountsForCaller();
        synchronized (accounts.cacheLock) {
            final SQLiteDatabase db = accounts.openHelper.getReadableDatabase();
            String[] args = { String.valueOf(callerUid), authTokenType,
                    account.name, account.type};
            final boolean permissionGranted =
                    DatabaseUtils.longForQuery(db, COUNT_OF_MATCHING_GRANTS, args) != 0;
            if (!permissionGranted && ActivityManager.isRunningInTestHarness()) {
                // TODO: Skip this check when running automated tests. Replace this
                // with a more general solution.
                Log.d(TAG, "no credentials permission for usage of " + account + ", "
                        + authTokenType + " by uid " + callerUid
                        + " but ignoring since device is in test harness.");
                return true;
            }
            return permissionGranted;
        }
    }

    private boolean isSystemUid(int callingUid) {
        String[] packages = null;
        long ident = Binder.clearCallingIdentity();
        try {
            packages = VPackageManagerService.getService().getPackagesForUid(callingUid);
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
        if (packages != null) {
            for (String name : packages) {
                PackageInfo packageInfo = VPackageManagerService.getService().getPackageInfo(name, 0 /* flags */, VUserHandle.getUserId(callingUid));
                if (packageInfo != null
                        && (packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM)
                        != 0) {
                    return true;
                }
            }
        } else {
            Log.w(TAG, "No known packages with uid " + callingUid);
        }
        return false;
    }

    /** Succeeds if any of the specified permissions are granted. */
    private void checkReadAccountsPermitted(
            int callingUid,
            String accountType,
            int userId) {
        if (!isAccountVisibleToCaller(accountType, callingUid, userId)) {
            String msg = String.format(
                    "caller uid %s cannot access %s accounts",
                    callingUid,
                    accountType);
            Log.w(TAG, "  " + msg);
            throw new SecurityException(msg);
        }
    }

    private boolean canUserModifyAccounts(int userId) {
        // TODO: Check permission
        return true;
    }

    private boolean canUserModifyAccountsForType(int userId, String accountType) {
        DevicePolicyManager dpm = (DevicePolicyManager) mContext
                .getSystemService(Context.DEVICE_POLICY_SERVICE);
        String[] typesArray = dpm.getAccountTypesWithManagementDisabledAsUser(userId);
        if (typesArray == null) {
            return true;
        }
        for (String forbiddenType : typesArray) {
            if (forbiddenType.equals(accountType)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void updateAppPermission(Account account, String authTokenType, int uid, boolean value)
            throws RemoteException {
        final int callingUid = getCallingUid();

        if (callingUid != Process.SYSTEM_UID) {
            throw new SecurityException();
        }

        if (value) {
            grantAppPermission(account, authTokenType, uid);
        } else {
            revokeAppPermission(account, authTokenType, uid);
        }
    }

    /**
     * Allow callers with the given uid permission to get credentials for account/authTokenType.
     * <p>
     * Although this is public it can only be accessed via the VAccountManagerService object
     * which is in the system. This means we don't need to protect it with permissions.
     * @hide
     */
    private void grantAppPermission(Account account, String authTokenType, int uid) {
        if (account == null || authTokenType == null) {
            Log.e(TAG, "grantAppPermission: called with invalid arguments", new Exception());
            return;
        }
        UserAccounts accounts = getUserAccounts(VUserHandle.getUserId(uid));
        synchronized (accounts.cacheLock) {
            final SQLiteDatabase db = accounts.openHelper.getWritableDatabase();
            db.beginTransaction();
            try {
                long accountId = getAccountIdLocked(db, account);
                if (accountId >= 0) {
                    ContentValues values = new ContentValues();
                    values.put(GRANTS_ACCOUNTS_ID, accountId);
                    values.put(GRANTS_AUTH_TOKEN_TYPE, authTokenType);
                    values.put(GRANTS_GRANTEE_UID, uid);
                    db.insert(TABLE_GRANTS, GRANTS_ACCOUNTS_ID, values);
                    db.setTransactionSuccessful();
                }
            } finally {
                db.endTransaction();
            }
            cancelNotification(getCredentialPermissionNotificationId(account, authTokenType, uid),
                    new VUserHandle(accounts.userId));
        }
    }

    /**
     * Don't allow callers with the given uid permission to get credentials for
     * account/authTokenType.
     * <p>
     * Although this is public it can only be accessed via the VAccountManagerService object
     * which is in the system. This means we don't need to protect it with permissions.
     * @hide
     */
    private void revokeAppPermission(Account account, String authTokenType, int uid) {
        if (account == null || authTokenType == null) {
            Log.e(TAG, "revokeAppPermission: called with invalid arguments", new Exception());
            return;
        }
        UserAccounts accounts = getUserAccounts(VUserHandle.getUserId(uid));
        synchronized (accounts.cacheLock) {
            final SQLiteDatabase db = accounts.openHelper.getWritableDatabase();
            db.beginTransaction();
            try {
                long accountId = getAccountIdLocked(db, account);
                if (accountId >= 0) {
                    db.delete(TABLE_GRANTS,
                            GRANTS_ACCOUNTS_ID + "=? AND " + GRANTS_AUTH_TOKEN_TYPE + "=? AND "
                                    + GRANTS_GRANTEE_UID + "=?",
                            new String[]{String.valueOf(accountId), authTokenType,
                                    String.valueOf(uid)});
                    db.setTransactionSuccessful();
                }
            } finally {
                db.endTransaction();
            }
            cancelNotification(getCredentialPermissionNotificationId(account, authTokenType, uid),
                    new VUserHandle(accounts.userId));
        }
    }

    static final private String stringArrayToString(String[] value) {
        return value != null ? ("[" + TextUtils.join(",", value) + "]") : null;
    }

    private void removeAccountFromCacheLocked(UserAccounts accounts, Account account) {
        final Account[] oldAccountsForType = accounts.accountCache.get(account.type);
        if (oldAccountsForType != null) {
            ArrayList<Account> newAccountsList = new ArrayList<Account>();
            for (Account curAccount : oldAccountsForType) {
                if (!curAccount.equals(account)) {
                    newAccountsList.add(curAccount);
                }
            }
            if (newAccountsList.isEmpty()) {
                accounts.accountCache.remove(account.type);
            } else {
                Account[] newAccountsForType = new Account[newAccountsList.size()];
                newAccountsForType = newAccountsList.toArray(newAccountsForType);
                accounts.accountCache.put(account.type, newAccountsForType);
            }
        }
        accounts.userDataCache.remove(account);
        accounts.authTokenCache.remove(account);
        accounts.previousNameCache.remove(account);
    }

    /**
     * This assumes that the caller has already checked that the account is not already present.
     */
    private void insertAccountIntoCacheLocked(UserAccounts accounts, Account account) {
        Account[] accountsForType = accounts.accountCache.get(account.type);
        int oldLength = (accountsForType != null) ? accountsForType.length : 0;
        Account[] newAccountsForType = new Account[oldLength + 1];
        if (accountsForType != null) {
            System.arraycopy(accountsForType, 0, newAccountsForType, 0, oldLength);
        }
        newAccountsForType[oldLength] = account;
        accounts.accountCache.put(account.type, newAccountsForType);
    }

    private Account[] filterSharedAccounts(UserAccounts userAccounts, Account[] unfiltered,
            int callingUid, String callingPackage) {
        if (getUserManager() == null || userAccounts == null || userAccounts.userId < 0
                || callingUid == Process.myUid()) {
            return unfiltered;
        }
        UserInfo user = mUserManager.getUserInfo(userAccounts.userId);
        if (user != null && user.isRestricted()) {
            String[] packages = VPackageManagerService.getService().getPackagesForUid(callingUid);
            // If any of the packages is a white listed package, return the full set,
            // otherwise return non-shared accounts only.
            // This might be a temporary way to specify a whitelist
            String whiteList = mContext.getResources().getString(
                    com.android.internal.R.string.config_appsAuthorizedForSharedAccounts);
            for (String packageName : packages) {
                if (whiteList.contains(";" + packageName + ";")) {
                    return unfiltered;
                }
            }
            ArrayList<Account> allowed = new ArrayList<Account>();
            Account[] sharedAccounts = getSharedAccountsAsUser(userAccounts.userId);
            if (sharedAccounts == null || sharedAccounts.length == 0) return unfiltered;
            String requiredAccountType = "";
            // If there's an explicit callingPackage specified, check if that package
            // opted in to see restricted accounts.
            if (callingPackage != null) {
                PackageInfo pi = VPackageManagerService.getService().getPackageInfo(callingPackage, 0, user.id);
                if (pi != null && pi.restrictedAccountType != null) {
                    requiredAccountType = pi.restrictedAccountType;
                }
            } else {
                // Otherwise check if the callingUid has a package that has opted in
                for (String packageName : packages) {
                    PackageInfo pi = VPackageManagerService.getService().getPackageInfo(packageName, 0, user.id);
                    if (pi != null && pi.restrictedAccountType != null) {
                        requiredAccountType = pi.restrictedAccountType;
                        break;
                    }
                }
            }

            for (Account account : unfiltered) {
                if (account.type.equals(requiredAccountType)) {
                    allowed.add(account);
                } else {
                    boolean found = false;
                    for (Account shared : sharedAccounts) {
                        if (shared.equals(account)) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        allowed.add(account);
                    }
                }
            }
            Account[] filtered = new Account[allowed.size()];
            allowed.toArray(filtered);
            return filtered;
        } else {
            return unfiltered;
        }
    }

    /*
     * packageName can be null. If not null, it should be used to filter out restricted accounts
     * that the package is not allowed to access.
     */
    protected Account[] getAccountsFromCacheLocked(UserAccounts userAccounts, String accountType,
            int callingUid, String callingPackage) {
        if (accountType != null) {
            final Account[] accounts = userAccounts.accountCache.get(accountType);
            if (accounts == null) {
                return EMPTY_ACCOUNT_ARRAY;
            } else {
                return filterSharedAccounts(userAccounts, Arrays.copyOf(accounts, accounts.length),
                        callingUid, callingPackage);
            }
        } else {
            int totalLength = 0;
            for (Account[] accounts : userAccounts.accountCache.values()) {
                totalLength += accounts.length;
            }
            if (totalLength == 0) {
                return EMPTY_ACCOUNT_ARRAY;
            }
            Account[] accounts = new Account[totalLength];
            totalLength = 0;
            for (Account[] accountsOfType : userAccounts.accountCache.values()) {
                System.arraycopy(accountsOfType, 0, accounts, totalLength,
                        accountsOfType.length);
                totalLength += accountsOfType.length;
            }
            return filterSharedAccounts(userAccounts, accounts, callingUid, callingPackage);
        }
    }

    protected void writeUserDataIntoCacheLocked(UserAccounts accounts, final SQLiteDatabase db,
            Account account, String key, String value) {
        HashMap<String, String> userDataForAccount = accounts.userDataCache.get(account);
        if (userDataForAccount == null) {
            userDataForAccount = readUserDataForAccountFromDatabaseLocked(db, account);
            accounts.userDataCache.put(account, userDataForAccount);
        }
        if (value == null) {
            userDataForAccount.remove(key);
        } else {
            userDataForAccount.put(key, value);
        }
    }

    protected String readCachedTokenInternal(
            UserAccounts accounts,
            Account account,
            String tokenType,
            String callingPackage,
            byte[] pkgSigDigest) {
        synchronized (accounts.cacheLock) {
            return accounts.accountTokenCaches.get(
                    account, tokenType, callingPackage, pkgSigDigest);
        }
    }

    protected void writeAuthTokenIntoCacheLocked(UserAccounts accounts, final SQLiteDatabase db,
            Account account, String key, String value) {
        HashMap<String, String> authTokensForAccount = accounts.authTokenCache.get(account);
        if (authTokensForAccount == null) {
            authTokensForAccount = readAuthTokensForAccountFromDatabaseLocked(db, account);
            accounts.authTokenCache.put(account, authTokensForAccount);
        }
        if (value == null) {
            authTokensForAccount.remove(key);
        } else {
            authTokensForAccount.put(key, value);
        }
    }

    protected String readAuthTokenInternal(UserAccounts accounts, Account account,
            String authTokenType) {
        synchronized (accounts.cacheLock) {
            HashMap<String, String> authTokensForAccount = accounts.authTokenCache.get(account);
            if (authTokensForAccount == null) {
                // need to populate the cache for this account
                final SQLiteDatabase db = accounts.openHelper.getReadableDatabase();
                authTokensForAccount = readAuthTokensForAccountFromDatabaseLocked(db, account);
                accounts.authTokenCache.put(account, authTokensForAccount);
            }
            return authTokensForAccount.get(authTokenType);
        }
    }

    protected String readUserDataInternal(UserAccounts accounts, Account account, String key) {
        synchronized (accounts.cacheLock) {
            HashMap<String, String> userDataForAccount = accounts.userDataCache.get(account);
            if (userDataForAccount == null) {
                // need to populate the cache for this account
                final SQLiteDatabase db = accounts.openHelper.getReadableDatabase();
                userDataForAccount = readUserDataForAccountFromDatabaseLocked(db, account);
                accounts.userDataCache.put(account, userDataForAccount);
            }
            return userDataForAccount.get(key);
        }
    }

    protected HashMap<String, String> readUserDataForAccountFromDatabaseLocked(
            final SQLiteDatabase db, Account account) {
        HashMap<String, String> userDataForAccount = new HashMap<String, String>();
        Cursor cursor = db.query(TABLE_EXTRAS,
                COLUMNS_EXTRAS_KEY_AND_VALUE,
                SELECTION_USERDATA_BY_ACCOUNT,
                new String[]{account.name, account.type},
                null, null, null);
        try {
            while (cursor.moveToNext()) {
                final String tmpkey = cursor.getString(0);
                final String value = cursor.getString(1);
                userDataForAccount.put(tmpkey, value);
            }
        } finally {
            cursor.close();
        }
        return userDataForAccount;
    }

    protected HashMap<String, String> readAuthTokensForAccountFromDatabaseLocked(
            final SQLiteDatabase db, Account account) {
        HashMap<String, String> authTokensForAccount = new HashMap<String, String>();
        Cursor cursor = db.query(TABLE_AUTHTOKENS,
                COLUMNS_AUTHTOKENS_TYPE_AND_AUTHTOKEN,
                SELECTION_AUTHTOKENS_BY_ACCOUNT,
                new String[]{account.name, account.type},
                null, null, null);
        try {
            while (cursor.moveToNext()) {
                final String type = cursor.getString(0);
                final String authToken = cursor.getString(1);
                authTokensForAccount.put(type, authToken);
            }
        } finally {
            cursor.close();
        }
        return authTokensForAccount;
    }

}
