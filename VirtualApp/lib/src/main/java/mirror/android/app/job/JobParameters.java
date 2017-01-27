package mirror.android.app.job;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.IBinder;
import android.os.PersistableBundle;

import mirror.MethodParams;
import mirror.RefClass;
import mirror.RefConstructor;
import mirror.RefInt;
import mirror.RefObject;

/**
 * @author Lody
 */

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class JobParameters {
    public static Class<?> TYPE = RefClass.load(JobParameters.class, android.app.job.JobParameters.class);

    public static RefObject<IBinder> callback;
    public static RefObject<PersistableBundle> extras;
    public static RefInt jobId;


}
