package mirror.android.app.job;

import mirror.RefBoolean;
import mirror.RefClass;

/**
 * @author Lody
 */

public class JobInfo {
    public static Class<?> TYPE = RefClass.load(JobInfo.class, android.app.job.JobInfo.class);

    public static RefBoolean hasEarlyConstraint;
    public static RefBoolean hasLateConstraint;
}
