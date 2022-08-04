package mirror.android.content;


import android.os.Bundle;
import android.os.IBinder;

import mirror.MethodParams;
import mirror.RefBoolean;
import mirror.RefClass;
import mirror.RefConstructor;
import mirror.RefInt;
import mirror.RefMethod;
import mirror.RefObject;

public class BroadcastReceiver {
    public static Class<?> TYPE = RefClass.load(BroadcastReceiver.class, android.content.BroadcastReceiver.class);
    public static RefMethod<android.content.BroadcastReceiver.PendingResult> getPendingResult;
    @MethodParams({android.content.BroadcastReceiver.PendingResult.class})
    public static RefMethod<Void> setPendingResult;

    public static class PendingResult {
        public static Class<?> TYPE = RefClass.load(PendingResult.class, android.content.BroadcastReceiver.PendingResult.class);
        @MethodParams({int.class, String.class, Bundle.class, int.class, boolean.class, boolean.class, IBinder.class})
        public static RefConstructor<android.content.BroadcastReceiver.PendingResult> ctor;
        public static RefBoolean mAbortBroadcast;
        public static RefBoolean mFinished;
        public static RefBoolean mInitialStickyHint;
        public static RefBoolean mOrderedHint;
        public static RefInt mResultCode;
        public static RefObject<String> mResultData;
        public static RefObject<Bundle> mResultExtras;
        public static RefObject<IBinder> mToken;
        public static RefInt mType;
    }

    public static class PendingResultJBMR1 {
        public static Class<?> TYPE = RefClass.load(PendingResultJBMR1.class, android.content.BroadcastReceiver.PendingResult.class);
        @MethodParams({int.class, String.class, Bundle.class, int.class, boolean.class, boolean.class, IBinder.class, int.class})
        public static RefConstructor<android.content.BroadcastReceiver.PendingResult> ctor;
        public static RefBoolean mAbortBroadcast;
        public static RefBoolean mFinished;
        public static RefBoolean mInitialStickyHint;
        public static RefBoolean mOrderedHint;
        public static RefInt mResultCode;
        public static RefObject<String> mResultData;
        public static RefObject<Bundle> mResultExtras;
        public static RefInt mSendingUser;
        public static RefObject<IBinder> mToken;
        public static RefInt mType;
    }

    public static class PendingResultMNC {
        public static Class<?> TYPE = RefClass.load(PendingResultMNC.class, android.content.BroadcastReceiver.PendingResult.class);
        @MethodParams({int.class, String.class, Bundle.class, int.class, boolean.class, boolean.class, IBinder.class, int.class, int.class})
        public static RefConstructor<android.content.BroadcastReceiver.PendingResult> ctor;
        public static RefBoolean mAbortBroadcast;
        public static RefBoolean mFinished;
        public static RefInt mFlags;
        public static RefBoolean mInitialStickyHint;
        public static RefBoolean mOrderedHint;
        public static RefInt mResultCode;
        public static RefObject<String> mResultData;
        public static RefObject<Bundle> mResultExtras;
        public static RefInt mSendingUser;
        public static RefObject<IBinder> mToken;
        public static RefInt mType;
    }
}