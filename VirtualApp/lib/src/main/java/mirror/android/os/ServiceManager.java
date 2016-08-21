package mirror.android.os;

import android.os.IBinder;
import android.os.IInterface;

import java.util.Map;

import mirror.ClassDef;
import mirror.MethodInfo;
import mirror.StaticFieldDef;
import mirror.StaticMethodDef;

public class ServiceManager {
    public static Class<?> Class = ClassDef.init(ServiceManager.class, "android.os.ServiceManager");
    @MethodInfo({String.class, IBinder.class})
    public static StaticMethodDef<Void> addService;
    public static StaticMethodDef<IBinder> checkService;
    public static StaticMethodDef<IInterface> getIServiceManager;
    public static StaticMethodDef<IBinder> getService;
    public static StaticMethodDef<String[]> listServices;
    public static StaticFieldDef<Map<String, IBinder>> sCache;
}
