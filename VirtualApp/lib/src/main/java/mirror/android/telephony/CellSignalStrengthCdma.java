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
public class CellSignalStrengthCdma {
    public static Class<?> TYPE = RefClass.load(CellSignalStrengthCdma.class, android.telephony.CellSignalStrengthCdma.class);
    public static RefConstructor<android.telephony.CellSignalStrengthCdma> ctor;
    public static RefInt mCdmaDbm;
    public static RefInt mCdmaEcio;
    public static RefInt mEvdoDbm;
    public static RefInt mEvdoEcio;
    public static RefInt mEvdoSnr;
}
