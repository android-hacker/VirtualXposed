package mirror.android.widget;

import android.os.IInterface;

import mirror.RefClass;
import mirror.RefStaticObject;

public class Toast {
    public static Class<?> TYPE = RefClass.load(Toast.class, android.widget.Toast.class);

    public static RefStaticObject<IInterface> sService;

}
