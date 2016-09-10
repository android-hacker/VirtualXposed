package com.lc.interceptor.proto;

import android.telephony.CellIdentityLte;
import android.telephony.CellInfo;
import android.telephony.CellInfoLte;
import android.telephony.CellSignalStrengthLte;

import mirror.RefBoolean;
import mirror.RefClass;
import mirror.RefConstructor;
import mirror.RefInt;
import mirror.RefLong;
import mirror.RefObject;

/**
 * @author legency
 */
public class CellInfoMirror {
    public static Class<?> TYPE = RefClass.load(CellInfoMirror.class, CellInfo.class);
    public static RefBoolean mRegistered;

    public static RefLong mTimeStamp;

    public RefInt mTimeStampType;

    public static class CellInfoLteMirror {
        public static Class<?> TYPE = RefClass.load(CellInfoLteMirror.class, CellInfoLte.class);
        public static RefConstructor<CellInfoLte> ctor;
        public static RefObject<CellIdentityLte> mCellIdentityLte;
        public static RefObject<CellSignalStrengthLte> mCellSignalStrengthLte;
    }

}
