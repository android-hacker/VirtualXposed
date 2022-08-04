package com.lody.virtual.server.accounts;

import android.accounts.Account;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SyncAdapterType;
import android.content.SyncRequest;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.util.Xml;

import com.lody.virtual.server.pm.VAppManagerService;
import com.lody.virtual.server.pm.VPackageManagerService;

import org.xmlpull.v1.XmlPullParser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mirror.android.content.SyncAdapterTypeN;
import mirror.com.android.internal.R_Hide;

/**
 * @author Lody
 */

public class VContentService {

    private Context mContext;
    private final SparseArray<Map<VSyncRecord.SyncRecordKey, VSyncRecord>> mRecords = new SparseArray<>();
    private final Map<String, SyncAdapterInfo> mAppSyncAdapterInfos = new HashMap<>();

    private class SyncAdapterInfo {
        SyncAdapterType adapterType;
        ServiceInfo serviceInfo;

        SyncAdapterInfo(SyncAdapterType adapterType, ServiceInfo serviceInfo) {
            this.adapterType = adapterType;
            this.serviceInfo = serviceInfo;
        }
    }

    public void refreshServiceCache(String packageName) {
        Intent intent = new Intent("android.content.SyncAdapter");
        if (packageName != null) {
            intent.setPackage(packageName);
        }
        generateServicesMap(
                VPackageManagerService.get().queryIntentServices(
                        intent, null, PackageManager.GET_META_DATA, 0
                ),
                mAppSyncAdapterInfos,
                new RegisteredServicesParser()
        );
    }

    public void syncAsUser(SyncRequest request, int userId) {
        Account account = mirror.android.content.SyncRequest.mAccountToSync.get(request);
        String authority = mirror.android.content.SyncRequest.mAuthority.get(request);
        Bundle extras = mirror.android.content.SyncRequest.mExtras.get(request);
        boolean isPeriodic = mirror.android.content.SyncRequest.mIsPeriodic.get(request);
        long syncRunTimeSecs = mirror.android.content.SyncRequest.mSyncRunTimeSecs.get(request);
        if (!isAccountExist(userId, account, authority)) {
            return;
        }
        VSyncRecord.SyncRecordKey key = new VSyncRecord.SyncRecordKey(account, authority);
        VSyncRecord.SyncExtras syncExtras = new VSyncRecord.SyncExtras(extras);
        int isSyncable = getIsSyncableAsUser(account, authority, userId);
        synchronized (mRecords) {
            Map<VSyncRecord.SyncRecordKey, VSyncRecord> map = mRecords.get(userId);
            if (map == null) {
                map = new HashMap<>();
                mRecords.put(userId, map);
            }
            VSyncRecord record = map.get(key);
            if (record == null) {
                record = new VSyncRecord(userId, account, authority);
                map.put(key, record);
            }
            if (isSyncable < 0) {
                // Initialisation sync.
                Bundle newExtras = new Bundle();
                newExtras.putBoolean(ContentResolver.SYNC_EXTRAS_INITIALIZE, true);
                record.extras.add(new VSyncRecord.SyncExtras(newExtras));
            }
            if (isPeriodic) {
                VSyncRecord.PeriodicSyncConfig periodicSyncConfig = new VSyncRecord.PeriodicSyncConfig(syncRunTimeSecs);
                record.configs.put(syncExtras, periodicSyncConfig);
            } else {
                record.extras.add(syncExtras);
            }


        }
    }


    private boolean isAccountExist(int userId, Account account, String providerName) {
        synchronized (mAppSyncAdapterInfos) {
            SyncAdapterInfo info = mAppSyncAdapterInfos.get(account.type + "/" + providerName);
            return info != null
                    && VAppManagerService.get().isAppInstalled(info.serviceInfo.packageName);
        }
    }

    public int getIsSyncableAsUser(Account account, String providerName, int userId) {
        VSyncRecord.SyncRecordKey key = new VSyncRecord.SyncRecordKey(account, providerName);
        synchronized (mRecords) {
            Map<VSyncRecord.SyncRecordKey, VSyncRecord> map = mRecords.get(userId);
            if (map == null) {
                return -1;
            }
            VSyncRecord record = map.get(key);
            if (record == null) {
                return -1;
            }
            return record.syncable;
        }

    }

    private void generateServicesMap(List<ResolveInfo> services, Map<String, SyncAdapterInfo> map,
                                     RegisteredServicesParser accountParser) {
        for (ResolveInfo info : services) {
            XmlResourceParser parser = accountParser.getParser(mContext, info.serviceInfo, "android.content.SyncAdapter");
            if (parser != null) {
                try {
                    AttributeSet attributeSet = Xml.asAttributeSet(parser);
                    int type;
                    while ((type = parser.next()) != XmlPullParser.END_DOCUMENT && type != XmlPullParser.START_TAG) {
                        // Nothing to do
                    }
                    if ("sync-adapter".equals(parser.getName())) {
                        SyncAdapterType adapterType = parseSyncAdapterType(
                                accountParser.getResources(mContext, info.serviceInfo.applicationInfo), attributeSet);
                        if (adapterType != null) {
                            String key = adapterType.accountType + "/" + adapterType.authority;
                            map.put(key, new SyncAdapterInfo(adapterType, info.serviceInfo));
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private SyncAdapterType parseSyncAdapterType(Resources res, AttributeSet set) {
        TypedArray obtainAttributes = res.obtainAttributes(set, R_Hide.styleable.SyncAdapter.get());
        try {
            String contentAuthority = obtainAttributes.getString(R_Hide.styleable.SyncAdapter_contentAuthority.get());
            String accountType = obtainAttributes.getString(R_Hide.styleable.SyncAdapter_accountType.get());
            if (contentAuthority == null || accountType == null) {
                obtainAttributes.recycle();
                return null;
            }
            boolean userVisible = obtainAttributes.getBoolean(R_Hide.styleable.SyncAdapter_userVisible.get(), true);
            boolean supportsUploading = obtainAttributes.getBoolean(R_Hide.styleable.SyncAdapter_supportsUploading.get(), true);
            boolean isAlwaysSyncable = obtainAttributes.getBoolean(R_Hide.styleable.SyncAdapter_isAlwaysSyncable.get(), true);
            boolean allowParallelSyncs = obtainAttributes.getBoolean(R_Hide.styleable.SyncAdapter_allowParallelSyncs.get(), true);
            String settingsActivity = obtainAttributes.getString(R_Hide.styleable.SyncAdapter_settingsActivity.get());
            SyncAdapterType type;
            if (SyncAdapterTypeN.ctor != null) {
                type = SyncAdapterTypeN.ctor.newInstance(contentAuthority, accountType, userVisible, supportsUploading, isAlwaysSyncable, allowParallelSyncs, settingsActivity, null);
                obtainAttributes.recycle();
                return type;
            }
            type = mirror.android.content.SyncAdapterType.ctor.newInstance(contentAuthority, accountType, userVisible, supportsUploading, isAlwaysSyncable, allowParallelSyncs, settingsActivity);
            obtainAttributes.recycle();
            return type;
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }
}
