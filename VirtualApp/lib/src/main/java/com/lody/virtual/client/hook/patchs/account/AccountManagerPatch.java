package com.lody.virtual.client.hook.patchs.account;

import android.accounts.Account;
import android.accounts.IAccountManagerResponse;
import android.content.Context;
import android.os.Bundle;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.hook.base.PatchDelegate;
import com.lody.virtual.client.hook.binders.AccountBinderDelegate;
import com.lody.virtual.client.ipc.VAccountManager;

import java.lang.reflect.Method;

import mirror.android.os.ServiceManager;

/**
 * @author Lody
 */
public class AccountManagerPatch extends PatchDelegate<AccountBinderDelegate> {

	private static VAccountManager Mgr = VAccountManager.get();

	@Override
	protected AccountBinderDelegate createHookDelegate() {
		return new AccountBinderDelegate();
	}

	@Override
	public void inject() throws Throwable {
		getHookDelegate().replaceService(Context.ACCOUNT_SERVICE);
	}

	@Override
	protected void onBindHooks() {
		super.onBindHooks();
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
		return ServiceManager.getService.call(Context.ACCOUNT_SERVICE) != getHookDelegate();
	}


	private static class getPassword extends Hook {
		@Override
		public String getName() {
			return "getPassword";
		}

		@Override
		public Object call(Object who, Method method, Object... args) throws Throwable {
			Account account = (Account) args[0];
			return Mgr.getPassword(account);
		}
	}

	private static class getUserData extends Hook {
		@Override
		public String getName() {
			return "getUserData";
		}

		@Override
		public Object call(Object who, Method method, Object... args) throws Throwable {
			Account account = (Account) args[0];
			String key = (String) args[1];
			return Mgr.getUserData(account, key);
		}
	}

	private static class getAuthenticatorTypes extends Hook {
		@Override
		public String getName() {
			return "getAuthenticatorTypes";
		}

		@Override
		public Object call(Object who, Method method, Object... args) throws Throwable {
			return Mgr.getAuthenticatorTypes();
		}
	}

	private static class getAccounts extends Hook {
		@Override
		public String getName() {
			return "getAccounts";
		}

		@Override
		public Object call(Object who, Method method, Object... args) throws Throwable {
			String accountType = (String) args[0];
			return Mgr.getAccounts(accountType);
		}
	}

	private static class getAccountsForPackage extends Hook {
		@Override
		public String getName() {
			return "getAccountsForPackage";
		}

		@Override
		public Object call(Object who, Method method, Object... args) throws Throwable {
			String packageName = (String) args[0];
			return Mgr.getAccounts(null);
		}
	}

	private static class getAccountsByTypeForPackage extends Hook {
		@Override
		public String getName() {
			return "getAccountsByTypeForPackage";
		}

		@Override
		public Object call(Object who, Method method, Object... args) throws Throwable {
			String type = (String) args[0];
			String packageName = (String) args[1];
			return Mgr.getAccounts(type);
		}
	}

	private static class getAccountsAsUser extends Hook {
		@Override
		public String getName() {
			return "getAccountsAsUser";
		}

		@Override
		public Object call(Object who, Method method, Object... args) throws Throwable {
			String accountType = (String) args[0];
			return Mgr.getAccounts(accountType);
		}
	}

	private static class hasFeatures extends Hook {
		@Override
		public String getName() {
			return "hasFeatures";
		}

		@Override
		public Object call(Object who, Method method, Object... args) throws Throwable {
			IAccountManagerResponse response = (IAccountManagerResponse) args[0];
			Account account = (Account) args[1];
			String[] features = (String[]) args[2];
			Mgr.hasFeatures(response, account, features);
			return 0;
		}
	}

	private static class getAccountsByFeatures extends Hook {
		@Override
		public String getName() {
			return "getAccountsByFeatures";
		}

		@Override
		public Object call(Object who, Method method, Object... args) throws Throwable {
			IAccountManagerResponse response = (IAccountManagerResponse) args[0];
			String accountType = (String) args[1];
			String[] features = (String[]) args[2];
			Mgr.getAccountsByFeatures(response, accountType, features);
			return 0;
		}
	}

	private static class addAccountExplicitly extends Hook {
		@Override
		public String getName() {
			return "addAccountExplicitly";
		}

		@Override
		public Object call(Object who, Method method, Object... args) throws Throwable {
			Account account = (Account) args[0];
			String password = (String) args[1];
			Bundle extras = (Bundle) args[2];
			return Mgr.addAccountExplicitly(account, password, extras);
		}
	}

	private static class removeAccount extends Hook {
		@Override
		public String getName() {
			return "removeAccount";
		}

		@Override
		public Object call(Object who, Method method, Object... args) throws Throwable {
			IAccountManagerResponse response = (IAccountManagerResponse) args[0];
			Account account = (Account) args[1];
			boolean expectActivityLaunch = (boolean) args[2];
			Mgr.removeAccount(response, account, expectActivityLaunch);
			return 0;
		}
	}

	private static class removeAccountAsUser extends Hook {
		@Override
		public String getName() {
			return "removeAccountAsUser";
		}

		@Override
		public Object call(Object who, Method method, Object... args) throws Throwable {
			IAccountManagerResponse response = (IAccountManagerResponse) args[0];
			Account account = (Account) args[1];
			boolean expectActivityLaunch = (boolean) args[2];
			Mgr.removeAccount(response, account, expectActivityLaunch);
			return 0;
		}
	}

	private static class removeAccountExplicitly extends Hook {
		@Override
		public String getName() {
			return "removeAccountExplicitly";
		}

		@Override
		public Object call(Object who, Method method, Object... args) throws Throwable {
			Account account = (Account) args[0];
			return Mgr.removeAccountExplicitly(account);
		}
	}

	private static class copyAccountToUser extends Hook {
		@Override
		public String getName() {
			return "copyAccountToUser";
		}

		@Override
		public Object call(Object who, Method method, Object... args) throws Throwable {
			IAccountManagerResponse response = (IAccountManagerResponse) args[0];
			Account account = (Account) args[1];
			int userFrom = (int) args[2];
			int userTo = (int) args[3];
			method.invoke(who, args);
			return 0;
		}
	}

	private static class invalidateAuthToken extends Hook {
		@Override
		public String getName() {
			return "invalidateAuthToken";
		}

		@Override
		public Object call(Object who, Method method, Object... args) throws Throwable {
			String accountType = (String) args[0];
			String authToken = (String) args[1];
			Mgr.invalidateAuthToken(accountType, authToken);
			return 0;
		}
	}

	private static class peekAuthToken extends Hook {
		@Override
		public String getName() {
			return "peekAuthToken";
		}

		@Override
		public Object call(Object who, Method method, Object... args) throws Throwable {
			Account account = (Account) args[0];
			String authTokenType = (String) args[1];
			return Mgr.peekAuthToken(account, authTokenType);
		}
	}

	private static class setAuthToken extends Hook {
		@Override
		public String getName() {
			return "setAuthToken";
		}

		@Override
		public Object call(Object who, Method method, Object... args) throws Throwable {
			Account account = (Account) args[0];
			String authTokenType = (String) args[1];
			String authToken = (String) args[2];
			Mgr.setAuthToken(account, authTokenType, authToken);
			return 0;
		}
	}

	private static class setPassword extends Hook {
		@Override
		public String getName() {
			return "setPassword";
		}

		@Override
		public Object call(Object who, Method method, Object... args) throws Throwable {
			Account account = (Account) args[0];
			String password = (String) args[1];
			Mgr.setPassword(account, password);
			return 0;
		}
	}

	private static class clearPassword extends Hook {
		@Override
		public String getName() {
			return "clearPassword";
		}

		@Override
		public Object call(Object who, Method method, Object... args) throws Throwable {
			Account account = (Account) args[0];
			Mgr.clearPassword(account);
			return 0;
		}
	}

	private static class setUserData extends Hook {
		@Override
		public String getName() {
			return "setUserData";
		}

		@Override
		public Object call(Object who, Method method, Object... args) throws Throwable {
			Account account = (Account) args[0];
			String key = (String) args[1];
			String value = (String) args[2];
			Mgr.setUserData(account, key, value);
			return 0;
		}
	}

	private static class updateAppPermission extends Hook {
		@Override
		public String getName() {
			return "updateAppPermission";
		}

		@Override
		public Object call(Object who, Method method, Object... args) throws Throwable {
			Account account = (Account) args[0];
			String authTokenType = (String) args[1];
			int uid = (int) args[2];
			boolean val = (boolean) args[3];
			method.invoke(who, args);
			return 0;
		}
	}

	private static class getAuthToken extends Hook {
		@Override
		public String getName() {
			return "getAuthToken";
		}

		@Override
		public Object call(Object who, Method method, Object... args) throws Throwable {
			IAccountManagerResponse response = (IAccountManagerResponse) args[0];
			Account account = (Account) args[1];
			String authTokenType = (String) args[2];
			boolean notifyOnAuthFailure = (boolean) args[3];
			boolean expectActivityLaunch = (boolean) args[4];
			Bundle options = (Bundle) args[5];
			Mgr.getAuthToken(response, account, authTokenType, notifyOnAuthFailure, expectActivityLaunch, options);
			return 0;
		}
	}

	private static class addAccount extends Hook {
		@Override
		public String getName() {
			return "addAccount";
		}

		@Override
		public Object call(Object who, Method method, Object... args) throws Throwable {
			IAccountManagerResponse response = (IAccountManagerResponse) args[0];
			String accountType = (String) args[1];
			String authTokenType = (String) args[2];
			String[] requiredFeatures = (String[]) args[3];
			boolean expectActivityLaunch = (boolean) args[4];
			Bundle options = (Bundle) args[5];
			Mgr.addAccount(response, accountType, authTokenType, requiredFeatures, expectActivityLaunch, options);
			return 0;
		}
	}

	private static class addAccountAsUser extends Hook {
		@Override
		public String getName() {
			return "addAccountAsUser";
		}

		@Override
		public Object call(Object who, Method method, Object... args) throws Throwable {
			IAccountManagerResponse response = (IAccountManagerResponse) args[0];
			String accountType = (String) args[1];
			String authTokenType = (String) args[2];
			String[] requiredFeatures = (String[]) args[3];
			boolean expectActivityLaunch = (boolean) args[4];
			Bundle options = (Bundle) args[5];
			Mgr.addAccount(response, accountType, authTokenType, requiredFeatures, expectActivityLaunch, options);
			return 0;
		}
	}

	private static class updateCredentials extends Hook {
		@Override
		public String getName() {
			return "updateCredentials";
		}

		@Override
		public Object call(Object who, Method method, Object... args) throws Throwable {
			IAccountManagerResponse response = (IAccountManagerResponse) args[0];
			Account account = (Account) args[1];
			String authTokenType = (String) args[2];
			boolean expectActivityLaunch = (boolean) args[3];
			Bundle options = (Bundle) args[4];
			Mgr.updateCredentials(response, account, authTokenType, expectActivityLaunch, options);
			return 0;
		}
	}

	private static class editProperties extends Hook {
		@Override
		public String getName() {
			return "editProperties";
		}

		@Override
		public Object call(Object who, Method method, Object... args) throws Throwable {
			IAccountManagerResponse response = (IAccountManagerResponse) args[0];
			String authTokenType = (String) args[1];
			boolean expectActivityLaunch = (boolean) args[2];
			Mgr.editProperties(response, authTokenType, expectActivityLaunch);
			return 0;
		}
	}

	private static class confirmCredentialsAsUser extends Hook {
		@Override
		public String getName() {
			return "confirmCredentialsAsUser";
		}

		@Override
		public Object call(Object who, Method method, Object... args) throws Throwable {
			IAccountManagerResponse response = (IAccountManagerResponse) args[0];
			Account account = (Account) args[1];
			Bundle options = (Bundle) args[2];
			boolean expectActivityLaunch = (boolean) args[3];
			Mgr.confirmCredentials(response, account, options, expectActivityLaunch);
			return 0;

		}
	}

	private static class accountAuthenticated extends Hook {
		@Override
		public String getName() {
			return "accountAuthenticated";
		}

		@Override
		public Object call(Object who, Method method, Object... args) throws Throwable {
			Account account = (Account) args[0];
			return Mgr.accountAuthenticated(account);
		}
	}

	private static class getAuthTokenLabel extends Hook {
		@Override
		public String getName() {
			return "getAuthTokenLabel";
		}

		@Override
		public Object call(Object who, Method method, Object... args) throws Throwable {
			IAccountManagerResponse response = (IAccountManagerResponse) args[0];
			String accountType = (String) args[1];
			String authTokenType = (String) args[2];
			Mgr.getAuthTokenLabel(response, accountType, authTokenType);
			return 0;
		}
	}

	private static class addSharedAccountAsUser extends Hook {
		@Override
		public String getName() {
			return "addSharedAccountAsUser";
		}

		@Override
		public Object call(Object who, Method method, Object... args) throws Throwable {
			Account account = (Account) args[0];
			int userId = (int) args[1];
			return method.invoke(who, args);
		}
	}

	private static class getSharedAccountsAsUser extends Hook {
		@Override
		public String getName() {
			return "getSharedAccountsAsUser";
		}

		@Override
		public Object call(Object who, Method method, Object... args) throws Throwable {
			int userId = (int) args[0];
			return method.invoke(who, args);
		}
	}

	private static class removeSharedAccountAsUser extends Hook {
		@Override
		public String getName() {
			return "removeSharedAccountAsUser";
		}

		@Override
		public Object call(Object who, Method method, Object... args) throws Throwable {
			Account account = (Account) args[0];
			int userId = (int) args[1];
			return method.invoke(who, args);
		}
	}

	private static class renameAccount extends Hook {
		@Override
		public String getName() {
			return "renameAccount";
		}

		@Override
		public Object call(Object who, Method method, Object... args) throws Throwable {
			IAccountManagerResponse response = (IAccountManagerResponse) args[0];
			Account accountToRename = (Account) args[1];
			String newName = (String) args[2];
			Mgr.renameAccount(response, accountToRename, newName);
			return 0;
		}
	}

	private static class getPreviousName extends Hook {
		@Override
		public String getName() {
			return "getPreviousName";
		}

		@Override
		public Object call(Object who, Method method, Object... args) throws Throwable {
			Account account = (Account) args[0];
			return Mgr.getPreviousName(account);
		}
	}

	private static class renameSharedAccountAsUser extends Hook {
		@Override
		public String getName() {
			return "renameSharedAccountAsUser";
		}

		@Override
		public Object call(Object who, Method method, Object... args) throws Throwable {
			Account accountToRename = (Account) args[0];
			String newName = (String) args[1];
			int userId = (int) args[2];
			return method.invoke(who, args);
		}
	}
}
