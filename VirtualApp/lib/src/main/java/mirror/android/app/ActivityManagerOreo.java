package mirror.android.app;

import android.os.IInterface;

import mirror.RefClass;
import mirror.RefStaticMethod;
import mirror.RefStaticObject;

/**
 * @author Lody
 */

public class ActivityManagerOreo {

    public static Class<?> TYPE = RefClass.load(ActivityManagerOreo.class, "android.app.ActivityManager");

    public static RefStaticMethod<IInterface> getService;
    public static RefStaticObject<Object> IActivityManagerSingleton;

}
