package mirror.android.app;

import mirror.RefClass;
import mirror.RefStaticObject;

/**
 * @author Lody
 */

public class ActivityManagerOreo {

    public static Class<?> TYPE = RefClass.load(ActivityManagerOreo.class, "android.app.ActivityManager");

    public static RefStaticObject<Object> IActivityManagerSingleton;

}
