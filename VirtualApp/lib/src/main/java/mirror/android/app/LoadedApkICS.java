package mirror.android.app;


import mirror.RefClass;
import mirror.RefObject;

public class LoadedApkICS {
    public static Class<?> Class = RefClass.load(LoadedApkICS.class, "android.app.LoadedApk");
    public static RefObject<Object> mCompatibilityInfo;
}