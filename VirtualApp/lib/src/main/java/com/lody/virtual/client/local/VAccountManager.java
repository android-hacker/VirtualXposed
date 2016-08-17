package com.lody.virtual.client.local;

import android.accounts.Account;
import android.accounts.AuthenticatorDescription;
import android.accounts.IAccountManagerResponse;
import android.os.Bundle;
import android.os.RemoteException;

import com.lody.virtual.client.env.VirtualRuntime;
import com.lody.virtual.client.service.ServiceManagerNative;
import com.lody.virtual.service.IAccountManager;

/**
 * @author Lody
 */

public class VAccountManager {

	private static VAccountManager sMgr = new VAccountManager();

	private IAccountManager mRemote;

	public static VAccountManager get() {
		return sMgr;
	}

	public IAccountManager getRemote() {
		if (mRemote == null) {
			mRemote = IAccountManager.Stub
					.asInterface(ServiceManagerNative.getService(ServiceManagerNative.ACCOUNT_MANAGER));
		}
		return mRemote;
	}

	public String getPassword(Account account) {
		try {
			return getRemote().getPassword(account);
		} catch (RemoteException e) {
			return VirtualRuntime.crash(e);
		}
	}

	public String getUserData(Account account, String key) {
		try {
			return getRemote().getUserData(account, key);
		} catch (RemoteException e) {
			return VirtualRuntime.crash(e);
		}
	}

	public AuthenticatorDescription[] getAuthenticatorTypes(int userId) {
		try {
			return getRemote().getAuthenticatorTypes(userId);
		} catch (RemoteException e) {
			return VirtualRuntime.crash(e);
		}
	}

	public Account[] getAccounts(String accountType) {
		try {
			return getRemote().getAccounts(accountType);
		} catch (RemoteException e) {
			return VirtualRuntime.crash(e);
		}
	}

	public Account[] getAccountsForPackage(String packageName, int uid) {
		try {
			return getRemote().getAccountsForPackage(packageName, uid);
		} catch (RemoteException e) {
			return VirtualRuntime.crash(e);
		}
	}

	public Account[] getAccountsByTypeForPackage(String type, String packageName) {
		try {
			return getRemote().getAccountsByTypeForPackage(type, packageName);
		} catch (RemoteException e) {
			return VirtualRuntime.crash(e);
		}
	}

	public Account[] getAccountsAsUser(String accountType, int userId) {
		try {
			return getRemote().getAccountsAsUser(accountType, userId);
		} catch (RemoteException e) {
			return VirtualRuntime.crash(e);
		}
	}

	public void hasFeatures(IAccountManagerResponse response, Account account, String[] features) {
		try {
			getRemote().hasFeatures(response, account, features);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public void getAccountsByFeatures(IAccountManagerResponse response, String accountType, String[] features) {
		try {
			getRemote().getAccountsByFeatures(response, accountType, features);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public boolean addAccountExplicitly(Account account, String password, Bundle extras) {
		try {
			return getRemote().addAccountExplicitly(account, password, extras);
		} catch (RemoteException e) {
			return VirtualRuntime.crash(e);
		}
	}

	public void removeAccount(IAccountManagerResponse response, Account account, boolean expectActivityLaunch) {
		try {
			getRemote().removeAccount(response, account, expectActivityLaunch);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public void removeAccountAsUser(IAccountManagerResponse response, Account account, boolean expectActivityLaunch, int userId) {
		try {
			getRemote().removeAccountAsUser(response, account, expectActivityLaunch, userId);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public boolean removeAccountExplicitly(Account account) {
		try {
			return getRemote().removeAccountExplicitly(account);
		} catch (RemoteException e) {
			return VirtualRuntime.crash(e);
		}
	}

	public void copyAccountToUser(IAccountManagerResponse response, Account account, int userFrom, int userTo) {
		try {
			getRemote().copyAccountToUser(response, account, userFrom, userTo);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public void invalidateAuthToken(String accountType, String authToken) {
		try {
			getRemote().invalidateAuthToken(accountType, authToken);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public String peekAuthToken(Account account, String authTokenType) {
		try {
			return getRemote().peekAuthToken(account, authTokenType);
		} catch (RemoteException e) {
			return VirtualRuntime.crash(e);
		}
	}

	public void setAuthToken(Account account, String authTokenType, String authToken) {
		try {
			getRemote().setAuthToken(account, authTokenType, authToken);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public void setPassword(Account account, String password) {
		try {
			getRemote().setPassword(account, password);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public void clearPassword(Account account) {
		try {
			getRemote().clearPassword(account);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public void setUserData(Account account, String key, String value) {
		try {
			getRemote().setUserData(account, key, value);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public void updateAppPermission(Account account, String authTokenType, int uid, boolean value) {
		try {
			getRemote().updateAppPermission(account, authTokenType, uid, value);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public void getAuthToken(IAccountManagerResponse response, Account account, String authTokenType, boolean notifyOnAuthFailure, boolean expectActivityLaunch, Bundle options) {
		try {
			getRemote().getAuthToken(response, account, authTokenType, notifyOnAuthFailure, expectActivityLaunch, options);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public void addAccount(IAccountManagerResponse response, String accountType, String authTokenType, String[] requiredFeatures, boolean expectActivityLaunch, Bundle options) {
		try {
			getRemote().addAccount(response, accountType, authTokenType, requiredFeatures, expectActivityLaunch, options);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public void addAccountAsUser(IAccountManagerResponse response, String accountType, String authTokenType, String[] requiredFeatures, boolean expectActivityLaunch, Bundle options, int userId) {
		try {
			getRemote().addAccountAsUser(response, accountType, authTokenType, requiredFeatures, expectActivityLaunch, options, userId);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public void updateCredentials(IAccountManagerResponse response, Account account, String authTokenType, boolean expectActivityLaunch, Bundle options) {
		try {
			getRemote().updateCredentials(response, account, authTokenType, expectActivityLaunch, options);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public void editProperties(IAccountManagerResponse response, String accountType, boolean expectActivityLaunch) {
		try {
			getRemote().editProperties(response, accountType, expectActivityLaunch);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public void confirmCredentialsAsUser(IAccountManagerResponse response, Account account, Bundle options, boolean expectActivityLaunch, int userId) {
		try {
			getRemote().confirmCredentialsAsUser(response, account, options, expectActivityLaunch, userId);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public boolean accountAuthenticated(Account account) {
		try {
			return getRemote().accountAuthenticated(account);
		} catch (RemoteException e) {
			return VirtualRuntime.crash(e);
		}
	}

	public void getAuthTokenLabel(IAccountManagerResponse response, String accountType, String authTokenType) {
		try {
			getRemote().getAuthTokenLabel(response, accountType, authTokenType);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public boolean addSharedAccountAsUser(Account account, int userId) {
		try {
			return getRemote().addSharedAccountAsUser(account, userId);
		} catch (RemoteException e) {
			return VirtualRuntime.crash(e);
		}
	}

	public Account[] getSharedAccountsAsUser(int userId) {
		try {
			return getRemote().getSharedAccountsAsUser(userId);
		} catch (RemoteException e) {
			return VirtualRuntime.crash(e);
		}
	}

	public boolean removeSharedAccountAsUser(Account account, int userId) {
		try {
			return getRemote().removeSharedAccountAsUser(account, userId);
		} catch (RemoteException e) {
			return VirtualRuntime.crash(e);
		}
	}

	public void renameAccount(IAccountManagerResponse response, Account accountToRename, String newName) {
		try {
			getRemote().renameAccount(response, accountToRename, newName);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public String getPreviousName(Account account) {
		try {
			return getRemote().getPreviousName(account);
		} catch (RemoteException e) {
			return VirtualRuntime.crash(e);
		}
	}

	public boolean renameSharedAccountAsUser(Account accountToRename, String newName, int userId) {
		try {
			return getRemote().renameSharedAccountAsUser(accountToRename, newName, userId);
		} catch (RemoteException e) {
			return VirtualRuntime.crash(e);
		}
	}
}
