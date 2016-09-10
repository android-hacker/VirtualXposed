package com.lc.interceptor.proto;

import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;

import java.net.InetAddress;

import mirror.MethodParams;
import mirror.RefBoolean;
import mirror.RefClass;
import mirror.RefConstructor;
import mirror.RefDouble;
import mirror.RefInt;
import mirror.RefObject;
import mirror.RefStaticMethod;

/**
 * @author legency
 */
public class WifiInfoMirror {

    public static Class<?> TYPE = RefClass.load(WifiInfoMirror.class, WifiInfo.class);

    public static RefConstructor<WifiInfo> ctor;

    public static class WifiSsid {
        public static Class<?> TYPE = RefClass.load(WifiSsid.class, "android.net.wifi.WifiSsid");

        @MethodParams({String.class})
        public static RefStaticMethod<Object> createFromAsciiEncoded;
    }
    public static class InetAddressL{
        public static Class<?> TYPE = RefClass.load(InetAddressL.class, InetAddress.class);

        @MethodParams({int.class, byte[].class, String.class})
        public static RefConstructor<InetAddress> ctor;
    }

    public static RefInt mNetworkId;
    public static RefInt mRssi;
    public static RefInt mLinkSpeed;
    public static RefInt mFrequency;
    public static RefObject<InetAddress> mIpAddress;
    public static RefObject<Object> mWifiSsid;
    public static RefObject<String> mBSSID;
    public static RefObject<String> mMacAddress;
    public static RefBoolean mMeteredHint;
    public static RefBoolean mEphemeral;
    public static RefInt score;
    public static RefDouble txSuccessRate;
    public static RefDouble txRetriesRate;
    public static RefDouble txBadRate;
    public static RefDouble rxSuccessRate;
    public static RefInt badRssiCount;
    public static RefInt lowRssiCount;

    public static class Builder {
        private int mNetworkId;
        private int mRssi;
        private int mLinkSpeed;
        private int mFrequency;
        private InetAddress mIpAddress;
        private Object mWifiSsid;
        private String mBSSID;
        private String mMacAddress;
        private boolean mMeteredHint;
        private boolean mEphemeral;
        public int score;
        public double txSuccessRate;
        public double txRetriesRate;
        public double txBadRate;
        public double rxSuccessRate;
        public int badRssiCount;
        public int lowRssiCount;

        private SupplicantState mSupplicantState;

        public Builder setNetworkId(int mNetworkId) {
            this.mNetworkId = mNetworkId;
            return this;
        }

        public Builder setRssi(int mRssi) {
            this.mRssi = mRssi;
            return this;
        }

        public Builder setLinkSpeed(int mLinkSpeed) {
            this.mLinkSpeed = mLinkSpeed;
            return this;
        }

        public Builder setFrequency(int mFrequency) {
            this.mFrequency = mFrequency;
            return this;
        }

        public Builder setIpAddress(InetAddress mIpAddress) {
            this.mIpAddress = mIpAddress;
            return this;
        }

        public Builder setWifiSsid(String wifiSsid) {
            this.mWifiSsid = WifiInfoMirror.WifiSsid.createFromAsciiEncoded.call(wifiSsid);
            return this;
        }

        public Builder setBSSID(String mBSSID) {
            this.mBSSID = mBSSID;
            return this;
        }

        public Builder setMacAddress(String mMacAddress) {
            this.mMacAddress = mMacAddress;
            return this;
        }

        public Builder setMeteredHint(boolean mMeteredHint) {
            this.mMeteredHint = mMeteredHint;
            return this;
        }

        public Builder setEphemeral(boolean mEphemeral) {
            this.mEphemeral = mEphemeral;
            return this;
        }

        public Builder setScore(int score) {
            this.score = score;
            return this;
        }

        public Builder setTxSuccessRate(double txSuccessRate) {
            this.txSuccessRate = txSuccessRate;
            return this;
        }

        public Builder setTxRetriesRate(double txRetriesRate) {
            this.txRetriesRate = txRetriesRate;
            return this;
        }

        public Builder setTxBadRate(double txBadRate) {
            this.txBadRate = txBadRate;
            return this;
        }

        public Builder setRxSuccessRate(double rxSuccessRate) {
            this.rxSuccessRate = rxSuccessRate;
            return this;
        }

        public Builder setBadRssiCount(int badRssiCount) {
            this.badRssiCount = badRssiCount;
            return this;
        }

        public Builder setLowRssiCount(int lowRssiCount) {
            this.lowRssiCount = lowRssiCount;
            return this;
        }

        public Builder setSupplicantState(SupplicantState mSupplicantState) {
            this.mSupplicantState = mSupplicantState;
            return this;
        }

        public WifiInfo create() {
            WifiInfo wifiInfo = WifiInfoMirror.ctor.newInstance();
            WifiInfoMirror.mNetworkId.set(wifiInfo, mNetworkId);
            WifiInfoMirror.mRssi.set(wifiInfo, mRssi);
            WifiInfoMirror.mLinkSpeed.set(wifiInfo, mLinkSpeed);
            WifiInfoMirror.mFrequency.set(wifiInfo, mFrequency);
            WifiInfoMirror.mIpAddress.set(wifiInfo, mIpAddress);
            WifiInfoMirror.mWifiSsid.set(wifiInfo, mWifiSsid);
            WifiInfoMirror.mBSSID.set(wifiInfo, mBSSID);
            WifiInfoMirror.mMacAddress.set(wifiInfo, mMacAddress);
            WifiInfoMirror.mMeteredHint.set(wifiInfo, mMeteredHint);
            WifiInfoMirror.mEphemeral.set(wifiInfo, mEphemeral);
            WifiInfoMirror.score.set(wifiInfo, score);
            WifiInfoMirror.txSuccessRate.set(wifiInfo, txSuccessRate);
            WifiInfoMirror.txRetriesRate.set(wifiInfo, txRetriesRate);
            WifiInfoMirror.txBadRate.set(wifiInfo, txBadRate);
            WifiInfoMirror.rxSuccessRate.set(wifiInfo, rxSuccessRate);
            WifiInfoMirror.badRssiCount.set(wifiInfo, badRssiCount);
            WifiInfoMirror.lowRssiCount.set(wifiInfo, lowRssiCount);
            return wifiInfo;
        }
    }

}

