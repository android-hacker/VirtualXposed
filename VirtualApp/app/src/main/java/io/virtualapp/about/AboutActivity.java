package io.virtualapp.about;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import java.util.Calendar;

import io.virtualapp.R;
import io.virtualapp.abs.ui.VActivity;
import mehdi.sakout.aboutpage.AboutPage;
import mehdi.sakout.aboutpage.Element;
import moe.feng.alipay.zerosdk.AlipayZeroSdk;

/**
 * author: weishu on 18/1/12.
 */
public class AboutActivity extends VActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View aboutPage = new AboutPage(this)
                .isRTL(false)
                .setImage(R.mipmap.ic_launcher)
                .addItem(getVersionElement())
                .addItem(getFeedbackElement())
                .addItem(getFeedbackWechatElement())
                .addEmail("twsxtd@gmail.com")
                .addWebsite("https://github.com/android-hacker/VAExposed")
                .addGitHub("tiann")
                .addItem(getDonateElement())
                .addItem(getCopyRightsElement())
                .create();

        setContentView(aboutPage);
    }

    Element getCopyRightsElement() {
        Element copyRightsElement = new Element();
        final String copyrights = String.format(getString(R.string.copy_right), Calendar.getInstance().get(Calendar.YEAR));
        copyRightsElement.setTitle(copyrights);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
            copyRightsElement.setIconDrawable(R.drawable.about_icon_copy_right);
        }
        copyRightsElement.setIconTint(mehdi.sakout.aboutpage.R.color.about_item_icon_color);
        copyRightsElement.setIconNightTint(android.R.color.white);
        copyRightsElement.setGravity(Gravity.CENTER);
        copyRightsElement.setOnClickListener(v ->
                Toast.makeText(v.getContext(), copyrights, Toast.LENGTH_SHORT).show());
        return copyRightsElement;
    }

    Element getVersionElement() {
        Element version = new Element();
        String versionName = "unknown";
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            versionName = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException ignored) {
        }
        version.setTitle(getResources().getString(R.string.about_version_title, versionName));
        return version;
    }

    Element getFeedbackElement() {
        Element feedback = new Element();
        final String qqGroup = "597478474";
        feedback.setTitle(getResources().getString(R.string.about_feedback_title, qqGroup));

        feedback.setOnClickListener(v -> {
            ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            if (clipboardManager != null) {
                clipboardManager.setPrimaryClip(ClipData.newPlainText(null, qqGroup));
            }
            Toast.makeText(v.getContext(), getResources().getString(R.string.about_feedback_tips), Toast.LENGTH_SHORT).show();
        });
        return feedback;
    }

    Element getFeedbackWechatElement() {
        Element feedback = new Element();
        final String weChatGroup = "CSYJZF";
        feedback.setTitle(getResources().getString(R.string.about_feedback_wechat_title, weChatGroup));

        feedback.setOnClickListener(v -> {
            ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            if (clipboardManager != null) {
                clipboardManager.setPrimaryClip(ClipData.newPlainText(null, weChatGroup));
            }
            Toast.makeText(v.getContext(), getResources().getString(R.string.about_feedback_tips), Toast.LENGTH_SHORT).show();
        });
        return feedback;
    }

    Element getDonateElement() {
        Element donate = new Element();
        donate.setTitle(getResources().getString(R.string.about_donate_title));
        donate.setIconDrawable(R.drawable.ic_menu_donate);
        donate.setOnClickListener(v -> {
            AlertDialog alertDialog = new AlertDialog.Builder(this, R.style.Theme_AppCompat_DayNight_Dialog_Alert)
                    .setTitle(R.string.donate_dialog_title)
                    .setMessage(R.string.donate_dialog_content)
                    .setPositiveButton(R.string.donate_dialog_yes, (dialog, which) -> {
                        if (!AlipayZeroSdk.hasInstalledAlipayClient(v.getContext())) {
                            Toast.makeText(v.getContext(), R.string.prompt_alipay_not_found, Toast.LENGTH_SHORT).show();
                            return;
                        }
                        AlipayZeroSdk.startAlipayClient(AboutActivity.this, "FKX016770URBZGZSR37U37");
                    })
                    .setNegativeButton(R.string.donate_dialog_no, ((dialog, which) -> {
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_VIEW);
                        intent.addCategory(Intent.CATEGORY_BROWSABLE);
                        intent.setData(Uri.parse("https://github.com/android-hacker/exposed"));
                        startActivity(intent);
                    }))
                    .create();
            try {
                alertDialog.show();
            } catch (Throwable ignored) {
                // BadTokenException.
            }
        });
        return donate;
    }
}
