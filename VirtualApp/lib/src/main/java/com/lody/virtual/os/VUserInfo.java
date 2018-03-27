package com.lody.virtual.os;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Per-user information.
 */
public class VUserInfo implements Parcelable {

    /** 8 bits for user type */
    public static final int FLAG_MASK_USER_TYPE = 0x000000FF;

    /**
     * *************************** NOTE ***************************
     * These flag values CAN NOT CHANGE because they are written
     * directly to storage.
     */

    /**
     * Primary user. Only one user can have this flag set. Meaning of this
     * flag TBD.
     */
    public static final int FLAG_PRIMARY = 0x00000001;

    /**
     * User with administrative privileges. Such a user can create and
     * delete users.
     */
    public static final int FLAG_ADMIN   = 0x00000002;

    /**
     * Indicates a guest user that may be transient.
     */
    public static final int FLAG_GUEST   = 0x00000004;

    /**
     * Indicates the user has restrictions in privileges, in addition to those for normal users.
     * Exact meaning TBD. For instance, maybe they can't install apps or administer WiFi access pts.
     */
    public static final int FLAG_RESTRICTED = 0x00000008;

    /**
     * Indicates that this user has gone through its first-time initialization.
     */
    public static final int FLAG_INITIALIZED = 0x00000010;

    /**
     * Indicates that this user is a profile of another user, for example holding a users
     * corporate data.
     */
    public static final int FLAG_MANAGED_PROFILE = 0x00000020;

    /**
     * Indicates that this user is disabled.
     */
    public static final int FLAG_DISABLED = 0x00000040;


    public static final int NO_PROFILE_GROUP_ID = -1;

    public int id;
    public int serialNumber;
    public String name;
    public String iconPath;
    public int flags;
    public long creationTime;
    public long lastLoggedInTime;
    public int profileGroupId;

    /** User is only partially created. */
    public boolean partial;

    public VUserInfo(int id, String name, int flags) {
        this(id, name, null, flags);
    }

    public VUserInfo(int id, String name, String iconPath, int flags) {
        this.id = id;
        this.name = name;
        this.flags = flags;
        this.iconPath = iconPath;
        this.profileGroupId = NO_PROFILE_GROUP_ID;
    }

    public boolean isPrimary() {
        return (flags & FLAG_PRIMARY) == FLAG_PRIMARY;
    }

    public boolean isAdmin() {
        return (flags & FLAG_ADMIN) == FLAG_ADMIN;
    }

    public boolean isGuest() {
        return (flags & FLAG_GUEST) == FLAG_GUEST;
    }

    public boolean isRestricted() {
        return (flags & FLAG_RESTRICTED) == FLAG_RESTRICTED;
    }

    public boolean isManagedProfile() {
        return (flags & FLAG_MANAGED_PROFILE) == FLAG_MANAGED_PROFILE;
    }

    public boolean isEnabled() {
        return (flags & FLAG_DISABLED) != FLAG_DISABLED;
    }

    public VUserInfo() {
    }

    public VUserInfo(VUserInfo orig) {
        name = orig.name;
        iconPath = orig.iconPath;
        id = orig.id;
        flags = orig.flags;
        serialNumber = orig.serialNumber;
        creationTime = orig.creationTime;
        lastLoggedInTime = orig.lastLoggedInTime;
        partial = orig.partial;
        profileGroupId = orig.profileGroupId;
    }

    @Override
    public String toString() {
        return "UserInfo{" + id + ":" + name + ":" + Integer.toHexString(flags) + "}";
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int parcelableFlags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeString(iconPath);
        dest.writeInt(flags);
        dest.writeInt(serialNumber);
        dest.writeLong(creationTime);
        dest.writeLong(lastLoggedInTime);
        dest.writeInt(partial ? 1 : 0);
        dest.writeInt(profileGroupId);
    }

    public static final Parcelable.Creator<VUserInfo> CREATOR
            = new Parcelable.Creator<VUserInfo>() {
        public VUserInfo createFromParcel(Parcel source) {
            return new VUserInfo(source);
        }
        public VUserInfo[] newArray(int size) {
            return new VUserInfo[size];
        }
    };

    private VUserInfo(Parcel source) {
        id = source.readInt();
        name = source.readString();
        iconPath = source.readString();
        flags = source.readInt();
        serialNumber = source.readInt();
        creationTime = source.readLong();
        lastLoggedInTime = source.readLong();
        partial = source.readInt() != 0;
        profileGroupId = source.readInt();
    }
}
