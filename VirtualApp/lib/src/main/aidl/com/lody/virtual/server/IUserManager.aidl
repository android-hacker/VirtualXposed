package com.lody.virtual.server;

import android.os.ParcelFileDescriptor;
import com.lody.virtual.os.VUserInfo;
import android.graphics.Bitmap;

/**
*
 */
interface IUserManager {
    VUserInfo createUser(in String name, int flags);
    boolean removeUser(int userHandle);
    void setUserName(int userHandle, String name);
    void setUserIcon(int userHandle, in Bitmap icon);
    Bitmap getUserIcon(int userHandle);
    List<VUserInfo> getUsers(boolean excludeDying);
    VUserInfo getUserInfo(int userHandle);
    void setGuestEnabled(boolean enable);
    boolean isGuestEnabled();
    void wipeUser(int userHandle);
    int getUserSerialNumber(int userHandle);
    int getUserHandle(int userSerialNumber);
}
