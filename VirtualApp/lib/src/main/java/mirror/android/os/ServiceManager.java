package mirror.android.os;

import android.os.IBinder;

import mirror.ClassDef;
import mirror.MethodInfo;
import mirror.StaticFieldDef;
import mirror.StaticMethodDef;

public class ServiceManager {
    public static Class<?> Class = ClassDef.init(ServiceManager.class, "android.os.ServiceManager");
    @MethodInfo({String.class, IBinder.class})
    public static StaticMethodDef addService;
    public static StaticMethodDef checkService;
    public static StaticMethodDef getIServiceManager;
    public static StaticMethodDef getService;
    public static StaticMethodDef listServices;
    public static StaticFieldDef sCache;
}
