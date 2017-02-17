package mirror.com.android.internal.telephony;

import android.os.IBinder;
import android.os.IInterface;

import mirror.MethodParams;
import mirror.RefClass;
import mirror.RefStaticMethod;

public class ITelephonyRegistry {
	public static Class<?> TYPE = RefClass.load(ITelephonyRegistry.class, "com.android.internal.telephony.ITelephonyRegistry");

	public static class Stub {
		public static Class<?> TYPE = RefClass.load(ITelephonyRegistry.Stub.class, "com.android.internal.telephony.ITelephonyRegistry$Stub");
		@MethodParams({IBinder.class})
		public static RefStaticMethod<IInterface> asInterface;
	}
}
