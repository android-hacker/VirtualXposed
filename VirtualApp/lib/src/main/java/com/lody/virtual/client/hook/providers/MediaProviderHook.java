package com.lody.virtual.client.hook.providers;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;

import com.lody.virtual.client.NativeEngine;
import com.lody.virtual.client.hook.base.MethodBox;

import java.lang.reflect.InvocationTargetException;

/**
 * @author weishu
 * @date 2018/6/28.
 */
class MediaProviderHook extends ProviderHook {
    private static final String COLUMN_NAME = "_data";

    MediaProviderHook(Object base) {
        super(base);
    }

    @Override
    public Uri insert(MethodBox methodBox, Uri url, ContentValues initialValues) throws InvocationTargetException {
        if (!(MediaStore.Audio.Media.INTERNAL_CONTENT_URI.equals(url) ||
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI.equals(url)) ||
                MediaStore.Video.Media.INTERNAL_CONTENT_URI.equals(url) ||
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI.equals(url) ||
                MediaStore.Images.Media.INTERNAL_CONTENT_URI.equals(url) ||
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI.equals(url)
                ) {
            return super.insert(methodBox, url, initialValues);
        }

        Object v2 = initialValues.get(COLUMN_NAME);
        if (!(v2 instanceof String)) {
            return super.insert(methodBox, url, initialValues);
        }
        String path = NativeEngine.getEscapePath((String) v2);
        initialValues.put(COLUMN_NAME, path);
        return super.insert(methodBox, url, initialValues);
    }

    @Override
    public Cursor query(MethodBox methodBox, Uri url, String[] projection, String selection, String[] selectionArgs, String sortOrder, Bundle originQueryArgs) throws InvocationTargetException {
        Cursor cursor = super.query(methodBox, url, projection, selection, selectionArgs, sortOrder, originQueryArgs);
        return new QueryRedirectCursor(cursor, COLUMN_NAME);
    }
}
