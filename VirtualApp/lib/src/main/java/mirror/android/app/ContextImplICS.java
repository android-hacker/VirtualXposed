package mirror.android.app;

import java.io.File;

import mirror.ClassDef;
import mirror.FieldDef;

public class ContextImplICS {
    public static Class<?> Class = ClassDef.init(ContextImplICS.class, "android.app.ContextImpl");
    public static FieldDef<File> mExternalCacheDir;
    public static FieldDef<File> mExternalFilesDir;
}
