package mirror.android.view;


import mirror.MethodReflectParams;
import mirror.RefClass;
import mirror.RefMethod;

public class DisplayAdjustments {
    public static Class<?> Class = RefClass.load(DisplayAdjustments.class, "android.view.DisplayAdjustments");
    @MethodReflectParams({"android.content.res.CompatibilityInfo"})
    public static RefMethod<Void> setCompatibilityInfo;
}