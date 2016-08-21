package mirror.android.util;


import mirror.ClassDef;
import mirror.FieldDef;
import mirror.MethodDef;

public class Singleton {
    public static Class<?> Class = ClassDef.init(Singleton.class, "android.util.Singleton");
    public static MethodDef<Object> get;
    public static FieldDef<Object> mInstance;
}
