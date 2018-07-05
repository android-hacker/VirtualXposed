package io.virtualapp.settings;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import com.android.launcher3.LauncherFiles;
import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.env.Constants;
import com.lody.virtual.client.ipc.VActivityManager;

import java.io.File;
import java.io.IOException;

import io.virtualapp.R;
import io.virtualapp.gms.FakeGms;
import io.virtualapp.home.ListAppActivity;
import moe.feng.alipay.zerosdk.AlipayZeroSdk;

/**
 * Settings activity for Launcher. Currently implements the following setting: Allow rotation
 */
public class SettingsActivity extends Activity {

    private static final String ADVANCE_SETTINGS_KEY = "settings_advance";
    private static final String ADD_APP_KEY = "settings_add_app";
    private static final String MODULE_MANAGE_KEY = "settings_module_manage";
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
    private static final String RECOMMEND_PLUGIN = "settings_plugin_recommend";
    private static final String DISABLE_RESIDENT_NOTIFICATION = "advance_settings_disable_resident_notification";
    private static final String ALLOW_FAKE_SIGNATURE = "advance_settings_allow_fake_signature";
    private static final String DISABLE_XPOSED = "advance_settings_disable_xposed";
    private static final String FILE_MANAGE = "settings_file_manage";
    private static final String PERMISSION_MANAGE = "settings_permission_manage";

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
            Preference moduleManage = findPreference(MODULE_MANAGE_KEY);
            Preference recommend = findPreference(RECOMMEND_PLUGIN);
            Preference appManage = findPreference(APP_MANAGE_KEY);
            Preference taskManage = findPreference(TASK_MANAGE_KEY);
            Preference desktop = findPreference(DESKTOP_SETTINGS_KEY);
            Preference faq = findPreference(FAQ_SETTINGS_KEY);
            Preference donate = findPreference(DONATE_KEY);
            Preference about = findPreference(ABOUT_KEY);
            Preference reboot = findPreference(REBOOT_KEY);
            Preference fileMange = findPreference(FILE_MANAGE);
            Preference permissionManage = findPreference(PERMISSION_MANAGE);


            SwitchPreference disableInstaller = (SwitchPreference) findPreference(DISABLE_INSTALLER_KEY);
            SwitchPreference disableResidentNotification = (SwitchPreference) findPreference(DISABLE_RESIDENT_NOTIFICATION);
            SwitchPreference allowFakeSignature = (SwitchPreference) findPreference(ALLOW_FAKE_SIGNATURE);
            SwitchPreference disableXposed = (SwitchPreference) findPreference(DISABLE_XPOSED);

            addApp.setOnPreferenceClickListener(preference -> {
                ListAppActivity.gotoListApp(getActivity());
                return false;
            });

            moduleManage.setOnPreferenceClickListener(preference -> {
                try {
                    Intent t = new Intent();
                    t.setComponent(new ComponentName("de.robv.android.xposed.installer", "de.robv.android.xposed.installer.WelcomeActivity"));
                    t.putExtra("fragment", 1);
                    int ret = VActivityManager.get().startActivity(t, 0);
                    if (ret < 0) {
                        Toast.makeText(getActivity(), R.string.xposed_installer_not_found, Toast.LENGTH_SHORT).show();
                    }
                } catch (Throwable ignored) {
                    ignored.printStackTrace();
                }
                return false;
            });

            recommend.setOnPreferenceClickListener(preference -> {
                startActivity(new Intent(getActivity(), RecommendPluginActivity.class));
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
                            // show chooser dialog

                            final String alipay = getResources().getString(R.string.donate_alipay);
                            final String[] items = {alipay, "Paypal", "Bitcoin"};

                            AlertDialog chooseDialog = new AlertDialog.Builder(getActivity(), R.style.Theme_AppCompat_DayNight_Dialog_Alert)
                                    .setTitle(R.string.donate_choose_title)
                                    .setItems(items, (dialog1, which1) -> {
                                        dialog1.dismiss();
                                        if (which1 == 0) {
                                            if (!AlipayZeroSdk.hasInstalledAlipayClient(getActivity())) {
                                                Toast.makeText(getActivity(), R.string.prompt_alipay_not_found, Toast.LENGTH_SHORT).show();
                                                return;
                                            }
                                            AlipayZeroSdk.startAlipayClient(getActivity(), "FKX016770URBZGZSR37U37");
                                        } else if (which1 == 1) {
                                            try {
                                                Intent t = new Intent(Intent.ACTION_VIEW);
                                                t.setData(Uri.parse("https://paypal.me/virtualxposed"));
                                                startActivity(t);
                                            } catch (Throwable ignored) {
                                                ignored.printStackTrace();
                                            }
                                        } else if (which1 == 2) {
                                            final String address = "39Wst8oL74pRP2vKPkPihH6RFQF4hWoBqU";

                                            try {
                                                ClipboardManager clipboardManager = (ClipboardManager) getActivity().getSystemService(CLIPBOARD_SERVICE);
                                                if (clipboardManager != null) {
                                                    clipboardManager.setPrimaryClip(ClipData.newPlainText(null, address));
                                                }
                                                Toast.makeText(getActivity(), getResources().getString(R.string.donate_bitconins_tips), Toast.LENGTH_SHORT).show();
                                            } catch (Throwable ignored) {
                                                ignored.printStackTrace();
                                            }
                                        }
                                    })
                                    .create();
                            chooseDialog.show();
                        })
                        .setNegativeButton(R.string.donate_dialog_no, ((dialog, which) -> {
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_VIEW);
                            intent.addCategory(Intent.CATEGORY_BROWSABLE);
                            intent.setData(Uri.parse("https://github.com/android-hacker/VirtualXposed"));
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

            Preference installGms = findPreference(INSTALL_GMS_KEY);
            installGms.setOnPreferenceClickListener(preference -> {
                boolean alreadyInstalled = FakeGms.isAlreadyInstalled(getActivity());
                if (alreadyInstalled) {
                    FakeGms.uninstallGms(getActivity());
                } else {
                    FakeGms.installGms(getActivity());
                }
                return true;
            });

            fileMange.setOnPreferenceClickListener(preference -> {
                OnlinePlugin.openOrDownload(getActivity(), OnlinePlugin.FILE_MANAGE_PACKAGE,
                        OnlinePlugin.FILE_MANAGE_URL, getString(R.string.install_file_manager_tips));
                return false;
            });

            permissionManage.setOnPreferenceClickListener(preference -> {
                OnlinePlugin.openOrDownload(getActivity(), OnlinePlugin.PERMISSION_MANAGE_PACKAGE,
                        OnlinePlugin.PERMISSION_MANAGE_URL, getString(R.string.install_permission_manager_tips));
                return false;
            });

            disableXposed.setOnPreferenceChangeListener((preference, newValue) -> {

                if (!(newValue instanceof Boolean)) {
                    return false;
                }

                boolean on = (boolean) newValue;

                File disableXposedFile = getActivity().getFileStreamPath(".disable_xposed"); // 文件不存在代表是保守模式
                if (on) {
                    boolean success;
                    try {
                        success = disableXposedFile.createNewFile();
                    } catch (IOException e) {
                        success = false;
                    }
                    return success;
                } else {
                    return !disableXposedFile.exists() || disableXposedFile.delete();
                }
            });

            disableResidentNotification.setOnPreferenceChangeListener(((preference, newValue) -> {

                if (!(newValue instanceof Boolean)) {
                    return false;
                }

                boolean on = (boolean) newValue;

                File flag = getActivity().getFileStreamPath(Constants.NO_NOTIFICATION_FLAG);
                if (on) {
                    boolean success;
                    try {
                        success = flag.createNewFile();
                    } catch (IOException e) {
                        success = false;
                    }
                    return success;
                } else {
                    return !flag.exists() || flag.delete();
                }
            }));

            if (android.os.Build.VERSION.SDK_INT < 25) {
                // Android NR1 below do not need this.
                PreferenceScreen advance = (PreferenceScreen) findPreference(ADVANCE_SETTINGS_KEY);
                advance.removePreference(disableResidentNotification);
            }

            allowFakeSignature.setOnPreferenceChangeListener((preference, newValue) -> {
                if (!(newValue instanceof Boolean)) {
                    return false;
                }

                boolean on = (boolean) newValue;
                File flag = getActivity().getFileStreamPath(Constants.FAKE_SIGNATURE_FLAG);
                if (on) {
                    boolean success;
                    try {
                        success = flag.createNewFile();
                    } catch (IOException e) {
                        success = false;
                    }
                    return success;
                } else {
                    return !flag.exists() || flag.delete();
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

        @Override
        public void startActivity(Intent intent) {
            try {
                super.startActivity(intent);
            } catch (Throwable ignored) {
                Toast.makeText(getActivity(), "startActivity failed.", Toast.LENGTH_SHORT).show();
                ignored.printStackTrace();
            }
        }
    }
}
