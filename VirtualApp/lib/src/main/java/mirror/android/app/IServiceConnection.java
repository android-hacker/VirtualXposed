package mirror.android.app;

import android.content.ComponentName;
import android.os.IBinder;
import android.os.IInterface;

import mirror.MethodParams;
import mirror.RefClass;
import mirror.RefMethod;
import mirror.RefStaticMethod;

public class IServiceConnection {
    public static Class<?> TYPE = RefClass.load(IServiceConnection.class, "android.app.IServiceConnection");

    @MethodParams({ComponentName.class, IBinder.class})
    public static RefMethod<Void> connected;
}
