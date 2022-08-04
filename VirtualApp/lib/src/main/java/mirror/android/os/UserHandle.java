package mirror.android.os;

import android.annotation.TargetApi;
import android.os.Build;

import mirror.MethodParams;
import mirror.RefClass;
import mirror.RefConstructor;
import mirror.RefMethod;

/**
 * author: weishu on 18/2/11.
 */

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
public class UserHandle {
    public static Class<?> TYPE = RefClass.load(UserHandle.class, android.os.UserHandle.class);
    @MethodParams({int.class})
    public static RefConstructor<android.os.UserHandle> ctor;
    public static RefMethod<Integer> getIdentifier;

}
