package mirror.android.content.pm;

import android.content.pm.ApplicationInfo;
import mirror.RefClass;
import mirror.RefObject;

public class ApplicationInfoL {
    public static Class<?> TYPE = RefClass.load(ApplicationInfoL.class, ApplicationInfo.class);
    public static RefObject<String> primaryCpuAbi;
    public static RefObject<String> scanPublicSourceDir;
    public static RefObject<String> scanSourceDir;
    public static RefObject<String[]> splitPublicSourceDirs;
    public static RefObject<String[]> splitSourceDirs;
}
