package mirror.android.view;

import android.os.IInterface;

import mirror.RefClass;
import mirror.RefStaticObject;

public class Display {
    public static Class<?> TYPE = RefClass.load(Display.class, android.view.Display.class);
    public static RefStaticObject<IInterface> sWindowManager;
}
