package com.lody.virtual.server.pm.installer;


import android.annotation.TargetApi;
import android.content.pm.PackageInfo;
import android.content.pm.PackageInstaller;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;

import mirror.android.content.pm.PackageInstaller.SessionParamsLOLLIPOP;
import mirror.android.content.pm.PackageInstaller.SessionParamsMarshmallow;


@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class VSessionParams {

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

    public VSessionParams(int i) {
        mode = i;
    }

    public int describeContents() {
        return 0;
    }



    public PackageInstaller.SessionParams a() {
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

    public static VSessionParams a(PackageInstaller.SessionParams sessionParams) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            VSessionParams params = new VSessionParams(SessionParamsMarshmallow.mode.get(sessionParams));
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
        VSessionParams params = new VSessionParams(SessionParamsLOLLIPOP.mode.get(sessionParams));
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

}