package com.lody.virtual.client.hook.patchs.account;

import android.accounts.Account;

/**
 * @author Lody
 */

public class AccountUtils {

    public static final String ACCOUNT_TYPE = "com.lody.virtualapp.auth";

    public static void replaceAccount(Object[] args) {
        for (int N = 0; N < args.length; N++) {
            Object arg = args[N];
            if (arg instanceof Account) {
                args[N] = fixAccount((Account) arg);
            }
        }
    }

    public static Account fixAccount(Account account) {
        if (account != null) {
            if (account.type.equals(ACCOUNT_TYPE)) {
                return account;
            }
            String name = account.type + "|" + account.name;
            return new Account(name, ACCOUNT_TYPE);
        }
        return null;
    }

    public static Account restoreAccount(Account account) {
        if (account != null) {
            if (account.type.equals(ACCOUNT_TYPE)) {
                String name = account.name.replaceFirst(ACCOUNT_TYPE + "|", "");
                return new Account(name, ACCOUNT_TYPE);
            }
        }
        return null;
    }
}
