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
public class CellIdentityCdma {
    public static Class<?> TYPE = RefClass.load(CellIdentityCdma.class, android.telephony.CellIdentityCdma.class);
    public static RefConstructor<android.telephony.CellIdentityCdma> ctor;
    public static RefInt mNetworkId;
    public static RefInt mSystemId;
    public static RefInt mBasestationId;
}
