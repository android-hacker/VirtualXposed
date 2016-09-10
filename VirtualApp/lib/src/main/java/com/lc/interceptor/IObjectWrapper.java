package com.lc.interceptor;

import android.os.Parcel;
import android.os.Parcelable;

import com.lc.interceptor.service.providers.base.InterceptorDataProvider;

/**
 * @author legency
 */
public class IObjectWrapper<T> implements Parcelable{
    public IObjectWrapper() {
    }

    public IObjectWrapper(T parcelable) {
        if(parcelable instanceof InterceptorDataProvider) {
            //Reflect get the original object when void returns
            //可能是void 的返回值
            parcelable = null;
        }
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
