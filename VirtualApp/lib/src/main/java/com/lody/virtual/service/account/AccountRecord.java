package com.lody.virtual.service.account;

import android.accounts.Account;

import java.util.Map;

/**
 * @author Lody
 */

public class AccountRecord {
    String name;
    String type;
    Map<String, String> extra;

    public AccountRecord(String name, String type, Map<String, String> extra) {
        this.name = name;
        this.type = type;
        this.extra = extra;
    }

    public AccountRecord(Account account, Map<String, String> extra) {
        this(account.name, account.type, extra);
    }

}
