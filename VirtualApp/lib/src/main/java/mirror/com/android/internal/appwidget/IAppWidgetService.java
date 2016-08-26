package mirror.com.android.internal.appwidget;

import android.os.IBinder;
import android.os.IInterface;

import mirror.ClassDef;
import mirror.MethodInfo;
import mirror.StaticMethodDef;

public class IAppWidgetService {
    public static Class<?> Class = ClassDef.init(IAppWidgetService.class, "com.android.internal.appwidget.IAppWidgetService");

    public static class Stub {
        public static Class<?> Class = ClassDef.init(Stub.class, "com.android.internal.appwidget.IAppWidgetService$Stub");
        @MethodInfo({IBinder.class})
        public static StaticMethodDef<IInterface> asInterface;
    }
}