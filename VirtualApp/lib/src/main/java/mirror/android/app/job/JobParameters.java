package mirror.android.app.job;

import android.os.IBinder;
import android.os.PersistableBundle;

import mirror.MethodParams;
import mirror.RefClass;
import mirror.RefConstructor;

/**
 * @author Lody
 */

public class JobParameters {
    public static Class<?> TYPE = RefClass.load(JobParameters.class, android.app.job.JobParameters.class);

    @MethodParams({IBinder.class, int.class, PersistableBundle.class, boolean.class})
    public static RefConstructor<android.app.job.JobParameters> ctor;

}
