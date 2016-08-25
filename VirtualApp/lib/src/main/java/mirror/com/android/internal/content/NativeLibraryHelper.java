package mirror.com.android.internal.content;

import java.io.File;

import mirror.ClassDef;
import mirror.MethodInfo;
import mirror.StaticMethodDef;

/**
 * @author Lody
 */

public class NativeLibraryHelper {
    public static Class<?> Class = ClassDef.init(NativeLibraryHelper.class, "com.android.internal.content.NativeLibraryHelper");

    @MethodInfo({Handle.class, File.class, String.class})
    public static StaticMethodDef<Integer> copyNativeBinaries;

    @MethodInfo({Handle.class, String[].class})
    public static StaticMethodDef<Integer> findSupportedAbi;

    public static class Handle {
        public static Class<?> Class = ClassDef.init(Handle.class, "com.android.internal.content.NativeLibraryHelper$Handle");

        @MethodInfo({File.class})
        public static StaticMethodDef<Object> create;
    }
}
