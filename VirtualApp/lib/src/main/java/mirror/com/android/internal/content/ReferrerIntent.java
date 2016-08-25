package mirror.com.android.internal.content;

import android.content.Intent;

import mirror.ClassDef;
import mirror.CtorDef;
import mirror.MethodInfo;

/**
 * @author Lody
 */

public class ReferrerIntent {
    public static Class<?> Class = ClassDef.init(ReferrerIntent.class, "com.android.internal.content.ReferrerIntent");
    @MethodInfo({Intent.class, String.class})
    public static CtorDef<Intent> ctor;
}
