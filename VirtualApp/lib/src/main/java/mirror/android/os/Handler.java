package mirror.android.os;


import mirror.RefClass;
import mirror.RefObject;

public class Handler {
    public static Class<?> TYPE = RefClass.load(Handler.class, "android.os.Handler");
    public static RefObject<android.os.Handler.Callback> mCallback;
}
