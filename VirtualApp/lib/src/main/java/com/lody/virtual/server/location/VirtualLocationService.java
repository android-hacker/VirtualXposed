package com.lody.virtual.server.location;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteException;

import com.lody.virtual.helper.PersistenceLayer;
import com.lody.virtual.helper.collection.SparseArray;
import com.lody.virtual.os.VEnvironment;
import com.lody.virtual.remote.vloc.VCell;
import com.lody.virtual.remote.vloc.VLocation;
import com.lody.virtual.server.interfaces.IVirtualLocationManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Lody
 */

public class VirtualLocationService implements IVirtualLocationManager {

    private static final VirtualLocationService sInstance = new VirtualLocationService();
    private final SparseArray<Map<String, VLocConfig>> mLocConfigs = new SparseArray<>();
    private final VLocConfig mGlobalConfig = new VLocConfig();

    private static final int MODE_CLOSE = 0;
    private static final int MODE_USE_GLOBAL = 1;
    private static final int MODE_USE_SELF = 2;

    private static class VLocConfig implements Parcelable {
        int mode;
        VCell cell;
        List<VCell> allCell;
        List<VCell> neighboringCell;
        VLocation location;

        public void set(VLocConfig other) {
            this.mode = other.mode;
            this.cell = other.cell;
            this.allCell = other.allCell;
            this.neighboringCell = other.neighboringCell;
            this.location = other.location;
        }

        VLocConfig() {
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.mode);
            dest.writeParcelable(this.cell, flags);
            dest.writeTypedList(this.allCell);
            dest.writeTypedList(this.neighboringCell);
            dest.writeParcelable(this.location, flags);
        }

        VLocConfig(Parcel in) {
            this.mode = in.readInt();
            this.cell = in.readParcelable(VCell.class.getClassLoader());
            this.allCell = in.createTypedArrayList(VCell.CREATOR);
            this.neighboringCell = in.createTypedArrayList(VCell.CREATOR);
            this.location = in.readParcelable(VLocation.class.getClassLoader());
        }

        public static final Creator<VLocConfig> CREATOR = new Creator<VLocConfig>() {
            @Override
            public VLocConfig createFromParcel(Parcel source) {
                return new VLocConfig(source);
            }

            @Override
            public VLocConfig[] newArray(int size) {
                return new VLocConfig[size];
            }
        };
    }

    private final PersistenceLayer mPersistenceLayer = new PersistenceLayer(VEnvironment.getVirtualLocationFile()) {
        @Override
        public int getCurrentVersion() {
            return 1;
        }

        @Override
        public void writePersistenceData(Parcel p) {
            mGlobalConfig.writeToParcel(p, 0);
            p.writeInt(mLocConfigs.size());
            for (int i = 0; i < mLocConfigs.size(); i++) {
                int userId = mLocConfigs.keyAt(i);
                Map<String, VLocConfig> pkgs = mLocConfigs.valueAt(i);
                p.writeInt(userId);
                p.writeMap(pkgs);
            }
        }

        @Override
        public void readPersistenceData(Parcel p) {
            mGlobalConfig.set(new VLocConfig(p));
            mLocConfigs.clear();
            int size = p.readInt();
            while (size-- > 0) {
                int userId = p.readInt();
                //noinspection unchecked
                Map<String, VLocConfig> pkgs = p.readHashMap(getClass().getClassLoader());
                mLocConfigs.put(userId, pkgs);
            }
        }
    };

    public static VirtualLocationService get() {
        return sInstance;
    }

    private VirtualLocationService() {
        mPersistenceLayer.read();
    }

    @Override
    public int getMode(int userId, String pkg) {
        synchronized (mLocConfigs) {
            VLocConfig config = getOrCreateConfig(userId, pkg);
            mPersistenceLayer.save();
            return config.mode;
        }
    }

    @Override
    public void setMode(int userId, String pkg, int mode) {
        synchronized (mLocConfigs) {
            getOrCreateConfig(userId, pkg).mode = mode;
            mPersistenceLayer.save();
        }
    }

    private VLocConfig getOrCreateConfig(int userId, String pkg) {
        Map<String, VLocConfig> pkgs = mLocConfigs.get(userId);
        if (pkgs == null) {
            pkgs = new HashMap<>();
            mLocConfigs.put(userId, pkgs);
        }
        VLocConfig config = pkgs.get(pkg);
        if (config == null) {
            config = new VLocConfig();
            config.mode = MODE_CLOSE;
            pkgs.put(pkg, config);
        }
        return config;
    }

    @Override
    public void setCell(int userId, String pkg, VCell cell) {
        getOrCreateConfig(userId, pkg).cell = cell;
        mPersistenceLayer.save();
    }

    @Override
    public void setAllCell(int userId, String pkg, List<VCell> cell) {
        getOrCreateConfig(userId, pkg).allCell = cell;
        mPersistenceLayer.save();
    }

    @Override
    public void setNeighboringCell(int userId, String pkg, List<VCell> cell) {
        getOrCreateConfig(userId, pkg).neighboringCell = cell;
        mPersistenceLayer.save();
    }

    @Override
    public void setGlobalCell(VCell cell) {
        mGlobalConfig.cell = cell;
        mPersistenceLayer.save();
    }

    @Override
    public void setGlobalAllCell(List<VCell> cell) {
        mGlobalConfig.allCell = cell;
        mPersistenceLayer.save();
    }

    @Override
    public void setGlobalNeighboringCell(List<VCell> cell) {
        mGlobalConfig.neighboringCell = cell;
        mPersistenceLayer.save();
    }

    @Override
    public VCell getCell(int userId, String pkg) {
        VLocConfig config = getOrCreateConfig(userId, pkg);
        mPersistenceLayer.save();
        switch (config.mode) {
            case MODE_USE_SELF:
                return config.cell;
            case MODE_USE_GLOBAL:
                return mGlobalConfig.cell;
            case MODE_CLOSE:
            default:
                return null;
        }
    }

    @Override
    public List<VCell> getAllCell(int userId, String pkg) {
        VLocConfig config = getOrCreateConfig(userId, pkg);
        mPersistenceLayer.save();
        switch (config.mode) {
            case MODE_USE_SELF:
                return config.allCell;
            case MODE_USE_GLOBAL:
                return mGlobalConfig.allCell;
            case MODE_CLOSE:
            default:
                return null;
        }
    }

    @Override
    public List<VCell> getNeighboringCell(int userId, String pkg) {
        VLocConfig config = getOrCreateConfig(userId, pkg);
        mPersistenceLayer.save();
        switch (config.mode) {
            case MODE_USE_SELF:
                return config.neighboringCell;
            case MODE_USE_GLOBAL:
                return mGlobalConfig.neighboringCell;
            case MODE_CLOSE:
            default:
                return null;
        }
    }

    @Override
    public void setLocation(int userId, String pkg, VLocation loc) {
        getOrCreateConfig(userId, pkg).location = loc;
        mPersistenceLayer.save();
    }

    @Override
    public VLocation getLocation(int userId, String pkg) {
        VLocConfig config = getOrCreateConfig(userId, pkg);
        mPersistenceLayer.save();
        switch (config.mode) {
            case MODE_USE_SELF:
                return config.location;
            case MODE_USE_GLOBAL:
                return mGlobalConfig.location;
            case MODE_CLOSE:
            default:
                return null;
        }
    }

    @Override
    public void setGlobalLocation(VLocation loc) {
        mGlobalConfig.location = loc;
    }

    @Override
    public VLocation getGlobalLocation() {
        return mGlobalConfig.location;
    }

}
