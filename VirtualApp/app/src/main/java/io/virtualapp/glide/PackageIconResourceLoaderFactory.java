package io.virtualapp.glide;

import android.content.Context;
import android.support.annotation.NonNull;

import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.MultiModelLoaderFactory;

import java.io.InputStream;

/**
 * Created by Windy on 2018/10/25
 */
public class PackageIconResourceLoaderFactory implements ModelLoaderFactory<String, InputStream> {

    private Context context;

    public PackageIconResourceLoaderFactory(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public ModelLoader<String, InputStream> build(@NonNull MultiModelLoaderFactory multiFactory) {
        return new PackageIconResourceLoader(context);
    }

    @Override
    public void teardown() {

    }
}
