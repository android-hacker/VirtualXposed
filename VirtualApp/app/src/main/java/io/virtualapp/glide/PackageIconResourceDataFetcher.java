package io.virtualapp.glide;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.data.DataFetcher;
import com.lody.virtual.helper.utils.VLog;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static io.virtualapp.glide.PackageIconResourceLoader.DATA_PACKAGE_FILE_PATH_PREFIX;
import static io.virtualapp.glide.PackageIconResourceLoader.DATA_PACKAGE_PREFIX;

/**
 * Created by Windy on 2018/10/25
 */
public class PackageIconResourceDataFetcher implements DataFetcher<InputStream> {

    private static final String TAG = PackageIconResourceDataFetcher.class.getSimpleName();

    private Context context;
    private String packageModel;

    private InputStream data;

    public PackageIconResourceDataFetcher(Context context, String packageName) {
        this.context = context.getApplicationContext();
        this.packageModel = packageName;
    }

    @Override
    public void loadData(@NonNull Priority priority, @NonNull DataCallback<? super InputStream> callback) {
        try {
            data = loadResource();
        } catch (Exception e) {
            VLog.e(TAG, "Failed to load data from asset manager", e);
            callback.onLoadFailed(e);
            return;
        }
        callback.onDataReady(data);
    }

    @Override
    public void cleanup() {
        if (data == null) {
            return;
        }
        try {
            data.close();
        } catch (IOException e) {
            // Ignored.
        }
    }

    @Override
    public void cancel() {

    }

    @NonNull
    @Override
    public Class<InputStream> getDataClass() {
        return InputStream.class;
    }

    @NonNull
    @Override
    public DataSource getDataSource() {
        return DataSource.LOCAL;
    }

    //load icon res accord to package name, or apk path
    private InputStream loadResource() {
        PackageInfo packageInfo = null;
        Drawable drawable = null;
        try {
            packageInfo = getPackageInfo();
            if (packageInfo == null) {
                return null;
            }

            drawable = packageInfo.applicationInfo.loadIcon(context.getPackageManager());
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (drawable == null) {
            return null;
        }
        return drawableToInputStream(drawable);
    }

    private PackageInfo getPackageInfo() throws PackageManager.NameNotFoundException {
        if (packageModel.startsWith(DATA_PACKAGE_PREFIX)) {
            return context.getPackageManager().getPackageInfo(getPackageTrueModel(DATA_PACKAGE_PREFIX), 0);
        } else if (packageModel.startsWith(DATA_PACKAGE_FILE_PATH_PREFIX)) {
            return context.getPackageManager().getPackageArchiveInfo(getPackageTrueModel(DATA_PACKAGE_FILE_PATH_PREFIX), 0);
        }
        return null;
    }

    private String getPackageTrueModel(String prefix) {
        return packageModel.replaceAll(prefix, "");
    }

    private InputStream drawableToInputStream(Drawable drawable) {
        Bitmap bitmap = drawableToBitmap(drawable);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream); //use the compression format of your need
        return new ByteArrayInputStream(stream.toByteArray());
    }

    private static Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        int width = drawable.getIntrinsicWidth();
        width = width > 0 ? width : 1;
        int height = drawable.getIntrinsicHeight();
        height = height > 0 ? height : 1;

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

}
