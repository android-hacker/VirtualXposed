package com.lc.interceptor.service.providers;

import android.telephony.CellIdentityLte;
import android.telephony.CellInfo;
import android.telephony.CellInfoLte;
import android.telephony.TelephonyManager;

import com.lc.interceptor.service.providers.base.InterceptorDataProvider;
import com.lody.virtual.client.hook.base.PatchDelegate;
import com.lody.virtual.client.hook.patchs.telephony.TelephonyPatch;
import com.lody.virtual.helper.utils.Reflect;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by lichen:) on 2016/9/9.
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

    /**
     * @see com.lc.interceptor.client.hook.patch.interceptor.telephony.Interceptor_GetActivePhoneTypeForSubscriber
     * @param pkg
     * @return
     */
    List<CellInfo> getAllCellInfo(String pkg) {
        List<CellInfo> list = new ArrayList<>();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
//            int uid =2147483647;
            int uid = 123;
            //主要关心tac ci 值
            CellIdentityLte[] cellIdentityLtes = {Reflect.on(CellIdentityLte.class).create(460, 1, 111210241, 21, 9673).get(),
//                    new CellIdentityLte(uid, uid, uid, 77, uid),
//                    new CellIdentityLte(uid, uid, uid, 43, uid),
//                    new CellIdentityLte(uid, uid, uid, 303, uid),
//                    new CellIdentityLte(uid, uid, uid, 395, uid),
//                    new CellIdentityLte(uid, uid, uid, 187, uid)
            };
            for (CellIdentityLte c : cellIdentityLtes) {
                CellInfoLte cellInfoLte = Reflect.on(CellInfoLte.class).create().get();
                Reflect.on(cellInfoLte).call("setCellIdentity", c);
                Reflect.on(cellInfoLte).call("setRegistered", false);
                list.add(cellInfoLte);
            }
        }
        return list;
    }

    /**
     * @see com.lc.interceptor.client.hook.patch.interceptor.telephony.Interceptor_GetAllCellInfoUsingSubId
     * @param subId
     * @return
     */
    public List<CellInfo> getAllCellInfoUsingSubId(int subId) {
        return getAllCellInfo(null);
    }


}
