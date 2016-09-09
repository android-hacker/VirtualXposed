package mirror.android.app;

import java.io.File;

import mirror.RefClass;
import mirror.RefObject;

public class ContextImplICS {
    public static Class<?> TYPE = RefClass.load(ContextImplICS.class, "android.app.ContextImpl");
    public static RefObject<File> mExternalCacheDir;
    public static RefObject<File> mExternalFilesDir;
}
