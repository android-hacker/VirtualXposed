package com.lody.virtual.server.pm;

import android.app.Activity;
import android.app.IStopUserCallback;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.util.SparseArray;
import android.util.Xml;

import com.lody.virtual.R;
import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.env.Constants;
import com.lody.virtual.helper.compat.ActivityManagerCompat;
import com.lody.virtual.helper.utils.ArrayUtils;
import com.lody.virtual.helper.utils.AtomicFile;
import com.lody.virtual.helper.utils.FastXmlSerializer;
import com.lody.virtual.helper.utils.VLog;
import com.lody.virtual.os.VBinder;
import com.lody.virtual.os.VEnvironment;
import com.lody.virtual.os.VUserHandle;
import com.lody.virtual.os.VUserInfo;
import com.lody.virtual.os.VUserManager;
import com.lody.virtual.server.am.VActivityManagerService;
import com.lody.virtual.server.IUserManager;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;


/**
 * @author Lody
 */
public class VUserManagerService extends IUserManager.Stub {

    private static final String LOG_TAG = "VUserManagerService";

    private static final boolean DBG = false;

    private static final String TAG_NAME = "name";
    private static final String ATTR_FLAGS = "flags";
    private static final String ATTR_ICON_PATH = "icon";
    private static final String ATTR_ID = "id";
    private static final String ATTR_CREATION_TIME = "created";
    private static final String ATTR_LAST_LOGGED_IN_TIME = "lastLoggedIn";
    private static final String ATTR_SERIAL_NO = "serialNumber";
    private static final String ATTR_NEXT_SERIAL_NO = "nextSerialNumber";
    private static final String ATTR_PARTIAL = "partial";
    private static final String ATTR_USER_VERSION = "version";
    private static final String TAG_USERS = "users";
    private static final String TAG_USER = "user";

    private static final String USER_INFO_DIR = "system" + File.separator + "users";
    private static final String USER_LIST_FILENAME = "userlist.xml";
    private static final String USER_PHOTO_FILENAME = "photo.png";

    private static final int MIN_USER_ID = 1;

    private static final int USER_VERSION = 1;

    private static final long EPOCH_PLUS_30_YEARS = 30L * 365 * 24 * 60 * 60 * 1000L; // ms
    private static VUserManagerService sInstance;
    private final Context mContext;
    private final VPackageManagerService mPm;
    private final Object mInstallLock;
    private final Object mPackagesLock;
    private final File mUsersDir;
    private final File mUserListFile;
    private final File mBaseUserPath;
    private SparseArray<VUserInfo> mUsers = new SparseArray<VUserInfo>();
    private HashSet<Integer> mRemovingUserIds = new HashSet<Integer>();
    private int[] mUserIds;
    private boolean mGuestEnabled;
    private int mNextSerialNumber;
    // This resets on a reboot. Otherwise it keeps incrementing so that user ids are
    // not reused in quick succession
    private int mNextUserId = MIN_USER_ID;
    private int mUserVersion = 0;

    /**
     * Called by package manager to create the service.  This is closely
     * associated with the package manager, and the given lock is the
     * package manager's own lock.
     */
    VUserManagerService(Context context, VPackageManagerService pm,
                        Object installLock, Object packagesLock) {
        this(context, pm, installLock, packagesLock,
                VEnvironment.getDataDirectory(),
                new File(VEnvironment.getDataDirectory(), "user"));
    }

    /**
     * Available for testing purposes.
     */
    private VUserManagerService(Context context, VPackageManagerService pm,
                                Object installLock, Object packagesLock,
                                File dataDir, File baseUserPath) {
        mContext = context;
        mPm = pm;
        mInstallLock = installLock;
        mPackagesLock = packagesLock;
        synchronized (mInstallLock) {
            synchronized (mPackagesLock) {
                mUsersDir = new File(dataDir, USER_INFO_DIR);
                mUsersDir.mkdirs();
                // Make zeroth user directory, for services to migrate their files to that location
                File userZeroDir = new File(mUsersDir, "0");
                userZeroDir.mkdirs();
                mBaseUserPath = baseUserPath;
//                FileUtils.setPermissions(mUsersDir.toString(),
//                        FileUtils.S_IRWXU|FileUtils.S_IRWXG
//                        |FileUtils.S_IROTH|FileUtils.S_IXOTH,
//                        -1, -1);
                mUserListFile = new File(mUsersDir, USER_LIST_FILENAME);
                readUserListLocked();
                // Prune out any partially created/partially removed users.
                ArrayList<VUserInfo> partials = new ArrayList<VUserInfo>();
                for (int i = 0; i < mUsers.size(); i++) {
                    VUserInfo ui = mUsers.valueAt(i);
                    if (ui.partial && i != 0) {
                        partials.add(ui);
                    }
                }
                for (int i = 0; i < partials.size(); i++) {
                    VUserInfo ui = partials.get(i);
                    VLog.w(LOG_TAG, "Removing partially created user #" + i
                            + " (name=" + ui.name + ")");
                    removeUserStateLocked(ui.id);
                }
                sInstance = this;
            }
        }
    }

    public static VUserManagerService get() {
        synchronized (VUserManagerService.class) {
            return sInstance;
        }
    }

    /**
     * Enforces that only the system UID or root's UID or apps that have the
     * {android.Manifest.permission.MANAGE_USERS MANAGE_USERS}
     * permission can make certain calls to the VUserManager.
     *
     * @param message used as message if SecurityException is thrown
     * @throws SecurityException if the caller is not system or root
     */
    private static void checkManageUsersPermission(String message) {
        final int uid = VBinder.getCallingUid();
        if (uid != VirtualCore.get().myUid()) {
            throw new SecurityException("You need MANAGE_USERS permission to: " + message);
        }
    }

    @Override
    public List<VUserInfo> getUsers(boolean excludeDying) {
        //checkManageUsersPermission("query users");
        synchronized (mPackagesLock) {
            ArrayList<VUserInfo> users = new ArrayList<VUserInfo>(mUsers.size());
            for (int i = 0; i < mUsers.size(); i++) {
                VUserInfo ui = mUsers.valueAt(i);
                if (ui.partial) {
                    continue;
                }
                if (!excludeDying || !mRemovingUserIds.contains(ui.id)) {
                    users.add(ui);
                }
            }
            return users;
        }
    }

    @Override
    public VUserInfo getUserInfo(int userId) {
        //checkManageUsersPermission("query user");
        synchronized (mPackagesLock) {
            return getUserInfoLocked(userId);
        }
    }

    /*
     * Should be locked on mUsers before calling this.
     */
    private VUserInfo getUserInfoLocked(int userId) {
        VUserInfo ui = mUsers.get(userId);
        // If it is partial and not in the process of being removed, return as unknown user.
        if (ui != null && ui.partial && !mRemovingUserIds.contains(userId)) {
            VLog.w(LOG_TAG, "getUserInfo: unknown user #" + userId);
            return null;
        }
        return ui;
    }

    public boolean exists(int userId) {
        synchronized (mPackagesLock) {
            return ArrayUtils.contains(mUserIds, userId);
        }
    }

    @Override
    public void setUserName(int userId, String name) {
        checkManageUsersPermission("rename users");
        boolean changed = false;
        synchronized (mPackagesLock) {
            VUserInfo info = mUsers.get(userId);
            if (info == null || info.partial) {
                VLog.w(LOG_TAG, "setUserName: unknown user #" + userId);
                return;
            }
            if (name != null && !name.equals(info.name)) {
                info.name = name;
                writeUserLocked(info);
                changed = true;
            }
        }
        if (changed) {
            sendUserInfoChangedBroadcast(userId);
        }
    }

    @Override
    public void setUserIcon(int userId, Bitmap bitmap) {
        checkManageUsersPermission("update users");
        synchronized (mPackagesLock) {
            VUserInfo info = mUsers.get(userId);
            if (info == null || info.partial) {
                VLog.w(LOG_TAG, "setUserIcon: unknown user #" + userId);
                return;
            }
            writeBitmapLocked(info, bitmap);
            writeUserLocked(info);
        }
        sendUserInfoChangedBroadcast(userId);
    }

    private void sendUserInfoChangedBroadcast(int userId) {
        Intent changedIntent = new Intent(Constants.ACTION_USER_INFO_CHANGED);
        changedIntent.putExtra(Constants.EXTRA_USER_HANDLE, userId);
        changedIntent.addFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY);
        VActivityManagerService.get().sendBroadcastAsUser(changedIntent, new VUserHandle(userId));
    }

    @Override
    public Bitmap getUserIcon(int userId) {
        //checkManageUsersPermission("read users");
        synchronized (mPackagesLock) {
            VUserInfo info = mUsers.get(userId);
            if (info == null || info.partial) {
                VLog.w(LOG_TAG, "getUserIcon: unknown user #" + userId);
                return null;
            }
            if (info.iconPath == null) {
                return null;
            }
            return BitmapFactory.decodeFile(info.iconPath);
        }
    }

    @Override
    public boolean isGuestEnabled() {
        synchronized (mPackagesLock) {
            return mGuestEnabled;
        }
    }

    @Override
    public void setGuestEnabled(boolean enable) {
        checkManageUsersPermission("enable guest users");
        synchronized (mPackagesLock) {
            if (mGuestEnabled != enable) {
                mGuestEnabled = enable;
                // Erase any guest user that currently exists
                for (int i = 0; i < mUsers.size(); i++) {
                    VUserInfo user = mUsers.valueAt(i);
                    if (!user.partial && user.isGuest()) {
                        if (!enable) {
                            removeUser(user.id);
                        }
                        return;
                    }
                }
                // No guest was found
                if (enable) {
                    createUser("Guest", VUserInfo.FLAG_GUEST);
                }
            }
        }
    }

    @Override
    public void wipeUser(int userHandle) {
        checkManageUsersPermission("wipe user");
        // TODO:
    }

    public void makeInitialized(int userId) {
        checkManageUsersPermission("makeInitialized");
        synchronized (mPackagesLock) {
            VUserInfo info = mUsers.get(userId);
            if (info == null || info.partial) {
                VLog.w(LOG_TAG, "makeInitialized: unknown user #" + userId);
            }
            if ((info.flags& VUserInfo.FLAG_INITIALIZED) == 0) {
                info.flags |= VUserInfo.FLAG_INITIALIZED;
                writeUserLocked(info);
            }
        }
    }

    /**
     * Check if we've hit the limit of how many users can be created.
     */
    private boolean isUserLimitReachedLocked() {
        int nUsers = mUsers.size();
        return nUsers >= VUserManager.getMaxSupportedUsers();
    }

    private void writeBitmapLocked(VUserInfo info, Bitmap bitmap) {
        try {
            File dir = new File(mUsersDir, Integer.toString(info.id));
            File file = new File(dir, USER_PHOTO_FILENAME);
            if (!dir.exists()) {
                dir.mkdir();
//                FileUtils.setPermissions(
//                        dir.getPath(),
//                        FileUtils.S_IRWXU|FileUtils.S_IRWXG|FileUtils.S_IXOTH,
//                        -1, -1);
            }
            FileOutputStream os;
            if (bitmap.compress(Bitmap.CompressFormat.PNG, 100, os = new FileOutputStream(file))) {
                info.iconPath = file.getAbsolutePath();
            }
            try {
                os.close();
            } catch (IOException ioe) {
                // What the ... !
            }
        } catch (FileNotFoundException e) {
            VLog.w(LOG_TAG, "Error setting photo for user ", e);
        }
    }

    /**
     * Returns an array of user ids. This array is cached here for quick access, so do not modify or
     * cache it elsewhere.
     * @return the array of user ids.
     */
    public int[] getUserIds() {
        synchronized (mPackagesLock) {
            return mUserIds;
        }
    }

    int[] getUserIdsLPr() {
        return mUserIds;
    }

    private void readUserList() {
        synchronized (mPackagesLock) {
            readUserListLocked();
        }
    }

    private void readUserListLocked() {
        mGuestEnabled = false;
        if (!mUserListFile.exists()) {
            fallbackToSingleUserLocked();
            return;
        }
        FileInputStream fis = null;
        AtomicFile userListFile = new AtomicFile(mUserListFile);
        try {
            fis = userListFile.openRead();
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(fis, null);
            int type;
            while ((type = parser.next()) != XmlPullParser.START_TAG
                    && type != XmlPullParser.END_DOCUMENT) {
                ;
            }

            if (type != XmlPullParser.START_TAG) {
                VLog.e(LOG_TAG, "Unable to read user list");
                fallbackToSingleUserLocked();
                return;
            }

            mNextSerialNumber = -1;
            if (parser.getName().equals(TAG_USERS)) {
                String lastSerialNumber = parser.getAttributeValue(null, ATTR_NEXT_SERIAL_NO);
                if (lastSerialNumber != null) {
                    mNextSerialNumber = Integer.parseInt(lastSerialNumber);
                }
                String versionNumber = parser.getAttributeValue(null, ATTR_USER_VERSION);
                if (versionNumber != null) {
                    mUserVersion = Integer.parseInt(versionNumber);
                }
            }

            while ((type = parser.next()) != XmlPullParser.END_DOCUMENT) {
                if (type == XmlPullParser.START_TAG && parser.getName().equals(TAG_USER)) {
                    String id = parser.getAttributeValue(null, ATTR_ID);
                    VUserInfo user = readUser(Integer.parseInt(id));

                    if (user != null) {
                        mUsers.put(user.id, user);
                        if (user.isGuest()) {
                            mGuestEnabled = true;
                        }
                        if (mNextSerialNumber < 0 || mNextSerialNumber <= user.id) {
                            mNextSerialNumber = user.id + 1;
                        }
                    }
                }
            }
            updateUserIdsLocked();
            upgradeIfNecessary();
        } catch (IOException ioe) {
            fallbackToSingleUserLocked();
        } catch (XmlPullParserException pe) {
            fallbackToSingleUserLocked();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * This fixes an incorrect initialization of user name for the owner.
     * TODO: Remove in the next release.
     */
    private void upgradeIfNecessary() {
        int userVersion = mUserVersion;
        if (userVersion < 1) {
            // Assign a proper name for the owner, if not initialized correctly before
            VUserInfo user = mUsers.get(VUserHandle.USER_OWNER);
            if ("Primary".equals(user.name)) {
                user.name = "Admin";
                writeUserLocked(user);
            }
            userVersion = 1;
        }

        if (userVersion < USER_VERSION) {
            VLog.w(LOG_TAG, "User version " + mUserVersion + " didn't upgrade as expected to "
                    + USER_VERSION);
        } else {
            mUserVersion = userVersion;
            writeUserListLocked();
        }
    }

    private void fallbackToSingleUserLocked() {
        // Create the primary user
        VUserInfo primary = new VUserInfo(0,
                mContext.getResources().getString(R.string.owner_name), null,
                VUserInfo.FLAG_ADMIN | VUserInfo.FLAG_PRIMARY | VUserInfo.FLAG_INITIALIZED);
        mUsers.put(0, primary);
        mNextSerialNumber = MIN_USER_ID;
        updateUserIdsLocked();

        writeUserListLocked();
        writeUserLocked(primary);
    }

    /*
     * Writes the user file in this format:
     *
     * <user flags="20039023" id="0">
     *   <name>Primary</name>
     * </user>
     */
    private void writeUserLocked(VUserInfo userInfo) {
        FileOutputStream fos = null;
        AtomicFile userFile = new AtomicFile(new File(mUsersDir, userInfo.id + ".xml"));
        try {
            fos = userFile.startWrite();
            final BufferedOutputStream bos = new BufferedOutputStream(fos);

            // XmlSerializer serializer = XmlUtils.serializerInstance();
            final XmlSerializer serializer = new FastXmlSerializer();
            serializer.setOutput(bos, "utf-8");
            serializer.startDocument(null, true);
            serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);

            serializer.startTag(null, TAG_USER);
            serializer.attribute(null, ATTR_ID, Integer.toString(userInfo.id));
            serializer.attribute(null, ATTR_SERIAL_NO, Integer.toString(userInfo.serialNumber));
            serializer.attribute(null, ATTR_FLAGS, Integer.toString(userInfo.flags));
            serializer.attribute(null, ATTR_CREATION_TIME, Long.toString(userInfo.creationTime));
            serializer.attribute(null, ATTR_LAST_LOGGED_IN_TIME,
                    Long.toString(userInfo.lastLoggedInTime));
            if (userInfo.iconPath != null) {
                serializer.attribute(null,  ATTR_ICON_PATH, userInfo.iconPath);
            }
            if (userInfo.partial) {
                serializer.attribute(null, ATTR_PARTIAL, "true");
            }

            serializer.startTag(null, TAG_NAME);
            serializer.text(userInfo.name);
            serializer.endTag(null, TAG_NAME);

            serializer.endTag(null, TAG_USER);

            serializer.endDocument();
            userFile.finishWrite(fos);
        } catch (Exception ioe) {
            VLog.e(LOG_TAG, "Error writing user info " + userInfo.id + "\n" + ioe);
            userFile.failWrite(fos);
        }
    }

    /*
     * Writes the user list file in this format:
     *
     * <users nextSerialNumber="3">
     *   <user id="0"></user>
     *   <user id="2"></user>
     * </users>
     */
    private void writeUserListLocked() {
        FileOutputStream fos = null;
        AtomicFile userListFile = new AtomicFile(mUserListFile);
        try {
            fos = userListFile.startWrite();
            final BufferedOutputStream bos = new BufferedOutputStream(fos);

            // XmlSerializer serializer = XmlUtils.serializerInstance();
            final XmlSerializer serializer = new FastXmlSerializer();
            serializer.setOutput(bos, "utf-8");
            serializer.startDocument(null, true);
            serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);

            serializer.startTag(null, TAG_USERS);
            serializer.attribute(null, ATTR_NEXT_SERIAL_NO, Integer.toString(mNextSerialNumber));
            serializer.attribute(null, ATTR_USER_VERSION, Integer.toString(mUserVersion));

            for (int i = 0; i < mUsers.size(); i++) {
                VUserInfo user = mUsers.valueAt(i);
                serializer.startTag(null, TAG_USER);
                serializer.attribute(null, ATTR_ID, Integer.toString(user.id));
                serializer.endTag(null, TAG_USER);
            }

            serializer.endTag(null, TAG_USERS);

            serializer.endDocument();
            userListFile.finishWrite(fos);
        } catch (Exception e) {
            userListFile.failWrite(fos);
            VLog.e(LOG_TAG, "Error writing user list");
        }
    }

    private VUserInfo readUser(int id) {
        int flags = 0;
        int serialNumber = id;
        String name = null;
        String iconPath = null;
        long creationTime = 0L;
        long lastLoggedInTime = 0L;
        boolean partial = false;

        FileInputStream fis = null;
        try {
            AtomicFile userFile =
                    new AtomicFile(new File(mUsersDir, Integer.toString(id) + ".xml"));
            fis = userFile.openRead();
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(fis, null);
            int type;
            while ((type = parser.next()) != XmlPullParser.START_TAG
                    && type != XmlPullParser.END_DOCUMENT) {
                ;
            }

            if (type != XmlPullParser.START_TAG) {
                VLog.e(LOG_TAG, "Unable to read user " + id);
                return null;
            }

            if (parser.getName().equals(TAG_USER)) {
                int storedId = readIntAttribute(parser, ATTR_ID, -1);
                if (storedId != id) {
                    VLog.e(LOG_TAG, "User id does not match the file name");
                    return null;
                }
                serialNumber = readIntAttribute(parser, ATTR_SERIAL_NO, id);
                flags = readIntAttribute(parser, ATTR_FLAGS, 0);
                iconPath = parser.getAttributeValue(null, ATTR_ICON_PATH);
                creationTime = readLongAttribute(parser, ATTR_CREATION_TIME, 0);
                lastLoggedInTime = readLongAttribute(parser, ATTR_LAST_LOGGED_IN_TIME, 0);
                String valueString = parser.getAttributeValue(null, ATTR_PARTIAL);
                if ("true".equals(valueString)) {
                    partial = true;
                }

                while ((type = parser.next()) != XmlPullParser.START_TAG
                        && type != XmlPullParser.END_DOCUMENT) {
                }
                if (type == XmlPullParser.START_TAG && parser.getName().equals(TAG_NAME)) {
                    type = parser.next();
                    if (type == XmlPullParser.TEXT) {
                        name = parser.getText();
                    }
                }
            }

            VUserInfo userInfo = new VUserInfo(id, name, iconPath, flags);
            userInfo.serialNumber = serialNumber;
            userInfo.creationTime = creationTime;
            userInfo.lastLoggedInTime = lastLoggedInTime;
            userInfo.partial = partial;
            return userInfo;

        } catch (IOException ioe) {
        } catch (XmlPullParserException pe) {
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                }
            }
        }
        return null;
    }

    private int readIntAttribute(XmlPullParser parser, String attr, int defaultValue) {
        String valueString = parser.getAttributeValue(null, attr);
        if (valueString == null) return defaultValue;
        try {
            return Integer.parseInt(valueString);
        } catch (NumberFormatException nfe) {
            return defaultValue;
        }
    }

    private long readLongAttribute(XmlPullParser parser, String attr, long defaultValue) {
        String valueString = parser.getAttributeValue(null, attr);
        if (valueString == null) return defaultValue;
        try {
            return Long.parseLong(valueString);
        } catch (NumberFormatException nfe) {
            return defaultValue;
        }
    }

    @Override
    public VUserInfo createUser(String name, int flags) {
        checkManageUsersPermission("Only the system can create users");

        final long ident = Binder.clearCallingIdentity();
        final VUserInfo userInfo;
        try {
            synchronized (mInstallLock) {
                synchronized (mPackagesLock) {
                    if (isUserLimitReachedLocked()) return null;
                    int userId = getNextAvailableIdLocked();
                    userInfo = new VUserInfo(userId, name, null, flags);
                    File userPath = new File(mBaseUserPath, Integer.toString(userId));
                    userInfo.serialNumber = mNextSerialNumber++;
                    long now = System.currentTimeMillis();
                    userInfo.creationTime = (now > EPOCH_PLUS_30_YEARS) ? now : 0;
                    userInfo.partial = true;
                    VEnvironment.getUserSystemDirectory(userInfo.id).mkdirs();
                    mUsers.put(userId, userInfo);
                    writeUserListLocked();
                    writeUserLocked(userInfo);
                    mPm.createNewUser(userId, userPath);
                    userInfo.partial = false;
                    writeUserLocked(userInfo);
                    updateUserIdsLocked();
                }
            }
            Intent addedIntent = new Intent(Constants.ACTION_USER_ADDED);
            addedIntent.putExtra(Constants.EXTRA_USER_HANDLE, userInfo.id);
            VActivityManagerService.get().sendBroadcastAsUser(addedIntent, VUserHandle.ALL,
                        null);
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
        return userInfo;
    }

    /**
     * Removes a user and all data directories created for that user. This method should be called
     * after the user's processes have been terminated.
     * @param userHandle the user's id
     */
    public boolean removeUser(int userHandle) {
        checkManageUsersPermission("Only the system can remove users");
        final VUserInfo user;
        synchronized (mPackagesLock) {
            user = mUsers.get(userHandle);
            if (userHandle == 0 || user == null) {
                return false;
            }
            mRemovingUserIds.add(userHandle);
            // Set this to a partially created user, so that the user will be purged
            // on next startup, in case the runtime stops now before stopping and
            // removing the user completely.
            user.partial = true;
            writeUserLocked(user);
        }
        if (DBG) VLog.i(LOG_TAG, "Stopping user " + userHandle);
        int res = VActivityManagerService.get().stopUser(userHandle,
                    new IStopUserCallback.Stub() {
                        @Override
                        public void userStopped(int userId) {
                            finishRemoveUser(userId);
                        }
                        @Override
                        public void userStopAborted(int userId) {
                        }
            });
        return res == ActivityManagerCompat.USER_OP_SUCCESS;
    }

    void finishRemoveUser(final int userHandle) {
        if (DBG) VLog.i(LOG_TAG, "finishRemoveUser " + userHandle);
        // Let other services shutdown any activity and clean up their state before completely
        // wiping the user's system directory and removing from the user list
        long identity = Binder.clearCallingIdentity();
        try {
            Intent addedIntent = new Intent(Constants.ACTION_USER_REMOVED);
            addedIntent.putExtra(Constants.EXTRA_USER_HANDLE, userHandle);
            VActivityManagerService.get().sendOrderedBroadcastAsUser(addedIntent, VUserHandle.ALL,
                   null,
                    new BroadcastReceiver() {
                        @Override
                        public void onReceive(Context context, Intent intent) {
                            if (DBG) {
                                VLog.i(LOG_TAG,
                                        "USER_REMOVED broadcast sent, cleaning up user data "
                                        + userHandle);
                            }
                            new Thread() {
                                public void run() {
                                    synchronized (mInstallLock) {
                                        synchronized (mPackagesLock) {
                                            removeUserStateLocked(userHandle);
                                        }
                                    }
                                }
                            }.start();
                        }
                    },
                    null, Activity.RESULT_OK, null, null);
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    private void removeUserStateLocked(int userHandle) {
        // Cleanup package manager settings
        mPm.cleanUpUser(userHandle);

        // Remove this user from the list
        mUsers.remove(userHandle);
        mRemovingUserIds.remove(userHandle);
        // Remove user file
        AtomicFile userFile = new AtomicFile(new File(mUsersDir, userHandle + ".xml"));
        userFile.delete();
        // Update the user list
        writeUserListLocked();
        updateUserIdsLocked();
        removeDirectoryRecursive(VEnvironment.getUserSystemDirectory(userHandle));
    }

    private void removeDirectoryRecursive(File parent) {
        if (parent.isDirectory()) {
            String[] files = parent.list();
            for (String filename : files) {
                File child = new File(parent, filename);
                removeDirectoryRecursive(child);
            }
        }
        parent.delete();
    }

    @Override
    public int getUserSerialNumber(int userHandle) {
        synchronized (mPackagesLock) {
            if (!exists(userHandle)) return -1;
            return getUserInfoLocked(userHandle).serialNumber;
        }
    }

    @Override
    public int getUserHandle(int userSerialNumber) {
        synchronized (mPackagesLock) {
            for (int userId : mUserIds) {
                if (getUserInfoLocked(userId).serialNumber == userSerialNumber) return userId;
            }
            // Not found
            return -1;
        }
    }

    /**
     * Caches the list of user ids in an array, adjusting the array size when necessary.
     */
    private void updateUserIdsLocked() {
        int num = 0;
        for (int i = 0; i < mUsers.size(); i++) {
            if (!mUsers.valueAt(i).partial) {
                num++;
            }
        }
        final int[] newUsers = new int[num];
        int n = 0;
        for (int i = 0; i < mUsers.size(); i++) {
            if (!mUsers.valueAt(i).partial) {
                newUsers[n++] = mUsers.keyAt(i);
            }
        }
        mUserIds = newUsers;
    }

    /**
     * Make a note of the last started time of a user.
     * @param userId the user that was just foregrounded
     */
    public void userForeground(int userId) {
        synchronized (mPackagesLock) {
            VUserInfo user = mUsers.get(userId);
            long now = System.currentTimeMillis();
            if (user == null || user.partial) {
                VLog.w(LOG_TAG, "userForeground: unknown user #" + userId);
                return;
            }
            if (now > EPOCH_PLUS_30_YEARS) {
                user.lastLoggedInTime = now;
                writeUserLocked(user);
            }
        }
    }

    /**
     * Returns the next available user id, filling in any holes in the ids.
     * TODO: May not be a good idea to recycle ids, in case it results in confusion
     * for data and battery stats collection, or unexpected cross-talk.
     * @return
     */
    private int getNextAvailableIdLocked() {
        synchronized (mPackagesLock) {
            int i = mNextUserId;
            while (i < Integer.MAX_VALUE) {
                if (mUsers.indexOfKey(i) < 0 && !mRemovingUserIds.contains(i)) {
                    break;
                }
                i++;
            }
            mNextUserId = i + 1;
            return i;
        }
    }

    @Override
    protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {

    }
}
