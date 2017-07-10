package com.lody.virtual.server.pm.installer;


import android.annotation.TargetApi;
import android.content.pm.PackageInfo;
import android.content.pm.PackageInstaller;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import mirror.android.content.pm.PackageInstaller.SessionParamsLOLLIPOP;
import mirror.android.content.pm.PackageInstaller.SessionParamsMarshmallow;


@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class SessionParams implements Parcelable {

    public static final int MODE_INVALID = -1;

    /**
     * Mode for an install session whose staged APKs should fully replace any
     * existing APKs for the target app.
     */
    public static final int MODE_FULL_INSTALL = 1;

    /**
     * Mode for an install session that should inherit any existing APKs for the
     * target app, unless they have been explicitly overridden (based on split
     * name) by the session. For example, this can be used to add one or more
     * split APKs to an existing installation.
     * <p>
     * If there are no existing APKs for the target app, this behaves like
     * {@link #MODE_FULL_INSTALL}.
     */
    public static final int MODE_INHERIT_EXISTING = 2;

    public int mode = MODE_INVALID;
    public int installFlags;
    public int installLocation = PackageInfo.INSTALL_LOCATION_INTERNAL_ONLY;
    public long sizeBytes = -1;
    public String appPackageName;
    public Bitmap appIcon;
    public String appLabel;
    public long appIconLastModified = -1;
    public Uri originatingUri;
    public Uri referrerUri;
    public String abiOverride;
    public String volumeUuid;
    public String[] grantedRuntimePermissions;

    public SessionParams(int mode) {
        this.mode = mode;
    }


    public PackageInstaller.SessionParams build() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PackageInstaller.SessionParams params = new PackageInstaller.SessionParams(mode);
            SessionParamsMarshmallow.installFlags.set(params, installFlags);
            SessionParamsMarshmallow.installLocation.set(params, installLocation);
            SessionParamsMarshmallow.sizeBytes.set(params, sizeBytes);
            SessionParamsMarshmallow.appPackageName.set(params, appPackageName);
            SessionParamsMarshmallow.appIcon.set(params, appIcon);
            SessionParamsMarshmallow.appLabel.set(params, appLabel);
            SessionParamsMarshmallow.appIconLastModified.set(params, appIconLastModified);
            SessionParamsMarshmallow.originatingUri.set(params, originatingUri);
            SessionParamsMarshmallow.referrerUri.set(params, referrerUri);
            SessionParamsMarshmallow.abiOverride.set(params, abiOverride);
            SessionParamsMarshmallow.volumeUuid.set(params, volumeUuid);
            SessionParamsMarshmallow.grantedRuntimePermissions.set(params, grantedRuntimePermissions);
            return params;
        }
        PackageInstaller.SessionParams params = new PackageInstaller.SessionParams(mode);
        SessionParamsLOLLIPOP.installFlags.set(params, installFlags);
        SessionParamsLOLLIPOP.installLocation.set(params, installLocation);
        SessionParamsLOLLIPOP.sizeBytes.set(params, sizeBytes);
        SessionParamsLOLLIPOP.appPackageName.set(params, appPackageName);
        SessionParamsLOLLIPOP.appIcon.set(params, appIcon);
        SessionParamsLOLLIPOP.appLabel.set(params, appLabel);
        SessionParamsLOLLIPOP.appIconLastModified.set(params, appIconLastModified);
        SessionParamsLOLLIPOP.originatingUri.set(params, originatingUri);
        SessionParamsLOLLIPOP.referrerUri.set(params, referrerUri);
        SessionParamsLOLLIPOP.abiOverride.set(params, abiOverride);
        return params;
    }

    public static SessionParams create(PackageInstaller.SessionParams sessionParams) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            SessionParams params = new SessionParams(SessionParamsMarshmallow.mode.get(sessionParams));
            params.installFlags = SessionParamsMarshmallow.installFlags.get(sessionParams);
            params.installLocation = SessionParamsMarshmallow.installLocation.get(sessionParams);
            params.sizeBytes = SessionParamsMarshmallow.sizeBytes.get(sessionParams);
            params.appPackageName = SessionParamsMarshmallow.appPackageName.get(sessionParams);
            params.appIcon = SessionParamsMarshmallow.appIcon.get(sessionParams);
            params.appLabel = SessionParamsMarshmallow.appLabel.get(sessionParams);
            params.appIconLastModified = SessionParamsMarshmallow.appIconLastModified.get(sessionParams);
            params.originatingUri = SessionParamsMarshmallow.originatingUri.get(sessionParams);
            params.referrerUri = SessionParamsMarshmallow.referrerUri.get(sessionParams);
            params.abiOverride = SessionParamsMarshmallow.abiOverride.get(sessionParams);
            params.volumeUuid = SessionParamsMarshmallow.volumeUuid.get(sessionParams);
            params.grantedRuntimePermissions = SessionParamsMarshmallow.grantedRuntimePermissions.get(sessionParams);
            return params;
        }
        SessionParams params = new SessionParams(SessionParamsLOLLIPOP.mode.get(sessionParams));
        params.installFlags = SessionParamsLOLLIPOP.installFlags.get(sessionParams);
        params.installLocation = SessionParamsLOLLIPOP.installLocation.get(sessionParams);
        params.sizeBytes = SessionParamsLOLLIPOP.sizeBytes.get(sessionParams);
        params.appPackageName = SessionParamsLOLLIPOP.appPackageName.get(sessionParams);
        params.appIcon = SessionParamsLOLLIPOP.appIcon.get(sessionParams);
        params.appLabel = SessionParamsLOLLIPOP.appLabel.get(sessionParams);
        params.appIconLastModified = SessionParamsLOLLIPOP.appIconLastModified.get(sessionParams);
        params.originatingUri = SessionParamsLOLLIPOP.originatingUri.get(sessionParams);
        params.referrerUri = SessionParamsLOLLIPOP.referrerUri.get(sessionParams);
        params.abiOverride = SessionParamsLOLLIPOP.abiOverride.get(sessionParams);
        return params;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mode);
        dest.writeInt(this.installFlags);
        dest.writeInt(this.installLocation);
        dest.writeLong(this.sizeBytes);
        dest.writeString(this.appPackageName);
        dest.writeParcelable(this.appIcon, flags);
        dest.writeString(this.appLabel);
        dest.writeLong(this.appIconLastModified);
        dest.writeParcelable(this.originatingUri, flags);
        dest.writeParcelable(this.referrerUri, flags);
        dest.writeString(this.abiOverride);
        dest.writeString(this.volumeUuid);
        dest.writeStringArray(this.grantedRuntimePermissions);
    }

    protected SessionParams(Parcel in) {
        this.mode = in.readInt();
        this.installFlags = in.readInt();
        this.installLocation = in.readInt();
        this.sizeBytes = in.readLong();
        this.appPackageName = in.readString();
        this.appIcon = in.readParcelable(Bitmap.class.getClassLoader());
        this.appLabel = in.readString();
        this.appIconLastModified = in.readLong();
        this.originatingUri = in.readParcelable(Uri.class.getClassLoader());
        this.referrerUri = in.readParcelable(Uri.class.getClassLoader());
        this.abiOverride = in.readString();
        this.volumeUuid = in.readString();
        this.grantedRuntimePermissions = in.createStringArray();
    }

    public static final Parcelable.Creator<SessionParams> CREATOR = new Parcelable.Creator<SessionParams>() {
        @Override
        public SessionParams createFromParcel(Parcel source) {
            return new SessionParams(source);
        }

        @Override
        public SessionParams[] newArray(int size) {
            return new SessionParams[size];
        }
    };
}