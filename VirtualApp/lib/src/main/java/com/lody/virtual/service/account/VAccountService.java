package com.lody.virtual.service.account;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorDescription;
import android.accounts.GrantCredentialsPermissionActivity;
import android.accounts.IAccountAuthenticator;
import android.accounts.IAccountAuthenticatorResponse;
import android.accounts.IAccountManagerResponse;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.RegisteredServicesCache;
import android.content.pm.ResolveInfo;
import android.content.pm.Signature;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
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

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.helper.compat.AccountManagerCompat;
import com.lody.virtual.helper.utils.XLog;
import com.lody.virtual.service.IAccountManager;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Lody
 *
 * Implemention of Account & Sync System.
 *
 */

public class VAccountService extends IAccountManager.Stub {

	private static final String TAG = "VAccountManagerService";

	private static final AtomicReference<VAccountService> sThis = new AtomicReference<VAccountService>();

	private static final Account[] EMPTY_ACCOUNT_ARRAY = new Account[]{};

	// Messages that can be sent on mHandler
	private static final int MESSAGE_TIMED_OUT = 3;

	private static final String DATABASE_NAME = "accounts.db";
	private static final int DATABASE_VERSION = 8;
	/**
	 * Table name
	 */
	private static final String TABLE_ACCOUNTS = "accounts";
	/**
	 * [id] field
	 */
	private static final String ACCOUNTS_ID = "_id";
	/**
	 * [name] field
	 */
	private static final String ACCOUNTS_NAME = "name";
	/**
	 * [type] field
	 */
	private static final String ACCOUNTS_TYPE = "type";
	/**
	 * [count:type] method
	 */
	private static final String ACCOUNTS_TYPE_COUNT = "count(type)";
	/**
	 * [password] field
	 */
	private static final String ACCOUNTS_PASSWORD = "password";
	/**
	 * [previous_name] field
	 */
	private static final String ACCOUNTS_PREVIOUS_NAME = "previous_name";
	/**
	 * [last_password_entry_time_millis_epoch] field
	 */
	private static final String ACCOUNTS_LAST_AUTHENTICATE_TIME_EPOCH_MILLIS = "last_password_entry_time_millis_epoch";
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
	private static final String[] ACCOUNT_TYPE_COUNT_PROJECTION = new String[]{ACCOUNTS_TYPE, ACCOUNTS_TYPE_COUNT};
	private static final String COUNT_OF_MATCHING_GRANTS = "" + "SELECT COUNT(*) FROM " + TABLE_GRANTS + ", "
			+ TABLE_ACCOUNTS + " WHERE " + GRANTS_ACCOUNTS_ID + "=" + ACCOUNTS_ID + " AND " + GRANTS_GRANTEE_UID + "=?"
			+ " AND " + GRANTS_AUTH_TOKEN_TYPE + "=?" + " AND " + ACCOUNTS_NAME + "=?" + " AND " + ACCOUNTS_TYPE + "=?";
	private static final String SELECTION_AUTHTOKENS_BY_ACCOUNT = AUTHTOKENS_ACCOUNTS_ID
			+ "=(select _id FROM accounts WHERE name=? AND type=?)";
	private static final String[] COLUMNS_AUTHTOKENS_TYPE_AND_AUTHTOKEN = {AUTHTOKENS_TYPE, AUTHTOKENS_AUTHTOKEN};
	private static final String SELECTION_USERDATA_BY_ACCOUNT = EXTRAS_ACCOUNTS_ID
			+ "=(select _id FROM accounts WHERE name=? AND type=?)";
	private static final String[] COLUMNS_EXTRAS_KEY_AND_VALUE = {EXTRAS_KEY, EXTRAS_VALUE};
	private static final Intent ACCOUNTS_CHANGED_INTENT;

	static {
		ACCOUNTS_CHANGED_INTENT = new Intent(AccountManager.LOGIN_ACCOUNTS_CHANGED_ACTION);
		ACCOUNTS_CHANGED_INTENT.setFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY_BEFORE_BOOT);
	}

	private final LinkedHashMap<String, Session> mSessions = new LinkedHashMap<String, Session>();
	private final AtomicInteger mNotificationIds = new AtomicInteger(1);
	private final IAccountAuthenticatorCache mAuthenticatorCache;
	private final MessageHandler mMessageHandler;
	private Context mContext;
	private UserAccounts userAccounts;
	private PackageManager mPackageManager;

	public VAccountService(Context context) {
		mContext = context;
		mPackageManager = mContext.getPackageManager();
		mMessageHandler = new MessageHandler(Looper.getMainLooper());
		userAccounts = new UserAccounts(mContext);
		mAuthenticatorCache = new AccountAuthenticatorCache(context);
	}

	private static String getDatabaseName(Context context) {
		File dir = context.getDir("accounts", Context.MODE_PRIVATE);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		return new File(dir, DATABASE_NAME).getPath();
	}

	public static VAccountService getService() {
		return sThis.get();
	}

	public static void systemReady(Context context) {
		VAccountService accountManager = new VAccountService(context);
		sThis.set(accountManager);
	}

	static private String stringArrayToString(String[] value) {
		return value != null ? ("[" + TextUtils.join(",", value) + "]") : null;
	}

	public UserAccounts getUserAccountsForCaller() {
		return userAccounts;
	}

	@Override
	public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
		try {
			return super.onTransact(code, data, reply, flags);
		} catch (RuntimeException e) {
			// The account manager only throws security exceptions, so let's
			// log all others.
			if (!(e instanceof SecurityException)) {
				XLog.w(TAG, "Account Manager Crash", e);
			}
			throw e;
		}
	}

	@Override
	public String getPassword(Account account) throws RemoteException {
		XLog.v(TAG, "getPassword: " + account + ", caller's uid " + Binder.getCallingUid() + ", pid "
				+ Binder.getCallingPid());
		if (account == null)
			throw new IllegalArgumentException("account is null");

		return readPasswordInternal(userAccounts, account);
	}

	private String readPasswordInternal(UserAccounts accounts, Account account) {
		if (account == null) {
			return null;
		}
		synchronized (accounts.cacheLock) {
			final SQLiteDatabase db = accounts.openHelper.getReadableDatabase();
			Cursor cursor = db.query(TABLE_ACCOUNTS, new String[]{ACCOUNTS_PASSWORD},
					ACCOUNTS_NAME + "=? AND " + ACCOUNTS_TYPE + "=?", new String[]{account.name, account.type}, null,
					null, null);
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
	public String getUserData(Account account, String key) throws RemoteException {
		XLog.v(TAG, "getUserData( account: %s, key: %s, callerUid: %s, pid: %s", account, key, Binder.getCallingUid(),
				Binder.getCallingPid());
		if (account == null)
			throw new IllegalArgumentException("account is null");
		if (key == null)
			throw new IllegalArgumentException("key is null");

		return readUserDataInternal(userAccounts, account, key);
	}

	private boolean updateLastAuthenticatedTime(Account account) {
		final UserAccounts accounts = getUserAccountsForCaller();
		synchronized (accounts.cacheLock) {
			final ContentValues values = new ContentValues();
			values.put(ACCOUNTS_LAST_AUTHENTICATE_TIME_EPOCH_MILLIS, System.currentTimeMillis());
			final SQLiteDatabase db = accounts.openHelper.getWritableDatabase();
			int i = db.update(TABLE_ACCOUNTS, values, ACCOUNTS_NAME + "=? AND " + ACCOUNTS_TYPE + "=?",
					new String[]{account.name, account.type});
			if (i > 0) {
				return true;
			}
		}
		return false;
	}

	protected String readAuthTokenInternal(UserAccounts accounts, Account account, String authTokenType) {
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

	protected HashMap<String, String> readAuthTokensForAccountFromDatabaseLocked(final SQLiteDatabase db,
			Account account) {
		HashMap<String, String> authTokensForAccount = new HashMap<String, String>();
		Cursor cursor = db.query(TABLE_AUTHTOKENS, COLUMNS_AUTHTOKENS_TYPE_AND_AUTHTOKEN,
				SELECTION_AUTHTOKENS_BY_ACCOUNT, new String[]{account.name, account.type}, null, null, null);
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

	protected HashMap<String, String> readUserDataForAccountFromDatabaseLocked(final SQLiteDatabase db,
			Account account) {
		HashMap<String, String> userDataForAccount = new HashMap<String, String>();
		Cursor cursor = db.query(TABLE_EXTRAS, COLUMNS_EXTRAS_KEY_AND_VALUE, SELECTION_USERDATA_BY_ACCOUNT,
				new String[]{account.name, account.type}, null, null, null);
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

	@Override
	public AuthenticatorDescription[] getAuthenticatorTypes(int userId) throws RemoteException {
		XLog.v(TAG, "getAuthenticatorTypes: " + "for user id " + userId + "caller's uid " + Binder.getCallingUid()
				+ ", pid " + Binder.getCallingPid());

		return getAuthenticatorTypesInternal(userId);
	}

	/**
	 * Should only be called inside of a clearCallingIdentity block.
	 */
	private AuthenticatorDescription[] getAuthenticatorTypesInternal(int userId) {
		Collection<AccountAuthenticatorCache.ServiceInfo<AuthenticatorDescription>> authenticatorCollection = mAuthenticatorCache
				.getAllServices(userId);
		AuthenticatorDescription[] types = new AuthenticatorDescription[authenticatorCollection.size()];
		int i = 0;
		for (AccountAuthenticatorCache.ServiceInfo<AuthenticatorDescription> authenticator : authenticatorCollection) {
			types[i] = authenticator.type;
			i++;
		}
		return types;
	}

	@Override
	public Account[] getAccounts(String accountType) throws RemoteException {
		return getAccountsAsUser(accountType, userAccounts.userId, null);
	}

	private Account[] getAccountsAsUser(String type, int userId, String callingPackage) {

		if (Log.isLoggable(TAG, Log.VERBOSE)) {
			Log.v(TAG, "getAccounts: accountType " + type + ", caller's uid " + Binder.getCallingUid() + ", pid "
					+ Binder.getCallingPid());
		}

		List<String> visibleAccountTypes = getTypesVisibleToCaller(userId);
		if (visibleAccountTypes.isEmpty() || (type != null && !visibleAccountTypes.contains(type))) {
			return new Account[0];
		} else if (visibleAccountTypes.contains(type)) {
			// Prune the list down to just the requested type.
			visibleAccountTypes = new ArrayList<>();
			visibleAccountTypes.add(type);
		} // else aggregate all the visible accounts (it won't matter if the
			// list is empty).

		UserAccounts accounts = userAccounts;
		return getAccountsInternal(accounts, callingPackage, visibleAccountTypes);
	}

	private Account[] getAccountsInternal(UserAccounts userAccounts, String callingPackage,
			List<String> visibleAccountTypes) {
		synchronized (userAccounts.cacheLock) {
			ArrayList<Account> visibleAccounts = new ArrayList<>();
			for (String visibleType : visibleAccountTypes) {
				Account[] accountsForType = getAccountsFromCacheLocked(userAccounts, visibleType, callingPackage);
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

	/*
	 * packageName can be null. If not null, it should be used to filter out
	 * restricted accounts that the package is not allowed to access.
	 */
	protected Account[] getAccountsFromCacheLocked(UserAccounts userAccounts, String accountType,
			String callingPackage) {
		if (accountType != null) {
			final Account[] accounts = userAccounts.accountCache.get(accountType);
			if (accounts == null) {
				return EMPTY_ACCOUNT_ARRAY;
			} else {
				return filterSharedAccounts(userAccounts, Arrays.copyOf(accounts, accounts.length), callingPackage);
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
				System.arraycopy(accountsOfType, 0, accounts, totalLength, accountsOfType.length);
				totalLength += accountsOfType.length;
			}
			return filterSharedAccounts(userAccounts, accounts, callingPackage);
		}
	}

	private Account[] filterSharedAccounts(UserAccounts userAccounts, Account[] unfiltered, String callingPackage) {
		return unfiltered;
	}

	private List<String> getTypesVisibleToCaller(int userId) {
		boolean isPermitted = true;
		Log.i(TAG, String.format("getTypesVisibleToCaller: isPermitted? %s", isPermitted));
		return getTypesForCaller(userId);
	}

	private List<String> getTypesForCaller(int userId) {
		List<String> managedAccountTypes = new ArrayList<>();
		long identityToken = Binder.clearCallingIdentity();
		Collection<RegisteredServicesCache.ServiceInfo<AuthenticatorDescription>> serviceInfos;
		try {
			serviceInfos = mAuthenticatorCache.getAllServices(userId);
		} finally {
			Binder.restoreCallingIdentity(identityToken);
		}
		for (RegisteredServicesCache.ServiceInfo<AuthenticatorDescription> serviceInfo : serviceInfos) {
			managedAccountTypes.add(serviceInfo.type.type);
		}
		return managedAccountTypes;
	}

	@Override
	public Account[] getAccountsForPackage(String packageName, int uid) throws RemoteException {
		return getAccountsAsUser(null, userAccounts.userId, packageName);
	}

	@Override
	public Account[] getAccountsByTypeForPackage(String type, String packageName) throws RemoteException {
		return getAccountsAsUser(type, userAccounts.userId, packageName);
	}

	@Override
	public void hasFeatures(IAccountManagerResponse response, Account account, String[] features)
			throws RemoteException {
		XLog.v(TAG, "hasFeatures: " + account + ", response " + response + ", features " + stringArrayToString(features)
				+ ", caller's uid " + Binder.getCallingUid() + ", pid " + Binder.getCallingUid());
		if (response == null)
			throw new IllegalArgumentException("response is null");
		if (account == null)
			throw new IllegalArgumentException("account is null");
		if (features == null)
			throw new IllegalArgumentException("features is null");
	}

	@Override
	public void getAccountsByFeatures(IAccountManagerResponse response, String type, String[] features)
			throws RemoteException {
		XLog.v(TAG,
				"getAccounts: accountType " + type + ", response " + response + ", features "
						+ stringArrayToString(features) + ", caller's uid " + Binder.getCallingUid() + ", pid "
						+ Binder.getCallingPid());
		if (response == null)
			throw new IllegalArgumentException("response is null");
		if (type == null)
			throw new IllegalArgumentException("accountType is null");
		List<String> visibleAccountTypes = getTypesVisibleToCaller(userAccounts.userId);
		if (!visibleAccountTypes.contains(type)) {
			Bundle result = new Bundle();
			// Need to return just the accounts that are from matching
			// signatures.
			result.putParcelableArray(AccountManager.KEY_ACCOUNTS, new Account[0]);
			try {
				response.onResult(result);
			} catch (RemoteException e) {
				Log.e(TAG, "Cannot respond to caller do to exception.", e);
			}
			return;
		}
		if (features == null || features.length == 0) {
			Account[] accounts;
			synchronized (userAccounts.cacheLock) {
				accounts = getAccountsFromCacheLocked(userAccounts, type, null);
			}
			Bundle result = new Bundle();
			result.putParcelableArray(AccountManager.KEY_ACCOUNTS, accounts);
			onResult(response, result);
			return;
		}
		new GetAccountsByTypeAndFeatureSession(userAccounts, response, type, features).bind();

	}

	private void onResult(IAccountManagerResponse response, Bundle result) {
		if (result == null) {
			XLog.e(TAG, "the result is unexpectedly null", new Exception());
		}
		XLog.v(TAG, getClass().getSimpleName() + " calling onResult() on response " + response);
		try {
			response.onResult(result);
		} catch (RemoteException e) {
			// if the caller is dead then there is no one to care about remote
			// exceptions
			XLog.v(TAG, "failure while notifying response", e);
		}
	}

	@Override
	public boolean addAccountExplicitly(Account account, String password, Bundle extras) throws RemoteException {
		XLog.v(TAG, "addAccountExplicitly: " + account + ", caller's uid " + Binder.getCallingUid() + ", pid "
				+ Binder.getCallingPid());
		if (account == null)
			throw new IllegalArgumentException("account is null");
		return addAccountInternal(userAccounts, account, password, extras, false);
	}

	private boolean addAccountInternal(UserAccounts accounts, Account account, String password, Bundle extras,
			boolean restricted) {
		if (account == null) {
			return false;
		}
		synchronized (accounts.cacheLock) {
			final SQLiteDatabase db = accounts.openHelper.getWritableDatabase();
			db.beginTransaction();
			try {
				long numMatches = DatabaseUtils.longForQuery(db, "select count(*) from " + TABLE_ACCOUNTS + " WHERE "
						+ ACCOUNTS_NAME + "=? AND " + ACCOUNTS_TYPE + "=?", new String[]{account.name, account.type});
				if (numMatches > 0) {
					Log.w(TAG, "insertAccountIntoDatabase: " + account + ", skipping since the account already exists");
					return false;
				}
				ContentValues values = new ContentValues();
				values.put(ACCOUNTS_NAME, account.name);
				values.put(ACCOUNTS_TYPE, account.type);
				values.put(ACCOUNTS_PASSWORD, password);
				values.put(ACCOUNTS_LAST_AUTHENTICATE_TIME_EPOCH_MILLIS, System.currentTimeMillis());
				long accountId = db.insert(TABLE_ACCOUNTS, ACCOUNTS_NAME, values);
				if (accountId < 0) {
					Log.w(TAG, "insertAccountIntoDatabase: " + account + ", skipping the DB insert failed");
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

				insertAccountIntoCacheLocked(accounts, account);
			} finally {
				db.endTransaction();
			}
			sendAccountsChangedBroadcast();
		}
		return true;
	}

	private void sendAccountsChangedBroadcast() {
		Log.i(TAG, "the accounts changed, sending broadcast of " + ACCOUNTS_CHANGED_INTENT.getAction());
		mContext.sendBroadcast(ACCOUNTS_CHANGED_INTENT);
	}

	/**
	 * This assumes that the caller has already checked that the account is not
	 * already present.
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

	private long insertExtraLocked(SQLiteDatabase db, long accountId, String key, String value) {
		ContentValues values = new ContentValues();
		values.put(EXTRAS_KEY, key);
		values.put(EXTRAS_ACCOUNTS_ID, accountId);
		values.put(EXTRAS_VALUE, value);
		return db.insert(TABLE_EXTRAS, EXTRAS_KEY, values);
	}

	@Override
	public void removeAccount(IAccountManagerResponse response, Account account, boolean expectActivityLaunch)
			throws RemoteException {
		Log.v(TAG, "removeAccount: " + account + ", response " + response + ", caller's uid " + Binder.getCallingUid()
				+ ", pid " + Binder.getCallingPid());
		if (response == null)
			throw new IllegalArgumentException("response is null");
		if (account == null)
			throw new IllegalArgumentException("account is null");
		cancelNotification(getSigninRequiredNotificationId(userAccounts, account));
		synchronized (userAccounts.credentialsPermissionNotificationIds) {
			for (Pair<Pair<Account, String>, Integer> pair : userAccounts.credentialsPermissionNotificationIds
					.keySet()) {
				if (account.equals(pair.first.first)) {
					int id = userAccounts.credentialsPermissionNotificationIds.get(pair);
					cancelNotification(id);
				}
			}
		}
		new RemoveAccountSession(userAccounts, response, account, expectActivityLaunch).bind();

	}

	@Override
	public boolean removeAccountExplicitly(Account account) throws RemoteException {
		XLog.v(TAG, "removeAccountExplicitly: " + account + ", caller's uid " + Binder.getCallingUid() + ", pid "
				+ Binder.getCallingPid());
		if (account == null) {
			/*
			 * Null accounts should result in returning false, as per
			 * AccountManage.addAccountExplicitly(...) java doc.
			 */
			Log.e(TAG, "account is null");
			return false;
		}
		UserAccounts accounts = getUserAccountsForCaller();
		return removeAccountInternal(accounts, account);
	}

	@Override
	public void copyAccountToUser(IAccountManagerResponse response, Account account, int userFrom, int userTo)
			throws RemoteException {
		// Nothing to do, only one account
	}

	@Override
	public void invalidateAuthToken(String accountType, String authToken) throws RemoteException {
		XLog.v(TAG, "invalidateAuthToken: accountType " + accountType + ", caller's uid " + Binder.getCallingUid()
				+ ", pid " + Binder.getCallingPid());
		if (accountType == null)
			throw new IllegalArgumentException("accountType is null");
		if (authToken == null)
			throw new IllegalArgumentException("authToken is null");
		UserAccounts accounts = userAccounts;
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
	}

	private void invalidateCustomTokenLocked(UserAccounts accounts, String accountType, String authToken) {
		if (authToken == null || accountType == null) {
			return;
		}
		// Also wipe out cached token in memory.
		accounts.accountTokenCaches.remove(accountType, authToken);
	}

	private void invalidateAuthTokenLocked(UserAccounts accounts, SQLiteDatabase db, String accountType,
			String authToken) {
		if (authToken == null || accountType == null) {
			return;
		}
		Cursor cursor = db.rawQuery("SELECT " + TABLE_AUTHTOKENS + "." + AUTHTOKENS_ID + ", " + TABLE_ACCOUNTS + "."
				+ ACCOUNTS_NAME + ", " + TABLE_AUTHTOKENS + "." + AUTHTOKENS_TYPE + " FROM " + TABLE_ACCOUNTS + " JOIN "
				+ TABLE_AUTHTOKENS + " ON " + TABLE_ACCOUNTS + "." + ACCOUNTS_ID + " = " + AUTHTOKENS_ACCOUNTS_ID
				+ " WHERE " + AUTHTOKENS_AUTHTOKEN + " = ? AND " + TABLE_ACCOUNTS + "." + ACCOUNTS_TYPE + " = ?",
				new String[]{authToken, accountType});
		try {
			while (cursor.moveToNext()) {
				long authTokenId = cursor.getLong(0);
				String accountName = cursor.getString(1);
				String authTokenType = cursor.getString(2);
				db.delete(TABLE_AUTHTOKENS, AUTHTOKENS_ID + "=" + authTokenId, null);
				writeAuthTokenIntoCacheLocked(accounts, db, new Account(accountName, accountType), authTokenType, null);
			}
		} finally {
			cursor.close();
		}
	}

	protected void writeAuthTokenIntoCacheLocked(UserAccounts accounts, final SQLiteDatabase db, Account account,
			String key, String value) {
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

	@Override
	public String peekAuthToken(Account account, String authTokenType) throws RemoteException {
		Log.v(TAG, "peekAuthToken: " + account + ", authTokenType " + authTokenType + ", caller's uid "
				+ Binder.getCallingUid() + ", pid " + Binder.getCallingPid());
		if (account == null)
			throw new IllegalArgumentException("account is null");
		if (authTokenType == null)
			throw new IllegalArgumentException("authTokenType is null");

		return readAuthTokenInternal(userAccounts, account, authTokenType);
	}

	@Override
	public void setAuthToken(Account account, String authTokenType, String authToken) throws RemoteException {
		XLog.v(TAG, "setAuthToken: " + account + ", authTokenType " + authTokenType + ", caller's uid "
				+ Binder.getCallingUid() + ", pid " + Binder.getCallingPid());

		if (account == null)
			throw new IllegalArgumentException("account is null");
		if (authTokenType == null)
			throw new IllegalArgumentException("authTokenType is null");
		UserAccounts accounts = userAccounts;
		saveAuthTokenToDatabase(accounts, account, authTokenType, authToken);
	}

	private boolean saveAuthTokenToDatabase(UserAccounts accounts, Account account, String type, String authToken) {
		if (account == null || type == null) {
			return false;
		}
		cancelNotification(getSigninRequiredNotificationId(accounts, account));
		synchronized (accounts.cacheLock) {
			final SQLiteDatabase db = accounts.openHelper.getWritableDatabase();
			db.beginTransaction();
			try {
				long accountId = getAccountIdLocked(db, account);
				if (accountId < 0) {
					return false;
				}
				db.delete(TABLE_AUTHTOKENS, AUTHTOKENS_ACCOUNTS_ID + "=" + accountId + " AND " + AUTHTOKENS_TYPE + "=?",
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
	public void setPassword(Account account, String password) throws RemoteException {
		XLog.v(TAG, "setAuthToken: " + account + ", caller's uid " + Binder.getCallingUid() + ", pid "
				+ Binder.getCallingPid());
		if (account == null)
			throw new IllegalArgumentException("account is null");
		UserAccounts accounts = userAccounts;
		setPasswordInternal(accounts, account, password);

	}

	private void setPasswordInternal(UserAccounts accounts, Account account, String password) {
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
				}
			} finally {
				db.endTransaction();
			}
			sendAccountsChangedBroadcast();
		}
	}

	@Override
	public void clearPassword(Account account) throws RemoteException {
		XLog.v(TAG, "clearPassword: " + account + ", caller's uid " + Binder.getCallingUid() + ", pid "
				+ Binder.getCallingPid());

		if (account == null)
			throw new IllegalArgumentException("account is null");

		UserAccounts accounts = userAccounts;
		setPasswordInternal(accounts, account, null);
	}

	@Override
	public void setUserData(Account account, String key, String value) throws RemoteException {
		XLog.v(TAG, "setUserData: " + account + ", key " + key + ", caller's uid " + Binder.getCallingUid() + ", pid "
				+ Binder.getCallingPid());

		if (key == null)
			throw new IllegalArgumentException("key is null");
		if (account == null)
			throw new IllegalArgumentException("account is null");
		UserAccounts accounts = userAccounts;
		setUserdataInternal(accounts, account, key, value);
	}

	private void setUserdataInternal(UserAccounts accounts, Account account, String key, String value) {
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
				if (extrasId < 0) {
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

	protected void writeUserDataIntoCacheLocked(UserAccounts accounts, final SQLiteDatabase db, Account account,
			String key, String value) {
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

	@Override
	public void updateAppPermission(Account account, String authTokenType, int uid, boolean value)
			throws RemoteException {
		throw new SecurityException();
	}

	@Override
	public void getAuthToken(IAccountManagerResponse response, final Account account, final String authTokenType,
			final boolean notifyOnAuthFailure, boolean expectActivityLaunch, final Bundle loginOptions)
			throws RemoteException {
		XLog.v(TAG, "getAuthToken: " + account + ", response " + response + ", authTokenType " + authTokenType
				+ ", notifyOnAuthFailure " + notifyOnAuthFailure + ", expectActivityLaunch " + expectActivityLaunch
				+ ", caller's uid " + Binder.getCallingUid() + ", pid " + Binder.getCallingPid());
		if (response == null)
			throw new IllegalArgumentException("response is null");
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
		final UserAccounts accounts;
		final RegisteredServicesCache.ServiceInfo<AuthenticatorDescription> authenticatorInfo;
		accounts = userAccounts;
		authenticatorInfo = mAuthenticatorCache.getServiceInfo(AuthenticatorDescription.newKey(account.type),
				accounts.userId);

		final boolean customTokens = authenticatorInfo != null && authenticatorInfo.type.customTokens;
		final boolean permissionGranted = customTokens;
		final String callerPkg = loginOptions.getString(AccountManager.KEY_ANDROID_PACKAGE_NAME);
		if (callerPkg == null || !VirtualCore.getCore().isAppInstalled(callerPkg)) {
			String msg = String.format("Attempting to illegally masquerade as package %s!", callerPkg);
			throw new SecurityException(msg);
		}
		// let authenticator know the identity of the caller
		loginOptions.putInt(AccountManager.KEY_CALLER_UID, Binder.getCallingUid());
		loginOptions.putInt(AccountManager.KEY_CALLER_PID, Binder.getCallingPid());

		if (notifyOnAuthFailure) {
			loginOptions.putBoolean(AccountManager.KEY_NOTIFY_ON_FAILURE, true);
		}
		// Distill the caller's package signatures into a single digest.
		final byte[] callerPkgSigDigest = calculatePackageSignatureDigest(callerPkg);

		// if the caller has permission, do the peek. otherwise go the more
		// expensive
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
			 * Look up tokens in the new cache only if the loginOptions don't
			 * have parameters outside of those expected to be injected by the
			 * AccountManager, e.g. ANDORID_PACKAGE_NAME.
			 */
			String token = readCachedTokenInternal(accounts, account, authTokenType, callerPkg, callerPkgSigDigest);
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

		new Session(accounts, response, account.type, expectActivityLaunch, false /* stripAuthTokenFromResult */,
				account.name, false /* authDetailsRequired */) {
			@Override
			protected String toDebugString(long now) {
				if (loginOptions != null)
					loginOptions.keySet();
				return super.toDebugString(now) + ", getAuthToken" + ", " + account + ", authTokenType " + authTokenType
						+ ", loginOptions " + loginOptions + ", notifyOnAuthFailure " + notifyOnAuthFailure;
			}

			@Override
			public void run() throws RemoteException {
				// If the caller doesn't have permission then create and return
				// the
				// "grant permission" intent instead of the "getAuthToken"
				// intent.
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
						Intent intent = newGrantCredentialsPermissionIntent(account, Binder.getCallingUid(),
								new AccountAuthenticatorResponse(this), authTokenType);
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
							saveAuthTokenToDatabase(mAccounts, resultAccount, authTokenType, authToken);
						}
						long expiryMillis = 0;
						if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
							expiryMillis = result.getLong(AccountManagerCompat.KEY_CUSTOM_TOKEN_EXPIRY, 0L);
						}
						if (customTokens && expiryMillis > System.currentTimeMillis()) {
							saveCachedToken(mAccounts, account, callerPkg, callerPkgSigDigest, authTokenType, authToken,
									expiryMillis);
						}
					}

					Intent intent = result.getParcelable(AccountManager.KEY_INTENT);
					if (intent != null && notifyOnAuthFailure && !customTokens) {
						doNotification(mAccounts, account, result.getString(AccountManager.KEY_AUTH_FAILED_MESSAGE),
								intent, accounts.userId);
					}
				}
				super.onResult(result);
			}
		}.bind();

	}

	private void doNotification(UserAccounts accounts, Account account, CharSequence message, Intent intent,
			int userId) {
		// TODO: should we implement this function?
	}

	private void saveCachedToken(UserAccounts accounts, Account account, String callerPkg, byte[] callerSigDigest,
			String tokenType, String token, long expiryMillis) {

		if (account == null || tokenType == null || callerPkg == null || callerSigDigest == null) {
			return;
		}
		cancelNotification(getSigninRequiredNotificationId(accounts, account));
		synchronized (accounts.cacheLock) {
			accounts.accountTokenCaches.put(account, token, tokenType, callerPkg, callerSigDigest, expiryMillis);
		}
	}

	private Intent newGrantCredentialsPermissionIntent(Account account, int uid, AccountAuthenticatorResponse response,
			String authTokenType) {

		Intent intent = new Intent(mContext, GrantCredentialsPermissionActivity.class);
		// See FLAG_ACTIVITY_NEW_TASK docs for limitations and benefits of the
		// flag.
		// Since it was set in Eclair+ we can't change it without breaking apps
		// using
		// the intent from a non-Activity context.
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.addCategory(String.valueOf(getCredentialPermissionNotificationId(account, authTokenType, uid)));

		intent.putExtra(GrantCredentialsPermissionActivity.EXTRAS_ACCOUNT, account);
		intent.putExtra(GrantCredentialsPermissionActivity.EXTRAS_AUTH_TOKEN_TYPE, authTokenType);
		intent.putExtra(GrantCredentialsPermissionActivity.EXTRAS_RESPONSE, response);
		intent.putExtra(GrantCredentialsPermissionActivity.EXTRAS_REQUESTING_UID, uid);

		return intent;
	}

	private Integer getCredentialPermissionNotificationId(Account account, String authTokenType, int uid) {
		Integer id;
		UserAccounts accounts = userAccounts;
		synchronized (accounts.credentialsPermissionNotificationIds) {
			final Pair<Pair<Account, String>, Integer> key = new Pair<Pair<Account, String>, Integer>(
					new Pair<Account, String>(account, authTokenType), uid);
			id = accounts.credentialsPermissionNotificationIds.get(key);
			if (id == null) {
				id = mNotificationIds.incrementAndGet();
				accounts.credentialsPermissionNotificationIds.put(key, id);
			}
		}
		return id;
	}

	protected String readCachedTokenInternal(UserAccounts accounts, Account account, String tokenType,
			String callingPackage, byte[] pkgSigDigest) {
		synchronized (accounts.cacheLock) {
			return accounts.accountTokenCaches.get(account, tokenType, callingPackage, pkgSigDigest);
		}
	}

	private byte[] calculatePackageSignatureDigest(String callerPkg) {
		MessageDigest digester;
		try {
			digester = MessageDigest.getInstance("SHA-256");
			PackageInfo pkgInfo = mPackageManager.getPackageInfo(callerPkg, PackageManager.GET_SIGNATURES);
			for (Signature sig : pkgInfo.signatures) {
				digester.update(sig.toByteArray());
			}
		} catch (NoSuchAlgorithmException x) {
			Log.wtf(TAG, "SHA-256 should be available", x);
			digester = null;
		} catch (PackageManager.NameNotFoundException e) {
			Log.w(TAG, "Could not find packageinfo for: " + callerPkg);
			digester = null;
		}
		return (digester == null) ? null : digester.digest();
	}

	@Override
	public void addAccount(IAccountManagerResponse response, final String accountType, final String authTokenType,
			final String[] requiredFeatures, boolean expectActivityLaunch, Bundle optionsIn) throws RemoteException {
		XLog.v(TAG,
				"addAccount: accountType " + accountType + ", response " + response + ", authTokenType " + authTokenType
						+ ", requiredFeatures " + stringArrayToString(requiredFeatures) + ", expectActivityLaunch "
						+ expectActivityLaunch + ", caller's uid " + Binder.getCallingUid() + ", pid "
						+ Binder.getCallingPid());
		if (response == null)
			throw new IllegalArgumentException("response is null");
		if (accountType == null)
			throw new IllegalArgumentException("accountType is null");

		final int pid = Binder.getCallingPid();
		final int uid = Binder.getCallingUid();
		final Bundle options = (optionsIn == null) ? new Bundle() : optionsIn;
		options.putInt(AccountManager.KEY_CALLER_UID, uid);
		options.putInt(AccountManager.KEY_CALLER_PID, pid);
		UserAccounts accounts = userAccounts;
		new Session(accounts, response, accountType, expectActivityLaunch, true /* stripAuthTokenFromResult */,
				null /* accountName */, false /* authDetailsRequired */, true /* updateLastAuthenticationTime */) {
			@Override
			public void run() throws RemoteException {
				mAuthenticator.addAccount(this, mAccountType, authTokenType, requiredFeatures, options);
			}

			@Override
			protected String toDebugString(long now) {
				return super.toDebugString(now) + ", addAccount" + ", accountType " + accountType
						+ ", requiredFeatures "
						+ (requiredFeatures != null ? TextUtils.join(",", requiredFeatures) : null);
			}
		}.bind();

	}

	@Override
	public void updateCredentials(IAccountManagerResponse response, final Account account, final String authTokenType,
			boolean expectActivityLaunch, final Bundle loginOptions) throws RemoteException {
		XLog.v(TAG,
				"updateCredentials: " + account + ", response " + response + ", authTokenType " + authTokenType
						+ ", expectActivityLaunch " + expectActivityLaunch + ", caller's uid " + Binder.getCallingUid()
						+ ", pid " + Binder.getCallingPid());
		if (response == null)
			throw new IllegalArgumentException("response is null");
		if (account == null)
			throw new IllegalArgumentException("account is null");
		if (authTokenType == null)
			throw new IllegalArgumentException("authTokenType is null");
		UserAccounts accounts = userAccounts;
		new Session(accounts, response, account.type, expectActivityLaunch, true /* stripAuthTokenFromResult */,
				account.name, false /* authDetailsRequired */, true /* updateLastCredentialTime */) {
			@Override
			public void run() throws RemoteException {
				mAuthenticator.updateCredentials(this, account, authTokenType, loginOptions);
			}
			@Override
			protected String toDebugString(long now) {
				if (loginOptions != null)
					loginOptions.keySet();
				return super.toDebugString(now) + ", updateCredentials" + ", " + account + ", authTokenType "
						+ authTokenType + ", loginOptions " + loginOptions;
			}
		}.bind();
	}

	@Override
	public void editProperties(IAccountManagerResponse response, final String accountType, boolean expectActivityLaunch)
			throws RemoteException {
		XLog.v(TAG,
				"editProperties: accountType " + accountType + ", response " + response + ", expectActivityLaunch "
						+ expectActivityLaunch + ", caller's uid " + Binder.getCallingUid() + ", pid "
						+ Binder.getCallingPid());

		if (response == null)
			throw new IllegalArgumentException("response is null");
		if (accountType == null)
			throw new IllegalArgumentException("accountType is null");

		UserAccounts accounts = userAccounts;
		new Session(accounts, response, accountType, expectActivityLaunch, true /* stripAuthTokenFromResult */,
				null /* accountName */, false /* authDetailsRequired */) {
			@Override
			public void run() throws RemoteException {
				mAuthenticator.editProperties(this, mAccountType);
			}
			@Override
			protected String toDebugString(long now) {
				return super.toDebugString(now) + ", editProperties" + ", accountType " + accountType;
			}
		}.bind();

	}

	@Override
	public void confirmCredentialsAsUser(IAccountManagerResponse response, final Account account, final Bundle options,
			boolean expectActivityLaunch, int userId) throws RemoteException {
		XLog.v(TAG,
				"confirmCredentials: " + account + ", response " + response + ", expectActivityLaunch "
						+ expectActivityLaunch + ", caller's uid " + Binder.getCallingUid() + ", pid "
						+ Binder.getCallingPid());

		if (response == null)
			throw new IllegalArgumentException("response is null");
		if (account == null)
			throw new IllegalArgumentException("account is null");

		UserAccounts accounts = userAccounts;
		new Session(accounts, response, account.type, expectActivityLaunch, true /* stripAuthTokenFromResult */,
				account.name, true /* authDetailsRequired */, true /* updateLastAuthenticatedTime */) {
			@Override
			public void run() throws RemoteException {
				mAuthenticator.confirmCredentials(this, account, options);
			}
			@Override
			protected String toDebugString(long now) {
				return super.toDebugString(now) + ", confirmCredentials" + ", " + account;
			}
		}.bind();
	}

	@Override
	public boolean accountAuthenticated(Account account) throws RemoteException {
		XLog.v(TAG, "accountAuthenticated( account: %s, callerUid: %s)", account, Binder.getCallingUid());
		if (account == null) {
			throw new IllegalArgumentException("account is null");
		}
		return updateLastAuthenticatedTime(account);
	}

	@Override
	public void getAuthTokenLabel(IAccountManagerResponse response, final String accountType,
			final String authTokenType) throws RemoteException {
		if (accountType == null)
			throw new IllegalArgumentException("accountType is null");
		if (authTokenType == null)
			throw new IllegalArgumentException("authTokenType is null");

		UserAccounts accounts = userAccounts;
		new Session(accounts, response, accountType, false /* expectActivityLaunch */,
				false /* stripAuthTokenFromResult */, null /* accountName */, false /* authDetailsRequired */) {
			@Override
			protected String toDebugString(long now) {
				return super.toDebugString(now) + ", getAuthTokenLabel" + ", " + accountType + ", authTokenType "
						+ authTokenType;
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
				} else {
					super.onResult(null);
				}
			}
		}.bind();
	}

	@Override
	public boolean addSharedAccountAsUser(Account account, int userId) throws RemoteException {
		UserAccounts accounts = userAccounts;
		SQLiteDatabase db = accounts.openHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(ACCOUNTS_NAME, account.name);
		values.put(ACCOUNTS_TYPE, account.type);
		db.delete(TABLE_SHARED_ACCOUNTS, ACCOUNTS_NAME + "=? AND " + ACCOUNTS_TYPE + "=?",
				new String[]{account.name, account.type});
		long accountId = db.insert(TABLE_SHARED_ACCOUNTS, ACCOUNTS_NAME, values);
		if (accountId < 0) {
			Log.w(TAG, "insertAccountIntoDatabase: " + account + ", skipping the DB insert failed");
			return false;
		}
		return true;
	}

	@Override
	public Account[] getSharedAccountsAsUser(int userId) {
		UserAccounts accounts = userAccounts;
		ArrayList<Account> accountList = new ArrayList<Account>();
		Cursor cursor = null;
		try {
			cursor = accounts.openHelper.getReadableDatabase().query(TABLE_SHARED_ACCOUNTS,
					new String[]{ACCOUNTS_NAME, ACCOUNTS_TYPE}, null, null, null, null, null);
			if (cursor != null && cursor.moveToFirst()) {
				int nameIndex = cursor.getColumnIndex(ACCOUNTS_NAME);
				int typeIndex = cursor.getColumnIndex(ACCOUNTS_TYPE);
				do {
					accountList.add(new Account(cursor.getString(nameIndex), cursor.getString(typeIndex)));
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

	@Override
	public boolean removeSharedAccountAsUser(Account account, int userId) throws RemoteException {
		UserAccounts accounts = userAccounts;
		SQLiteDatabase db = accounts.openHelper.getWritableDatabase();
		int r = db.delete(TABLE_SHARED_ACCOUNTS, ACCOUNTS_NAME + "=? AND " + ACCOUNTS_TYPE + "=?",
				new String[]{account.name, account.type});
		if (r > 0) {
			removeAccountInternal(accounts, account);
		}
		return r > 0;
	}

	@Override
	public void renameAccount(IAccountManagerResponse response, Account accountToRename, String newName)
			throws RemoteException {
		XLog.v(TAG, "renameAccount: " + accountToRename + " -> " + newName + ", caller's uid " + Binder.getCallingUid()
				+ ", pid " + Binder.getCallingPid());
		if (accountToRename == null)
			throw new IllegalArgumentException("account is null");
		UserAccounts accounts = userAccounts;
		Account resultingAccount = renameAccountInternal(accounts, accountToRename, newName);
		Bundle result = new Bundle();
		result.putString(AccountManager.KEY_ACCOUNT_NAME, resultingAccount.name);
		result.putString(AccountManager.KEY_ACCOUNT_TYPE, resultingAccount.type);
		try {
			response.onResult(result);
		} catch (RemoteException e) {
			XLog.w(TAG, e.getMessage());
		}

	}

	@Override
	public String getPreviousName(Account account) throws RemoteException {
		XLog.v(TAG, "getPreviousName: " + account + ", caller's uid " + Binder.getCallingUid() + ", pid "
				+ Binder.getCallingPid());
		if (account == null)
			throw new IllegalArgumentException("account is null");
		return readPreviousNameInternal(userAccounts, account);
	}

	private String readPreviousNameInternal(UserAccounts accounts, Account account) {
		if (account == null) {
			return null;
		}
		synchronized (accounts.cacheLock) {
			AtomicReference<String> previousNameRef = accounts.previousNameCache.get(account);
			if (previousNameRef == null) {
				final SQLiteDatabase db = accounts.openHelper.getReadableDatabase();
				Cursor cursor = db.query(TABLE_ACCOUNTS, new String[]{ACCOUNTS_PREVIOUS_NAME},
						ACCOUNTS_NAME + "=? AND " + ACCOUNTS_TYPE + "=?", new String[]{account.name, account.type},
						null, null, null);
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
	public boolean renameSharedAccountAsUser(Account account, String newName, int userId) throws RemoteException {
		UserAccounts accounts = userAccounts;
		SQLiteDatabase db = accounts.openHelper.getWritableDatabase();
		final ContentValues values = new ContentValues();
		values.put(ACCOUNTS_NAME, newName);
		values.put(ACCOUNTS_PREVIOUS_NAME, account.name);
		int r = db.update(TABLE_SHARED_ACCOUNTS, values, ACCOUNTS_NAME + "=? AND " + ACCOUNTS_TYPE + "=?",
				new String[]{account.name, account.type});
		if (r > 0) {
			// Recursively rename the account.
			renameAccountInternal(accounts, account, newName);
		}
		return r > 0;
	}

	private Account renameAccountInternal(UserAccounts accounts, Account accountToRename, String newName) {
		Account resultAccount = null;
		/*
		 * Cancel existing notifications. Let authenticators re-post
		 * notifications as required. But we don't know if the authenticators
		 * have bound their notifications to now stale account name data.
		 *
		 * With a rename api, we might not need to do this anymore but it
		 * shouldn't hurt.
		 */
		cancelNotification(getSigninRequiredNotificationId(accounts, accountToRename));
		synchronized (accounts.credentialsPermissionNotificationIds) {
			for (Pair<Pair<Account, String>, Integer> pair : accounts.credentialsPermissionNotificationIds.keySet()) {
				if (accountToRename.equals(pair.first.first)) {
					int id = accounts.credentialsPermissionNotificationIds.get(pair);
					cancelNotification(id);
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
					final String[] argsAccountId = {String.valueOf(accountId)};
					db.update(TABLE_ACCOUNTS, values, ACCOUNTS_ID + "=?", argsAccountId);
					db.setTransactionSuccessful();
					isSuccessful = true;
				}
			} finally {
				db.endTransaction();
				if (isSuccessful) {
					/*
					 * Database transaction was successful. Clean up cached data
					 * associated with the account in the user profile.
					 */
					insertAccountIntoCacheLocked(accounts, renamedAccount);
					/*
					 * Extract the data and token caches before removing the old
					 * account to preserve the user data associated with the
					 * account.
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
					accounts.previousNameCache.put(renamedAccount, new AtomicReference<String>(accountToRename.name));
					resultAccount = renamedAccount;
					sendAccountsChangedBroadcast();
				}
			}
		}
		return resultAccount;
	}

	private long getAccountIdFromSharedTable(SQLiteDatabase db, Account account) {
		Cursor cursor = db.query(TABLE_SHARED_ACCOUNTS, new String[]{ACCOUNTS_ID}, "name=? AND type=?",
				new String[]{account.name, account.type}, null, null, null);
		try {
			if (cursor.moveToNext()) {
				return cursor.getLong(0);
			}
			return -1;
		} finally {
			cursor.close();
		}
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

	private boolean removeAccountInternal(UserAccounts accounts, Account account) {
		int deleted;
		synchronized (accounts.cacheLock) {
			final SQLiteDatabase db = accounts.openHelper.getWritableDatabase();
			// final long accountId = getAccountIdLocked(db, account);
			deleted = db.delete(TABLE_ACCOUNTS, ACCOUNTS_NAME + "=? AND " + ACCOUNTS_TYPE + "=?",
					new String[]{account.name, account.type});
			removeAccountFromCacheLocked(accounts, account);
			sendAccountsChangedBroadcast();
		}
		return (deleted > 0);
	}

	private long getAccountIdLocked(SQLiteDatabase db, Account account) {
		Cursor cursor = db.query(TABLE_ACCOUNTS, new String[]{ACCOUNTS_ID}, "name=? AND type=?",
				new String[]{account.name, account.type}, null, null, null);
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
				EXTRAS_ACCOUNTS_ID + "=" + accountId + " AND " + EXTRAS_KEY + "=?", new String[]{key}, null, null,
				null);
		try {
			if (cursor.moveToNext()) {
				return cursor.getLong(0);
			}
			return -1;
		} finally {
			cursor.close();
		}
	}

	protected void cancelNotification(int id) {
		((NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE)).cancel(null, id);
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

	static class DatabaseHelper extends SQLiteOpenHelper {

		public DatabaseHelper(Context context) {
			super(context, VAccountService.getDatabaseName(context), null, DATABASE_VERSION);
		}

		/**
		 * This call needs to be made while the mCacheLock is held. The way to
		 * ensure this is to get the lock any time a method is called ont the
		 * DatabaseHelper
		 *
		 * @param db
		 *            The database.
		 */
		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE " + TABLE_ACCOUNTS + " ( " + ACCOUNTS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
					+ ACCOUNTS_NAME + " TEXT NOT NULL, " + ACCOUNTS_TYPE + " TEXT NOT NULL, " + ACCOUNTS_PASSWORD
					+ " TEXT, " + ACCOUNTS_PREVIOUS_NAME + " TEXT, " + ACCOUNTS_LAST_AUTHENTICATE_TIME_EPOCH_MILLIS
					+ " INTEGER DEFAULT 0, " + "UNIQUE(" + ACCOUNTS_NAME + "," + ACCOUNTS_TYPE + "))");

			db.execSQL("CREATE TABLE " + TABLE_AUTHTOKENS + " (  " + AUTHTOKENS_ID
					+ " INTEGER PRIMARY KEY AUTOINCREMENT,  " + AUTHTOKENS_ACCOUNTS_ID + " INTEGER NOT NULL, "
					+ AUTHTOKENS_TYPE + " TEXT NOT NULL,  " + AUTHTOKENS_AUTHTOKEN + " TEXT,  " + "UNIQUE ("
					+ AUTHTOKENS_ACCOUNTS_ID + "," + AUTHTOKENS_TYPE + "))");

			createGrantsTable(db);

			db.execSQL("CREATE TABLE " + TABLE_EXTRAS + " ( " + EXTRAS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
					+ EXTRAS_ACCOUNTS_ID + " INTEGER, " + EXTRAS_KEY + " TEXT NOT NULL, " + EXTRAS_VALUE + " TEXT, "
					+ "UNIQUE(" + EXTRAS_ACCOUNTS_ID + "," + EXTRAS_KEY + "))");

			db.execSQL("CREATE TABLE " + TABLE_META + " ( " + META_KEY + " TEXT PRIMARY KEY NOT NULL, " + META_VALUE
					+ " TEXT)");

			createSharedAccountsTable(db);

			createAccountsDeletionTrigger(db);
		}

		private void createSharedAccountsTable(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE " + TABLE_SHARED_ACCOUNTS + " ( " + ACCOUNTS_ID
					+ " INTEGER PRIMARY KEY AUTOINCREMENT, " + ACCOUNTS_NAME + " TEXT NOT NULL, " + ACCOUNTS_TYPE
					+ " TEXT NOT NULL, " + "UNIQUE(" + ACCOUNTS_NAME + "," + ACCOUNTS_TYPE + "))");
		}

		private void addLastSuccessfullAuthenticatedTimeColumn(SQLiteDatabase db) {
			db.execSQL("ALTER TABLE " + TABLE_ACCOUNTS + " ADD COLUMN " + ACCOUNTS_LAST_AUTHENTICATE_TIME_EPOCH_MILLIS
					+ " DEFAULT 0");
		}

		private void addOldAccountNameColumn(SQLiteDatabase db) {
			db.execSQL("ALTER TABLE " + TABLE_ACCOUNTS + " ADD COLUMN " + ACCOUNTS_PREVIOUS_NAME);
		}

		private void createAccountsDeletionTrigger(SQLiteDatabase db) {
			db.execSQL("" + " CREATE TRIGGER " + TABLE_ACCOUNTS + "Delete DELETE ON " + TABLE_ACCOUNTS + " BEGIN"
					+ "   DELETE FROM " + TABLE_AUTHTOKENS + "     WHERE " + AUTHTOKENS_ACCOUNTS_ID + "=OLD."
					+ ACCOUNTS_ID + " ;" + "   DELETE FROM " + TABLE_EXTRAS + "     WHERE " + EXTRAS_ACCOUNTS_ID
					+ "=OLD." + ACCOUNTS_ID + " ;" + "   DELETE FROM " + TABLE_GRANTS + "     WHERE "
					+ GRANTS_ACCOUNTS_ID + "=OLD." + ACCOUNTS_ID + " ;" + " END");
		}

		private void createGrantsTable(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE " + TABLE_GRANTS + " (  " + GRANTS_ACCOUNTS_ID + " INTEGER NOT NULL, "
					+ GRANTS_AUTH_TOKEN_TYPE + " STRING NOT NULL,  " + GRANTS_GRANTEE_UID + " INTEGER NOT NULL,  "
					+ "UNIQUE (" + GRANTS_ACCOUNTS_ID + "," + GRANTS_AUTH_TOKEN_TYPE + "," + GRANTS_GRANTEE_UID + "))");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			XLog.e(TAG, "upgrade from version " + oldVersion + " to version " + newVersion);

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
				db.execSQL("UPDATE " + TABLE_ACCOUNTS + " SET " + ACCOUNTS_TYPE + " = 'com.google' WHERE "
						+ ACCOUNTS_TYPE + " == 'com.google.GAIA'");
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
				oldVersion++;
			}

			if (oldVersion != newVersion) {
				XLog.e(TAG, "failed to upgrade version " + oldVersion + " to version " + newVersion);
			}
		}

		@Override
		public void onOpen(SQLiteDatabase db) {
			XLog.d(TAG, "opened database " + DATABASE_NAME);
		}
	}

	/**
	 * @author Lody
	 */
	/* package */ static class UserAccounts {
		private final DatabaseHelper openHelper;
		private final HashMap<Pair<Pair<Account, String>, Integer>, Integer> credentialsPermissionNotificationIds = new HashMap<Pair<Pair<Account, String>, Integer>, Integer>();
		private final HashMap<Account, Integer> signinRequiredNotificationIds = new HashMap<Account, Integer>();
		private final Object cacheLock = new Object();
		/** protected by the {@link #cacheLock} */
		private final HashMap<String, Account[]> accountCache = new LinkedHashMap<String, Account[]>();
		/** protected by the {@link #cacheLock} */
		private final HashMap<Account, HashMap<String, String>> userDataCache = new HashMap<Account, HashMap<String, String>>();
		/** protected by the {@link #cacheLock} */
		private final HashMap<Account, HashMap<String, String>> authTokenCache = new HashMap<Account, HashMap<String, String>>();

		/** protected by the {@link #cacheLock} */
		private final TokenCache accountTokenCaches = new TokenCache();

		/**
		 * protected by the {@link #cacheLock}
		 *
		 * Caches the previous names associated with an account. Previous names
		 * should be cached because we expect that when an Account is renamed,
		 * many clients will receive a LOGIN_ACCOUNTS_CHANGED broadcast and want
		 * to know if the accounts they care about have been renamed.
		 *
		 * The previous names are wrapped in an {@link AtomicReference} so that
		 * we can distinguish between those accounts with no previous names and
		 * those whose previous names haven't been cached (yet).
		 */
		private final HashMap<Account, AtomicReference<String>> previousNameCache = new HashMap<Account, AtomicReference<String>>();

		public int userId = Process.myUid();

		UserAccounts(Context context) {
			synchronized (cacheLock) {
				openHelper = new DatabaseHelper(context);
			}
		}
	}

	private class MessageHandler extends Handler {

		MessageHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case MESSAGE_TIMED_OUT :
					Session session = (Session) msg.obj;
					session.onTimedOut();
					break;

				default :
					throw new IllegalStateException("unhandled message: " + msg.what);
			}
		}
	}

	private class GetAccountsByTypeAndFeatureSession extends Session {
		private final String[] mFeatures;
		private volatile Account[] mAccountsOfType = null;
		private volatile ArrayList<Account> mAccountsWithFeatures = null;
		private volatile int mCurrentAccount = 0;

		public GetAccountsByTypeAndFeatureSession(UserAccounts accounts, IAccountManagerResponse response, String type,
				String[] features) {
			super(accounts, response, type, false /* expectActivityLaunch */, true /* stripAuthTokenFromResult */,
					null /* accountName */, false /* authDetailsRequired */);
			mFeatures = features;
		}

		@Override
		public void run() throws RemoteException {
			synchronized (mAccounts.cacheLock) {
				mAccountsOfType = getAccountsFromCacheLocked(mAccounts, mAccountType, null);
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
				// It is possible that the authenticator has died, which is
				// indicated by
				// mAuthenticator being set to null. If this happens then just
				// abort.
				// There is no need to send back a result or error in this case
				// since
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
						Log.v(TAG, getClass().getSimpleName() + " calling onResult() on response " + response);
					}
					Bundle result = new Bundle();
					result.putParcelableArray(AccountManager.KEY_ACCOUNTS, accounts);
					response.onResult(result);
				} catch (RemoteException e) {
					// if the caller is dead then there is no one to care about
					// remote exceptions
					if (Log.isLoggable(TAG, Log.VERBOSE)) {
						Log.v(TAG, "failure while notifying response", e);
					}
				}
			}
		}

		@Override
		protected String toDebugString(long now) {
			return super.toDebugString(now) + ", getAccountsByTypeAndFeatures" + ", "
					+ (mFeatures != null ? TextUtils.join(",", mFeatures) : null);
		}
	}

	private abstract class Session extends IAccountAuthenticatorResponse.Stub
			implements
				IBinder.DeathRecipient,
				ServiceConnection {
		protected final UserAccounts mAccounts;
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
		private final boolean mStripAuthTokenFromResult;
		public int mNumResults = 0;
		IAccountManagerResponse mResponse;
		IAccountAuthenticator mAuthenticator = null;
		private int mNumRequestContinued = 0;
		private int mNumErrors = 0;

		public Session(UserAccounts accounts, IAccountManagerResponse response, String accountType,
				boolean expectActivityLaunch, boolean stripAuthTokenFromResult, String accountName,
				boolean authDetailsRequired) {
			this(accounts, response, accountType, expectActivityLaunch, stripAuthTokenFromResult, accountName,
					authDetailsRequired, false /* updateLastAuthenticatedTime */);
		}

		public Session(UserAccounts accounts, IAccountManagerResponse response, String accountType,
				boolean expectActivityLaunch, boolean stripAuthTokenFromResult, String accountName,
				boolean authDetailsRequired, boolean updateLastAuthenticatedTime) {
			super();
			// if (response == null) throw new
			// IllegalArgumentException("response is null");
			if (accountType == null)
				throw new IllegalArgumentException("accountType is null");
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
			close(); // this clears mResponse so we need to save the response
						// before this call
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

				// clear this so that we don't accidentally send any further
				// results
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
			return "Session: expectLaunch " + mExpectActivityLaunch + ", connected " + (mAuthenticator != null)
					+ ", stats (" + mNumResults + "/" + mNumRequestContinued + "/" + mNumErrors + ")" + ", lifetime "
					+ ((now - mCreationTime) / 1000.0);
		}

		void bind() {
			XLog.v(TAG, "initiating bind to authenticator type " + mAccountType);
			if (!bindToAuthenticator(mAccountType)) {
				XLog.d(TAG, "bind attempt failed for " + toDebugString());
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
				onError(AccountManager.ERROR_CODE_REMOTE_EXCEPTION, "remote exception");
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mAuthenticator = null;
			IAccountManagerResponse response = getResponseAndClose();
			if (response != null) {
				try {
					response.onError(AccountManager.ERROR_CODE_REMOTE_EXCEPTION, "disconnected");
				} catch (RemoteException e) {
					XLog.v(TAG, "Session.onServiceDisconnected: " + "caught RemoteException while responding", e);
				}
			}
		}

		public abstract void run() throws RemoteException;

		public void onTimedOut() {
			IAccountManagerResponse response = getResponseAndClose();
			if (response != null) {
				try {
					response.onError(AccountManager.ERROR_CODE_REMOTE_EXCEPTION, "timeout");
				} catch (RemoteException e) {
					XLog.v(TAG, "Session.onTimedOut: caught RemoteException while responding", e);
				}
			}
		}

		@Override
		public void onResult(Bundle result) {
			mNumResults++;
			Intent intent = null;
			if (result != null) {
				boolean isSuccessfulConfirmCreds = result.getBoolean(AccountManager.KEY_BOOLEAN_RESULT, false);
				boolean isSuccessfulUpdateCredsOrAddAccount = result.containsKey(AccountManager.KEY_ACCOUNT_NAME)
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
									"select " + ACCOUNTS_LAST_AUTHENTICATE_TIME_EPOCH_MILLIS + " from " + TABLE_ACCOUNTS
											+ " WHERE " + ACCOUNTS_NAME + "=? AND " + ACCOUNTS_TYPE + "=?",
									new String[]{mAccountName, mAccountType});
						}
						result.putLong(AccountManagerCompat.KEY_LAST_AUTHENTICATED_TIME, lastAuthenticatedTime);
					}
				}
			}
			if (result != null && (intent = result.getParcelable(AccountManager.KEY_INTENT)) != null) {
				/*
				 * The Authenticator API allows third party authenticators to
				 * supply arbitrary intents to other apps that they can run,
				 * this can be very bad when those apps are in the system like
				 * the System Settings.
				 */
				int authenticatorUid = Binder.getCallingUid();
				long bid = Binder.clearCallingIdentity();
				try {
					PackageManager pm = mContext.getPackageManager();
					ResolveInfo resolveInfo = pm.resolveActivity(intent, 0);
					int targetUid = resolveInfo.activityInfo.applicationInfo.uid;
					if (PackageManager.SIGNATURE_MATCH != pm.checkSignatures(authenticatorUid, targetUid)) {
						throw new SecurityException(
								"Activity to be started with KEY_INTENT must " + "share Authenticator's signatures");
					}
				} finally {
					Binder.restoreCallingIdentity(bid);
				}
			}
			if (result != null && !TextUtils.isEmpty(result.getString(AccountManager.KEY_AUTHTOKEN))) {
				String accountName = result.getString(AccountManager.KEY_ACCOUNT_NAME);
				String accountType = result.getString(AccountManager.KEY_ACCOUNT_TYPE);
				if (!TextUtils.isEmpty(accountName) && !TextUtils.isEmpty(accountType)) {
					Account account = new Account(accountName, accountType);
					cancelNotification(getSigninRequiredNotificationId(mAccounts, account));
				}
			}
			IAccountManagerResponse response;
			if (mExpectActivityLaunch && result != null && result.containsKey(AccountManager.KEY_INTENT)) {
				response = mResponse;
			} else {
				response = getResponseAndClose();
			}
			if (response != null) {
				try {
					if (result == null) {
						XLog.v(TAG, getClass().getSimpleName() + " calling onError() on response " + response);
						response.onError(AccountManager.ERROR_CODE_INVALID_RESPONSE, "null bundle returned");
					} else {
						if (mStripAuthTokenFromResult) {
							result.remove(AccountManager.KEY_AUTHTOKEN);
						}
						XLog.v(TAG, getClass().getSimpleName() + " calling onResult() on response " + response);
						if ((result.getInt(AccountManager.KEY_ERROR_CODE, -1) > 0) && (intent == null)) {
							// All AccountManager error codes are greater than 0
							response.onError(result.getInt(AccountManager.KEY_ERROR_CODE),
									result.getString(AccountManager.KEY_ERROR_MESSAGE));
						} else {
							response.onResult(result);
						}
					}
				} catch (RemoteException e) {
					// if the caller is dead then there is no one to care about
					// remote exceptions
					XLog.v(TAG, "failure while notifying response", e);
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
				XLog.v(TAG, getClass().getSimpleName() + " calling onError() on response " + response);
				try {
					response.onError(errorCode, errorMessage);
				} catch (RemoteException e) {
					XLog.v(TAG, "Session.onError: caught RemoteException while responding", e);
				}
			} else {
				XLog.v(TAG, "Session.onError: already closed");
			}
		}

		/**
		 * find the component name for the authenticator and initiate a bind if
		 * no authenticator or the bind fails then return false, otherwise
		 * return true
		 */
		private boolean bindToAuthenticator(String authenticatorType) {
			final AccountAuthenticatorCache.ServiceInfo<AuthenticatorDescription> authenticatorInfo;
			authenticatorInfo = mAuthenticatorCache.getServiceInfo(AuthenticatorDescription.newKey(authenticatorType),
					mAccounts.userId);
			if (authenticatorInfo == null) {
				XLog.v(TAG, "there is no authenticator for " + authenticatorType + ", bailing out");
				return false;
			}

			Intent intent = new Intent();
			intent.setAction(AccountManager.ACTION_AUTHENTICATOR_INTENT);
			intent.setComponent(authenticatorInfo.componentName);
			XLog.v(TAG, "performing bindService to " + authenticatorInfo.componentName);
			if (!mContext.bindService(intent, this, Context.BIND_AUTO_CREATE)) {
				XLog.v(TAG, "bindService to " + authenticatorInfo.componentName + " failed");
				return false;
			}

			return true;
		}
	}

	private class RemoveAccountSession extends Session {
		final Account mAccount;
		public RemoveAccountSession(UserAccounts accounts, IAccountManagerResponse response, Account account,
				boolean expectActivityLaunch) {
			super(accounts, response, account.type, expectActivityLaunch, true /* stripAuthTokenFromResult */,
					account.name, false /* authDetailsRequired */);
			mAccount = account;
		}

		@Override
		protected String toDebugString(long now) {
			return super.toDebugString(now) + ", removeAccount" + ", account " + mAccount;
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
						Log.v(TAG, getClass().getSimpleName() + " calling onResult() on response " + response);
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
}
