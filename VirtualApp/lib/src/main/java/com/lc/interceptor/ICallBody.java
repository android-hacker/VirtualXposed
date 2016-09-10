package com.lc.interceptor;

import android.os.Parcel;
import android.os.Parcelable;

import com.lc.interceptor.client.hook.base.InterceptorHook;

/**
 * Created by lichen:) on 2016/9/9.
 */
public class ICallBody implements Parcelable {

    public String module;

    public String method;

    public Object[] args;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.module);
        dest.writeString(this.method);
        dest.writeArray(args);
    }

    public ICallBody(InterceptorHook interceptorHook) {
        module = interceptorHook.getDelegatePatch().getCanonicalName();
        method = interceptorHook.getName();
    }
    public ICallBody arg(Object ... par){
        this.args = par;
        return this;
    }

    protected ICallBody(Parcel in, ClassLoader classLoader) {
        this.module = in.readString();
        this.method = in.readString();
        this.args = in.readArray(classLoader);
    }

    public static final Parcelable.ClassLoaderCreator<ICallBody> CREATOR
            = new Parcelable.ClassLoaderCreator<ICallBody>() {
        @Override
        public ICallBody createFromParcel(Parcel source, ClassLoader loader) {
            return new ICallBody(source, loader);
        }

        public ICallBody createFromParcel(Parcel in) {
            return new ICallBody(in, null);
        }


        public ICallBody[] newArray(int size) {
            return new ICallBody[size];
        }
    };
}
