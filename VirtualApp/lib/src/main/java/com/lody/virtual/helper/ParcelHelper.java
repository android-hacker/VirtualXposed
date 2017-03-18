package com.lody.virtual.helper;

import android.os.Bundle;
import android.os.Parcel;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Lody
 */

public class ParcelHelper {

    public static void writeMeta(Parcel p, Bundle meta) {
        Map<String, String> map = new HashMap<>();
        if (meta != null) {
            for (String key : meta.keySet()) {
                map.put(key, meta.getString(key));
            }
        }
        p.writeMap(map);
    }

    public static Bundle readMeta(Parcel p) {
        Bundle meta = new Bundle();
        //noinspection unchecked
        Map<String, String> map = p.readHashMap(String.class.getClassLoader());
        for (Map.Entry<String, String> entry : map.entrySet()) {
            meta.putString(entry.getKey(), entry.getValue());
        }
        return meta;
    }
}
