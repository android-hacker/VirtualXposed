package mirror.android.app.job;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.os.Build;

import mirror.RefClass;
import mirror.RefInt;
import mirror.RefObject;

/**
 * @author Lody
 */

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class JobInfo {
    public static Class<?> TYPE = RefClass.load(JobInfo.class, android.app.job.JobInfo.class);

    public static RefInt jobId;
    public static RefObject<ComponentName> service;
}
