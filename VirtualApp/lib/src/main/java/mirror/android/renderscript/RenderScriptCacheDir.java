package mirror.android.renderscript;


import java.io.File;

import mirror.MethodParams;
import mirror.RefClass;
import mirror.RefStaticMethod;

public class RenderScriptCacheDir {
    public static Class<?> TYPE = RefClass.load(RenderScriptCacheDir.class, "android.renderscript.RenderScriptCacheDir");
    @MethodParams({File.class})
    public static RefStaticMethod<Void> setupDiskCache;
}