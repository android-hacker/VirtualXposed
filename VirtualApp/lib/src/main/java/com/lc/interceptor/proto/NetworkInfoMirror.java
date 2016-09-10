package com.lc.interceptor.proto;

import android.net.NetworkInfo;

import mirror.MethodParams;
import mirror.RefClass;
import mirror.RefConstructor;
import mirror.RefMethod;

/**
 * @author legency
 */
public class NetworkInfoMirror {

    public static Class<?> TYPE = RefClass.load(NetworkInfoMirror.class, NetworkInfo.class);

    @MethodParams({int.class, int.class, String.class, String.class})
    public static RefConstructor<NetworkInfo> ctor;

    @MethodParams({NetworkInfo.DetailedState.class, String.class, String.class})
    public static RefMethod<Void> setDetailedState;

    @MethodParams({boolean.class})
    public static RefMethod<Void> setIsAvailable;

    @MethodParams({boolean.class})
    public static RefMethod<Void> setFailover;

    @MethodParams({boolean.class})
    public static RefMethod<Void> setRoaming;

    public static class Builder {
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

        public Builder setDetailedState(NetworkInfo.DetailedState detailedState) {
            mDetailedState = detailedState;
            return this;
        }

        public Builder setExtraInfo(String extraInfo) {
            mExtraInfo = extraInfo;
            return this;
        }

        public Builder setAvailable(boolean available) {
            mIsAvailable = available;
            return this;
        }

        public Builder setFailover(boolean failover) {
            mIsFailover = failover;
            return this;
        }

        public Builder setRoaming(boolean roaming) {
            mIsRoaming = roaming;
            return this;
        }

        public Builder setNetworkType(int networkType) {
            mNetworkType = networkType;
            return this;
        }

        public Builder setReason(String reason) {
            mReason = reason;
            return this;
        }

        public Builder setState(NetworkInfo.State state) {
            mState = state;
            return this;
        }

        public Builder setSubtype(int subtype) {
            mSubtype = subtype;
            return this;
        }

        public Builder setSubtypeName(String subtypeName) {
            mSubtypeName = subtypeName;
            return this;
        }

        public Builder setTypeName(String typeName) {
            mTypeName = typeName;
            return this;
        }

        public NetworkInfo create() {
            NetworkInfo networkInfo = NetworkInfoMirror.ctor.newInstance(mNetworkType, mSubtype, mTypeName, mSubtypeName);
            NetworkInfoMirror.setDetailedState.call(networkInfo, mDetailedState, mReason, mExtraInfo);
            NetworkInfoMirror.setIsAvailable.call(networkInfo, mIsAvailable);
            NetworkInfoMirror.setFailover.call(networkInfo, mIsFailover);
            NetworkInfoMirror.setRoaming.call(networkInfo, mIsRoaming);
            return networkInfo;
        }
    }
}

