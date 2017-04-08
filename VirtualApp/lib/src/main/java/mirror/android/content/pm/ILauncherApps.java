package mirror.android.content.pm;

import android.os.IInterface;

import mirror.RefClass;
import mirror.RefStaticMethod;

/**
 * @author Lody
 */
public class ILauncherApps {

    public static final class Stub {
        public static Class<?> TYPE = RefClass.load(ILauncherApps.class, "android.content.pm.ILauncherApps$Stub");
        public static RefStaticMethod<IInterface> asInterface;
    }

}
