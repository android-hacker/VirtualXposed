package io.virtualapp.settings;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import com.android.launcher3.LauncherFiles;
import com.lody.virtual.client.core.VirtualCore;

import io.virtualapp.R;
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
    public static final String DIRECTLY_BACK_KEY = "advance_settings_directly_back";

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

            SwitchPreference disableInstaller = (SwitchPreference) findPreference(DISABLE_INSTALLER_KEY);

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
        }
    }
}
