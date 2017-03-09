package io.virtualapp.home.models;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.util.SparseBooleanArray;

import com.lody.virtual.os.VUserHandle;
import com.lody.virtual.remote.AppSetting;

/**
 * @author Lody
 */
public class PackageAppData implements AppData {

    public static final Creator<PackageAppData> CREATOR = new Creator<PackageAppData>() {
        @Override
        public PackageAppData createFromParcel(Parcel source) {
            return new PackageAppData(source);
        }

        @Override
        public PackageAppData[] newArray(int size) {
            return new PackageAppData[size];
        }
    };
    public String packageName;
    public String path;
    public String name;
    public Drawable icon;
    public boolean fastOpen;
    public boolean firstOpen;
    public boolean isLoading;
    private SparseBooleanArray markArray = new SparseBooleanArray();

    public PackageAppData() {
        // For Database
    }

    public PackageAppData(Context context, PackageInfo packageInfo) {
        this.packageName = packageInfo.packageName;
        this.path = packageInfo.applicationInfo.publicSourceDir;
        loadData(context, packageInfo.applicationInfo);
    }

    public PackageAppData(Context context, AppSetting appSetting) {
        this.packageName = appSetting.packageName;
        this.path = appSetting.apkPath;
        loadData(context, appSetting.getApplicationInfo(VUserHandle.USER_OWNER));
    }

    protected PackageAppData(Parcel in) {
        this.packageName = in.readString();
        this.path = in.readString();
        this.name = in.readString();
        this.fastOpen = in.readByte() != 0;
        // TODO: remove the temp code.
        firstOpen = true;
    }

    public void loadData(Context context, ApplicationInfo appInfo) {
        if (appInfo == null) {
            return;
        }
        PackageManager pm = context.getPackageManager();
        try {
            CharSequence sequence = appInfo.loadLabel(pm);
            if (sequence != null) {
                name = sequence.toString();
            }
            icon = appInfo.loadIcon(pm);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.packageName);
        dest.writeString(this.path);
        dest.writeString(this.name);
        dest.writeByte(this.fastOpen ? (byte) 1 : (byte) 0);
    }

    @Override
    public void mark(int tag) {
        markArray.put(tag, true);
    }

    @Override
    public void unMark(int tag) {
        markArray.delete(tag);
    }

    @Override
    public boolean isMarked(int tag) {
        return markArray.get(tag);
    }

    @Override
    public boolean isLoading() {
        return isLoading;
    }

    @Override
    public boolean isFirstOpen() {
        return firstOpen;
    }

    @Override
    public Drawable getIcon() {
        return icon;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean canReorder() {
        return true;
    }

    @Override
    public boolean canLaunch() {
        return true;
    }

    @Override
    public boolean canDelete() {
        return true;
    }

    @Override
    public boolean canCreateShortcut() {
        return true;
    }
}
