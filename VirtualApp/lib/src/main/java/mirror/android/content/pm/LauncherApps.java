package mirror.android.content.pm;

import android.content.pm.PackageManager;
import android.os.IInterface;

import mirror.RefClass;
import mirror.RefObject;
import mirror.com.android.internal.os.UserManager;

/**
 * @author Lody
 */

public class LauncherApps {

    public static Class<?> TYPE = RefClass.load(LauncherApps.class, "android.content.pm.LauncherApps");

    public static RefObject<PackageManager> mPm;
    public static RefObject<IInterface> mService;
    public static RefObject<UserManager> mUserManager;
}
