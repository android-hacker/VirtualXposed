package com.lody.virtual.helper.compat;

/**
 * @author Lody
 */

public class AccountManagerCompat {
	/**
	 * Bundle key used to supply the last time the credentials of the account
	 * were authenticated successfully. Time is specified in milliseconds since
	 * epoch. Associated time is updated on successful authentication of account
	 * on adding account, confirming credentials, or updating credentials.
	 */
	public static final String KEY_LAST_AUTHENTICATED_TIME = "lastAuthenticatedTime";

	/**
	 * Bundle key used for the {@code long} expiration time (in millis from the
	 * unix epoch) of the associated auth token.
	 */
	public static final String KEY_CUSTOM_TOKEN_EXPIRY = "android.accounts.expiry";
}
