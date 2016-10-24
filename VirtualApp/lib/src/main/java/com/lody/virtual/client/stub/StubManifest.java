package com.lody.virtual.client.stub;

import java.util.Locale;

/**
 * @author Lody
 */

public class StubManifest {
    public static String STUB_ACTIVITY = StubActivity.class.getName();
    public static String STUB_DIALOG = StubDialog.class.getName();
    public static String STUB_CP = StubContentProvider.class.getName();
    public static final String STUB_DEF_AUTHORITY = "virtual_stub_";
    public static String STUB_CP_AUTHORITY = "virtual_stub_";
    public static int STUB_COUNT = 50;

    public static String getStubActivityName(int index) {
        return String.format(Locale.ENGLISH, "%s$C%d", STUB_ACTIVITY, index);
    }

    public static String getStubDialogName(int index) {
        return String.format(Locale.ENGLISH, "%s$C%d", STUB_DIALOG, index);
    }

    public static String getStubCP(int index) {
        return String.format(Locale.ENGLISH, "%s$C%d", STUB_CP, index);
    }

    public static String getStubAuthority(int index) {
        return String.format(Locale.ENGLISH, "%s%d", STUB_CP_AUTHORITY, index);
    }
}
