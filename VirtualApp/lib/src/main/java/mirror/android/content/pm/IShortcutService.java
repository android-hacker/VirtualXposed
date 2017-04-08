package mirror.android.content.pm;

import android.os.IInterface;

import mirror.RefClass;
import mirror.RefStaticMethod;

/**
 * @author Lody
 */

public class IShortcutService {

    public static final class Stub {
        public static Class<?> TYPE = RefClass.load(IShortcutService.class, "android.content.pm.IShortcutService$Stub");
        public static RefStaticMethod<IInterface> asInterface;
    }
}
