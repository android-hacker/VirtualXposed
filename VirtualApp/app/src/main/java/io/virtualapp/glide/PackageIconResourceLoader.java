package io.virtualapp.glide;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.signature.ObjectKey;

import java.io.InputStream;

/**
 * Created by Windy on 2018/10/25
 */
public class PackageIconResourceLoader implements ModelLoader<String, InputStream> {

    public static final String DATA_PACKAGE_PREFIX = "data:packageName/";
    public static final String DATA_PACKAGE_FILE_PATH_PREFIX = "data:packageFilePath/";

    private Context context;


    public PackageIconResourceLoader(Context context) {
        this.context = context;
    }

    @Nullable
    @Override
    public LoadData<InputStream> buildLoadData(@NonNull String model, int width, int height, @NonNull Options options) {
        return new LoadData<>(new ObjectKey(model), new PackageIconResourceDataFetcher(context, model));
    }

    @Override
    public boolean handles(@NonNull String model) {
        return model.startsWith(DATA_PACKAGE_PREFIX) || model.startsWith(DATA_PACKAGE_FILE_PATH_PREFIX);
    }
}
