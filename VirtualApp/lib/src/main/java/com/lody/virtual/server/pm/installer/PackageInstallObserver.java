package com.lody.virtual.server.pm.installer;

import android.content.Intent;
import android.content.pm.IPackageInstallObserver2;
import android.os.Bundle;

public class PackageInstallObserver {
    private final IPackageInstallObserver2.Stub mBinder = new IPackageInstallObserver2.Stub() {
        @Override
        public void onUserActionRequired(Intent intent) {
            PackageInstallObserver.this.onUserActionRequired(intent);
        }

        @Override
        public void onPackageInstalled(String basePackageName, int returnCode,
                                       String msg, Bundle extras) {
            PackageInstallObserver.this.onPackageInstalled(basePackageName, returnCode, msg,
                    extras);
        }
    };

    /**
     * {@hide}
     */
    public IPackageInstallObserver2 getBinder() {
        return mBinder;
    }

    public void onUserActionRequired(Intent intent) {
    }

    /**
     * This method will be called to report the result of the package
     * installation attempt.
     *
     * @param basePackageName Name of the package whose installation was
     *                        attempted
     * @param extras          If non-null, this Bundle contains extras providing
     *                        additional information about an install failure. See
     *                        {@link android.content.pm.PackageManager} for documentation
     *                        about which extras apply to various failures; in particular
     *                        the strings named EXTRA_FAILURE_*.
     * @param returnCode      The numeric success or failure code indicating the
     *                        basic outcome
     * @hide
     */
    public void onPackageInstalled(String basePackageName, int returnCode, String msg,
                                   Bundle extras) {
    }
}
