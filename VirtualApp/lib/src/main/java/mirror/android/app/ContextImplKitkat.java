package mirror.android.app;

import java.io.File;

import mirror.RefClass;
import mirror.RefObject;

public class ContextImplKitkat {
    public static Class<?> TYPE = RefClass.load(ContextImplKitkat.class, "android.app.ContextImpl");
    public static RefObject<File[]> mExternalCacheDirs;
    public static RefObject<File[]> mExternalFilesDirs;
    public static RefObject<String> mOpPackageName;
}
