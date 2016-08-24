package mirror.android.app;

import java.io.File;

import mirror.ClassDef;
import mirror.FieldDef;

public class ContextImplKitkat {
    public static Class<?> Class = ClassDef.init(ContextImplKitkat.class, "android.app.ContextImpl");
    public static FieldDef<File[]> mExternalCacheDirs;
    public static FieldDef<File[]> mExternalFilesDirs;
    public static FieldDef<String> mOpPackageName;
}
