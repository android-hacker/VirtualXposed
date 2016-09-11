package com.lc.interceptor.service.providers;

import android.os.Bundle;
import android.telephony.CellIdentityLte;
import android.telephony.CellInfo;
import android.telephony.CellInfoLte;
import android.telephony.NeighboringCellInfo;
import android.telephony.TelephonyManager;

import com.lc.interceptor.proto.CellInfoMirror;
import com.lc.interceptor.service.providers.base.InterceptorDataProvider;
import com.lody.virtual.client.hook.base.PatchDelegate;
import com.lody.virtual.client.hook.patchs.telephony.TelephonyPatch;
import com.lody.virtual.helper.utils.Reflect;

import java.util.ArrayList;
import java.util.List;


/**
 * @author legency
 */
public class TelephonyManagerProvider extends InterceptorDataProvider {

    @Override
    public Class<? extends PatchDelegate> getDelegatePatch() {
        return TelephonyPatch.class;
    }

    /**
     * Returns a constant indicating the device phone type for a subscription.
     *
     * @see TelephonyManager#PHONE_TYPE_GSM
     * @see TelephonyManager#PHONE_TYPE_CDMA
     * @see TelephonyManager#PHONE_TYPE_NONE
     * @see android.telephony.TelephonyManager#PHONE_TYPE_SIP
     * @see com.lc.interceptor.client.hook.patch.interceptor.telephony.Interceptor_GetActivePhoneTypeForSubscriber
     */
    private int getActivePhoneTypeForSubscriber() {
        return TelephonyManager.PHONE_TYPE_GSM;
    }

    private int getActivePhoneTypeForSlot() {
        return TelephonyManager.PHONE_TYPE_GSM;
    }

    /**
     * @param pkg
     * @return
     * @see com.lc.interceptor.client.hook.patch.interceptor.telephony.Interceptor_GetActivePhoneTypeForSubscriber
     */
    List<CellInfo> getAllCellInfo(String pkg) {
        List<CellInfo> list = new ArrayList<>();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
//            int vuid =2147483647;
            int uid = 123;
            //主要关心tac ci 值
            CellIdentityLte[] cellIdentityLtes = {Reflect.on(CellIdentityLte.class).create(460, 1, 111210241, 21, 9673).get(),
//                    new CellIdentityLte(vuid, vuid, vuid, 77, vuid),
//                    new CellIdentityLte(vuid, vuid, vuid, 43, vuid),
//                    new CellIdentityLte(vuid, vuid, vuid, 303, vuid),
//                    new CellIdentityLte(vuid, vuid, vuid, 395, vuid),
//                    new CellIdentityLte(vuid, vuid, vuid, 187, vuid)
            };
            for (CellIdentityLte c : cellIdentityLtes) {
                CellInfoLte cellInfoLte = CellInfoMirror.CellInfoLteMirror.ctor.newInstance();
                CellInfoMirror.CellInfoLteMirror.mCellIdentityLte.set(cellInfoLte, c);
                CellInfoMirror.mRegistered.set(cellInfoLte, false);
                list.add(cellInfoLte);
            }
        }
        return list;
    }

    /**
     * @param subId
     * @return
     * @see com.lc.interceptor.client.hook.patch.interceptor.telephony.Interceptor_GetAllCellInfoUsingSubId
     */
    public List<CellInfo> getAllCellInfoUsingSubId(int subId) {
        return getAllCellInfo(null);
    }

    /**
     * 和 @see TelephonyManager#PHONE_TYPE_GSM 一一对应
     *
     * @return
     */
    public Bundle getCellLocation() {
        switch (getActivePhoneTypeForSlot()) {
            case TelephonyManager.PHONE_TYPE_CDMA:
                return createCdmaCellLocation();
            case TelephonyManager.PHONE_TYPE_GSM:
                return createGsmCellLocation();
            default:
                return null;
        }

    }

    List<NeighboringCellInfo> getNeighboringCellInfo(){
        List<NeighboringCellInfo> cellInfo = new ArrayList<>();
        NeighboringCellInfo n = new NeighboringCellInfo(3, "6156", TelephonyManager.NETWORK_TYPE_HSDPA);
        n.setCid(123);
        n.setRssi(132);
        cellInfo.add(n);
        return cellInfo;
    }

    private Bundle createGsmCellLocation() {
        Bundle bundle = new Bundle();
        bundle.putInt("lac", 9500);
        bundle.putInt("cid", 101010691);
        bundle.putInt("psc", -1);
        return bundle;
    }

    private Bundle createCdmaCellLocation() {
        Bundle bundle = new Bundle();
        bundle.putInt("baseStationId", 9500);
        bundle.putInt("baseStationLatitude", 101010691);
        bundle.putInt("baseStationLongitude", -1);
        bundle.putInt("systemId", -1);
        bundle.putInt("networkId", -1);
        return bundle;
    }
}
