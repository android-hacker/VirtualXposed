package com.lody.virtual.client.hook.patchs.account;

import android.accounts.Account;
import android.accounts.IAccountManager;
import android.accounts.IAccountManagerResponse;
import android.content.Context;
import android.os.Bundle;
import android.os.Process;
import android.os.ServiceManager;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.hook.base.PatchObject;
import com.lody.virtual.client.hook.binders.HookAccountBinder;
import com.lody.virtual.client.local.VAccountManager;
import com.lody.virtual.os.VUserHandle;

import java.lang.reflect.Method;

/**
 * @author Lody
 */
public class AccountManagerPatch extends PatchObject<IAccountManager, HookAccountBinder> {

	@Override
	protected HookAccountBinder initHookObject() {
		return new HookAccountBinder();
	}

	@Override
	public void inject() throws Throwable {
		getHookObject().injectService(Context.ACCOUNT_SERVICE);
	}

	@Override
	protected void applyHooks() {
		super.applyHooks();
		addHook(new getPassword());
		addHook(new getUserData());
		addHook(new getAuthenticatorTypes());
		addHook(new getAccounts());
		addHook(new getAccountsForPackage());
		addHook(new getAccountsByTypeForPackage());
		addHook(new getAccountsAsUser());
		addHook(new hasFeatures());
		addHook(new getAccountsByFeatures());
		addHook(new addAccountExplicitly());
		addHook(new removeAccount());
		addHook(new removeAccountAsUser());
		addHook(new removeAccountExplicitly());
		addHook(new copyAccountToUser());
		addHook(new invalidateAuthToken());
		addHook(new peekAuthToken());
		addHook(new setAuthToken());
		addHook(new setPassword());
		addHook(new clearPassword());
		addHook(new setUserData());
		addHook(new updateAppPermission());
		addHook(new getAuthToken());
		addHook(new addAccount());
		addHook(new addAccountAsUser());
		addHook(new updateCredentials());
		addHook(new editProperties());
		addHook(new confirmCredentialsAsUser());
		addHook(new accountAuthenticated());
		addHook(new getAuthTokenLabel());
		addHook(new addSharedAccountAsUser());
		addHook(new getSharedAccountsAsUser());
		addHook(new removeSharedAccountAsUser());
		addHook(new renameAccount());
		addHook(new getPreviousName());
		addHook(new renameSharedAccountAsUser());
	}

	@Override
	public boolean isEnvBad() {
		return ServiceManager.getService(Context.ACCOUNT_SERVICE) != getHookObject();
	}


	private class getPassword extends Hook {
		@Override
		public String getName() {
			return "getPassword";
		}

		@Override
		public Object onHook(Object who, Method method, Object... args) throws Throwable {
			Account account = (Account) args[0];
			return VAccountManager.get().getPassword(account);
		}
	}

	private class getUserData extends Hook {
		@Override
		public String getName() {
			return "getUserData";
		}

		@Override
		public Object onHook(Object who, Method method, Object... args) throws Throwable {
			Account account = (Account) args[0];
			String key = (String) args[1];
			return VAccountManager.get().getUserData(account, key);
		}
	}

	private class getAuthenticatorTypes extends Hook {
		@Override
		public String getName() {
			return "getAuthenticatorTypes";
		}

		@Override
		public Object onHook(Object who, Method method, Object... args) throws Throwable {
			int userId = VUserHandle.myUserId();
			return VAccountManager.get().getAuthenticatorTypes(userId);
		}
	}

	private class getAccounts extends Hook {
		@Override
		public String getName() {
			return "getAccounts";
		}

		@Override
		public Object onHook(Object who, Method method, Object... args) throws Throwable {
			String accountType = (String) args[0];
			return VAccountManager.get().getAccounts(accountType);
		}
	}

	private class getAccountsForPackage extends Hook {
		@Override
		public String getName() {
			return "getAccountsForPackage";
		}

		@Override
		public Object onHook(Object who, Method method, Object... args) throws Throwable {
			String packageName = (String) args[0];
			int uid = Process.myUid();
			if (args.length > 1) {
				uid = (int) args[1];
			}
			return VAccountManager.get().getAccountsForPackage(packageName, uid);
		}
	}

	private class getAccountsByTypeForPackage extends Hook {
		@Override
		public String getName() {
			return "getAccountsByTypeForPackage";
		}

		@Override
		public Object onHook(Object who, Method method, Object... args) throws Throwable {
			String type = (String) args[0];
			String packageName = (String) args[1];
			return VAccountManager.get().getAccountsByTypeForPackage(type, packageName);
		}
	}

	private class getAccountsAsUser extends Hook {
		@Override
		public String getName() {
			return "getAccountsAsUser";
		}

		@Override
		public Object onHook(Object who, Method method, Object... args) throws Throwable {
			String accountType = (String) args[0];
			int userId = VUserHandle.myUserId();
			return VAccountManager.get().getAccountsAsUser(accountType, userId);
		}
	}

	private class hasFeatures extends Hook {
		@Override
		public String getName() {
			return "hasFeatures";
		}

		@Override
		public Object onHook(Object who, Method method, Object... args) throws Throwable {
			IAccountManagerResponse response = (IAccountManagerResponse) args[0];
			Account account = (Account) args[1];
			String[] features = (String[]) args[2];
			VAccountManager.get().hasFeatures(response, account, features);
			return 0;
		}
	}

	private class getAccountsByFeatures extends Hook {
		@Override
		public String getName() {
			return "getAccountsByFeatures";
		}

		@Override
		public Object onHook(Object who, Method method, Object... args) throws Throwable {
			IAccountManagerResponse response = (IAccountManagerResponse) args[0];
			String accountType = (String) args[1];
			String[] features = (String[]) args[2];
			VAccountManager.get().getAccountsByFeatures(response, accountType, features);
			return 0;
		}
	}

	private class addAccountExplicitly extends Hook {
		@Override
		public String getName() {
			return "addAccountExplicitly";
		}

		@Override
		public Object onHook(Object who, Method method, Object... args) throws Throwable {
			Account account = (Account) args[0];
			String password = (String) args[1];
			Bundle extras = (Bundle) args[2];
			return VAccountManager.get().addAccountExplicitly(account, password, extras);
		}
	}

	private class removeAccount extends Hook {
		@Override
		public String getName() {
			return "removeAccount";
		}

		@Override
		public Object onHook(Object who, Method method, Object... args) throws Throwable {
			IAccountManagerResponse response = (IAccountManagerResponse) args[0];
			Account account = (Account) args[1];
			boolean expectActivityLaunch = (boolean) args[2];
			VAccountManager.get().removeAccount(response, account, expectActivityLaunch);
			return 0;
		}
	}

	private class removeAccountAsUser extends Hook {
		@Override
		public String getName() {
			return "removeAccountAsUser";
		}

		@Override
		public Object onHook(Object who, Method method, Object... args) throws Throwable {
			IAccountManagerResponse response = (IAccountManagerResponse) args[0];
			Account account = (Account) args[1];
			boolean expectActivityLaunch = (boolean) args[2];
			int userId = VUserHandle.myUserId();
			VAccountManager.get().removeAccountAsUser(response, account, expectActivityLaunch, userId);
			return 0;
		}
	}

	private class removeAccountExplicitly extends Hook {
		@Override
		public String getName() {
			return "removeAccountExplicitly";
		}

		@Override
		public Object onHook(Object who, Method method, Object... args) throws Throwable {
			Account account = (Account) args[0];
			return VAccountManager.get().removeAccountExplicitly(account);
		}
	}

	private class copyAccountToUser extends Hook {
		@Override
		public String getName() {
			return "copyAccountToUser";
		}

		@Override
		public Object onHook(Object who, Method method, Object... args) throws Throwable {
			IAccountManagerResponse response = (IAccountManagerResponse) args[0];
			Account account = (Account) args[1];
			int userFrom = (int) args[2];
			int userTo = (int) args[3];
			VAccountManager.get().copyAccountToUser(response, account, userFrom, userTo);
			return 0;
		}
	}

	private class invalidateAuthToken extends Hook {
		@Override
		public String getName() {
			return "invalidateAuthToken";
		}

		@Override
		public Object onHook(Object who, Method method, Object... args) throws Throwable {
			String accountType = (String) args[0];
			String authToken = (String) args[1];
			VAccountManager.get().invalidateAuthToken(accountType, authToken);
			return 0;
		}
	}

	private class peekAuthToken extends Hook {
		@Override
		public String getName() {
			return "peekAuthToken";
		}

		@Override
		public Object onHook(Object who, Method method, Object... args) throws Throwable {
			Account account = (Account) args[0];
			String authTokenType = (String) args[1];
			return VAccountManager.get().peekAuthToken(account, authTokenType);
		}
	}

	private class setAuthToken extends Hook {
		@Override
		public String getName() {
			return "setAuthToken";
		}

		@Override
		public Object onHook(Object who, Method method, Object... args) throws Throwable {
			Account account = (Account) args[0];
			String authTokenType = (String) args[1];
			String authToken = (String) args[2];
			VAccountManager.get().setAuthToken(account, authTokenType, authToken);
			return 0;
		}
	}

	private class setPassword extends Hook {
		@Override
		public String getName() {
			return "setPassword";
		}

		@Override
		public Object onHook(Object who, Method method, Object... args) throws Throwable {
			Account account = (Account) args[0];
			String password = (String) args[1];
			VAccountManager.get().setPassword(account, password);
			return 0;
		}
	}

	private class clearPassword extends Hook {
		@Override
		public String getName() {
			return "clearPassword";
		}

		@Override
		public Object onHook(Object who, Method method, Object... args) throws Throwable {
			Account account = (Account) args[0];
			VAccountManager.get().clearPassword(account);
			return 0;
		}
	}

	private class setUserData extends Hook {
		@Override
		public String getName() {
			return "setUserData";
		}

		@Override
		public Object onHook(Object who, Method method, Object... args) throws Throwable {
			Account account = (Account) args[0];
			String key = (String) args[1];
			String value = (String) args[2];
			VAccountManager.get().setUserData(account, key, value);
			return 0;
		}
	}

	private class updateAppPermission extends Hook {
		@Override
		public String getName() {
			return "updateAppPermission";
		}

		@Override
		public Object onHook(Object who, Method method, Object... args) throws Throwable {
			Account account = (Account) args[0];
			String authTokenType = (String) args[1];
			int uid = (int) args[2];
			boolean val = (boolean) args[3];
			VAccountManager.get().updateAppPermission(account, authTokenType, uid, val);
			return 0;
		}
	}

	private class getAuthToken extends Hook {
		@Override
		public String getName() {
			return "getAuthToken";
		}

		@Override
		public Object onHook(Object who, Method method, Object... args) throws Throwable {
			IAccountManagerResponse response = (IAccountManagerResponse) args[0];
			Account account = (Account) args[1];
			String authTokenType = (String) args[2];
			boolean notifyOnAuthFailure = (boolean) args[3];
			boolean expectActivityLaunch = (boolean) args[4];
			Bundle options = (Bundle) args[5];
			VAccountManager.get().getAuthToken(response, account, authTokenType, notifyOnAuthFailure, expectActivityLaunch, options);
			return 0;
		}
	}

	private class addAccount extends Hook {
		@Override
		public String getName() {
			return "addAccount";
		}

		@Override
		public Object onHook(Object who, Method method, Object... args) throws Throwable {
			IAccountManagerResponse response = (IAccountManagerResponse) args[0];
			String accountType = (String) args[1];
			String authTokenType = (String) args[2];
			String[] requiredFeatures = (String[]) args[3];
			boolean expectActivityLaunch = (boolean) args[4];
			Bundle options = (Bundle) args[5];
			VAccountManager.get().addAccount(response, accountType, authTokenType, requiredFeatures, expectActivityLaunch, options);
			return 0;
		}
	}

	private class addAccountAsUser extends Hook {
		@Override
		public String getName() {
			return "addAccountAsUser";
		}

		@Override
		public Object onHook(Object who, Method method, Object... args) throws Throwable {
			IAccountManagerResponse response = (IAccountManagerResponse) args[0];
			String accountType = (String) args[1];
			String authTokenType = (String) args[2];
			String[] requiredFeatures = (String[]) args[3];
			boolean expectActivityLaunch = (boolean) args[4];
			Bundle options = (Bundle) args[5];
			int userId = (int) args[6];
			VAccountManager.get().addAccountAsUser(response, accountType, authTokenType, requiredFeatures, expectActivityLaunch, options, userId);
			return 0;
		}
	}

	private class updateCredentials extends Hook {
		@Override
		public String getName() {
			return "updateCredentials";
		}

		@Override
		public Object onHook(Object who, Method method, Object... args) throws Throwable {
			IAccountManagerResponse response = (IAccountManagerResponse) args[0];
			Account account = (Account) args[1];
			String authTokenType = (String) args[2];
			boolean expectActivityLaunch = (boolean) args[3];
			Bundle options = (Bundle) args[4];
			VAccountManager.get().updateCredentials(response, account, authTokenType, expectActivityLaunch, options);
			return 0;
		}
	}

	private class editProperties extends Hook {
		@Override
		public String getName() {
			return "editProperties";
		}

		@Override
		public Object onHook(Object who, Method method, Object... args) throws Throwable {
			IAccountManagerResponse response = (IAccountManagerResponse) args[0];
			String authTokenType = (String) args[1];
			boolean expectActivityLaunch = (boolean) args[2];
			VAccountManager.get().editProperties(response, authTokenType, expectActivityLaunch);
			return 0;
		}
	}

	private class confirmCredentialsAsUser extends Hook {
		@Override
		public String getName() {
			return "confirmCredentialsAsUser";
		}

		@Override
		public Object onHook(Object who, Method method, Object... args) throws Throwable {
			IAccountManagerResponse response = (IAccountManagerResponse) args[0];
			Account account = (Account) args[1];
			Bundle options = (Bundle) args[2];
			boolean expectActivityLaunch = (boolean) args[3];
			int userId = (int) args[4];
			VAccountManager.get().confirmCredentialsAsUser(response, account, options, expectActivityLaunch, userId);
			return 0;

		}
	}

	private class accountAuthenticated extends Hook {
		@Override
		public String getName() {
			return "accountAuthenticated";
		}

		@Override
		public Object onHook(Object who, Method method, Object... args) throws Throwable {
			Account account = (Account) args[0];
			return VAccountManager.get().accountAuthenticated(account);
		}
	}

	private class getAuthTokenLabel extends Hook {
		@Override
		public String getName() {
			return "getAuthTokenLabel";
		}

		@Override
		public Object onHook(Object who, Method method, Object... args) throws Throwable {
			IAccountManagerResponse response = (IAccountManagerResponse) args[0];
			String accountType = (String) args[1];
			String authTokenType = (String) args[2];
			VAccountManager.get().getAuthTokenLabel(response, accountType, authTokenType);
			return 0;
		}
	}

	private class addSharedAccountAsUser extends Hook {
		@Override
		public String getName() {
			return "addSharedAccountAsUser";
		}

		@Override
		public Object onHook(Object who, Method method, Object... args) throws Throwable {
			Account account = (Account) args[0];
			int userId = (int) args[1];
			return VAccountManager.get().addSharedAccountAsUser(account, userId);
		}
	}

	private class getSharedAccountsAsUser extends Hook {
		@Override
		public String getName() {
			return "getSharedAccountsAsUser";
		}

		@Override
		public Object onHook(Object who, Method method, Object... args) throws Throwable {
			int userId = (int) args[0];
			return VAccountManager.get().getSharedAccountsAsUser(userId);
		}
	}

	private class removeSharedAccountAsUser extends Hook {
		@Override
		public String getName() {
			return "removeSharedAccountAsUser";
		}

		@Override
		public Object onHook(Object who, Method method, Object... args) throws Throwable {
			Account account = (Account) args[0];
			int userId = (int) args[1];
			return VAccountManager.get().removeSharedAccountAsUser(account, userId);
		}
	}

	private class renameAccount extends Hook {
		@Override
		public String getName() {
			return "renameAccount";
		}

		@Override
		public Object onHook(Object who, Method method, Object... args) throws Throwable {
			IAccountManagerResponse response = (IAccountManagerResponse) args[0];
			Account accountToRename = (Account) args[1];
			String newName = (String) args[2];
			VAccountManager.get().renameAccount(response, accountToRename, newName);
			return 0;
		}
	}

	private class getPreviousName extends Hook {
		@Override
		public String getName() {
			return "getPreviousName";
		}

		@Override
		public Object onHook(Object who, Method method, Object... args) throws Throwable {
			Account account = (Account) args[0];
			return VAccountManager.get().getPreviousName(account);
		}
	}

	private class renameSharedAccountAsUser extends Hook {
		@Override
		public String getName() {
			return "renameSharedAccountAsUser";
		}

		@Override
		public Object onHook(Object who, Method method, Object... args) throws Throwable {
			Account accountToRename = (Account) args[0];
			String newName = (String) args[1];
			int userId = (int) args[2];
			return VAccountManager.get().renameSharedAccountAsUser(accountToRename, newName, userId);
		}
	}
}
