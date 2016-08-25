package mirror.android.content.pm;

import android.content.pm.ApplicationInfo;
import mirror.ClassDef;
import mirror.FieldDef;

public class ApplicationInfoL {
    public static Class<?> Class = ClassDef.init(ApplicationInfoL.class, ApplicationInfo.class);
    public static FieldDef<String> primaryCpuAbi;
    public static FieldDef<String> scanPublicSourceDir;
    public static FieldDef<String> scanSourceDir;
    public static FieldDef<String[]> splitPublicSourceDirs;
    public static FieldDef<String[]> splitSourceDirs;
}
