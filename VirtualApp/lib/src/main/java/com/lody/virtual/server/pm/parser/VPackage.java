package com.lody.virtual.server.pm.parser;

import android.Manifest;
import android.content.ComponentName;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.ConfigurationInfo;
import android.content.pm.FeatureInfo;
import android.content.pm.InstrumentationInfo;
import android.content.pm.PackageParser;
import android.content.pm.PermissionGroupInfo;
import android.content.pm.PermissionInfo;
import android.content.pm.ProviderInfo;
import android.content.pm.ServiceInfo;
import android.content.pm.Signature;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Lody
 */

public class VPackage implements Parcelable {

    public static final Creator<VPackage> CREATOR = new Creator<VPackage>() {
        @Override
        public VPackage createFromParcel(Parcel source) {
            return new VPackage(source);
        }

        @Override
        public VPackage[] newArray(int size) {
            return new VPackage[size];
        }
    };
    public ArrayList<ActivityComponent> activities;
    public ArrayList<ActivityComponent> receivers;
    public ArrayList<ProviderComponent> providers;
    public ArrayList<ServiceComponent> services;
    public ArrayList<InstrumentationComponent> instrumentation;
    public ArrayList<PermissionComponent> permissions;
    public ArrayList<PermissionGroupComponent> permissionGroups;
    public ArrayList<String> requestedPermissions;
    public ArrayList<String> protectedBroadcasts;
    public ApplicationInfo applicationInfo;
    public Signature[] mSignatures;
    public Bundle mAppMetaData;
    public String packageName;
    public int mPreferredOrder;
    public String mVersionName;
    public String mSharedUserId;
    public ArrayList<String> usesLibraries;
    public int mVersionCode;
    public int mSharedUserLabel;
    // Applications hardware preferences
    public ArrayList<ConfigurationInfo> configPreferences = null;
    // Applications requested features
    public ArrayList<FeatureInfo> reqFeatures = null;
    public Object mExtras;

    public VPackage() {
    }

    protected VPackage(Parcel in) {
        int N = in.readInt();
        this.activities = new ArrayList<>(N);
        while (N-- > 0) {
            activities.add(new ActivityComponent(in));
        }
        N = in.readInt();
        this.receivers = new ArrayList<>(N);
        while (N-- > 0) {
            receivers.add(new ActivityComponent(in));
        }
        N = in.readInt();
        this.providers = new ArrayList<>(N);
        while (N-- > 0) {
            providers.add(new ProviderComponent(in));
        }
        N = in.readInt();
        this.services = new ArrayList<>(N);
        while (N-- > 0) {
            services.add(new ServiceComponent(in));
        }
        N = in.readInt();
        this.instrumentation = new ArrayList<>(N);
        while (N-- > 0) {
            instrumentation.add(new InstrumentationComponent(in));
        }
        N = in.readInt();
        this.permissions = new ArrayList<>(N);
        while (N-- > 0) {
            permissions.add(new PermissionComponent(in));
        }
        N = in.readInt();
        this.permissionGroups = new ArrayList<>(N);
        while (N-- > 0) {
            permissionGroups.add(new PermissionGroupComponent(in));
        }
        this.requestedPermissions = in.createStringArrayList();
        this.protectedBroadcasts = in.createStringArrayList();
        this.applicationInfo = in.readParcelable(ApplicationInfo.class.getClassLoader());
        this.mAppMetaData = in.readBundle(Bundle.class.getClassLoader());
        this.packageName = in.readString();
        this.mPreferredOrder = in.readInt();
        this.mVersionName = in.readString();
        this.mSharedUserId = in.readString();
        this.usesLibraries = in.createStringArrayList();
        this.mVersionCode = in.readInt();
        this.mSharedUserLabel = in.readInt();
        this.configPreferences = in.createTypedArrayList(ConfigurationInfo.CREATOR);
        this.reqFeatures = in.createTypedArrayList(FeatureInfo.CREATOR);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.activities.size());
        for (ActivityComponent component : activities) {
            dest.writeParcelable(component.info, 0);
            dest.writeString(component.className);
            dest.writeBundle(component.metaData);
            dest.writeInt(component.intents != null ? component.intents.size() : 0);
            if (component.intents != null) {
                for (ActivityIntentInfo info : component.intents) {
                    info.writeToParcel(dest, flags);
                }
            }
        }
        dest.writeInt(this.receivers.size());
        for (ActivityComponent component : receivers) {
            dest.writeParcelable(component.info, 0);
            dest.writeString(component.className);
            dest.writeBundle(component.metaData);
            dest.writeInt(component.intents != null ? component.intents.size() : 0);
            if (component.intents != null) {
                for (ActivityIntentInfo info : component.intents) {
                    info.writeToParcel(dest, flags);
                }
            }
        }
        dest.writeInt(this.providers.size());
        for (ProviderComponent component : providers) {
            dest.writeParcelable(component.info, 0);
            dest.writeString(component.className);
            dest.writeBundle(component.metaData);
            dest.writeInt(component.intents != null ? component.intents.size() : 0);
            if (component.intents != null) {
                for (ProviderIntentInfo info : component.intents) {
                    info.writeToParcel(dest, flags);
                }
            }
        }
        dest.writeInt(this.services.size());
        for (ServiceComponent component : services) {
            dest.writeParcelable(component.info, 0);
            dest.writeString(component.className);
            dest.writeBundle(component.metaData);
            dest.writeInt(component.intents != null ? component.intents.size() : 0);
            if (component.intents != null) {
                for (ServiceIntentInfo info : component.intents) {
                    info.writeToParcel(dest, flags);
                }
            }
        }
        dest.writeInt(this.instrumentation.size());
        for (InstrumentationComponent component : instrumentation) {
            dest.writeParcelable(component.info, 0);
            dest.writeString(component.className);
            dest.writeBundle(component.metaData);
            dest.writeInt(component.intents != null ? component.intents.size() : 0);
            if (component.intents != null) {
                for (IntentInfo info : component.intents) {
                    info.writeToParcel(dest, flags);
                }
            }
        }
        dest.writeInt(this.permissions.size());
        for (PermissionComponent component : permissions) {
            dest.writeParcelable(component.info, 0);
            dest.writeString(component.className);
            dest.writeBundle(component.metaData);
            dest.writeInt(component.intents != null ? component.intents.size() : 0);
            if (component.intents != null) {
                for (IntentInfo info : component.intents) {
                    info.writeToParcel(dest, flags);
                }
            }
        }
        dest.writeInt(this.permissionGroups.size());
        for (PermissionGroupComponent component : permissionGroups) {
            dest.writeParcelable(component.info, 0);
            dest.writeString(component.className);
            dest.writeBundle(component.metaData);
            dest.writeInt(component.intents != null ? component.intents.size() : 0);
            if (component.intents != null) {
                for (IntentInfo info : component.intents) {
                    info.writeToParcel(dest, flags);
                }
            }
        }
        dest.writeStringList(this.requestedPermissions);
        dest.writeStringList(this.protectedBroadcasts);
        dest.writeParcelable(this.applicationInfo, flags);
        dest.writeBundle(this.mAppMetaData);
        dest.writeString(this.packageName);
        dest.writeInt(this.mPreferredOrder);
        dest.writeString(this.mVersionName);
        dest.writeString(this.mSharedUserId);
        dest.writeStringList(this.usesLibraries);
        dest.writeInt(this.mVersionCode);
        dest.writeInt(this.mSharedUserLabel);
        dest.writeTypedList(this.configPreferences);
        dest.writeTypedList(this.reqFeatures);
    }

    public static class ActivityIntentInfo extends IntentInfo {

        public ActivityComponent activity;

        public ActivityIntentInfo(PackageParser.IntentInfo info) {
            super(info);
        }

        protected ActivityIntentInfo(Parcel in) {
            super(in);
        }
    }

    public static class ServiceIntentInfo extends IntentInfo {
        public ServiceComponent service;

        public ServiceIntentInfo(PackageParser.IntentInfo info) {
            super(info);
        }

        protected ServiceIntentInfo(Parcel in) {
            super(in);
        }
    }

    public static class ProviderIntentInfo extends IntentInfo {
        public ProviderComponent provider;

        public ProviderIntentInfo(PackageParser.IntentInfo info) {
            super(info);
        }

        protected ProviderIntentInfo(Parcel in) {
            super(in);
        }
    }

    public static class IntentInfo implements Parcelable {
        public static final Creator<IntentInfo> CREATOR = new Creator<IntentInfo>() {
            @Override
            public IntentInfo createFromParcel(Parcel source) {
                return new IntentInfo(source);
            }

            @Override
            public IntentInfo[] newArray(int size) {
                return new IntentInfo[size];
            }
        };
        public IntentFilter filter;
        public boolean hasDefault;
        public int labelRes;
        public String nonLocalizedLabel;
        public int icon;
        public int logo;
        public int banner;

        public IntentInfo(PackageParser.IntentInfo info) {
            this.filter = info;
            this.hasDefault = info.hasDefault;
            this.labelRes = info.labelRes;
            if (info.nonLocalizedLabel != null) {
                this.nonLocalizedLabel = info.nonLocalizedLabel.toString();
            }
            this.icon = info.icon;
            this.logo = info.logo;
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
                this.banner = info.banner;
            }

        }

        protected IntentInfo(Parcel in) {
            this.filter = in.readParcelable(VPackage.class.getClassLoader());
            this.hasDefault = in.readByte() != 0;
            this.labelRes = in.readInt();
            this.nonLocalizedLabel = in.readString();
            this.icon = in.readInt();
            this.logo = in.readInt();
            this.banner = in.readInt();
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeParcelable(this.filter, flags);
            dest.writeByte(this.hasDefault ? (byte) 1 : (byte) 0);
            dest.writeInt(this.labelRes);
            dest.writeString(this.nonLocalizedLabel);
            dest.writeInt(this.icon);
            dest.writeInt(this.logo);
            dest.writeInt(this.banner);
        }
    }

    public static class Component<II extends IntentInfo> {
        public VPackage owner;
        public ArrayList<II> intents;
        public String className;
        public Bundle metaData;
        private ComponentName componentName;

        protected Component() {

        }

        public Component(PackageParser.Component component) {
            this.className = component.className;
            this.metaData = component.metaData;
        }

        public ComponentName getComponentName() {
            if (componentName != null) {
                return componentName;
            }
            if (className != null) {
                componentName = new ComponentName(owner.packageName,
                        className);
            }
            return componentName;
        }

    }

    public static class ActivityComponent extends Component<ActivityIntentInfo> {
        public ActivityInfo info;

        public ActivityComponent(PackageParser.Activity activity) {
            super(activity);
            if (activity.intents != null) {
                this.intents = new ArrayList<>(activity.intents.size());
                for (Object o : activity.intents) {
                    intents.add(new ActivityIntentInfo((PackageParser.IntentInfo) o));
                }
            }
            info = activity.info;
        }

        protected ActivityComponent(Parcel src) {
            info = src.readParcelable(ActivityInfo.class.getClassLoader());
            className = src.readString();
            metaData = src.readBundle(Bundle.class.getClassLoader());
            int N = src.readInt();
            intents = new ArrayList<>(N);
            while (N-- > 0) {
                intents.add(new ActivityIntentInfo(src));
            }
        }
    }

    public static class ServiceComponent extends Component<ServiceIntentInfo> {
        public ServiceInfo info;

        public ServiceComponent(PackageParser.Service service) {
            super(service);
            if (service.intents != null) {
                this.intents = new ArrayList<>(service.intents.size());
                for (Object o : service.intents) {
                    intents.add(new ServiceIntentInfo((PackageParser.IntentInfo) o));
                }
            }
            this.info = service.info;
        }

        protected ServiceComponent(Parcel src) {
            info = src.readParcelable(ActivityInfo.class.getClassLoader());
            className = src.readString();
            metaData = src.readBundle(Bundle.class.getClassLoader());
            int N = src.readInt();
            intents = new ArrayList<>(N);
            while (N-- > 0) {
                intents.add(new ServiceIntentInfo(src));
            }
        }
    }

    public static class ProviderComponent extends Component<ProviderIntentInfo> {
        public ProviderInfo info;

        public ProviderComponent(PackageParser.Provider provider) {
            super(provider);
            if (provider.intents != null) {
                this.intents = new ArrayList<>(provider.intents.size());
                for (Object o : provider.intents) {
                    intents.add(new ProviderIntentInfo((PackageParser.IntentInfo) o));
                }
            }
            this.info = provider.info;
        }

        protected ProviderComponent(Parcel src) {
            info = src.readParcelable(ActivityInfo.class.getClassLoader());
            className = src.readString();
            metaData = src.readBundle(Bundle.class.getClassLoader());
            int N = src.readInt();
            intents = new ArrayList<>(N);
            while (N-- > 0) {
                intents.add(new ProviderIntentInfo(src));
            }
        }
    }

    public static class InstrumentationComponent extends Component<IntentInfo> {
        public InstrumentationInfo info;

        public InstrumentationComponent(PackageParser.Instrumentation i) {
            super(i);
            this.info = i.info;
        }

        protected InstrumentationComponent(Parcel src) {
            info = src.readParcelable(ActivityInfo.class.getClassLoader());
            className = src.readString();
            metaData = src.readBundle(Bundle.class.getClassLoader());
            int N = src.readInt();
            intents = new ArrayList<>(N);
            while (N-- > 0) {
                intents.add(new IntentInfo(src));
            }
        }
    }

    public static class PermissionComponent extends Component<IntentInfo> {

        // https://developer.android.com/guide/topics/security/permissions?hl=zh-cn
        public static Set<String> DANGEROUS_PERMISSION = new HashSet<String>() {{
            // CALENDAR group
            add(Manifest.permission.READ_CALENDAR);
            add(Manifest.permission.WRITE_CALENDAR);

            // CAMERA
            add(Manifest.permission.CAMERA);

            // CONTACTS
            add(Manifest.permission.READ_CONTACTS);
            add(Manifest.permission.WRITE_CONTACTS);
            add(Manifest.permission.GET_ACCOUNTS);

            // LOCATION
            add(Manifest.permission.ACCESS_FINE_LOCATION);
            add(Manifest.permission.ACCESS_COARSE_LOCATION);

            // PHONE
            add(Manifest.permission.READ_PHONE_STATE);
            add(Manifest.permission.CALL_PHONE);
            add(Manifest.permission.READ_CALL_LOG);
            add(Manifest.permission.WRITE_CALL_LOG);
            add(Manifest.permission.ADD_VOICEMAIL);
            add(Manifest.permission.USE_SIP);
            add(Manifest.permission.PROCESS_OUTGOING_CALLS);

            // SENSORS
            add(Manifest.permission.BODY_SENSORS);

            // SMS
            add(Manifest.permission.SEND_SMS);
            add(Manifest.permission.RECEIVE_SMS);
            add(Manifest.permission.READ_SMS);
            add(Manifest.permission.RECEIVE_WAP_PUSH);
            add(Manifest.permission.RECEIVE_MMS);

            // STORAGE
            add(Manifest.permission.READ_EXTERNAL_STORAGE);
            add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }};

        public PermissionInfo info;

        public PermissionComponent(PackageParser.Permission p) {
            super(p);
            this.info = p.info;
        }

        protected PermissionComponent(Parcel src) {
            info = src.readParcelable(ActivityInfo.class.getClassLoader());
            className = src.readString();
            metaData = src.readBundle(Bundle.class.getClassLoader());
            int N = src.readInt();
            intents = new ArrayList<>(N);
            while (N-- > 0) {
                intents.add(new IntentInfo(src));
            }
        }
    }

    public static class PermissionGroupComponent extends Component<IntentInfo> {
        public PermissionGroupInfo info;


        public PermissionGroupComponent(PackageParser.PermissionGroup p) {
            super(p);
            this.info = p.info;
        }

        protected PermissionGroupComponent(Parcel src) {
            info = src.readParcelable(ActivityInfo.class.getClassLoader());
            className = src.readString();
            metaData = src.readBundle(Bundle.class.getClassLoader());
            int N = src.readInt();
            intents = new ArrayList<>(N);
            while (N-- > 0) {
                intents.add(new IntentInfo(src));
            }
        }
    }
}
