package mirror.android.telephony;

import android.annotation.TargetApi;
import android.os.Build;
import android.telephony.CellSignalStrengthCdma;

import mirror.RefClass;
import mirror.RefConstructor;
import mirror.RefObject;

/**
 * @author Lody
 */

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
public class CellInfoCdma {
    public static Class<?> TYPE = RefClass.load(CellInfoCdma.class, android.telephony.CellInfoCdma.class);
    public static RefConstructor<android.telephony.CellInfoCdma> ctor;
    public static RefObject<android.telephony.CellIdentityCdma> mCellIdentityCdma;
    public static RefObject<CellSignalStrengthCdma> mCellSignalStrengthCdma;
}
