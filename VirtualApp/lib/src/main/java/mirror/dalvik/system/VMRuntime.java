package mirror.dalvik.system;

import mirror.RefClass;
import mirror.RefMethod;
import mirror.MethodParams;
import mirror.RefStaticMethod;

/**
 * @author Lody
 */

public class VMRuntime {
        public static Class<?> TYPE = RefClass.load(VMRuntime.class, "dalvik.system.VMRuntime");
        public static RefStaticMethod<Object> getRuntime;
        @MethodParams({int.class})
        public static RefMethod<Void> setTargetSdkVersion;
        public static RefMethod<Boolean> is64Bit;
        public static RefStaticMethod<String> getCurrentInstructionSet;
}
