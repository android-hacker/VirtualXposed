package mirror.android.content;


import mirror.MethodParams;
import mirror.RefClass;
import mirror.RefConstructor;

public class SyncAdapterType {
    public static Class<?> TYPE = RefClass.load(SyncAdapterType.class, android.content.SyncAdapterType.class);
    @MethodParams({String.class, String.class, boolean.class, boolean.class, boolean.class, boolean.class, String.class})
    public static RefConstructor<android.content.SyncAdapterType> ctor;
}