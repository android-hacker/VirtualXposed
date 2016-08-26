package mirror.com.android.internal.policy;

import android.os.IInterface;

import mirror.ClassDef;
import mirror.StaticFieldDef;

public class PhoneWindow {
    public static Class<?> Class;
    public static StaticFieldDef<IInterface> sWindowManager;

    static {
        Class init = ClassDef.init(PhoneWindow.class, "com.android.internal.policy.impl.PhoneWindow$WindowManagerHolder");
        Class = init;
        if (init == null) {
            Class = ClassDef.init(PhoneWindow.class, "com.android.internal.policy.PhoneWindow$WindowManagerHolder");
        }
    }
}