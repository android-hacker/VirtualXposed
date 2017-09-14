package mirror.android.app;

import android.content.ComponentName;
import android.os.IBinder;

import mirror.MethodParams;
import mirror.RefClass;
import mirror.RefMethod;

public class IServiceConnectionO {
    public static Class<?> TYPE = RefClass.load(IServiceConnectionO.class, "android.app.IServiceConnection");

    @MethodParams({ComponentName.class, IBinder.class, boolean.class})
    public static RefMethod<Void> connected;
}
