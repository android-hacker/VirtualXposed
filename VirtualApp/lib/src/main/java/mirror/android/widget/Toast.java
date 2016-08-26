package mirror.android.widget;

import android.os.IInterface;

import mirror.ClassDef;
import mirror.StaticFieldDef;

public class Toast {
    public static Class<?> Class = ClassDef.init(Toast.class, android.widget.Toast.class);

    public static StaticFieldDef<IInterface> sService;

}
