package com.lody.virtual.service;

import android.os.ParcelFileDescriptor;
import android.content.pm.UserInfo;
import android.graphics.Bitmap;

/**
*
 */
interface IUserManager {
    UserInfo createUser(in String name, int flags);
    boolean removeUser(int userHandle);
    void setUserName(int userHandle, String name);
    void setUserIcon(int userHandle, in Bitmap icon);
    Bitmap getUserIcon(int userHandle);
    List<UserInfo> getUsers(boolean excludeDying);
    UserInfo getUserInfo(int userHandle);
    void setGuestEnabled(boolean enable);
    boolean isGuestEnabled();
    void wipeUser(int userHandle);
    int getUserSerialNumber(int userHandle);
    int getUserHandle(int userSerialNumber);
}
