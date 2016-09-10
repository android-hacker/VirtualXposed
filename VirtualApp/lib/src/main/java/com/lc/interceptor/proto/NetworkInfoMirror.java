package com.lc.interceptor.proto;

import android.net.NetworkInfo;

import com.lody.virtual.helper.utils.Reflect;

/**
 * Created by lichen:) on 2016/9/10.
 */
public class NetworkInfoMirror {

    private int mNetworkType;
    private int mSubtype;
    private String mTypeName;
    private String mSubtypeName;
    private NetworkInfo.State mState;
    private NetworkInfo.DetailedState mDetailedState;
    private String mReason;
    private String mExtraInfo;
    private boolean mIsFailover;
    private boolean mIsRoaming;
    private boolean mIsAvailable;

    public NetworkInfoMirror setDetailedState(NetworkInfo.DetailedState detailedState) {
        mDetailedState = detailedState;
        return this;
    }

    public NetworkInfoMirror setExtraInfo(String extraInfo) {
        mExtraInfo = extraInfo;
        return this;
    }

    public NetworkInfoMirror setAvailable(boolean available) {
        mIsAvailable = available;
        return this;
    }

    public NetworkInfoMirror setFailover(boolean failover) {
        mIsFailover = failover;
        return this;
    }

    public NetworkInfoMirror setRoaming(boolean roaming) {
        mIsRoaming = roaming;
        return this;
    }

    public NetworkInfoMirror setNetworkType(int networkType) {
        mNetworkType = networkType;
        return this;
    }

    public NetworkInfoMirror setReason(String reason) {
        mReason = reason;
        return this;
    }

    public NetworkInfoMirror setState(NetworkInfo.State state) {
        mState = state;
        return this;
    }

    public NetworkInfoMirror setSubtype(int subtype) {
        mSubtype = subtype;
        return this;
    }

    public NetworkInfoMirror setSubtypeName(String subtypeName) {
        mSubtypeName = subtypeName;
        return this;
    }

    public NetworkInfoMirror setTypeName(String typeName) {
        mTypeName = typeName;
        return this;
    }

    public NetworkInfo create() {
        NetworkInfo networkInfo = Reflect.on(NetworkInfo.class).create(mNetworkType, mSubtype, mTypeName, mSubtypeName).get();
        Reflect.on(networkInfo).call("setDetailedState", mDetailedState, mReason, mExtraInfo);
        Reflect.on(networkInfo).call("setIsAvailable", mIsAvailable);
        Reflect.on(networkInfo).call("setFailover", mIsFailover);
        Reflect.on(networkInfo).call("setRoaming", mIsRoaming);
        return networkInfo;
    }
}
