package mirror.android.os;

import android.os.Parcel;

import mirror.RefClass;
import mirror.RefObject;


public class BaseBundle {
    public static Class<?> TYPE = RefClass.load(BaseBundle.class, "android.os.BaseBundle");
    public static RefObject<Parcel> mParcelledData;
}