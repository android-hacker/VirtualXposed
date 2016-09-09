package mirror.com.android.internal.view.inputmethod;

import android.os.IInterface;

import mirror.RefClass;
import mirror.RefObject;

public class InputMethodManager {
    public static Class<?> TYPE = RefClass.load(InputMethodManager.class, android.view.inputmethod.InputMethodManager.class);
    public static RefObject<IInterface> mService;
}
