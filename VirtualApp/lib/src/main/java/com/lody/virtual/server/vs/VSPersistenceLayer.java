package com.lody.virtual.server.vs;

import android.os.Parcel;
import android.util.SparseArray;

import com.lody.virtual.helper.PersistenceLayer;
import com.lody.virtual.os.VEnvironment;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Lody
 */

class VSPersistenceLayer extends PersistenceLayer {

    private static final char[] MAGIC = {'v', 's', 'a'};
    private static final int CURRENT_VERSION = 1;
    private final VirtualStorageService mService;

    VSPersistenceLayer(VirtualStorageService service) {
        super(VEnvironment.getVSConfigFile());
        this.mService = service;
    }

    @Override
    public int getCurrentVersion() {
        return CURRENT_VERSION;
    }

    @Override
    public void writeMagic(Parcel p) {
        p.writeCharArray(MAGIC);
    }

    @Override
    public boolean verifyMagic(Parcel p) {
        char[] magic = p.createCharArray();
        return Arrays.equals(magic, MAGIC);
    }


    @Override
    public void writePersistenceData(Parcel p) {
        final SparseArray<HashMap<String, VSConfig>> configs = mService.getConfigs();
        int N = configs.size();
        p.writeInt(N);
        while (N-- > 0) {
            int userId = configs.keyAt(N);
            Map<String, VSConfig> userMap = configs.valueAt(N);
            p.writeInt(userId);
            p.writeMap(userMap);
        }

    }

    @Override
    public void readPersistenceData(Parcel p) {
        final SparseArray<HashMap<String, VSConfig>> configs = mService.getConfigs();
        int N = p.readInt();
        while (N-- > 0) {
            int userId = p.readInt();
            //noinspection unchecked
            HashMap<String, VSConfig> userMap = p.readHashMap(VSConfig.class.getClassLoader());
            configs.put(userId, userMap);
        }
    }

    @Override
    public boolean onVersionConflict(int fileVersion, int currentVersion) {
        return false;
    }


    @Override
    public void onPersistenceFileDamage() {

    }
}
