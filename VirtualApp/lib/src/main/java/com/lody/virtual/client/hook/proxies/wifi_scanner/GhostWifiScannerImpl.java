package com.lody.virtual.client.hook.proxies.wifi_scanner;

import android.net.wifi.IWifiScanner;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Messenger;
import android.os.RemoteException;

import java.util.ArrayList;

import mirror.android.net.wifi.WifiScanner;

/**
 * @author Lody
 */

public class GhostWifiScannerImpl extends IWifiScanner.Stub {

    private final Handler mHandler = new Handler(Looper.getMainLooper());

    @Override
    public Messenger getMessenger() throws RemoteException {
        return new Messenger(mHandler);
    }

    @Override
    public Bundle getAvailableChannels(int band) throws RemoteException {
        Bundle bundle = new Bundle();
        bundle.putIntegerArrayList(WifiScanner.GET_AVAILABLE_CHANNELS_EXTRA.get(), new ArrayList<Integer>(0));
        return bundle;
    }
}
