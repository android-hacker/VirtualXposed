package mirror.android.location;

import mirror.RefBoolean;
import mirror.RefClass;
import mirror.RefMethod;
import mirror.RefObject;

public class LocationRequestL {
    public static Class<?> TYPE = RefClass.load(LocationRequestL.class, "android.location.LocationRequest");
    public static RefBoolean mHideFromAppOps;
    public static RefObject<Object> mWorkSource;
    public static RefObject<String> mProvider;
    public static RefMethod<String> getProvider;
}
