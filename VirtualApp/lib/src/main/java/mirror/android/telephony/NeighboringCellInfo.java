package mirror.android.telephony;

import mirror.RefClass;
import mirror.RefInt;

/**
 * @author Lody
 */

public class NeighboringCellInfo {
    public static Class<?> TYPE = RefClass.load(NeighboringCellInfo.class, android.telephony.NeighboringCellInfo.class);
    public static RefInt mLac;
    public static RefInt mCid;
    public static RefInt mRssi;
}
