package android.net.wifi;

import android.os.Messenger;
import android.os.Bundle;

interface IWifiScanner
{
    Messenger getMessenger();

    Bundle getAvailableChannels(int band);
}
