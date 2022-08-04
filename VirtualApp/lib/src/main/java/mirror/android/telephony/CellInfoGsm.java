package mirror.android.telephony;

import android.annotation.TargetApi;
import android.os.Build;

import mirror.RefClass;
import mirror.RefConstructor;
import mirror.RefObject;

/**
 * @author Lody
 */

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
public class CellInfoGsm {
    public static Class<?> TYPE = RefClass.load(CellInfoGsm.class, android.telephony.CellInfoGsm.class);
    public static RefConstructor<android.telephony.CellInfoGsm> ctor;
    public static RefObject<android.telephony.CellIdentityGsm> mCellIdentityGsm;
    public static RefObject<android.telephony.CellSignalStrengthGsm> mCellSignalStrengthGsm;

}
