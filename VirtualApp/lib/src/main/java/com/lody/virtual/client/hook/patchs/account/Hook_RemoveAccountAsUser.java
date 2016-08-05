package com.lody.virtual.client.hook.patchs.account;

import android.accounts.Account;
import android.accounts.IAccountManagerResponse;

/**
 * @author Lody
 *
 * @see android.accounts.IAccountManager#removeAccountAsUser(IAccountManagerResponse,
 *      Account, boolean, int)
 *
 */

public class Hook_RemoveAccountAsUser extends Hook_RemoveAccount {

	@Override
	public String getName() {
		return "removeAccountAsUser";
	}

}
