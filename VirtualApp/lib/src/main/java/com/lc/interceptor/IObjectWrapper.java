package com.lc.interceptor;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by lichen:) on 2016/9/9.
 */
public class IObjectWrapper<T> implements Parcelable{
    public IObjectWrapper(T parcelable) {
        this.parcelable = parcelable;
    }

    T parcelable;

    public T get(){
      return parcelable;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(parcelable);
    }

    public static final Parcelable.ClassLoaderCreator<IObjectWrapper> CREATOR
            = new Parcelable.ClassLoaderCreator<IObjectWrapper>() {
        @Override
        public IObjectWrapper createFromParcel(Parcel source, ClassLoader loader) {
            return new IObjectWrapper(source,loader);
        }

        public IObjectWrapper createFromParcel(Parcel in) {
            return new IObjectWrapper(in, null);
        }



        public IObjectWrapper[] newArray(int size) {
            return new IObjectWrapper[size];
        }
    };

    private IObjectWrapper(Parcel in, ClassLoader classLoader) {
        parcelable = (T)in.readValue(classLoader);
    }
}
