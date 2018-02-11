package mirror.android.os;

import android.annotation.TargetApi;
import android.os.Build;

import mirror.MethodParams;
import mirror.RefClass;
import mirror.RefMethod;
import mirror.RefStaticMethod;

/**
 * author: weishu on 18/2/11.
 */

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
public class UserHandle {
    public static Class<?> TYPE = RefClass.load(UserHandle.class, android.os.UserHandle.class);
    @MethodParams({int.class})
    public static RefStaticMethod<android.os.UserHandle> of;
    public static RefMethod<Integer> getIdentifier;

}
