package mirror.android.app;

import android.content.pm.ProviderInfo;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcelable;

import mirror.BooleanFieldDef;
import mirror.ClassDef;
import mirror.CtorDef;
import mirror.FieldDef;
import mirror.MethodDef;
import mirror.MethodInfo;
import mirror.StaticFieldDef;

public class IActivityManager {
    public static Class<?> Class = ClassDef.init(IActivityManager.class, "android.app.IActivityManager");
    @MethodInfo({IBinder.class, boolean.class})
    public static MethodDef<Integer> getTaskForActivity;
    @MethodInfo({IBinder.class, int.class})
    public static MethodDef<Void> setRequestedOrientation;
    @MethodInfo({IBinder.class, String.class, int.class, int.class})
    public static MethodDef<Void> overridePendingTransition;
    public static MethodDef<Integer> startActivity;

    public static class ContentProviderHolder {
        @MethodInfo(ProviderInfo.class)
        public static CtorDef<Object> ctor;
        public static StaticFieldDef<Parcelable.Creator> CREATOR;
        public static Class<?> Class = ClassDef.init(ContentProviderHolder.class, "android.app.IActivityManager$ContentProviderHolder");
        public static FieldDef<ProviderInfo> info;
        public static FieldDef<IInterface> provider;
        public static BooleanFieldDef noReleaseNeeded;
    }
}
