package mirror.android.app;


import android.os.IInterface;

import mirror.ClassDef;
import mirror.StaticFieldDef;
import mirror.StaticMethodDef;

public class ActivityManagerNative {
    public static Class<?> Class = ClassDef.init(ActivityManagerNative.class, "android.app.ActivityManagerNative");
    public static StaticFieldDef<Object> gDefault;
    public static StaticMethodDef<IInterface> getDefault;
}
