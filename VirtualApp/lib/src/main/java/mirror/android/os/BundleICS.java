package mirror.android.os;

import android.os.Parcel;

import mirror.RefClass;
import mirror.RefObject;


public class BundleICS {
    public static Class<?> TYPE = RefClass.load(BundleICS.class, "android.os.Bundle");
    public static RefObject<Parcel> mParcelledData;
}