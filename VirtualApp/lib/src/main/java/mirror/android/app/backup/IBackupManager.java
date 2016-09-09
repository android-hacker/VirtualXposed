package mirror.android.app.backup;

import android.os.IBinder;
import android.os.IInterface;

import mirror.RefClass;
import mirror.MethodParams;
import mirror.RefStaticMethod;

public class IBackupManager {
    public static Class<?> TYPE = RefClass.load(IBackupManager.class, "android.app.backup.IBackupManager");

    public static class Stub {
        public static Class<?> TYPE = RefClass.load(Stub.class, "android.app.backup.IBackupManager$Stub");
        @MethodParams({IBinder.class})
        public static RefStaticMethod<IInterface> asInterface;
    }
}