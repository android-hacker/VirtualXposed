package mirror.android.location;

import mirror.RefClass;
import mirror.RefObject;

/**
 * @author LittleAngry
 * @date 2021/11/28.
 */

public class GeocoderParams {
    public static Class<?> TYPE = RefClass.load(GeocoderParams.class, "android.location.GeocoderParams");

    public static RefObject mPackageName;
    public static RefObject mUid;
}
