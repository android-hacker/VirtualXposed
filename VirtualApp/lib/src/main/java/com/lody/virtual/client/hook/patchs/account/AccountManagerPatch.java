package com.lody.virtual.client.hook.patchs.account;

import android.accounts.IAccountManager;
import android.content.Context;
import android.os.ServiceManager;

import com.lody.virtual.client.hook.base.Patch;
import com.lody.virtual.client.hook.base.PatchObject;
import com.lody.virtual.client.hook.binders.HookAccountBinder;

/**
 * @author Lody
 */
@Patch({
		Hook_AddAccountExplicitly.class,
		Hook_RemoveAccount.class,
		Hook_InvalidateAuthToken.class,
		Hook_PeekAuthToken.class,
		Hook_SetPassword.class,
		Hook_ClearPassword.class,
		Hook_SetUserData.class,
		Hook_SetAuthToken.class,
		Hook_GetAccounts.class,
		Hook_GetAccountsAsUser.class,
		Hook_GetAccountsByType.class,
		Hook_GetAccountTypeForPackage.class,
		Hook_HasFeatures.class,
		Hook_GetAuthToken.class,
		Hook_GetUserData.class,
		Hook_GetPassword.class,
		Hook_GetAccountsByFeatures.class,
		Hook_EditProperties.class,
		Hook_ConfirmCredentials.class,
		Hook_ConfirmCredentialsAsUser.class,
		Hook_UpdateCredentials.class,

})
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
	public boolean isEnvBad() {
		return ServiceManager.getService(Context.ACCOUNT_SERVICE) != getHookObject();
	}
}
