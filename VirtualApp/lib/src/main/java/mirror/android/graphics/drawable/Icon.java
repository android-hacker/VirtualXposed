package mirror.android.graphics.drawable;

import android.annotation.TargetApi;
import android.os.Build;

import mirror.RefClass;
import mirror.RefMethod;

@TargetApi(Build.VERSION_CODES.M)
public class Icon {
    public static Class<?> TYPE = RefClass.load(Icon.class, android.graphics.drawable.Icon.class);
    public static RefMethod<Integer> getType;
    public static RefMethod<String> mString1;
}