package mirror.android.view;


import java.io.File;

import mirror.MethodParams;
import mirror.RefClass;
import mirror.RefStaticMethod;

public class ThreadedRenderer {
    public static Class<?> TYPE = RefClass.load(ThreadedRenderer.class, "android.view.ThreadedRenderer");
    @MethodParams({File.class})
    public static RefStaticMethod<Void> setupDiskCache;
}