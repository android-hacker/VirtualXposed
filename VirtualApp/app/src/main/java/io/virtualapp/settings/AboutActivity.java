package io.virtualapp.settings;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import java.util.Calendar;

import io.virtualapp.R;
import io.virtualapp.abs.ui.VActivity;
import io.virtualapp.update.VAVersionService;
import mehdi.sakout.aboutpage.AboutPage;
import mehdi.sakout.aboutpage.Element;

/**
 * author: weishu on 18/1/12.
 */
public class AboutActivity extends VActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AboutPage page = new AboutPage(this)
                .isRTL(false)
                .setImage(R.mipmap.ic_launcher)
                .addItem(getVersionElement())
                .addItem(getCheckUpdateElement())
                .addItem(getFeedbackElement())
                .addItem(getFeedbackWechatElement())
                .addItem(getThanksElement())
                .addEmail("va1xposed@gmail.com")
                .addWebsite("https://github.com/android-hacker/VAExposed")
                .addGitHub("tiann")
                .addItem(getCopyRightsElement());
        View aboutPage = page.create();
        setContentView(aboutPage);
    }

    Element getCopyRightsElement() {
        Element copyRightsElement = new Element();
        final String copyrights = String.format(getString(R.string.copy_right), Calendar.getInstance().get(Calendar.YEAR));
        copyRightsElement.setTitle(copyrights);
        copyRightsElement.setIconDrawable(R.drawable.about_icon_copy_right);
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

    Element getThanksElement() {
        Element thanks = new Element();
        thanks.setTitle(getResources().getString(R.string.about_thanks));
        thanks.setOnClickListener(v -> {
            AlertDialog alertDialog = new AlertDialog.Builder(this, R.style.Theme_AppCompat_DayNight_Dialog_Alert)
                    .setTitle(R.string.thanks_dialog_title)
                    .setMessage(R.string.thanks_dialog_content)
                    .setPositiveButton(R.string.about_icon_yes, null)
                    .create();
            try {
                alertDialog.show();
            } catch (Throwable ignored) {
                // BadTokenException.
            }
        });
        return thanks;
    }

    Element getCheckUpdateElement() {
        Element checkUpdate = new Element();
        checkUpdate.setTitle(getResources().getString(R.string.check_update));
        checkUpdate.setOnClickListener(v -> {
            VAVersionService.checkUpdateImmediately(getApplicationContext(), true);
        });
        return checkUpdate;
    }
}
