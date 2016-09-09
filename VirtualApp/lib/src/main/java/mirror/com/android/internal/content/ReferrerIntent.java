package mirror.com.android.internal.content;

import android.content.Intent;

import mirror.RefClass;
import mirror.RefConstructor;
import mirror.MethodParams;

/**
 * @author Lody
 */

public class ReferrerIntent {
    public static Class<?> TYPE = RefClass.load(ReferrerIntent.class, "com.android.internal.content.ReferrerIntent");
    @MethodParams({Intent.class, String.class})
    public static RefConstructor<Intent> ctor;
}
