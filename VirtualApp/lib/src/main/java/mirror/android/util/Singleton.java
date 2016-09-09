package mirror.android.util;


import mirror.RefClass;
import mirror.RefObject;
import mirror.RefMethod;

public class Singleton {
    public static Class<?> TYPE = RefClass.load(Singleton.class, "android.util.Singleton");
    public static RefMethod<Object> get;
    public static RefObject<Object> mInstance;
}
