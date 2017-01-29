/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lody.virtual.server.pm.installer;

import android.annotation.TargetApi;
import android.content.pm.PackageInstaller;
import android.os.Build;

/**
 * Constants used internally between the PackageManager
 * and media container service transports.
 * Some utility methods to invoke MountService api.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class PackageHelper {

    /**
     * Installation return code: this is passed to the
     * IPackageInstallObserver on success.
     *
     * @hide
     */
    public static final int INSTALL_SUCCEEDED = 1;

    /**
     * Installation return code: this is passed to the
     * IPackageInstallObserver if the package is already installed.
     *
     * @hide
     */
    public static final int INSTALL_FAILED_ALREADY_EXISTS = -1;

    /**
     * Installation return code: this is passed to the
     * IPackageInstallObserver if the package archive file is invalid.
     *
     * @hide
     */
    public static final int INSTALL_FAILED_INVALID_APK = -2;

    /**
     * Installation return code: this is passed to the
     * IPackageInstallObserver if the URI passed in is invalid.
     *
     * @hide
     */
    public static final int INSTALL_FAILED_INVALID_URI = -3;

    /**
     * Installation return code: this is passed to the
     * IPackageInstallObserver if the package manager service found that
     * the device didn't have enough storage space to install the app.
     *
     * @hide
     */
    public static final int INSTALL_FAILED_INSUFFICIENT_STORAGE = -4;

    /**
     * Installation return code: this is passed to the
     * IPackageInstallObserver if a package is already installed with
     * the same name.
     *
     * @hide
     */
    public static final int INSTALL_FAILED_DUPLICATE_PACKAGE = -5;

    /**
     * Installation return code: this is passed to the
     * IPackageInstallObserver if the requested shared user does not
     * exist.
     *
     * @hide
     */
    public static final int INSTALL_FAILED_NO_SHARED_USER = -6;

    /**
     * Installation return code: this is passed to the
     * IPackageInstallObserver if a previously installed package of the
     * same name has a different signature than the new package (and the old
     * package's data was not removed).
     *
     * @hide
     */
    public static final int INSTALL_FAILED_UPDATE_INCOMPATIBLE = -7;

    /**
     * Installation return code: this is passed to the
     * IPackageInstallObserver if the new package is requested a shared
     * user which is already installed on the device and does not have matching
     * signature.
     *
     * @hide
     */
    public static final int INSTALL_FAILED_SHARED_USER_INCOMPATIBLE = -8;

    /**
     * Installation return code: this is passed to the
     * IPackageInstallObserver if the new package uses a shared library
     * that is not available.
     *
     * @hide
     */
    public static final int INSTALL_FAILED_MISSING_SHARED_LIBRARY = -9;

    /**
     * Installation return code: this is passed to the
     * IPackageInstallObserver if the new package uses a shared library
     * that is not available.
     *
     * @hide
     */
    public static final int INSTALL_FAILED_REPLACE_COULDNT_DELETE = -10;

    /**
     * Installation return code: this is passed to the
     * IPackageInstallObserver if the new package failed while
     * optimizing and validating its dex files, either because there was not
     * enough storage or the validation failed.
     *
     * @hide
     */
    public static final int INSTALL_FAILED_DEXOPT = -11;

    /**
     * Installation return code: this is passed to the
     * IPackageInstallObserver if the new package failed because the
     * current SDK version is older than that required by the package.
     *
     * @hide
     */
    public static final int INSTALL_FAILED_OLDER_SDK = -12;

    /**
     * Installation return code: this is passed to the
     * IPackageInstallObserver if the new package failed because it
     * contains a content provider with the same authority as a provider already
     * installed in the system.
     *
     * @hide
     */
    public static final int INSTALL_FAILED_CONFLICTING_PROVIDER = -13;

    /**
     * Installation return code: this is passed to the
     * IPackageInstallObserver if the new package failed because the
     * current SDK version is newer than that required by the package.
     *
     * @hide
     */
    public static final int INSTALL_FAILED_NEWER_SDK = -14;

    /**
     * Installation return code: this is passed to the
     * IPackageInstallObserver if the new package failed because it has
     * specified that it is a test-only package and the caller has not supplied
     * the NSTALL_ALLOW_TEST flag.
     *
     * @hide
     */
    public static final int INSTALL_FAILED_TEST_ONLY = -15;

    /**
     * Installation return code: this is passed to the
     * IPackageInstallObserver if the package being installed contains
     * native code, but none that is compatible with the device's CPU_ABI.
     *
     * @hide
     */
    public static final int INSTALL_FAILED_CPU_ABI_INCOMPATIBLE = -16;

    /**
     * Installation return code: this is passed to the
     * IPackageInstallObserver if the new package uses a feature that is
     * not available.
     *
     * @hide
     */
    public static final int INSTALL_FAILED_MISSING_FEATURE = -17;

    // ------ Errors related to sdcard
    /**
     * Installation return code: this is passed to the
     * IPackageInstallObserver if a secure container mount point
     * couldn't be accessed on external media.
     *
     * @hide
     */
    public static final int INSTALL_FAILED_CONTAINER_ERROR = -18;

    /**
     * Installation return code: this is passed to the
     * IPackageInstallObserver if the new package couldn't be installed
     * in the specified install location.
     *
     * @hide
     */
    public static final int INSTALL_FAILED_INVALID_INSTALL_LOCATION = -19;

    /**
     * Installation return code: this is passed to the
     * IPackageInstallObserver if the new package couldn't be installed
     * in the specified install location because the media is not available.
     *
     * @hide
     */
    public static final int INSTALL_FAILED_MEDIA_UNAVAILABLE = -20;

    /**
     * Installation return code: this is passed to the
     * IPackageInstallObserver if the new package couldn't be installed
     * because the verification timed out.
     *
     * @hide
     */
    public static final int INSTALL_FAILED_VERIFICATION_TIMEOUT = -21;

    /**
     * Installation return code: this is passed to the
     * IPackageInstallObserver if the new package couldn't be installed
     * because the verification did not succeed.
     *
     * @hide
     */
    public static final int INSTALL_FAILED_VERIFICATION_FAILURE = -22;

    /**
     * Installation return code: this is passed to the
     * IPackageInstallObserver if the package changed from what the
     * calling program expected.
     *
     * @hide
     */
    public static final int INSTALL_FAILED_PACKAGE_CHANGED = -23;

    /**
     * Installation return code: this is passed to the
     * IPackageInstallObserver if the new package is assigned a
     * different UID than it previously held.
     *
     * @hide
     */
    public static final int INSTALL_FAILED_UID_CHANGED = -24;

    /**
     * Installation return code: this is passed to the
     * IPackageInstallObserver if the new package has an older version
     * code than the currently installed package.
     *
     * @hide
     */
    public static final int INSTALL_FAILED_VERSION_DOWNGRADE = -25;

    /**
     * Installation return code: this is passed to the
     * IPackageInstallObserver if the old package has target SDK high
     * enough to support runtime permission and the new package has target SDK
     * low enough to not support runtime permissions.
     *
     * @hide
     */
    public static final int INSTALL_FAILED_PERMISSION_MODEL_DOWNGRADE = -26;

    /**
     * Installation parse return code: this is passed to the
     * IPackageInstallObserver if the parser was given a path that is
     * not a file, or does not end with the expected '.apk' extension.
     *
     * @hide
     */
    public static final int INSTALL_PARSE_FAILED_NOT_APK = -100;

    /**
     * Installation parse return code: this is passed to the
     * IPackageInstallObserver if the parser was unable to retrieve the
     * AndroidManifest.xml file.
     *
     * @hide
     */
    public static final int INSTALL_PARSE_FAILED_BAD_MANIFEST = -101;

    /**
     * Installation parse return code: this is passed to the
     * IPackageInstallObserver if the parser encountered an unexpected
     * exception.
     *
     * @hide
     */
    public static final int INSTALL_PARSE_FAILED_UNEXPECTED_EXCEPTION = -102;

    /**
     * Installation parse return code: this is passed to the
     * IPackageInstallObserver if the parser did not find any
     * certificates in the .apk.
     *
     * @hide
     */
    public static final int INSTALL_PARSE_FAILED_NO_CERTIFICATES = -103;

    /**
     * Installation parse return code: this is passed to the
     * IPackageInstallObserver if the parser found inconsistent
     * certificates on the files in the .apk.
     *
     * @hide
     */
    public static final int INSTALL_PARSE_FAILED_INCONSISTENT_CERTIFICATES = -104;

    /**
     * Installation parse return code: this is passed to the
     * IPackageInstallObserver if the parser encountered a
     * CertificateEncodingException in one of the files in the .apk.
     *
     * @hide
     */
    public static final int INSTALL_PARSE_FAILED_CERTIFICATE_ENCODING = -105;

    /**
     * Installation parse return code: this is passed to the
     * IPackageInstallObserver if the parser encountered a bad or
     * missing package name in the manifest.
     *
     * @hide
     */
    public static final int INSTALL_PARSE_FAILED_BAD_PACKAGE_NAME = -106;

    /**
     * Installation parse return code: this is passed to the
     * IPackageInstallObserver if the parser encountered a bad shared
     * user id name in the manifest.
     *
     * @hide
     */
    public static final int INSTALL_PARSE_FAILED_BAD_SHARED_USER_ID = -107;

    /**
     * Installation parse return code: this is passed to the
     * IPackageInstallObserver if the parser encountered some structural
     * problem in the manifest.
     *
     * @hide
     */
    public static final int INSTALL_PARSE_FAILED_MANIFEST_MALFORMED = -108;

    /**
     * Installation parse return code: this is passed to the
     * IPackageInstallObserver if the parser did not find any actionable
     * tags (instrumentation or application) in the manifest.
     *
     * @hide
     */
    public static final int INSTALL_PARSE_FAILED_MANIFEST_EMPTY = -109;

    /**
     * Installation failed return code: this is passed to the
     * IPackageInstallObserver if the system failed to install the
     * package because of system issues.
     *
     * @hide
     */
    public static final int INSTALL_FAILED_INTERNAL_ERROR = -110;

    /**
     * Installation failed return code: this is passed to the
     * IPackageInstallObserver if the system failed to install the
     * package because the user is restricted from installing apps.
     *
     * @hide
     */
    public static final int INSTALL_FAILED_USER_RESTRICTED = -111;

    /**
     * Installation failed return code: this is passed to the
     * IPackageInstallObserver if the system failed to install the
     * package because it is attempting to define a permission that is already
     * defined by some existing package.
     * <p>
     * The package name of the app which has already defined the permission is
     * passed to a PackageInstallObserver, if any, as the
     * EXTRA_FAILURE_EXISTING_PACKAGE string extra; and the name of the
     * permission being redefined is passed in the
     * EXTRA_FAILURE_EXISTING_PERMISSION string extra.
     *
     * @hide
     */
    public static final int INSTALL_FAILED_DUPLICATE_PERMISSION = -112;

    /**
     * Installation failed return code: this is passed to the
     * IPackageInstallObserver if the system failed to install the
     * package because its packaged native code did not match any of the ABIs
     * supported by the system.
     *
     * @hide
     */
    public static final int INSTALL_FAILED_NO_MATCHING_ABIS = -113;

    /**
     * Internal return code for NativeLibraryHelper methods to indicate that the package
     * being processed did not contain any native code. This is placed here only so that
     * it can belong to the same value space as the other install failure codes.
     *
     * @hide
     */
    public static final int NO_NATIVE_LIBRARIES = -114;

    /** {@hide} */
    public static final int INSTALL_FAILED_ABORTED = -115;

    /**
     * Installation failed return code: ephemeral app installs are incompatible with some
     * other installation flags supplied for the operation; or other circumstances such
     * as trying to upgrade a system app via an ephemeral install.
     * @hide
     */
    public static final int INSTALL_FAILED_EPHEMERAL_INVALID = -116;

    
    
    
    /**
            * Return code for when package deletion succeeds. This is passed to the
     * IPackageDeleteObserver if the system succeeded in deleting the
     * package.
             *
             * @hide
     */
    public static final int DELETE_SUCCEEDED = 1;

    /**
     * Deletion failed return code: this is passed to the
     * IPackageDeleteObserver if the system failed to delete the package
     * for an unspecified reason.
     *
     * @hide
     */
    public static final int DELETE_FAILED_INTERNAL_ERROR = -1;

    /**
     * Deletion failed return code: this is passed to the
     * IPackageDeleteObserver if the system failed to delete the package
     * because it is the active DevicePolicy manager.
     *
     * @hide
     */
    public static final int DELETE_FAILED_DEVICE_POLICY_MANAGER = -2;

    /**
     * Deletion failed return code: this is passed to the
     * IPackageDeleteObserver if the system failed to delete the package
     * since the user is restricted.
     *
     * @hide
     */
    public static final int DELETE_FAILED_USER_RESTRICTED = -3;

    /**
     * Deletion failed return code: this is passed to the
     * IPackageDeleteObserver if the system failed to delete the package
     * because a profile or device owner has marked the package as
     * uninstallable.
     *
     * @hide
     */
    public static final int DELETE_FAILED_OWNER_BLOCKED = -4;

    /** {@hide} */
    public static final int DELETE_FAILED_ABORTED = -5;

    /**
     * Return code that is passed to the IPackageMoveObserver when the
     * package has been successfully moved by the system.
     *
     * @hide
     */
    public static final int MOVE_SUCCEEDED = -100;

    /**
     * Error code that is passed to the IPackageMoveObserver when the
     * package hasn't been successfully moved by the system because of
     * insufficient memory on specified media.
     *
     * @hide
     */
    public static final int MOVE_FAILED_INSUFFICIENT_STORAGE = -1;

    /**
     * Error code that is passed to the IPackageMoveObserver if the
     * specified package doesn't exist.
     *
     * @hide
     */
    public static final int MOVE_FAILED_DOESNT_EXIST = -2;

    /**
     * Error code that is passed to the IPackageMoveObserver if the
     * specified package cannot be moved since its a system package.
     *
     * @hide
     */
    public static final int MOVE_FAILED_SYSTEM_PACKAGE = -3;

    /**
     * Error code that is passed to the IPackageMoveObserver if the
     * specified package cannot be moved since its forward locked.
     *
     * @hide
     */
    public static final int MOVE_FAILED_FORWARD_LOCKED = -4;

    /**
     * Error code that is passed to the IPackageMoveObserver if the
     * specified package cannot be moved to the specified location.
     *
     * @hide
     */
    public static final int MOVE_FAILED_INVALID_LOCATION = -5;

    /**
     * Error code that is passed to the IPackageMoveObserver if the
     * specified package cannot be moved to the specified location.
     *
     * @hide
     */
    public static final int MOVE_FAILED_INTERNAL_ERROR = -6;

    /**
     * Error code that is passed to the IPackageMoveObserver if the
     * specified package already has an operation pending in the queue.
     *
     * @hide
     */
    public static final int MOVE_FAILED_OPERATION_PENDING = -7;

    /**
     * Error code that is passed to the IPackageMoveObserver if the
     * specified package cannot be moved since it contains a device admin.
     *
     * @hide
     */
    public static final int MOVE_FAILED_DEVICE_ADMIN = -8;


    public static String installStatusToString(int status, String msg) {
        final String str = installStatusToString(status);
        if (msg != null) {
            return str + ": " + msg;
        } else {
            return str;
        }
    }

    /** {@hide} */
    public static String installStatusToString(int status) {
        switch (status) {
            case INSTALL_SUCCEEDED: return "INSTALL_SUCCEEDED";
            case INSTALL_FAILED_ALREADY_EXISTS: return "INSTALL_FAILED_ALREADY_EXISTS";
            case INSTALL_FAILED_INVALID_APK: return "INSTALL_FAILED_INVALID_APK";
            case INSTALL_FAILED_INVALID_URI: return "INSTALL_FAILED_INVALID_URI";
            case INSTALL_FAILED_INSUFFICIENT_STORAGE: return "INSTALL_FAILED_INSUFFICIENT_STORAGE";
            case INSTALL_FAILED_DUPLICATE_PACKAGE: return "INSTALL_FAILED_DUPLICATE_PACKAGE";
            case INSTALL_FAILED_NO_SHARED_USER: return "INSTALL_FAILED_NO_SHARED_USER";
            case INSTALL_FAILED_UPDATE_INCOMPATIBLE: return "INSTALL_FAILED_UPDATE_INCOMPATIBLE";
            case INSTALL_FAILED_SHARED_USER_INCOMPATIBLE: return "INSTALL_FAILED_SHARED_USER_INCOMPATIBLE";
            case INSTALL_FAILED_MISSING_SHARED_LIBRARY: return "INSTALL_FAILED_MISSING_SHARED_LIBRARY";
            case INSTALL_FAILED_REPLACE_COULDNT_DELETE: return "INSTALL_FAILED_REPLACE_COULDNT_DELETE";
            case INSTALL_FAILED_DEXOPT: return "INSTALL_FAILED_DEXOPT";
            case INSTALL_FAILED_OLDER_SDK: return "INSTALL_FAILED_OLDER_SDK";
            case INSTALL_FAILED_CONFLICTING_PROVIDER: return "INSTALL_FAILED_CONFLICTING_PROVIDER";
            case INSTALL_FAILED_NEWER_SDK: return "INSTALL_FAILED_NEWER_SDK";
            case INSTALL_FAILED_TEST_ONLY: return "INSTALL_FAILED_TEST_ONLY";
            case INSTALL_FAILED_CPU_ABI_INCOMPATIBLE: return "INSTALL_FAILED_CPU_ABI_INCOMPATIBLE";
            case INSTALL_FAILED_MISSING_FEATURE: return "INSTALL_FAILED_MISSING_FEATURE";
            case INSTALL_FAILED_CONTAINER_ERROR: return "INSTALL_FAILED_CONTAINER_ERROR";
            case INSTALL_FAILED_INVALID_INSTALL_LOCATION: return "INSTALL_FAILED_INVALID_INSTALL_LOCATION";
            case INSTALL_FAILED_MEDIA_UNAVAILABLE: return "INSTALL_FAILED_MEDIA_UNAVAILABLE";
            case INSTALL_FAILED_VERIFICATION_TIMEOUT: return "INSTALL_FAILED_VERIFICATION_TIMEOUT";
            case INSTALL_FAILED_VERIFICATION_FAILURE: return "INSTALL_FAILED_VERIFICATION_FAILURE";
            case INSTALL_FAILED_PACKAGE_CHANGED: return "INSTALL_FAILED_PACKAGE_CHANGED";
            case INSTALL_FAILED_UID_CHANGED: return "INSTALL_FAILED_UID_CHANGED";
            case INSTALL_FAILED_VERSION_DOWNGRADE: return "INSTALL_FAILED_VERSION_DOWNGRADE";
            case INSTALL_PARSE_FAILED_NOT_APK: return "INSTALL_PARSE_FAILED_NOT_APK";
            case INSTALL_PARSE_FAILED_BAD_MANIFEST: return "INSTALL_PARSE_FAILED_BAD_MANIFEST";
            case INSTALL_PARSE_FAILED_UNEXPECTED_EXCEPTION: return "INSTALL_PARSE_FAILED_UNEXPECTED_EXCEPTION";
            case INSTALL_PARSE_FAILED_NO_CERTIFICATES: return "INSTALL_PARSE_FAILED_NO_CERTIFICATES";
            case INSTALL_PARSE_FAILED_INCONSISTENT_CERTIFICATES: return "INSTALL_PARSE_FAILED_INCONSISTENT_CERTIFICATES";
            case INSTALL_PARSE_FAILED_CERTIFICATE_ENCODING: return "INSTALL_PARSE_FAILED_CERTIFICATE_ENCODING";
            case INSTALL_PARSE_FAILED_BAD_PACKAGE_NAME: return "INSTALL_PARSE_FAILED_BAD_PACKAGE_NAME";
            case INSTALL_PARSE_FAILED_BAD_SHARED_USER_ID: return "INSTALL_PARSE_FAILED_BAD_SHARED_USER_ID";
            case INSTALL_PARSE_FAILED_MANIFEST_MALFORMED: return "INSTALL_PARSE_FAILED_MANIFEST_MALFORMED";
            case INSTALL_PARSE_FAILED_MANIFEST_EMPTY: return "INSTALL_PARSE_FAILED_MANIFEST_EMPTY";
            case INSTALL_FAILED_INTERNAL_ERROR: return "INSTALL_FAILED_INTERNAL_ERROR";
            case INSTALL_FAILED_USER_RESTRICTED: return "INSTALL_FAILED_USER_RESTRICTED";
            case INSTALL_FAILED_DUPLICATE_PERMISSION: return "INSTALL_FAILED_DUPLICATE_PERMISSION";
            case INSTALL_FAILED_NO_MATCHING_ABIS: return "INSTALL_FAILED_NO_MATCHING_ABIS";
            case INSTALL_FAILED_ABORTED: return "INSTALL_FAILED_ABORTED";
            default: return Integer.toString(status);
        }
    }

    /** {@hide} */
    public static int installStatusToPublicStatus(int status) {
        switch (status) {
            case INSTALL_SUCCEEDED: return PackageInstaller.STATUS_SUCCESS;
            case INSTALL_FAILED_ALREADY_EXISTS: return PackageInstaller.STATUS_FAILURE_CONFLICT;
            case INSTALL_FAILED_INVALID_APK: return PackageInstaller.STATUS_FAILURE_INVALID;
            case INSTALL_FAILED_INVALID_URI: return PackageInstaller.STATUS_FAILURE_INVALID;
            case INSTALL_FAILED_INSUFFICIENT_STORAGE: return PackageInstaller.STATUS_FAILURE_STORAGE;
            case INSTALL_FAILED_DUPLICATE_PACKAGE: return PackageInstaller.STATUS_FAILURE_CONFLICT;
            case INSTALL_FAILED_NO_SHARED_USER: return PackageInstaller.STATUS_FAILURE_CONFLICT;
            case INSTALL_FAILED_UPDATE_INCOMPATIBLE: return PackageInstaller.STATUS_FAILURE_CONFLICT;
            case INSTALL_FAILED_SHARED_USER_INCOMPATIBLE: return PackageInstaller.STATUS_FAILURE_CONFLICT;
            case INSTALL_FAILED_MISSING_SHARED_LIBRARY: return PackageInstaller.STATUS_FAILURE_INCOMPATIBLE;
            case INSTALL_FAILED_REPLACE_COULDNT_DELETE: return PackageInstaller.STATUS_FAILURE_CONFLICT;
            case INSTALL_FAILED_DEXOPT: return PackageInstaller.STATUS_FAILURE_INVALID;
            case INSTALL_FAILED_OLDER_SDK: return PackageInstaller.STATUS_FAILURE_INCOMPATIBLE;
            case INSTALL_FAILED_CONFLICTING_PROVIDER: return PackageInstaller.STATUS_FAILURE_CONFLICT;
            case INSTALL_FAILED_NEWER_SDK: return PackageInstaller.STATUS_FAILURE_INCOMPATIBLE;
            case INSTALL_FAILED_TEST_ONLY: return PackageInstaller.STATUS_FAILURE_INVALID;
            case INSTALL_FAILED_CPU_ABI_INCOMPATIBLE: return PackageInstaller.STATUS_FAILURE_INCOMPATIBLE;
            case INSTALL_FAILED_MISSING_FEATURE: return PackageInstaller.STATUS_FAILURE_INCOMPATIBLE;
            case INSTALL_FAILED_CONTAINER_ERROR: return PackageInstaller.STATUS_FAILURE_STORAGE;
            case INSTALL_FAILED_INVALID_INSTALL_LOCATION: return PackageInstaller.STATUS_FAILURE_STORAGE;
            case INSTALL_FAILED_MEDIA_UNAVAILABLE: return PackageInstaller.STATUS_FAILURE_STORAGE;
            case INSTALL_FAILED_VERIFICATION_TIMEOUT: return PackageInstaller.STATUS_FAILURE_ABORTED;
            case INSTALL_FAILED_VERIFICATION_FAILURE: return PackageInstaller.STATUS_FAILURE_ABORTED;
            case INSTALL_FAILED_PACKAGE_CHANGED: return PackageInstaller.STATUS_FAILURE_INVALID;
            case INSTALL_FAILED_UID_CHANGED: return PackageInstaller.STATUS_FAILURE_INVALID;
            case INSTALL_FAILED_VERSION_DOWNGRADE: return PackageInstaller.STATUS_FAILURE_INVALID;
            case INSTALL_FAILED_PERMISSION_MODEL_DOWNGRADE: return PackageInstaller.STATUS_FAILURE_INVALID;
            case INSTALL_PARSE_FAILED_NOT_APK: return PackageInstaller.STATUS_FAILURE_INVALID;
            case INSTALL_PARSE_FAILED_BAD_MANIFEST: return PackageInstaller.STATUS_FAILURE_INVALID;
            case INSTALL_PARSE_FAILED_UNEXPECTED_EXCEPTION: return PackageInstaller.STATUS_FAILURE_INVALID;
            case INSTALL_PARSE_FAILED_NO_CERTIFICATES: return PackageInstaller.STATUS_FAILURE_INVALID;
            case INSTALL_PARSE_FAILED_INCONSISTENT_CERTIFICATES: return PackageInstaller.STATUS_FAILURE_INVALID;
            case INSTALL_PARSE_FAILED_CERTIFICATE_ENCODING: return PackageInstaller.STATUS_FAILURE_INVALID;
            case INSTALL_PARSE_FAILED_BAD_PACKAGE_NAME: return PackageInstaller.STATUS_FAILURE_INVALID;
            case INSTALL_PARSE_FAILED_BAD_SHARED_USER_ID: return PackageInstaller.STATUS_FAILURE_INVALID;
            case INSTALL_PARSE_FAILED_MANIFEST_MALFORMED: return PackageInstaller.STATUS_FAILURE_INVALID;
            case INSTALL_PARSE_FAILED_MANIFEST_EMPTY: return PackageInstaller.STATUS_FAILURE_INVALID;
            case INSTALL_FAILED_INTERNAL_ERROR: return PackageInstaller.STATUS_FAILURE;
            case INSTALL_FAILED_USER_RESTRICTED: return PackageInstaller.STATUS_FAILURE_INCOMPATIBLE;
            case INSTALL_FAILED_DUPLICATE_PERMISSION: return PackageInstaller.STATUS_FAILURE_CONFLICT;
            case INSTALL_FAILED_NO_MATCHING_ABIS: return PackageInstaller.STATUS_FAILURE_INCOMPATIBLE;
            case INSTALL_FAILED_ABORTED: return PackageInstaller.STATUS_FAILURE_ABORTED;
            default: return PackageInstaller.STATUS_FAILURE;
        }
    }


    public static String deleteStatusToString(boolean status) {
        return status ? "DELETE_SUCCEEDED" : "DELETE_FAILED";
    }

}
