package mirror.android.os;


import mirror.RefClass;
import mirror.RefStaticObject;

public class Build {
    public static Class<?> TYPE = RefClass.load(Build.class, android.os.Build.class);
    public static RefStaticObject<String> DEVICE;
    public static RefStaticObject<String> SERIAL;
}