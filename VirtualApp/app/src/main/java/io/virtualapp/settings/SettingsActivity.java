package io.virtualapp.settings;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.support.v7.app.AlertDialog;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.launcher3.LauncherFiles;
import com.lody.virtual.GmsSupport;
import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.helper.utils.FileUtils;

import java.io.File;
import java.io.IOException;

import io.virtualapp.R;
import io.virtualapp.abs.ui.VUiKit;
import io.virtualapp.home.ListAppActivity;
import moe.feng.alipay.zerosdk.AlipayZeroSdk;

/**
 * Settings activity for Launcher. Currently implements the following setting: Allow rotation
 */
public class SettingsActivity extends Activity {

    private static final String ADD_APP_KEY = "settings_add_app";
    private static final String APP_MANAGE_KEY = "settings_app_manage";
    private static final String TASK_MANAGE_KEY = "settings_task_manage";
    private static final String DESKTOP_SETTINGS_KEY = "settings_desktop";
    private static final String FAQ_SETTINGS_KEY = "settings_faq";
    private static final String DONATE_KEY = "settings_donate";
    private static final String ABOUT_KEY = "settings_about";
    private static final String REBOOT_KEY = "settings_reboot";
    private static final String HIDE_SETTINGS_KEY = "advance_settings_hide_settings";
    private static final String DISABLE_INSTALLER_KEY = "advance_settings_disable_installer";
    private static final String INSTALL_GMS_KEY = "advance_settings_install_gms";
    public static final String DIRECTLY_BACK_KEY = "advance_settings_directly_back";
    private static final String COPY_FILE = "advance_settings_copy_file";
    private static final String YIELD_MODE = "advance_settings_yield_mode";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            // Display the fragment as the main content.
            getFragmentManager().beginTransaction()
                    .replace(android.R.id.content, new SettingsFragment())
                    .commit();
        }
    }

    /**
     * This fragment shows the launcher preferences.
     */
    public static class SettingsFragment extends PreferenceFragment {


        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            getPreferenceManager().setSharedPreferencesName(LauncherFiles.SHARED_PREFERENCES_KEY);
            addPreferencesFromResource(R.xml.settings_preferences);

            // Setup allow rotation preference

            Preference addApp = findPreference(ADD_APP_KEY);
            Preference appManage = findPreference(APP_MANAGE_KEY);
            Preference taskManage = findPreference(TASK_MANAGE_KEY);
            Preference desktop = findPreference(DESKTOP_SETTINGS_KEY);
            Preference faq = findPreference(FAQ_SETTINGS_KEY);
            Preference donate = findPreference(DONATE_KEY);
            Preference about = findPreference(ABOUT_KEY);
            Preference reboot = findPreference(REBOOT_KEY);
            Preference copyFile = findPreference(COPY_FILE);

            SwitchPreference disableInstaller = (SwitchPreference) findPreference(DISABLE_INSTALLER_KEY);
            SwitchPreference installGms = (SwitchPreference) findPreference(INSTALL_GMS_KEY);
            SwitchPreference yieldMode = (SwitchPreference) findPreference(YIELD_MODE);

            addApp.setOnPreferenceClickListener(preference -> {
                ListAppActivity.gotoListApp(getActivity());
                return false;
            });
            appManage.setOnPreferenceClickListener(preference -> {
                startActivity(new Intent(getActivity(), AppManageActivity.class));
                return false;
            });

            taskManage.setOnPreferenceClickListener(preference -> {
                startActivity(new Intent(getActivity(), TaskManageActivity.class));
                return false;
            });

            faq.setOnPreferenceClickListener(preference -> {
                Uri uri = Uri.parse("https://github.com/android-hacker/VAExposed/wiki/FAQ");
                Intent t = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(t);
                return false;
            });

            desktop.setOnPreferenceClickListener(preference -> {
                startActivity(new Intent(getActivity(), com.google.android.apps.nexuslauncher.SettingsActivity.class));
                return false;
            });

            donate.setOnPreferenceClickListener(preference -> {
                AlertDialog alertDialog = new AlertDialog.Builder(getActivity(), R.style.Theme_AppCompat_DayNight_Dialog_Alert)
                        .setTitle(R.string.donate_dialog_title)
                        .setMessage(R.string.donate_dialog_content)
                        .setPositiveButton(R.string.donate_dialog_yes, (dialog, which) -> {
                            if (!AlipayZeroSdk.hasInstalledAlipayClient(getActivity())) {
                                Toast.makeText(getActivity(), R.string.prompt_alipay_not_found, Toast.LENGTH_SHORT).show();
                                return;
                            }
                            AlipayZeroSdk.startAlipayClient(getActivity(), "FKX016770URBZGZSR37U37");
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
                return false;
            });
            about.setOnPreferenceClickListener(preference -> {
                startActivity(new Intent(getActivity(), AboutActivity.class));
                return false;
            });

            reboot.setOnPreferenceClickListener(preference -> {
                android.app.AlertDialog alertDialog = new android.app.AlertDialog.Builder(getActivity())
                        .setTitle(R.string.settings_reboot_title)
                        .setMessage(getResources().getString(R.string.settings_reboot_content))
                        .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                            VirtualCore.get().killAllApps();
                            Toast.makeText(getActivity(), R.string.reboot_tips_1, Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .create();
                try {
                    alertDialog.show();
                } catch (Throwable ignored) {
                }
                return false;
            });

            disableInstaller.setOnPreferenceChangeListener((preference, newValue) -> {
                if (!(newValue instanceof Boolean)) {
                    return false;
                }
                try {
                    boolean disable = (boolean) newValue;
                    PackageManager packageManager = getActivity().getPackageManager();
                    packageManager.setComponentEnabledSetting(new ComponentName(getActivity().getPackageName(), "vxp.installer"),
                            !disable ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED : PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                            PackageManager.DONT_KILL_APP);
                    return true;
                } catch (Throwable ignored) {
                    return false;
                }
            });

            installGms.setOnPreferenceChangeListener(((preference, newValue) -> {
                if (!(newValue instanceof Boolean)) {
                    return false;
                }
                boolean install = (boolean) newValue;
                if (install) {
                    if (!GmsSupport.isOutsideGoogleFrameworkExist()) {
                        Toast.makeText(getActivity(), "Sorry, your phone has no GMS supported.", Toast.LENGTH_SHORT).show();
                        return false;
                    }

                    Toast.makeText(getActivity(), "Coming soon.", Toast.LENGTH_SHORT).show();
                    // Installd.addGmsSupport();
                    return false;
                } else {
                    // TODO, delete.
                }
                return false;
            }));

            copyFile.setOnPreferenceClickListener((preference -> {
                Context context = getActivity();
                LinearLayout layout = new LinearLayout(context);
                EditText from = new EditText(context);
                from.setHint("from");
                EditText to = new EditText(context);
                to.setHint("to");

                LinearLayout.LayoutParams fromLayoutparams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                fromLayoutparams.rightMargin = dp2px(10);
                fromLayoutparams.leftMargin = dp2px(10);

                LinearLayout.LayoutParams toLayoutparams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                toLayoutparams.rightMargin = dp2px(10);
                toLayoutparams.leftMargin = dp2px(10);

                layout.setOrientation(LinearLayout.VERTICAL);
                layout.addView(from, fromLayoutparams);
                layout.addView(to, toLayoutparams);

                AlertDialog alertDialog = new AlertDialog.Builder(getActivity(), R.style.Theme_AppCompat_DayNight_Dialog_Alert)
                        .setTitle(R.string.advance_settings_copy_file)
                        .setView(layout)
                        .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                            String fromPath = from.getText().toString();
                            String toPath = to.getText().toString();
                            File fromFile = new File(fromPath);
                            if (!fromFile.exists()) {
                                Toast.makeText(context, "source file not exist!", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            ProgressDialog loading = new ProgressDialog(context);
                            try {
                                loading.show();
                            } catch (Throwable ignored) {
                                return;
                            }

                            VUiKit.defer().when(() -> {
                                try {
                                    FileUtils.copyFile(fromPath, toPath);
                                } catch (IOException e) {
                                    throw new RuntimeException("copy failed");
                                }
                            }).done((v) -> {
                                dismiss(loading);
                                Toast.makeText(context, "copy file success!", Toast.LENGTH_SHORT).show();
                            }).fail((v) -> {
                                dismiss(loading);
                                Toast.makeText(context, "copy file failed!", Toast.LENGTH_SHORT).show();
                            });
                        })
                        .create();
                try {
                    alertDialog.show();
                } catch (Throwable ignored) {
                    // BadTokenException.
                }
                return false;
            }));

            File yieldFile = getActivity().getFileStreamPath("yieldMode");
            yieldMode.setOnPreferenceChangeListener((preference, newValue) -> {

                if (!(newValue instanceof Boolean)) {
                    return false;
                }

                boolean on = (boolean) newValue;
                if (on) {
                    boolean success;
                    try {
                        success = yieldFile.createNewFile();
                    } catch (IOException e) {
                        success = false;
                    }
                    return success;
                } else {
                    return yieldFile.delete();
                }
            });
        }

        private static void dismiss(ProgressDialog dialog) {
            try {
                dialog.dismiss();
            } catch (Throwable ignored) {
            }
        }

        protected int dp2px(float dp) {
            final float scale = getResources().getDisplayMetrics().density;
            return (int) (dp * scale + 0.5f);
        }
    }

    @Override
    public void startActivity(Intent intent) {
        try {
            super.startActivity(intent);
        } catch (Throwable ignored) {
            Toast.makeText(this, "startActivity failed.", Toast.LENGTH_SHORT).show();
            ignored.printStackTrace();
        }
    }
}
