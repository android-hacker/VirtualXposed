package io.virtualapp.settings;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
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

    private AboutPage mPage;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPage = new AboutPage(this)
                .isRTL(false)
                .setImage(R.mipmap.ic_launcher)
                .addItem(getCopyRightsElement())
                .addItem(getVersionElement())
                .addItem(getCheckUpdateElement())
                .addItem(getFeedbackEmailElement())
                .addItem(getThanksElement())
                .addItem(getFeedbacTelegramElement())
                .addItem(getWebsiteElement())
                .addGitHub("tiann");

        View aboutPage = mPage.create();

        setContentView(aboutPage);
    }

    Element getCopyRightsElement() {
        Element copyRightsElement = new Element();
        final String copyrights = String.format(getString(R.string.copy_right), Calendar.getInstance().get(Calendar.YEAR));
        copyRightsElement.setTitle(copyrights);
        copyRightsElement.setGravity(Gravity.START);
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

        final int[] clickCount = {0};
        version.setOnClickListener(v -> {
            clickCount[0]++;
            if (clickCount[0] == 3) {
                mPage.addItem(getFeedbackQQElement());
                mPage.addItem(getFeedbackWechatElement());
            }
        });
        return version;
    }

    Element getFeedbackQQElement() {
        Element feedback = new Element();
        final String qqGroup = "597478474";
        feedback.setTitle(getResources().getString(R.string.about_feedback_qq_title));

        feedback.setOnClickListener(v -> {
            ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            if (clipboardManager != null) {
                clipboardManager.setPrimaryClip(ClipData.newPlainText(null, qqGroup));
            }
            Toast.makeText(v.getContext(), getResources().getString(R.string.about_feedback_tips), Toast.LENGTH_SHORT).show();
        });
        return feedback;
    }

    Element getFeedbackEmailElement() {
        Element emailElement = new Element();
        final String email = "virtualxposed@gmail.com";
        String title = getResources().getString(R.string.about_feedback_title);
        emailElement.setTitle(title);

        Uri uri = Uri.parse("mailto:" + email);
        Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
        intent.putExtra(Intent.EXTRA_SUBJECT, title); // 主题

        String hint = getResources().getString(R.string.about_feedback_hint);
        intent.putExtra(Intent.EXTRA_TEXT, hint);
        emailElement.setIntent(intent);
        return emailElement;
    }

    Element getFeedbackWechatElement() {
        Element feedback = new Element();
        // final String weChatGroup = "CSYJZF";
        feedback.setTitle(getResources().getString(R.string.about_feedback_wechat_title));

        feedback.setOnClickListener(v -> {
            ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            if (clipboardManager != null) {
                clipboardManager.setPrimaryClip(ClipData.newPlainText(null, "VirtualXposed"));
            }
            Toast.makeText(v.getContext(), getResources().getString(R.string.about_feedback_tips), Toast.LENGTH_SHORT).show();
        });
        return feedback;
    }

    Element getFeedbacTelegramElement() {
        Element feedback = new Element();
        final String weChatGroup = "VirtualXposed";
        feedback.setTitle(getResources().getString(R.string.about_feedback_tel_title, weChatGroup));

        feedback.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://t.me/joinchat/Gtti8Usj1JD4TchHQmy-ew"));
            try {
                startActivity(intent);
            } catch (Throwable ignored) {
            }
        });
        return feedback;
    }

    Element getWebsiteElement() {
        Element feedback = new Element();
        feedback.setTitle(getResources().getString(R.string.about_website_title));

        feedback.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("http://vxposed.com"));
            try {
                startActivity(intent);
            } catch (Throwable ignored) {
            }
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
