package mirror.android.location;

import android.location.Location;
import android.os.IBinder;
import android.os.IInterface;

import mirror.MethodParams;
import mirror.RefClass;
import mirror.RefMethod;
import mirror.RefStaticMethod;


/**
 * @author legency
 */
public class ILocationListener {
    public static Class<?> TYPE = RefClass.load(ILocationListener.class, "android.location.ILocationListener");
    public static class Stub {
        public static Class<?> TYPE = RefClass.load(Stub.class, "android.location.ILocationListener$Stub");
        @MethodParams({IBinder.class})
        public static RefStaticMethod<IInterface> asInterface;
    }
    @MethodParams(Location.class)
    public static RefMethod<Void> onLocationChanged;


}
