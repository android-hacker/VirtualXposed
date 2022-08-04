package io.virtualapp.utils;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import io.virtualapp.R;
import moe.feng.alipay.zerosdk.AlipayZeroSdk;

/**
 * @author weishu
 * @date 2018/10/29.
 */
public class Misc {
    public static void showDonate(Activity context) {
        final String alipay = context.getResources().getString(R.string.donate_alipay);
        final String[] items = {alipay, "PayPal", "Bitcoin"};

        AlertDialog chooseDialog = new AlertDialog.Builder(context, R.style.Theme_AppCompat_DayNight_Dialog_Alert)
                .setTitle(R.string.donate_choose_title)
                .setItems(items, (dialog1, which1) -> {
                    dialog1.dismiss();
                    if (which1 == 0) {
                        if (!AlipayZeroSdk.hasInstalledAlipayClient(context)) {
                            Toast.makeText(context, R.string.prompt_alipay_not_found, Toast.LENGTH_SHORT).show();
                            return;
                        }
                        AlipayZeroSdk.startAlipayClient(context, "FKX016770URBZGZSR37U37");
                    } else if (which1 == 1) {
                        try {
                            Intent t = new Intent(Intent.ACTION_VIEW);
                            t.setData(Uri.parse("https://paypal.me/virtualxposed"));
                            context.startActivity(t);
                        } catch (Throwable ignored) {
                            ignored.printStackTrace();
                        }
                    } else if (which1 == 2) {
                        final String address = "39Wst8oL74pRP2vKPkPihH6RFQF4hWoBqU";

                        try {
                            ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                            if (clipboardManager != null) {
                                clipboardManager.setPrimaryClip(ClipData.newPlainText(null, address));
                            }
                            Toast.makeText(context, context.getResources().getString(R.string.donate_bitconins_tips), Toast.LENGTH_SHORT).show();
                        } catch (Throwable ignored) {
                            ignored.printStackTrace();
                        }
                    }
                })
                .create();
        chooseDialog.show();
    }
}
