package mirror.android.view;

import android.os.IInterface;

import mirror.ClassDef;
import mirror.StaticFieldDef;

public class Display {
    public static Class<?> Class = ClassDef.init(Display.class, android.view.Display.class);
    public static StaticFieldDef<IInterface> sWindowManager;
}
