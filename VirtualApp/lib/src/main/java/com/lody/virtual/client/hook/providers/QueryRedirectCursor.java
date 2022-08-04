package com.lody.virtual.client.hook.providers;

import android.database.CharArrayBuffer;
import android.database.CrossProcessCursorWrapper;
import android.database.Cursor;

import com.lody.virtual.client.NativeEngine;

/**
 * @author weishu
 * @date 2018/6/29.
 */
class QueryRedirectCursor extends CrossProcessCursorWrapper {

    private int dataIndex;

    /**
     * Creates a cross process cursor wrapper.
     *
     * @param cursor The underlying cursor to wrap.
     */
    QueryRedirectCursor(Cursor cursor, String columnName) {
        super(cursor);
        dataIndex = cursor.getColumnIndex(columnName);
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
        return NativeEngine.getEscapePath(originalPath);
    }
}
