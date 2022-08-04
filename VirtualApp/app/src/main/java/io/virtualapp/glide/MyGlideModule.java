package io.virtualapp.glide;

import android.content.Context;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.load.engine.cache.LruResourceCache;
import com.bumptech.glide.load.engine.cache.MemorySizeCalculator;
import com.bumptech.glide.module.AppGlideModule;
import com.lody.virtual.helper.utils.VLog;

import java.io.InputStream;

/**
 * Created by Windy on 2018/10/25
 */
@GlideModule
public class MyGlideModule extends AppGlideModule {
    @Override
    public void applyOptions(Context context, GlideBuilder builder) {
        MemorySizeCalculator calculator = new MemorySizeCalculator.Builder(context)
                .build();
        builder.setMemoryCache(new LruResourceCache(calculator.getMemoryCacheSize() / 2));

        VLog.i("MyGlideModule", "applyOptions");
    }

    @Override
    public boolean isManifestParsingEnabled() {
        return false;
    }

    @Override
    public void registerComponents(Context context, Glide glide, Registry registry) {
        super.registerComponents(context, glide, registry);
        registry.prepend(String.class, InputStream.class, new PackageIconResourceLoaderFactory(context));
    }
}