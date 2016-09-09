package mirror.com.android.internal.policy;

import android.os.IInterface;

import mirror.RefClass;
import mirror.RefStaticObject;

public class PhoneWindow {
    public static Class<?> TYPE;
    public static RefStaticObject<IInterface> sWindowManager;

    static {
        TYPE = RefClass.load(PhoneWindow.class, "com.android.internal.policy.impl.PhoneWindow$WindowManagerHolder");
        if (TYPE == null) {
            TYPE = RefClass.load(PhoneWindow.class, "com.android.internal.policy.PhoneWindow$WindowManagerHolder");
        }
    }
}