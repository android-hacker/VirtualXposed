package com.lody.virtual.server.interfaces;

import android.graphics.Bitmap;
import android.os.RemoteException;

import com.lody.virtual.os.VUserInfo;

import java.util.List;

/**
 * @author Lody
 */
public interface IUserManager {
    VUserInfo createUser(String name, int flags) throws RemoteException;

    boolean removeUser(int userHandle) throws RemoteException;

    void setUserName(int userHandle, String name) throws RemoteException;

    void setUserIcon(int userHandle, Bitmap icon) throws RemoteException;

    Bitmap getUserIcon(int userHandle) throws RemoteException;

    List<VUserInfo> getUsers(boolean excludeDying) throws RemoteException;

    VUserInfo getUserInfo(int userHandle) throws RemoteException;

    void setGuestEnabled(boolean enable) throws RemoteException;

    boolean isGuestEnabled() throws RemoteException;

    void wipeUser(int userHandle) throws RemoteException;

    int getUserSerialNumber(int userHandle) throws RemoteException;

    int getUserHandle(int userSerialNumber) throws RemoteException;
}
