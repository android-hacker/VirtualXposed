/*
 * Copyright (C) 2011 The Android Open Source Project
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

package com.lody.virtual.os;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Process;
import android.util.SparseArray;

import com.lody.virtual.client.VClientImpl;

import java.io.PrintWriter;

/**
 * Representation of a user on the device.
 */
public final class VUserHandle implements Parcelable {
    /**
     * @hide Range of uids allocated for a user.
     */
    public static final int PER_USER_RANGE = 100000;

    /** @hide A user id to indicate all users on the device */
    public static final int USER_ALL = -1;

    /** @hide A user handle to indicate all users on the device */
    public static final VUserHandle ALL = new VUserHandle(USER_ALL);

    /** @hide A user id to indicate the currently active user */
    public static final int USER_CURRENT = -2;

    /** @hide A user handle to indicate the current user of the device */
    public static final VUserHandle CURRENT = new VUserHandle(USER_CURRENT);

    /** @hide A user id to indicate that we would like to send to the current
     *  user, but if this is calling from a user process then we will send it
     *  to the caller's user instead of failing with a security exception */
    public static final int USER_CURRENT_OR_SELF = -3;

    /** @hide A user handle to indicate that we would like to send to the current
     *  user, but if this is calling from a user process then we will send it
     *  to the caller's user instead of failing with a security exception */
    public static final VUserHandle CURRENT_OR_SELF = new VUserHandle(USER_CURRENT_OR_SELF);

    /** @hide An undefined user id */
    public static final int USER_NULL = -10000;

    /** @hide A user id constant to indicate the "owner" user of the device */
    public static final int USER_OWNER = 0;

    /** @hide A user handle to indicate the primary/owner user of the device */
    public static final VUserHandle OWNER = new VUserHandle(USER_OWNER);

    /**
     * @hide Enable multi-user related side effects. Set this to false if
     * there are problems with single user use-cases.
     */
    public static final boolean MU_ENABLED = true;
    /**
     * First gid for applications to share resources. Used when forward-locking
     * is enabled but all UserHandles need to be able to read the resources.
     * @hide
     */
    public static final int FIRST_SHARED_APPLICATION_GID = 50000;
    /**
     * Last gid for applications to share resources. Used when forward-locking
     * is enabled but all UserHandles need to be able to read the resources.
     * @hide
     */
    public static final int LAST_SHARED_APPLICATION_GID = 59999;
    /**
     * First vuid used for fully isolated sandboxed processes (with no permissions of their own)
     * @hide
     */
    public static final int FIRST_ISOLATED_UID = 99000;
    /**
     * Last vuid used for fully isolated sandboxed processes (with no permissions of their own)
     * @hide
     */
    public static final int LAST_ISOLATED_UID = 99999;
    public static final Parcelable.Creator<VUserHandle> CREATOR
            = new Parcelable.Creator<VUserHandle>() {
        public VUserHandle createFromParcel(Parcel in) {
            return new VUserHandle(in);
        }

        public VUserHandle[] newArray(int size) {
            return new VUserHandle[size];
        }
    };
    private static final SparseArray<VUserHandle> userHandles = new SparseArray<VUserHandle>();
    final int mHandle;

    /** @hide */
    public VUserHandle(int h) {
        mHandle = h;
    }


    /**
     * Instantiate a new VUserHandle from the data in a Parcel that was
     * previously written with {@link #writeToParcel(Parcel, int)}.  Note that you
     * must not use this with data written by
     * {@link #writeToParcel(VUserHandle, Parcel)} since it is not possible
     * to handle a null VUserHandle here.
     *
     * @param in The Parcel containing the previously written VUserHandle,
     * positioned at the location in the buffer where it was written.
     */
    public VUserHandle(Parcel in) {
        mHandle = in.readInt();
    }

    /**
     * Checks to see if the user id is the same for the two uids, i.e., they belong to the same
     * user.
     * @hide
     */
    public static boolean isSameUser(int uid1, int uid2) {
        return getUserId(uid1) == getUserId(uid2);
    }

    public static boolean accept(int userId) {
        if (userId == USER_ALL || userId == myUserId()) {
            return true;
        }
        return false;
    }

    /**
     * Checks to see if both uids are referring to the same app id, ignoring the user id part of the
     * uids.
     * @param uid1 vuid to compare
     * @param uid2 other vuid to compare
     * @return whether the appId is the same for both uids
     * @hide
     */
    public static final boolean isSameApp(int uid1, int uid2) {
        return getAppId(uid1) == getAppId(uid2);
    }

    /** @hide */
    public static final boolean isIsolated(int uid) {
        if (uid > 0) {
            final int appId = getAppId(uid);
            return appId >= FIRST_ISOLATED_UID && appId <= LAST_ISOLATED_UID;
        } else {
            return false;
        }
    }

    /** @hide */
    public static boolean isApp(int uid) {
        if (uid > 0) {
            final int appId = getAppId(uid);
            return appId >= Process.FIRST_APPLICATION_UID && appId <= Process.LAST_APPLICATION_UID;
        } else {
            return false;
        }
    }

    /**
     * Returns the user id for a given vuid.
     * @hide
     */
    public static int getUserId(int uid) {
        if (MU_ENABLED) {
            return uid / PER_USER_RANGE;
        } else {
            return 0;
        }
    }

    /** @hide */
    public static int getCallingUserId() {
        return getUserId(VBinder.getCallingUid());
    }

    /**
     * Generate a text representation of the vuid, breaking out its individual
     * components -- user, app, isolated, etc.
     * @hide
     */

    /** @hide */
    public static VUserHandle getCallingUserHandle() {
        int userId = getUserId(VBinder.getCallingUid());
        VUserHandle userHandle = userHandles.get(userId);
        // Intentionally not synchronized to save time
        if (userHandle == null) {
            userHandle = new VUserHandle(userId);
            userHandles.put(userId, userHandle);
        }
        return userHandle;
    }

    /**
     * Returns the vuid that is composed from the userId and the appId.
     * @hide
     */
    public static int getUid(int userId, int appId) {
        if (MU_ENABLED) {
            return userId * PER_USER_RANGE + (appId % PER_USER_RANGE);
        } else {
            return appId;
        }
    }

    /**
     * Returns the app id (or base vuid) for a given vuid, stripping out the user id from it.
     * @hide
     */
    public static int getAppId(int uid) {
        return uid % PER_USER_RANGE;
    }

    public static int myAppId() {
        return getAppId(VClientImpl.get().getVUid());
    }

    /**
     * Returns the app id for a given shared app gid.
     * @hide
     */
    public static int getAppIdFromSharedAppGid(int gid) {
        final int noUserGid = getAppId(gid);
        if (noUserGid < FIRST_SHARED_APPLICATION_GID ||
                noUserGid > LAST_SHARED_APPLICATION_GID) {
            throw new IllegalArgumentException(Integer.toString(gid) + " is not a shared app gid");
        }
        return (noUserGid + Process.FIRST_APPLICATION_UID) - FIRST_SHARED_APPLICATION_GID;
    }

    public static void formatUid(StringBuilder sb, int uid) {
        if (uid < Process.FIRST_APPLICATION_UID) {
            sb.append(uid);
        } else {
            sb.append('u');
            sb.append(getUserId(uid));
            final int appId = getAppId(uid);
            if (appId >= FIRST_ISOLATED_UID && appId <= LAST_ISOLATED_UID) {
                sb.append('i');
                sb.append(appId - FIRST_ISOLATED_UID);
            } else if (appId >= Process.FIRST_APPLICATION_UID) {
                sb.append('a');
                sb.append(appId - Process.FIRST_APPLICATION_UID);
            } else {
                sb.append('s');
                sb.append(appId);
            }
        }
    }

    /**
     * Generate a text representation of the vuid, breaking out its individual
     * components -- user, app, isolated, etc.
     * @hide
     */
    public static String formatUid(int uid) {
        StringBuilder sb = new StringBuilder();
        formatUid(sb, uid);
        return sb.toString();
    }

    /**
     * Generate a text representation of the vuid, breaking out its individual
     * components -- user, app, isolated, etc.
     * @hide
     */
    public static void formatUid(PrintWriter pw, int uid) {
        if (uid < Process.FIRST_APPLICATION_UID) {
            pw.print(uid);
        } else {
            pw.print('u');
            pw.print(getUserId(uid));
            final int appId = getAppId(uid);
            if (appId >= FIRST_ISOLATED_UID && appId <= LAST_ISOLATED_UID) {
                pw.print('i');
                pw.print(appId - FIRST_ISOLATED_UID);
            } else if (appId >= Process.FIRST_APPLICATION_UID) {
                pw.print('a');
                pw.print(appId - Process.FIRST_APPLICATION_UID);
            } else {
                pw.print('s');
                pw.print(appId);
            }
        }
    }

    /**
     * Returns the user id of the current process
     * @return user id of the current process
     * @hide
     */
    public static int myUserId() {
        return getUserId(VClientImpl.get().getVUid());
    }

    /**
     * Write a VUserHandle to a Parcel, handling null pointers.  Must be
     * read with {@link #readFromParcel(Parcel)}.
     *
     * @param h The VUserHandle to be written.
     * @param out The Parcel in which the VUserHandle will be placed.
     *
     * @see #readFromParcel(Parcel)
     */
    public static void writeToParcel(VUserHandle h, Parcel out) {
        if (h != null) {
            h.writeToParcel(out, 0);
        } else {
            out.writeInt(USER_NULL);
        }
    }

    /**
     * Read a VUserHandle from a Parcel that was previously written
     * with {@link #writeToParcel(VUserHandle, Parcel)}, returning either
     * a null or new object as appropriate.
     *
     * @param in The Parcel from which to read the VUserHandle
     * @return Returns a new VUserHandle matching the previously written
     * object, or null if a null had been written.
     *
     * @see #writeToParcel(VUserHandle, Parcel)
     */
    public static VUserHandle readFromParcel(Parcel in) {
        int h = in.readInt();
        return h != USER_NULL ? new VUserHandle(h) : null;
    }

    public static VUserHandle myUserHandle() {
        return new VUserHandle(myUserId());
    }
    
    /**
     * Returns true if this VUserHandle refers to the owner user; false otherwise.
     * @return true if this VUserHandle refers to the owner user; false otherwise.
     * @hide
     */
    public final boolean isOwner() {
        return this.equals(OWNER);
    }

    /**
     * Returns the userId stored in this VUserHandle.
     * @hide
     */
    public int getIdentifier() {
        return mHandle;
    }

    @Override
    public String toString() {
        return "VUserHandle{" + mHandle + "}";
    }
    
    @Override
    public boolean equals(Object obj) {
        try {
            if (obj != null) {
                VUserHandle other = (VUserHandle)obj;
                return mHandle == other.mHandle;
            }
        } catch (ClassCastException e) {
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return mHandle;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(mHandle);
    }
}
