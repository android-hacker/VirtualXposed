package com.lody.virtual.service.am;

import com.lody.virtual.os.VEnvironment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

import static android.os.Process.FIRST_APPLICATION_UID;

/**
 * @author Lody
 */

public class UidSystem {
	private final HashMap<String, Integer> mSharedUserIdMap = new HashMap<>();
	private int mFreeUid = FIRST_APPLICATION_UID;


	public void initUidList() {
		mSharedUserIdMap.clear();
		File uidFile = VEnvironment.getUidListFile();
		if (!uidFile.exists()) {
			return;
		}
		try {
			ObjectInputStream is = new ObjectInputStream(new FileInputStream(uidFile));
			mFreeUid = is.readInt();
			//noinspection unchecked
			Map<String, Integer> map = (HashMap<String, Integer>) is.readObject();
			mSharedUserIdMap.putAll(map);
			is.close();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	public void save() {
		try {
			ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(VEnvironment.getUidListFile()));
			os.writeInt(mFreeUid);
			os.writeObject(mSharedUserIdMap);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public int getOrCreateUid(String sharedUserId) {
		if (sharedUserId == null) {
			return ++mFreeUid;
		}
		Integer uid = mSharedUserIdMap.get(sharedUserId);
		if (uid != null) {
			return uid;
		}
		int newUid = ++mFreeUid;
		mSharedUserIdMap.put(sharedUserId, newUid);
		save();
		return newUid;
	}
}
