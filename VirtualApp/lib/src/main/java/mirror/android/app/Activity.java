package mirror.android.app;


import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.IBinder;

import mirror.RefBoolean;
import mirror.RefClass;
import mirror.RefObject;
import mirror.RefInt;

public class Activity {
    public static Class<?> TYPE = RefClass.load(Activity.class, "android.app.Activity");
    public static RefObject<ActivityInfo> mActivityInfo;
    public static RefBoolean mFinished;
    public static RefObject<android.app.Activity> mParent;
    public static RefInt mResultCode;
    public static RefObject<Intent> mResultData;
    public static RefObject<IBinder> mToken;
    public static RefObject<String> mEmbeddedID;
}
