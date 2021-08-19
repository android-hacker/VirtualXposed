package mirror.android.os;

import android.annotation.TargetApi;
import android.os.IBinder;
import android.os.IInterface;

import mirror.MethodParams;
import mirror.RefClass;
import mirror.RefStaticMethod;

/**
 * @author weishu
 * @date 2021/8/19.
 */
@TargetApi(29)
public class IDeviceIdentifiersPolicyService {
    public static Class<?> TYPE = RefClass.load(IDeviceIdentifiersPolicyService.class, "android.os.IDeviceIdentifiersPolicyService");

    public static class Stub {
        public static Class<?> TYPE = RefClass.load(IDeviceIdentifiersPolicyService.Stub.class, "android.os.IDeviceIdentifiersPolicyService$Stub");
        @MethodParams({IBinder.class})
        public static RefStaticMethod<IInterface> asInterface;
    }
}
