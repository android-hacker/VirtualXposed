package mirror.dalvik.system;

import mirror.ClassDef;
import mirror.MethodDef;
import mirror.MethodInfo;
import mirror.StaticMethodDef;

/**
 * @author Lody
 */

public class VMRuntime {
        public static Class<?> Class = ClassDef.init(VMRuntime.class, "dalvik.system.VMRuntime");
        public static StaticMethodDef<Object> getRuntime;
        @MethodInfo({int.class})
        public static MethodDef<Void> setTargetSdkVersion;
        public static MethodDef<Boolean> is64Bit;
}
