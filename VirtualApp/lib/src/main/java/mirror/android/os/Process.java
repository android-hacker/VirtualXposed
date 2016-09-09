package mirror.android.os;

import mirror.RefClass;
import mirror.MethodParams;
import mirror.RefStaticMethod;

public class Process {
    public static Class<?> TYPE = RefClass.load(Process.class, android.os.Process.class);
    @MethodParams({String.class})
    public static RefStaticMethod<Void> setArgV0;
}
