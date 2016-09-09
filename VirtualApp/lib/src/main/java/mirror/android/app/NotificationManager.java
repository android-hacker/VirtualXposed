package mirror.android.app;

import android.os.IInterface;

import mirror.RefClass;
import mirror.RefStaticObject;
import mirror.RefStaticMethod;

public class NotificationManager {
    public static Class<?> TYPE = RefClass.load(NotificationManager.class, "android.app.NotificationManager");

    public static RefStaticMethod<IInterface> getService;
    public static RefStaticObject<IInterface> sService;

}
