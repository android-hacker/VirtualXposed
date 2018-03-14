package com.lody.virtual.server.vs;

import android.os.RemoteException;
import android.util.SparseArray;

import com.lody.virtual.server.IVirtualStorageService;
import com.lody.virtual.server.pm.VUserManagerService;

import java.util.HashMap;

/**
 * @author Lody
 */

public class VirtualStorageService extends IVirtualStorageService.Stub {

    private static final VirtualStorageService sService = new VirtualStorageService();
    private final VSPersistenceLayer mLayer = new VSPersistenceLayer(this);
    private final SparseArray<HashMap<String, VSConfig>> mConfigs = new SparseArray<>();

    public static VirtualStorageService get() {
        return sService;
    }

    private VirtualStorageService() {
        mLayer.read();
    }

    SparseArray<HashMap<String, VSConfig>> getConfigs() {
        return mConfigs;
    }

    @Override
    public void setVirtualStorage(String packageName, int userId, String vsPath) throws RemoteException {
        checkUserId(userId);
        synchronized (mConfigs) {
            VSConfig config = getOrCreateVSConfigLocked(packageName, userId);
            config.vsPath = vsPath;
            mLayer.save();
        }
    }

    private VSConfig getOrCreateVSConfigLocked(String packageName, int userId) {
        HashMap<String, VSConfig> userMap = mConfigs.get(userId);
        if (userMap == null) {
            userMap = new HashMap<>();
            mConfigs.put(userId, userMap);
        }
        VSConfig config = userMap.get(packageName);
        if (config == null) {
            config = new VSConfig();
            config.enable = false;
            userMap.put(packageName, config);
        }
        return config;
    }


    @Override
    public String getVirtualStorage(String packageName, int userId) throws RemoteException {
        checkUserId(userId);
        synchronized (mConfigs) {
            VSConfig config = getOrCreateVSConfigLocked(packageName, userId);
            return config.vsPath;
        }
    }

    @Override
    public void setVirtualStorageState(String packageName, int userId, boolean enable) throws RemoteException {
        checkUserId(userId);
        synchronized (mConfigs) {
            VSConfig config = getOrCreateVSConfigLocked(packageName, userId);
            config.enable = enable;
            mLayer.save();
        }

    }

    @Override
    public boolean isVirtualStorageEnable(String packageName, int userId) throws RemoteException {
        checkUserId(userId);
        synchronized (mConfigs) {
            VSConfig config = getOrCreateVSConfigLocked(packageName, userId);
            return config.enable;
        }

    }

    private void checkUserId(int userId) {
        if (!VUserManagerService.get().exists(userId)) {
            throw new IllegalStateException("Invalid userId " + userId);
        }
    }
}
