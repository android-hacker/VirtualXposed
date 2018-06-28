package com.lody.virtual.client.hook.providers;

import android.content.ContentValues;
import android.database.CharArrayBuffer;
import android.database.CrossProcessCursorWrapper;
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

    static class MediaCursorProxy extends CrossProcessCursorWrapper {

        private int dataIndex;

        /**
         * Creates a cross process cursor wrapper.
         *
         * @param cursor The underlying cursor to wrap.
         */
        public MediaCursorProxy(Cursor cursor) {
            super(cursor);
            dataIndex = cursor.getColumnIndex(COLUMN_NAME);
        }

        @Override
        public void copyStringToBuffer(int columnIndex, CharArrayBuffer buffer) {
            if (columnIndex < 0 || columnIndex != this.dataIndex || buffer == null) {
                super.copyStringToBuffer(columnIndex, buffer);
                return;
            }

            String path = getString(columnIndex);
            if (path == null) {
                super.copyStringToBuffer(columnIndex, buffer);
                return;
            }

            char[] chars = path.toCharArray();
            int v1 = Math.min(chars.length, buffer.data.length);
            System.arraycopy(chars, 0, buffer.data, 0, v1);
            buffer.sizeCopied = v1;
        }

        @Override
        public String getString(int columnIndex) {
            String originalPath = super.getString(columnIndex);
            if (columnIndex < 0 || columnIndex != this.dataIndex) {
                return originalPath;
            }
            String path = NativeEngine.getEscapePath(originalPath);
            return path;
        }
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
        return new MediaCursorProxy(cursor);
    }
}
