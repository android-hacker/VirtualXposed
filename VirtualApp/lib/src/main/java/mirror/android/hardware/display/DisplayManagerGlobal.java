package mirror.android.hardware.display;

import android.os.IInterface;

import mirror.RefClass;
import mirror.RefObject;
import mirror.RefStaticMethod;

public class DisplayManagerGlobal {
    public static Class<?> TYPE = RefClass.load(DisplayManagerGlobal.class, "android.hardware.display.DisplayManagerGlobal");
    public static RefStaticMethod<Object> getInstance;
    public static RefObject<IInterface> mDm;
}
