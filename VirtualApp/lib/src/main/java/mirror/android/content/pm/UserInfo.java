package mirror.android.content.pm;


import mirror.MethodParams;
import mirror.RefClass;
import mirror.RefConstructor;
import mirror.RefStaticInt;

public class UserInfo {
    public static Class<?> TYPE = RefClass.load(UserInfo.class, "android.content.pm.UserInfo");
    public static RefStaticInt FLAG_PRIMARY;
    @MethodParams({int.class, String.class, int.class})
    public static RefConstructor<Object> ctor;
}