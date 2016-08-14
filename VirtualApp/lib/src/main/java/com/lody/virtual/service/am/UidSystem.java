package com.lody.virtual.service.am;

import java.util.HashMap;
import java.util.Map;

import static android.os.Process.FIRST_APPLICATION_UID;

/**
 * @author Lody
 */

public class UidSystem {
    private static final Map<String, Integer> mSharedUserIdMap = new HashMap<>();
    private int mFreeUid = FIRST_APPLICATION_UID;

    public int getOrCreateUid(String sharedUserId) {
        if (sharedUserId == null) {
            return ++mFreeUid;
        }
        Integer uid = mSharedUserIdMap.get(sharedUserId);
        if (uid != null) {
            return uid;
        }
        int newUid = ++mFreeUid;
        mSharedUserIdMap.put(sharedUserId, newUid);
        return newUid;
    }
}
