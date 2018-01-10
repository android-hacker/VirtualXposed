package com.lody.virtual.server.device;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.RemoteException;

import com.lody.virtual.helper.collection.SparseArray;
import com.lody.virtual.remote.VDeviceInfo;
import com.lody.virtual.server.interfaces.IDeviceInfoManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * @author Lody
 */

public class VDeviceManagerService implements IDeviceInfoManager {

    private static VDeviceManagerService sInstance = new VDeviceManagerService();
    private final SparseArray<VDeviceInfo> mDeviceInfos = new SparseArray<>();
    private DeviceInfoPersistenceLayer mPersistenceLayer = new DeviceInfoPersistenceLayer(this);
    private UsedDeviceInfoPool mPool = new UsedDeviceInfoPool();

    public static VDeviceManagerService get() {
        return sInstance;
    }

    private final class UsedDeviceInfoPool {
        List<String> deviceIds = new ArrayList<>();
        List<String> androidIds = new ArrayList<>();
        List<String> wifiMacs = new ArrayList<>();
        List<String> bluetoothMacs = new ArrayList<>();
        List<String> iccIds = new ArrayList<>();
    }

    public VDeviceManagerService() {
        mPersistenceLayer.read();
        for (int i = 0; i < mDeviceInfos.size(); i++) {
            VDeviceInfo info = mDeviceInfos.valueAt(i);
            addDeviceInfoToPool(info);
        }
    }

    private void addDeviceInfoToPool(VDeviceInfo info) {
        mPool.deviceIds.add(info.deviceId);
        mPool.androidIds.add(info.androidId);
        mPool.wifiMacs.add(info.wifiMac);
        mPool.bluetoothMacs.add(info.bluetoothMac);
        mPool.iccIds.add(info.iccId);
    }

    @Override
    public VDeviceInfo getDeviceInfo(int userId) {
        VDeviceInfo info;
        synchronized (mDeviceInfos) {
            info = mDeviceInfos.get(userId);
            if (info == null) {
                info = generateRandomDeviceInfo();
                mDeviceInfos.put(userId, info);
                mPersistenceLayer.save();
            }
        }
        return info;
    }

    @Override
    public void updateDeviceInfo(int userId, VDeviceInfo info) {
        synchronized (mDeviceInfos) {
            if (info != null) {
                mDeviceInfos.put(userId, info);
                mPersistenceLayer.save();
            }
        }
    }

    private VDeviceInfo generateRandomDeviceInfo() {
        VDeviceInfo info = new VDeviceInfo();
        String value;
        do {
            value = generate10(15);
            info.deviceId = value;
        } while (mPool.deviceIds.contains(value));
        do {
            value = generate16(16);
            info.androidId = value;
        } while (mPool.androidIds.contains(value));
        do {
            value = generateMac();
            info.wifiMac = value;
        } while (mPool.wifiMacs.contains(value));
        do {
            value = generateMac();
            info.bluetoothMac = value;
        } while (mPool.bluetoothMacs.contains(value));

        do {
            value = generate10(20);
            info.iccId = value;
        } while (mPool.iccIds.contains(value));

        info.serial = generateSerial();

        addDeviceInfoToPool(info);
        return info;
    }


    SparseArray<VDeviceInfo> getDeviceInfos() {
        return mDeviceInfos;
    }

    private static String generate10(int length) {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    private static String generate16(int length) {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int nextInt = random.nextInt(16);
            if (nextInt < 10) {
                sb.append(nextInt);
            } else {
                sb.append((char) (nextInt + 87));
            }
        }
        return sb.toString();
    }

    private static String generateMac() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        int next = 1;
        int cur = 0;
        while (cur < 12) {
            int val = random.nextInt(16);
            if (val < 10) {
                sb.append(val);
            } else {
                sb.append((char) (val + 87));
            }
            if (cur == next && cur != 11) {
                sb.append(":");
                next += 2;
            }
            cur++;
        }
        return sb.toString();
    }

    @SuppressLint("HardwareIds")
    private static String generateSerial() {
        String serial;
        if (Build.SERIAL == null || Build.SERIAL.length() <= 0) {
            serial = "0123456789ABCDEF";
        } else {
            serial = Build.SERIAL;
        }
        List<Character> list = new ArrayList<>();
        for (char c : serial.toCharArray()) {
            list.add(c);
        }
        Collections.shuffle(list);
        StringBuilder sb = new StringBuilder();
        for (Character c : list) {
            sb.append(c.charValue());
        }
        return sb.toString();
    }
}
