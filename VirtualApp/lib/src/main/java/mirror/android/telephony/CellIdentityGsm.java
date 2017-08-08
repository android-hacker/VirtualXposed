package mirror.android.telephony;

import android.annotation.TargetApi;
import android.os.Build;

import mirror.RefClass;
import mirror.RefConstructor;
import mirror.RefInt;

/**
 * @author Lody
 */

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
public class CellIdentityGsm {
    public static Class<?> TYPE = RefClass.load(CellIdentityGsm.class, android.telephony.CellIdentityGsm.class);
    public static RefConstructor<android.telephony.CellIdentityGsm> ctor;
    public static RefInt mMcc;
    public static RefInt mMnc;
    public static RefInt mLac;
    public static RefInt mCid;

}
