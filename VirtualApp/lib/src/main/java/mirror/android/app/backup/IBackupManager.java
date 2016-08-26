package mirror.android.app.backup;

import android.os.IBinder;
import android.os.IInterface;

import mirror.ClassDef;
import mirror.MethodInfo;
import mirror.StaticMethodDef;

public class IBackupManager {
    public static Class<?> Class = ClassDef.init(IBackupManager.class, "android.app.backup.IBackupManager");

    public static class Stub {
        public static Class<?> Class = ClassDef.init(Stub.class, "android.app.backup.IBackupManager$Stub");
        @MethodInfo({IBinder.class})
        public static StaticMethodDef<IInterface> asInterface;
    }
}