package mirror.android.app;

import android.os.IBinder;

import java.util.List;

import mirror.MethodParams;
import mirror.RefClass;
import mirror.RefMethod;

/**
 * @author Lody
 */

public class ActivityThreadNMR1 {
    public static Class<?> Class = RefClass.load(ActivityThreadNMR1.class, "android.app.ActivityThread");
    @MethodParams({IBinder.class, List.class, boolean.class})
    public static RefMethod<Void> performNewIntents;
}
