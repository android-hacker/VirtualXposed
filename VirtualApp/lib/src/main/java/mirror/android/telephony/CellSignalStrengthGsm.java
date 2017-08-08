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
public class CellSignalStrengthGsm {
    public static Class<?> TYPE = RefClass.load(CellSignalStrengthGsm.class, android.telephony.CellSignalStrengthGsm.class);
    public static RefConstructor<android.telephony.CellSignalStrengthGsm> ctor;
    public static RefInt mSignalStrength;
    public static RefInt mBitErrorRate;
}
