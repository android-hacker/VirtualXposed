package io.virtualapp.home.models;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;

import com.lody.virtual.helper.proto.AppInfo;


/**
 * @author Lody
 */
public class AppModel implements Parcelable {

    public Context context;
    public String packageName;
    public String path;
    public String name;
    public Drawable icon;

    public AppModel() {
        //For Database
    }

    public AppModel(Context context, PackageInfo packageInfo) {
        this.context = context;
        this.packageName = packageInfo.packageName;
        this.path = packageInfo.applicationInfo.publicSourceDir;
        loadData(packageInfo.applicationInfo);
    }


    public AppModel(Context context, AppInfo appInfo) {
        this.context = context;
        this.packageName = appInfo.packageName;
        this.path = appInfo.apkPath;
        loadData(appInfo.applicationInfo);

    }


    protected AppModel(Parcel in) {
        packageName = in.readString();
        path = in.readString();
        name = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(packageName);
        dest.writeString(path);
        dest.writeString(name);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<AppModel> CREATOR = new Creator<AppModel>() {
        @Override
        public AppModel createFromParcel(Parcel in) {
            return new AppModel(in);
        }

        @Override
        public AppModel[] newArray(int size) {
            return new AppModel[size];
        }
    };

    public void loadData(ApplicationInfo appInfo) {
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

}
