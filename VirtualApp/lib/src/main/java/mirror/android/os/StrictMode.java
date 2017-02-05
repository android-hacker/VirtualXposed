package mirror.android.os;


import mirror.RefClass;
import mirror.RefStaticInt;

public class StrictMode {
    public static Class<?> TYPE = RefClass.load(StrictMode.class, "android.os.StrictMode");
    public static RefStaticInt sVmPolicyMask;
}