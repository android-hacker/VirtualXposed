package com.lody.virtual.remote;

import android.content.BroadcastReceiver;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;


/**
 * @author Lody
 */

public class PendingResultData implements Parcelable {
    public static final Creator<PendingResultData> CREATOR = new Creator<PendingResultData>() {
        @Override
        public PendingResultData createFromParcel(Parcel source) {
            return new PendingResultData(source);
        }

        @Override
        public PendingResultData[] newArray(int size) {
            return new PendingResultData[size];
        }
    };
    public int mType;
    public boolean mOrderedHint;
    public boolean mInitialStickyHint;
    public IBinder mToken;
    public int mSendingUser;
    public int mFlags;
    public int mResultCode;
    public String mResultData;
    public Bundle mResultExtras;
    public boolean mAbortBroadcast;
    public boolean mFinished;

    public PendingResultData(BroadcastReceiver.PendingResult result) {
        if (mirror.android.content.BroadcastReceiver.PendingResultMNC.ctor != null) {
            mType = mirror.android.content.BroadcastReceiver.PendingResultMNC.mType.get(result);
            mOrderedHint = mirror.android.content.BroadcastReceiver.PendingResultMNC.mOrderedHint.get(result);
            mInitialStickyHint = mirror.android.content.BroadcastReceiver.PendingResultMNC.mInitialStickyHint.get(result);
            mToken = mirror.android.content.BroadcastReceiver.PendingResultMNC.mToken.get(result);
            mSendingUser = mirror.android.content.BroadcastReceiver.PendingResultMNC.mSendingUser.get(result);
            mFlags = mirror.android.content.BroadcastReceiver.PendingResultMNC.mFlags.get(result);
            mResultCode = mirror.android.content.BroadcastReceiver.PendingResultMNC.mResultCode.get(result);
            mResultData = mirror.android.content.BroadcastReceiver.PendingResultMNC.mResultData.get(result);
            mResultExtras = mirror.android.content.BroadcastReceiver.PendingResultMNC.mResultExtras.get(result);
            mAbortBroadcast = mirror.android.content.BroadcastReceiver.PendingResultMNC.mAbortBroadcast.get(result);
            mFinished = mirror.android.content.BroadcastReceiver.PendingResultMNC.mFinished.get(result);
        } else if (mirror.android.content.BroadcastReceiver.PendingResultJBMR1.ctor != null) {
            mType = mirror.android.content.BroadcastReceiver.PendingResultJBMR1.mType.get(result);
            mOrderedHint = mirror.android.content.BroadcastReceiver.PendingResultJBMR1.mOrderedHint.get(result);
            mInitialStickyHint = mirror.android.content.BroadcastReceiver.PendingResultJBMR1.mInitialStickyHint.get(result);
            mToken = mirror.android.content.BroadcastReceiver.PendingResultJBMR1.mToken.get(result);
            mSendingUser = mirror.android.content.BroadcastReceiver.PendingResultJBMR1.mSendingUser.get(result);
            mResultCode = mirror.android.content.BroadcastReceiver.PendingResultJBMR1.mResultCode.get(result);
            mResultData = mirror.android.content.BroadcastReceiver.PendingResultJBMR1.mResultData.get(result);
            mResultExtras = mirror.android.content.BroadcastReceiver.PendingResultJBMR1.mResultExtras.get(result);
            mAbortBroadcast = mirror.android.content.BroadcastReceiver.PendingResultJBMR1.mAbortBroadcast.get(result);
            mFinished = mirror.android.content.BroadcastReceiver.PendingResultJBMR1.mFinished.get(result);
        } else {
            mType = mirror.android.content.BroadcastReceiver.PendingResult.mType.get(result);
            mOrderedHint = mirror.android.content.BroadcastReceiver.PendingResult.mOrderedHint.get(result);
            mInitialStickyHint = mirror.android.content.BroadcastReceiver.PendingResult.mInitialStickyHint.get(result);
            mToken = mirror.android.content.BroadcastReceiver.PendingResult.mToken.get(result);
            mResultCode = mirror.android.content.BroadcastReceiver.PendingResult.mResultCode.get(result);
            mResultData = mirror.android.content.BroadcastReceiver.PendingResult.mResultData.get(result);
            mResultExtras = mirror.android.content.BroadcastReceiver.PendingResult.mResultExtras.get(result);
            mAbortBroadcast = mirror.android.content.BroadcastReceiver.PendingResult.mAbortBroadcast.get(result);
            mFinished = mirror.android.content.BroadcastReceiver.PendingResult.mFinished.get(result);
        }
    }


    protected PendingResultData(Parcel in) {
        this.mType = in.readInt();
        this.mOrderedHint = in.readByte() != 0;
        this.mInitialStickyHint = in.readByte() != 0;
        this.mToken = in.readStrongBinder();
        this.mSendingUser = in.readInt();
        this.mFlags = in.readInt();
        this.mResultCode = in.readInt();
        this.mResultData = in.readString();
        this.mResultExtras = in.readBundle();
        this.mAbortBroadcast = in.readByte() != 0;
        this.mFinished = in.readByte() != 0;
    }

    public BroadcastReceiver.PendingResult build() {
        if (mirror.android.content.BroadcastReceiver.PendingResultMNC.ctor != null) {
            return mirror.android.content.BroadcastReceiver.PendingResultMNC.ctor.newInstance(mResultCode, mResultData, mResultExtras, mType, mOrderedHint, mInitialStickyHint, mToken, mSendingUser, mFlags);
        }
        if (mirror.android.content.BroadcastReceiver.PendingResultJBMR1.ctor != null) {
            return mirror.android.content.BroadcastReceiver.PendingResultJBMR1.ctor.newInstance(mResultCode, mResultData, mResultExtras, mType, mOrderedHint, mInitialStickyHint, mToken, mSendingUser);
        }
        return mirror.android.content.BroadcastReceiver.PendingResult.ctor.newInstance(mResultCode, mResultData, mResultExtras, mType, mOrderedHint, mInitialStickyHint, mToken);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mType);
        dest.writeByte(this.mOrderedHint ? (byte) 1 : (byte) 0);
        dest.writeByte(this.mInitialStickyHint ? (byte) 1 : (byte) 0);
        dest.writeStrongBinder(this.mToken);
        dest.writeInt(this.mSendingUser);
        dest.writeInt(this.mFlags);
        dest.writeInt(this.mResultCode);
        dest.writeString(this.mResultData);
        dest.writeBundle(this.mResultExtras);
        dest.writeByte(this.mAbortBroadcast ? (byte) 1 : (byte) 0);
        dest.writeByte(this.mFinished ? (byte) 1 : (byte) 0);
    }

    public void finish() {
        try {
            build().finish();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
