package mirror.android.app.job;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;

import mirror.MethodParams;
import mirror.RefClass;
import mirror.RefConstructor;
import mirror.RefMethod;
import mirror.RefObject;

/**
 * @author Lody
 */

@TargetApi(Build.VERSION_CODES.O)
public class JobWorkItem {
    public static Class<?> TYPE = RefClass.load(JobWorkItem.class, android.app.job.JobWorkItem.class);

//    final Intent mIntent;
//    int mDeliveryCount;
//    int mWorkId;
//    Object mGrants;
    @MethodParams({Intent.class})
    public static RefConstructor<android.app.job.JobWorkItem> ctor;

    public static RefObject<Intent> mIntent;
    public static RefObject<Integer> mDeliveryCount;
    public static RefObject<Integer> mWorkId;
    public static RefObject<Object> mGrants;

    public static RefMethod<Intent> getIntent;
}
