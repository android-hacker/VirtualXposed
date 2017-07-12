package mirror.android.content;

import android.accounts.Account;

import mirror.MethodParams;
import mirror.RefClass;
import mirror.RefConstructor;

public class SyncInfo {
    public static Class<?> TYPE = RefClass.load(SyncInfo.class, android.content.SyncInfo.class);
    @MethodParams({int.class, Account.class, String.class, long.class})
    public static RefConstructor<android.content.SyncInfo> ctor;
}