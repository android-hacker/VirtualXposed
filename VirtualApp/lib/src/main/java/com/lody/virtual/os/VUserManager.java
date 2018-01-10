package com.lody.virtual.os;

import android.graphics.Bitmap;
import android.os.RemoteException;
import android.util.Log;

import com.lody.virtual.helper.ipcbus.IPCBus;
import com.lody.virtual.server.interfaces.IUserManager;

import java.util.List;

/**
 * Manages users and user details on a multi-user system.
 */
public class VUserManager {

    private static String TAG = "VUserManager";
    private final IUserManager mService;

    /**
     * Key for user restrictions. Specifies if a user is disallowed from adding and removing
     * accounts.
     * The default value is <code>false</code>.
     * <p/>
     * Type: Boolean
     */
    public static final String DISALLOW_MODIFY_ACCOUNTS = "no_modify_accounts";

    /**
     * Key for user restrictions. Specifies if a user is disallowed from changing Wi-Fi
     * access points.
     * The default value is <code>false</code>.
     * <p/>
     * Type: Boolean
     */
    public static final String DISALLOW_CONFIG_WIFI = "no_config_wifi";

    /**
     * Key for user restrictions. Specifies if a user is disallowed from installing applications.
     * The default value is <code>false</code>.
     * <p/>
     * Type: Boolean
     */
    public static final String DISALLOW_INSTALL_APPS = "no_install_apps";

    /**
     * Key for user restrictions. Specifies if a user is disallowed from uninstalling applications.
     * The default value is <code>false</code>.
     * <p/>
     * Type: Boolean
     */
    public static final String DISALLOW_UNINSTALL_APPS = "no_uninstall_apps";

    /**
     * Key for user restrictions. Specifies if a user is disallowed from toggling location sharing.
     * The default value is <code>false</code>.
     * <p/>
     * Type: Boolean
     */

    public static final String DISALLOW_SHARE_LOCATION = "no_share_location";

    /**
     * Key for user restrictions. Specifies if a user is disallowed from enabling the
     * "Unknown Sources" setting, that allows installation of apps from unknown sources.
     * The default value is <code>false</code>.
     * <p/>
     * Type: Boolean
     */
    public static final String DISALLOW_INSTALL_UNKNOWN_SOURCES = "no_install_unknown_sources";

    /**
     * Key for user restrictions. Specifies if a user is disallowed from configuring bluetooth.
     * The default value is <code>false</code>.
     * <p/>
     * Type: Boolean
     */
    public static final String DISALLOW_CONFIG_BLUETOOTH = "no_config_bluetooth";

    /**
     * Key for user restrictions. Specifies if a user is disallowed from transferring files over
     * USB. The default value is <code>false</code>.
     * <p/>
     * Type: Boolean
     */
    public static final String DISALLOW_USB_FILE_TRANSFER = "no_usb_file_transfer";

    /**
     * Key for user restrictions. Specifies if a user is disallowed from configuring user
     * credentials. The default value is <code>false</code>.
     * <p/>
     * Type: Boolean
     */
    public static final String DISALLOW_CONFIG_CREDENTIALS = "no_config_credentials";

    /**
     * Key for user restrictions. Specifies if a user is disallowed from removing users.
     * The default value is <code>false</code>.
     * <p/>
     * Type: Boolean
     */
    public static final String DISALLOW_REMOVE_USER = "no_remove_user";

    private static VUserManager sInstance = null;

    /** @hide */
    public synchronized static VUserManager get() {
        if (sInstance == null) {
            IUserManager remote = IPCBus.get(IUserManager.class);
            sInstance = new VUserManager(remote);
        }
        return sInstance;
    }

    /** @hide */
    public VUserManager(IUserManager service) {
        mService = service;
    }

    /**
     * Returns whether the system supports multiple users.
     * @return true if multiple users can be created, false if it is a single user device.
     * @hide
     */
    public static boolean supportsMultipleUsers() {
        return getMaxSupportedUsers() > 1;
    }

    /**
     * Returns the user handle for the user that this application is running for.
     * @return the user handle of the user making this call.
     * @hide
     */
    public int getUserHandle() {
        return VUserHandle.myUserId();
    }

    /**
     * Returns the user name of the user making this call.  This call is only
     * available to applications on the system image; it requires the
     * MANAGE_USERS permission.
     * @return the user name
     */
    public String getUserName() {
        try {
            return mService.getUserInfo(getUserHandle()).name;
        } catch (RemoteException re) {
            Log.w(TAG, "Could not get user name", re);
            return "";
        }
    }

   /**
     * Used to determine whether the user making this call is subject to
     * teleportations.
     * @return whether the user making this call is a goat
     */
    public boolean isUserAGoat() {
        return false;
    }

    /**
     * Returns the UserInfo object describing a specific user.
     * @param handle the user handle of the user whose information is being requested.
     * @return the UserInfo object for a specific user.
     * @hide
     */
    public VUserInfo getUserInfo(int handle) {
        try {
            return mService.getUserInfo(handle);
        } catch (RemoteException re) {
            Log.w(TAG, "Could not get user info", re);
            return null;
        }
    }

    /**
     * Return the serial number for a user.  This is a device-unique
     * number assigned to that user; if the user is deleted and then a new
     * user created, the new users will not be given the same serial number.
     * @param user The user whose serial number is to be retrieved.
     * @return The serial number of the given user; returns -1 if the
     * given VUserHandle does not exist.
     * @see #getUserForSerialNumber(long)
     */
    public long getSerialNumberForUser(VUserHandle user) {
        return getUserSerialNumber(user.getIdentifier());
    }

    /**
     * Return the user associated with a serial number previously
     * returned by {@link #getSerialNumberForUser(VUserHandle)}.
     * @param serialNumber The serial number of the user that is being
     * retrieved.
     * @return Return the user associated with the serial number, or null
     * if there is not one.
     * @see #getSerialNumberForUser(VUserHandle)
     */
    public VUserHandle getUserForSerialNumber(long serialNumber) {
        int ident = getUserHandle((int)serialNumber);
        return ident >= 0 ? new VUserHandle(ident) : null;
    }

    /**
     * Creates a user with the specified name and options.
     *
     * @param name the user's name
     * @param flags flags that identify the type of user and other properties.
     * @see VUserInfo
     *
     * @return the UserInfo object for the created user, or null if the user could not be created.
     * @hide
     */
    public VUserInfo createUser(String name, int flags) {
        try {
            return mService.createUser(name, flags);
        } catch (RemoteException re) {
            Log.w(TAG, "Could not create a user", re);
            return null;
        }
    }

    /**
     * Return the number of users currently created on the device.
     */
    public int getUserCount() {
        List<VUserInfo> users = getUsers();
        return users != null ? users.size() : 1;
    }

    /**
     * Returns information for all users on this device.
     * @return the list of users that were created.
     * @hide
     */
    public List<VUserInfo> getUsers() {
        try {
            return mService.getUsers(false);
        } catch (RemoteException re) {
            Log.w(TAG, "Could not get user list", re);
            return null;
        }
    }

    /**
     * Returns information for all users on this device.
     * @param excludeDying specify if the list should exclude users being removed.
     * @return the list of users that were created.
     * @hide
     */
    public List<VUserInfo> getUsers(boolean excludeDying) {
        try {
            return mService.getUsers(excludeDying);
        } catch (RemoteException re) {
            Log.w(TAG, "Could not get user list", re);
            return null;
        }
    }

    /**
     * Removes a user and all associated data.
     * @param handle the integer handle of the user, where 0 is the primary user.
     * @hide
     */
    public boolean removeUser(int handle) {
        try {
            return mService.removeUser(handle);
        } catch (RemoteException re) {
            Log.w(TAG, "Could not remove user ", re);
            return false;
        }
    }

    /**
     * Updates the user's name.
     *
     * @param handle the user's integer handle
     * @param name the new name for the user
     * @hide
     */
    public void setUserName(int handle, String name) {
        try {
            mService.setUserName(handle, name);
        } catch (RemoteException re) {
            Log.w(TAG, "Could not set the user name ", re);
        }
    }

    /**
     * Sets the user's photo.
     * @param handle the user for whom to change the photo.
     * @param icon the bitmap to set as the photo.
     * @hide
     */
    public void setUserIcon(int handle, Bitmap icon) {
        try {
            mService.setUserIcon(handle, icon);
        } catch (RemoteException re) {
            Log.w(TAG, "Could not set the user icon ", re);
        }
    }

    /**
     * Returns a file descriptor for the user's photo. PNG data can be read from this file.
     * @param handle the user whose photo we want to read.
     * @return a {@link Bitmap} of the user's photo, or null if there's no photo.
     * @hide
     */
    public Bitmap getUserIcon(int handle) {
        try {
            return mService.getUserIcon(handle);
        } catch (RemoteException re) {
            Log.w(TAG, "Could not get the user icon ", re);
            return null;
        }
    }

    /**
     * Enable or disable the use of a guest account. If disabled, the existing guest account
     * will be wiped.
     * @param enable whether to enable a guest account.
     * @hide
     */
    public void setGuestEnabled(boolean enable) {
        try {
            mService.setGuestEnabled(enable);
        } catch (RemoteException re) {
            Log.w(TAG, "Could not change guest account availability to " + enable);
        }
    }

    /**
     * Checks if a guest user is enabled for this device.
     * @return whether a guest user is enabled
     * @hide
     */
    public boolean isGuestEnabled() {
        try {
            return mService.isGuestEnabled();
        } catch (RemoteException re) {
            Log.w(TAG, "Could not retrieve guest enabled state");
            return false;
        }
    }

    /**
     * Wipes all the data for a user, but doesn't remove the user.
     * @param handle
     * @hide
     */
    public void wipeUser(int handle) {
        try {
            mService.wipeUser(handle);
        } catch (RemoteException re) {
            Log.w(TAG, "Could not wipe user " + handle);
        }
    }

    /**
     * Returns the maximum number of users that can be created on this device. A return value
     * of 1 means that it is a single user device.
     * @hide
     * @return a value greater than or equal to 1
     */
    public static int getMaxSupportedUsers() {
        return Integer.MAX_VALUE;
    }

    /**
     * Returns a serial number on this device for a given VUserHandle. User handles can be recycled
     * when deleting and creating users, but serial numbers are not reused until the device is wiped.
     * @param handle
     * @return a serial number associated with that user, or -1 if the VUserHandle is not valid.
     * @hide
     */
    public int getUserSerialNumber(int handle) {
        try {
            return mService.getUserSerialNumber(handle);
        } catch (RemoteException re) {
            Log.w(TAG, "Could not get serial number for user " + handle);
        }
        return -1;
    }

    /**
     * Returns a VUserHandle on this device for a given user serial number. User handles can be
     * recycled when deleting and creating users, but serial numbers are not reused until the device
     * is wiped.
     * @param userSerialNumber
     * @return the VUserHandle associated with that user serial number, or -1 if the serial number
     * is not valid.
     * @hide
     */
    public int getUserHandle(int userSerialNumber) {
        try {
            return mService.getUserHandle(userSerialNumber);
        } catch (RemoteException re) {
            Log.w(TAG, "Could not get VUserHandle for user " + userSerialNumber);
        }
        return -1;
    }


}
