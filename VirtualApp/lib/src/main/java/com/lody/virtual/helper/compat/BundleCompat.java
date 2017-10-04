package com.lody.virtual.helper.compat;

import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;

import mirror.android.os.BaseBundle;
import mirror.android.os.BundleICS;

/**
 * @author Lody
 */
public class BundleCompat {
    public static IBinder getBinder(Bundle bundle, String key) {
        if (Build.VERSION.SDK_INT >= 18) {
            return bundle.getBinder(key);
        } else {
            return mirror.android.os.Bundle.getIBinder.call(bundle, key);
        }
    }

    public static void putBinder(Bundle bundle, String key, IBinder value) {
        if (Build.VERSION.SDK_INT >= 18) {
            bundle.putBinder(key, value);
        } else {
            mirror.android.os.Bundle.putIBinder.call(bundle, key, value);
        }
    }

    public static void clearParcelledData(Bundle bundle) {
        Parcel obtain = Parcel.obtain();
        obtain.writeInt(0);
        obtain.setDataPosition(0);
        Parcel parcel;
        if (BaseBundle.TYPE != null) {
            parcel = BaseBundle.mParcelledData.get(bundle);
            if (parcel != null) {
                parcel.recycle();
            }
            BaseBundle.mParcelledData.set(bundle, obtain);
        } else if (BundleICS.TYPE != null) {
            parcel = BundleICS.mParcelledData.get(bundle);
            if (parcel != null) {
                parcel.recycle();
            }
            BundleICS.mParcelledData.set(bundle, obtain);
        }
    }
}
