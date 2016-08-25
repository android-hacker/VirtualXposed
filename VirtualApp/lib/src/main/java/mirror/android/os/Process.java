package mirror.android.os;

import mirror.ClassDef;
import mirror.MethodInfo;
import mirror.StaticMethodDef;

public class Process {
    public static Class<?> Class = ClassDef.init(Process.class, android.os.Process.class);
    @MethodInfo({String.class})
    public static StaticMethodDef<Void> setArgV0;
}
