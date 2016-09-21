package mirror.android.view;


import java.io.File;

import mirror.MethodParams;
import mirror.RefClass;
import mirror.RefStaticMethod;

public class HardwareRenderer {
    public static Class<?> TYPE = RefClass.load(HardwareRenderer.class, "android.view.HardwareRenderer");
    @MethodParams({File.class})
    public static RefStaticMethod<Void> setupDiskCache;
}