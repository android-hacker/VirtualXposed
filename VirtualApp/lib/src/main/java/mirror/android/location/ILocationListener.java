package mirror.android.location;

import android.location.Location;
import android.os.IBinder;
import android.os.IInterface;

import mirror.MethodParams;
import mirror.RefClass;
import mirror.RefMethod;


/**
 * Created by legency on 2016/9/1.
 */
public class ILocationListener {
    public static Class<?> Class = RefClass.load(ILocationListener.class, "android.location.ILocationListener");
    public static class Stub {
        public static Class<?> Class = RefClass.load(Stub.class, "android.location.ILocationListener$Stub");
        @MethodParams({IBinder.class})
        public static RefMethod<IInterface> asInterface;
    }
    @MethodParams(Location.class)
    public static RefMethod<Void> onLocationChanged;


}
