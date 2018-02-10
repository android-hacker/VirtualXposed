package io.virtualapp.home;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import io.virtualapp.R;
import scut.carson_ho.kawaii_loadingview.Kawaii_LoadingView;

/**
 * author: weishu on 18/2/9.
 */

public class LoadingDialog {
    Kawaii_LoadingView mLoadingView;
    TextView mLoadingText;

    AlertDialog mDialog;

    public LoadingDialog(Context context) {
        View root = LayoutInflater.from(context).inflate(R.layout.dialog_loading, null);

        mLoadingView = root.findViewById(R.id.loadingView);
        mLoadingText = root.findViewById(R.id.loading_text);

        mDialog = new AlertDialog.Builder(context)
                .setView(root)
                .setCancelable(false)
                .create();
        mDialog.getWindow().setBackgroundDrawable(new ColorDrawable());
    }

    public void startLoading() {
        mLoadingView.startMoving();
    }

    public void stopLoading() {
        mLoadingView.stopMoving();
    }

    public void setTitle(CharSequence title) {
        mLoadingText.setText(title);
    }

    public void show() {
        try {
            mDialog.show();
        } catch (Throwable ignored) {
        }
    }

    public void dismiss() {
        try {
            stopLoading();
            mDialog.dismiss();
        } catch (Throwable ignored) {
        }
    }
}
