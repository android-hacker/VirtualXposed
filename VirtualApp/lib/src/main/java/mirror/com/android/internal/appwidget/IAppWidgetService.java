package mirror.com.android.internal.appwidget;

import android.os.IBinder;
import android.os.IInterface;

import mirror.RefClass;
import mirror.MethodParams;
import mirror.RefStaticMethod;

public class IAppWidgetService {
    public static Class<?> TYPE = RefClass.load(IAppWidgetService.class, "com.android.internal.appwidget.IAppWidgetService");

    public static class Stub {
        public static Class<?> TYPE = RefClass.load(Stub.class, "com.android.internal.appwidget.IAppWidgetService$Stub");
        @MethodParams({IBinder.class})
        public static RefStaticMethod<IInterface> asInterface;
    }
}