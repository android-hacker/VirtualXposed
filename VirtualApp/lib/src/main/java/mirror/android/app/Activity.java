package mirror.android.app;


import mirror.BooleanFieldDef;
import mirror.ClassDef;
import mirror.FieldDef;
import mirror.IntFieldDef;

public class Activity {
    public static Class<?> Class = ClassDef.init(Activity.class, "android.app.Activity");
    public static FieldDef mActivityInfo;
    public static BooleanFieldDef mFinished;
    public static FieldDef mParent;
    public static IntFieldDef mResultCode;
    public static FieldDef mResultData;
    public static FieldDef mToken;
}
