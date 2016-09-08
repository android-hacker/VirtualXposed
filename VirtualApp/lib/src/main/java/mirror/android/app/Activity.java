package mirror.android.app;


import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.IBinder;

import mirror.BooleanFieldDef;
import mirror.ClassDef;
import mirror.FieldDef;
import mirror.IntFieldDef;

public class Activity {
    public static Class<?> Class = ClassDef.init(Activity.class, "android.app.Activity");
    public static FieldDef<ActivityInfo> mActivityInfo;
    public static BooleanFieldDef mFinished;
    public static FieldDef<android.app.Activity> mParent;
    public static IntFieldDef mResultCode;
    public static FieldDef<Intent> mResultData;
    public static FieldDef<IBinder> mToken;
    public static FieldDef<String> mEmbeddedID;
}
