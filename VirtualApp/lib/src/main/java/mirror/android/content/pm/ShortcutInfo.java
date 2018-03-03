package mirror.android.content.pm;

import android.content.Intent;
import android.os.PersistableBundle;

import mirror.RefClass;
import mirror.RefObject;

/**
 * author: weishu on 18/3/3.
 */

public class ShortcutInfo {
    public static Class<?> TYPE = RefClass.load(ShortcutInfo.class, "android.content.pm.ShortcutInfo");
    public static RefObject<String> mPackageName;
    public static RefObject<Intent[]> mIntents;
    public static RefObject<PersistableBundle[]> mIntentPersistableExtrases;
}
