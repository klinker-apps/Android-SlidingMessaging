package com.klinker.android.messaging_donate.settings;

import android.annotation.SuppressLint;
import android.app.*;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.*;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.*;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.*;
import android.widget.Toast;
import com.klinker.android.messaging_card.theme.PopupChooserActivity;
import com.klinker.android.messaging_donate.R;
import com.klinker.android.messaging_sliding.DeleteOldService;
import com.klinker.android.messaging_sliding.backup.BackupService;
import com.klinker.android.messaging_sliding.blacklist.BlacklistActivity;
import com.klinker.android.messaging_sliding.custom_dialogs.NumberPickerDialog;
import com.klinker.android.messaging_sliding.notifications.NotificationsSettingsActivity;
import com.klinker.android.messaging_sliding.receivers.CacheService;
import com.klinker.android.messaging_sliding.receivers.NotificationReceiver;
import com.klinker.android.messaging_sliding.scheduled.ScheduledSms;
import com.klinker.android.messaging_sliding.security.SetPasswordActivity;
import com.klinker.android.messaging_sliding.security.SetPinActivity;
import com.klinker.android.messaging_sliding.templates.TemplateActivity;
//import group.pals.android.lib.ui.lockpattern.LockPatternActivity;

import java.io.*;
import java.util.*;

import group.pals.android.lib.ui.lockpattern.LockPatternActivity;
import group.pals.android.lib.ui.lockpattern.prefs.SecurityPrefs;

public class SettingsPagerActivity extends FragmentActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
     * will keep every loaded fragment in memory. If this becomes too memory
     * intensive, it may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;
    SharedPreferences sharedPrefs;

    private static final int REQ_CREATE_PATTERN = 3;

    /**
     * The {@link android.support.v4.view.ViewPager} that will host the section contents.
     */
    public static ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_main);

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        mSectionsPagerAdapter = new SectionsPagerAdapter(
                getFragmentManager());

        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        try {
            if (getIntent().getBooleanExtra("mms", false)) {
                mViewPager.setCurrentItem(6, true);
            }
        } catch (Exception e) {

        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case android.R.id.home:
                if (mViewPager.getCurrentItem() == 0)
                {
                   onBackPressed();
                } else
                {
                    mViewPager.setCurrentItem(0, true);
                }

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {

    }

    @Override
    public void onBackPressed()
    {
        if (mViewPager.getCurrentItem() == 0)
        {
            Intent i = new Intent(this, com.klinker.android.messaging_donate.MainActivity.class);
            startActivity(i);
            finish();
            overridePendingTransition(R.anim.activity_slide_in_left, R.anim.activity_slide_out_right);
        } else
        {
            mViewPager.setCurrentItem(0, true);
        }
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public static final int NUM_PAGES = 10;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {

            PreferenceFragment fragment = new PrefFragment();
            Bundle args = new Bundle();
            args.putInt("position", position);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getResources().getString(R.string.menu_settings);
                case 1:
                    return getResources().getString(R.string.theme_settings);
                case 2:
                    return getResources().getString(R.string.sliding_notification_settings);
                case 3:
                    return getResources().getString(R.string.popup_settings);
                case 4:
                    return getResources().getString(R.string.slideover_settings);
                case 5:
                    return getResources().getString(R.string.text_settings);
                case 6:
                    return getResources().getString(R.string.conversation_settings);
                case 7:
                    return getResources().getString(R.string.mms_settings);
                case 8:
                    return getResources().getString(R.string.security_settings);
                case 9:
                    return getResources().getString(R.string.advanced_settings);
            }
            return null;
        }
    }

    /**
     * A dummy fragment representing a section of the app, but that simply
     * displays dummy text.
     */
    public class PrefFragment extends PreferenceFragment {

        public static final String ARG_SECTION_NUMBER = "section_number";
        public int position;

        public PrefFragment() {
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            Bundle args = getArguments();
            position = args.getInt("position");

            switch(position)
            {
                case 0:
                    addPreferencesFromResource(R.xml.sliding_settings);
                    setUpSlidingSettings();
                    break;
                case 1:
                    addPreferencesFromResource(R.xml.sliding_theme_settings);
                    setUpThemeSettings();
                    break;
                case 2:
                    addPreferencesFromResource(R.xml.sliding_notification_settings);
                    setUpNotificationSettings();
                    break;
                case 3:
                    addPreferencesFromResource(R.xml.popup_settings);
                    setUpPopupSettings();
                    break;
                case 4:
                    addPreferencesFromResource(R.xml.slideover_settings);
                    setUpSlideOverSettings();
                    break;
                case 5:
                    addPreferencesFromResource(R.xml.sliding_message_settings);
                    setUpMessageSettings();
                    break;
                case 6:
                    addPreferencesFromResource(R.xml.sliding_conversation_settings);
                    setUpConversationSettings();
                    break;
                case 7:
                    addPreferencesFromResource(R.xml.mms_settings);
                    setUpMmsSettings();
                    break;
                case 8:
                    addPreferencesFromResource(R.xml.sliding_security_settings);
                    setUpSecuritySettings();
                    break;
                case 9:
                    addPreferencesFromResource(R.xml.sliding_advanced_settings);
                    setUpAdvancedSettings();
                    break;
            }
        }

        public void setUpSlidingSettings()
        {

            Preference themeSettings = (Preference) findPreference("theme_settings");
            themeSettings.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    mViewPager.setCurrentItem(1, true);
                    return true;
                }
            });

            Preference notificationSettings = (Preference) findPreference("notification_settings");
            notificationSettings.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    mViewPager.setCurrentItem(2, true);
                    return true;
                }
            });

            Preference popupSettings = (Preference) findPreference("popup_settings");
            popupSettings.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    mViewPager.setCurrentItem(3, true);
                    return true;
                }
            });

            Preference slideoverSettings = (Preference) findPreference("slideover_settings");
            slideoverSettings.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    mViewPager.setCurrentItem(4, true);
                    return true;
                }
            });

            Preference messageSettings = (Preference) findPreference("message_settings");
            messageSettings.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    mViewPager.setCurrentItem(5, true);
                    return true;
                }
            });

            Preference conversationSettings = (Preference) findPreference("conversation_settings");
            conversationSettings.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    mViewPager.setCurrentItem(6, true);
                    return true;
                }
            });

            Preference mmsSettings = (Preference) findPreference("mms_settings");
            mmsSettings.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    mViewPager.setCurrentItem(7, true);
                    return true;
                }
            });

            Preference securitySettings = (Preference) findPreference("security_settings");
            securitySettings.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    mViewPager.setCurrentItem(8, true);
                    return true;
                }
            });

            Preference advancedSettings = (Preference) findPreference("advanced_settings");
            advancedSettings.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    mViewPager.setCurrentItem(9, true);
                    return true;
                }
            });

            final Context context = getActivity();

            Preference templates = findPreference("quick_template_settings");
            templates.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

                @Override
                public boolean onPreferenceClick(Preference arg0) {
                    Intent intent = new Intent(context, TemplateActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.activity_slide_in_right, R.anim.activity_slide_out_left);
                    return false;
                }

            });

            Preference scheduledSMS = findPreference("scheduled_sms");
            scheduledSMS.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

                @Override
                public boolean onPreferenceClick(Preference arg0) {
                    Intent intent = new Intent(context, ScheduledSms.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.activity_slide_in_right, R.anim.activity_slide_out_left);
                    return false;
                }

            });

            Preference deleteAll = findPreference("delete_all");
            deleteAll.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

                @Override
                public boolean onPreferenceClick(Preference arg0) {
                    AlertDialog.Builder builder2 = new AlertDialog.Builder(context);
                    builder2.setMessage(context.getResources().getString(R.string.delete_all));
                    builder2.setPositiveButton(context.getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                        @SuppressLint("SimpleDateFormat")
                        public void onClick(DialogInterface dialog, int id) {

                            final ProgressDialog progDialog = new ProgressDialog(context);
                            progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                            progDialog.setMessage(context.getResources().getString(R.string.deleting));
                            progDialog.show();

                            new Thread(new Runnable(){

                                @Override
                                public void run() {
                                    deleteSMS(context);
                                    writeToFile(new ArrayList<String>(), context);

                                    ((Activity) context).getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {

                                        @Override
                                        public void run() {
                                            progDialog.dismiss();
                                        }

                                    });
                                }

                            }).start();

                        }
                    });
                    builder2.setNegativeButton(context.getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });
                    AlertDialog dialog2 = builder2.create();

                    dialog2.show();

                    return true;
                }

            });
        }

        public void setUpThemeSettings()
        {
            final Context context = getActivity();
            final SharedPreferences sharedPrefs  = PreferenceManager.getDefaultSharedPreferences(getActivity());

            if (sharedPrefs.getString("run_as", "sliding").equals("sliding") || sharedPrefs.getString("run_as", "sliding").equals("hangout") || sharedPrefs.getString("run_as", "sliding").equals("card2"))
            {
                getPreferenceScreen().removePreference(findPreference("card_theme"));
                getPreferenceScreen().removePreference(findPreference("font_settings"));
                getPreferenceScreen().removePreference(findPreference("display_contact_cards"));
                getPreferenceScreen().removePreference(findPreference("display_contact_names"));
                getPreferenceScreen().removePreference(findPreference("simple_cards"));
                getPreferenceScreen().removePreference(findPreference("top_actionbar"));

                Preference titleSettings = findPreference("title_prefs");
                titleSettings.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    public boolean onPreferenceClick(Preference preference) {
                        Intent intent = new Intent(context, TitleBarSettingsActivity.class);
                        startActivity(intent);
                        overridePendingTransition(R.anim.activity_slide_in_right, R.anim.activity_slide_out_left);
                        return true;
                    }
                });

                Preference customThemeSettings = findPreference("custom_theme_prefs");
                customThemeSettings.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    public boolean onPreferenceClick(Preference preference) {
                        Intent intent = new Intent(context, ThemeChooserActivity.class);
                        startActivity(intent);
                        overridePendingTransition(R.anim.activity_slide_in_right, R.anim.activity_slide_out_left);

                        return true;
                    }
                });

                Preference customBackground = (Preference) findPreference("custom_background");
                customBackground.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

                    @Override
                    public boolean onPreferenceClick(Preference arg0) {
                        if (sharedPrefs.getBoolean("custom_background", false))
                        {
                            Intent intent = new Intent();
                            intent.setType("image/*");
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            startActivityForResult(Intent.createChooser(intent, "Select Menu Background Picture"), 1);
                        }

                        return true;
                    }

                });

                Preference customBackground2 = (Preference) findPreference("custom_background2");
                customBackground2.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

                    @Override
                    public boolean onPreferenceClick(Preference arg0) {
                        if (sharedPrefs.getBoolean("custom_background2", false))
                        {
                            Intent intent = new Intent();
                            intent.setType("image/*");
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            startActivityForResult(Intent.createChooser(intent, "Select Message Background Picture"), 2);
                        }

                        return true;
                    }

                });
            } else
            {
                getPreferenceScreen().removePreference(findPreference("custom_theme_prefs"));
                getPreferenceScreen().removePreference(findPreference("title_prefs"));
                getPreferenceScreen().removePreference(findPreference("page_or_menu2"));
                getPreferenceScreen().removePreference(findPreference("custom_background"));
                getPreferenceScreen().removePreference(findPreference("custom_background2"));

                Preference customFont = (Preference) findPreference("font_settings");
                customFont.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

                    @Override
                    public boolean onPreferenceClick(Preference arg0) {
                        Intent intent3 = new Intent(context, com.klinker.android.messaging_sliding.theme.CustomFontSettingsActivity.class);
                        startActivity(intent3);
                        overridePendingTransition(R.anim.activity_slide_in_right, R.anim.activity_slide_out_left);

                        return true;
                    }

                });
            }

            findPreference("run_as").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (sharedPrefs.getString("run_as", "classic").equals("card")) {
                                new AlertDialog.Builder(context)
                                        .setTitle("Note:")
                                        .setMessage("Cards UI is more of a proof of concept that we feel would look great for a messaging app. Because of how it is implemented, it will be slower on some devices. You have been warned.\n\n" +
                                                    "Cards UI 2.0 however, does improve on the performance and brings the UI up to speed with the rest of the app. It is suggested that you use that instead.")
                                        .setPositiveButton(context.getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {

                                            }
                                        })
                                        .create()
                                        .show();
                            }
                        }
                    }, 200);

                    return true;
                }
            });
        }

        public void setUpNotificationSettings()
        {
            final Context context = getActivity();

            Preference indiv = (Preference) findPreference("individual_notification_settings");
            indiv.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

                @Override
                public boolean onPreferenceClick(Preference arg0) {
                    Intent intent = new Intent(context, NotificationsSettingsActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.activity_slide_in_right, R.anim.activity_slide_out_left);
                    return false;
                }

            });

            Preference blacklistSettings = (Preference) findPreference("blacklist_settings");
            blacklistSettings.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(context, BlacklistActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.activity_slide_in_right, R.anim.activity_slide_out_left);
                    return true;
                }
            });

            Preference testNotification = findPreference("test_notification");
            testNotification.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    giveTestNotification();
                    return true;
                }
            });
        }

        public void setUpPopupSettings()
        {
            final Context context = getActivity();
            final SharedPreferences sharedPrefs  = PreferenceManager.getDefaultSharedPreferences(getActivity());
            Preference customPopup = (Preference) findPreference("popup_theme");

            if (sharedPrefs.getBoolean("full_app_popup", true)) {
                customPopup.setEnabled(false);
                customPopup.setSelectable(false);
                customPopup.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    public boolean onPreferenceClick(Preference preference) {
                        return true;
                    }
                });
                findPreference("enable_view_conversation").setEnabled(false);
                findPreference("text_alignment2").setEnabled(false);
                findPreference("use_old_popup").setEnabled(false);
                findPreference("dark_theme_quick_reply").setEnabled(false);
                findPreference("halo_popup").setEnabled(false);
            } else {
                if (!sharedPrefs.getBoolean("use_old_popup", false))
                {
                    customPopup.setEnabled(true);
                    customPopup.setSelectable(true);
                    customPopup.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                        public boolean onPreferenceClick(Preference preference) {
                            Intent intent = new Intent(context, PopupChooserActivity.class);
                            startActivity(intent);
                            overridePendingTransition(R.anim.activity_slide_in_right, R.anim.activity_slide_out_left);
                            return true;
                        }
                    });
                } else
                {
                    customPopup.setEnabled(false);
                    customPopup.setSelectable(false);
                    customPopup.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                        public boolean onPreferenceClick(Preference preference) {
                            return true;
                        }
                    });
                }

                findPreference("enable_view_conversation").setEnabled(true);
                findPreference("text_alignment2").setEnabled(true);
                findPreference("use_old_popup").setEnabled(true);
                findPreference("dark_theme_quick_reply").setEnabled(true);
                findPreference("halo_popup").setEnabled(true);
            }

            Preference oldPopup = findPreference("use_old_popup");
            oldPopup.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    setUpPopupSettings();
                    return true;
                }
            });

            Preference slideOver = findPreference("full_app_popup");
            slideOver.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    setUpPopupSettings();
                    return true;
                }
            });
        }

        public void setUpSlideOverSettings() {

        }

        public void setUpMessageSettings()
        {
            final SharedPreferences sharedPrefs  = PreferenceManager.getDefaultSharedPreferences(getActivity());

            if (sharedPrefs.getString("run_as", "sliding").equals("sliding") || sharedPrefs.getString("run_as", "sliding").equals("hangout") || sharedPrefs.getString("run_as", "sliding").equals("card2"))
            {

            } else
            {
                getPreferenceScreen().removePreference(findPreference("text_alignment"));
                getPreferenceScreen().removePreference(findPreference("contact_pictures"));
                getPreferenceScreen().removePreference(findPreference("auto_insert_draft"));
            }
        }

        public void setUpConversationSettings()
        {
            final SharedPreferences sharedPrefs  = PreferenceManager.getDefaultSharedPreferences(getActivity());

            if (sharedPrefs.getString("run_as", "sliding").equals("sliding") || sharedPrefs.getString("run_as", "sliding").equals("hangout") || sharedPrefs.getString("run_as", "sliding").equals("card2"))
            {
                getPreferenceScreen().removePreference(findPreference("hide_contact_number"));
                getPreferenceScreen().removePreference(findPreference("open_to_first"));
            } else
            {
                getPreferenceScreen().removePreference(findPreference("pin_conversation_list"));
                getPreferenceScreen().removePreference(findPreference("contact_pictures2"));
                getPreferenceScreen().removePreference(findPreference("open_contact_menu"));
                getPreferenceScreen().removePreference(findPreference("slide_messages"));
            }
        }

        public void setUpMmsSettings()
        {
            Preference mmsc, proxy, port;
            final Context context = getActivity();
            final SharedPreferences sharedPrefs  = PreferenceManager.getDefaultSharedPreferences(getActivity());

            Preference smsToStore = (Preference) findPreference("mms_after");
            smsToStore.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

                @Override
                public boolean onPreferenceClick(Preference arg0) {
                    if (sharedPrefs.getBoolean("send_as_mms", false))
                    {
                        NumberPickerDialog.OnNumberSetListener mSmsLimitListener =
                                new NumberPickerDialog.OnNumberSetListener() {
                                    public void onNumberSet(int limit) {
                                        SharedPreferences.Editor editor = sharedPrefs.edit();

                                        editor.putInt("mms_after", limit);
                                        editor.commit();
                                    }
                                };

                        new NumberPickerDialog(context, mSmsLimitListener, sharedPrefs.getInt("mms_after", 4), 1, 1000, R.string.mms_after).show();
                    }

                    return false;
                }

            });

            mmsc = (Preference) findPreference("mmsc_url");
            mmsc.setSummary(sharedPrefs.getString("mmsc_url", ""));

            proxy = (Preference) findPreference("mms_proxy");
            proxy.setSummary(sharedPrefs.getString("mms_proxy", ""));

            port = (Preference) findPreference("mms_port");
            port.setSummary(sharedPrefs.getString("mms_port", ""));

            Preference presets = (Preference) findPreference("preset_apns");
            presets.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

                @Override
                public boolean onPreferenceClick(Preference arg0) {
                    Intent intent = new Intent(getActivity(), com.klinker.android.messaging_sliding.mms.APNSettingsActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.activity_slide_in_right, R.anim.activity_slide_out_left);
                    return false;
                }

            });

            Preference getHelp = (Preference) findPreference("get_apn_help");
            getHelp.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

                @Override
                public boolean onPreferenceClick(Preference arg0) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse("http://www.google.com"));
                    startActivity(intent);
                    return false;
                }

            });

//            Preference report = (Preference) findPreference("mms_report");
//            report.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
//
//                @Override
//                public boolean onPreferenceClick(Preference arg0) {
//                    String uriText =
//                            "mailto:jklinker1@gmail.com" +
//                                    "?subject=" + URLEncoder.encode("Sliding Messaging APNs") +
//                                    "&body=" + URLEncoder.encode("MMSC: " + sharedPrefs.getString("mmsc_url", "") + "\nMMS Proxy: " + sharedPrefs.getString("mms_proxy", "") + "\nMMS Port: " + sharedPrefs.getString("mms_port", "") +
//                                    "\n\n My carrier is: ________\nMMS is working or not working: ________");
//
//                    Uri uri = Uri.parse(uriText);
//
//                    Intent sendIntent = new Intent(Intent.ACTION_SENDTO);
//                    sendIntent.setData(uri);
//                    startActivity(Intent.createChooser(sendIntent, "Send email"));
//                    return false;
//                }
//
//            });
        }

        // used for the list preference to determine when it changes and when to call the intents
        SharedPreferences.OnSharedPreferenceChangeListener myPrefListner;

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(myPrefListner);
        }

        @Override
        public void onPause() {
            super.onPause();
            getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(myPrefListner);

        }

        public void setUpSecuritySettings()
        {
            final Context context = getActivity();
            final SharedPreferences sharedPrefs  = PreferenceManager.getDefaultSharedPreferences(context);

            // sets up the preferences dynamically depending on what security type you have
            if (sharedPrefs.getString("security_option", "none").equals("none"))
            {
                getPreferenceScreen().findPreference("set_password").setEnabled(false);
                getPreferenceScreen().findPreference("auto_unlock").setEnabled(false);
                getPreferenceScreen().findPreference("timeout_settings").setEnabled(false);
            } else if (sharedPrefs.getString("security_option", "none").equals("password"))
            {
                getPreferenceScreen().findPreference("set_password").setEnabled(true);
                getPreferenceScreen().findPreference("auto_unlock").setEnabled(false);
                getPreferenceScreen().findPreference("timeout_settings").setEnabled(true);
            } else if (sharedPrefs.getString("security_option", "none").equals("pin"))
            {
                getPreferenceScreen().findPreference("set_password").setEnabled(true);
                getPreferenceScreen().findPreference("auto_unlock").setEnabled(true);
                getPreferenceScreen().findPreference("timeout_settings").setEnabled(true);
            } else if (sharedPrefs.getString("security_option", "none").equals("pattern"))
            {
                getPreferenceScreen().findPreference("set_password").setEnabled(true);
                getPreferenceScreen().findPreference("auto_unlock").setEnabled(false);
                getPreferenceScreen().findPreference("timeout_settings").setEnabled(true);
            }

            // listner for list preference change to call intents and change preferences
            myPrefListner = new SharedPreferences.OnSharedPreferenceChangeListener(){
                public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                    if(key.equals("security_option")) {
                        //Get the value from the list_preference with default: "Nothing"
                        String value = sharedPrefs.getString(key, "none");

                        if(value.equals("none"))
                        {
                            getPreferenceScreen().findPreference("set_password").setEnabled(false);
                            getPreferenceScreen().findPreference("auto_unlock").setEnabled(false);
                            getPreferenceScreen().findPreference("timeout_settings").setEnabled(false);
                        } else if(value.equals("pin")) {
                            getPreferenceScreen().findPreference("auto_unlock").setEnabled(true);
                            getPreferenceScreen().findPreference("set_password").setEnabled(true);
                            getPreferenceScreen().findPreference("timeout_settings").setEnabled(true);

                            Intent intent = new Intent(getActivity(), SetPinActivity.class);
                            startActivity(intent);
                        } else if (value.equals("password"))
                        {
                            getPreferenceScreen().findPreference("auto_unlock").setEnabled(false);
                            getPreferenceScreen().findPreference("set_password").setEnabled(true);
                            getPreferenceScreen().findPreference("timeout_settings").setEnabled(true);

                            Intent intent = new Intent(getActivity(), SetPasswordActivity.class);
                            startActivity(intent);
                        } else if (value.equals("pattern")) // could be implemented for pattern, but it wasn't working before
                        {
                            getPreferenceScreen().findPreference("set_password").setEnabled(true);
                            getPreferenceScreen().findPreference("auto_unlock").setEnabled(false);
                            getPreferenceScreen().findPreference("timeout_settings").setEnabled(true);

                            SecurityPrefs.setAutoSavePattern(getActivity(), true);
                            Intent intent = new Intent(LockPatternActivity.ACTION_CREATE_PATTERN, null,
                                    getActivity(), LockPatternActivity.class);
                            startActivityForResult(intent, REQ_CREATE_PATTERN);
                        }
                    }
                }
            };

            // calls the correct intent for clicking set password preference
            Preference setPassword = (Preference) findPreference("set_password");
            setPassword.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

                @Override
                public boolean onPreferenceClick(Preference arg0) {
                    if (sharedPrefs.getString("security_option", "none").equals("pin"))
                    {
                        Intent intent = new Intent(getActivity(), SetPinActivity.class);
                        startActivity(intent);
                    } else if (sharedPrefs.getString("security_option", "none").equals("password"))
                    {
                        Intent intent = new Intent(getActivity(), SetPasswordActivity.class);
                        startActivity(intent);
                    } else if (sharedPrefs.getString("security_option", "none").equals("pattern"))
                    {
                        SecurityPrefs.setAutoSavePattern(context, true);
                        Intent intent = new Intent(LockPatternActivity.ACTION_CREATE_PATTERN, null,
                                getActivity(), LockPatternActivity.class);
                        startActivityForResult(intent, REQ_CREATE_PATTERN);
                    }
                    return false;
                }

            });
        }

        public void setUpSpeedSettings()
        {
            Preference scheduleBackup = (Preference) findPreference("schedule_backup");
            scheduleBackup.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

                @Override
                public boolean onPreferenceClick(Preference arg0) {
                    Intent intent = new Intent(getActivity(), com.klinker.android.messaging_sliding.backup.ScheduleBackup.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.activity_slide_in_right, R.anim.activity_slide_out_left);
                    return false;
                }

            });

            Preference sdcard = (Preference) findPreference("sd_backup");
            sdcard.setEnabled(false);

            Preference runNow = (Preference) findPreference("run_backup");
            runNow.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

                @Override
                public boolean onPreferenceClick(Preference arg0) {
                    Intent intent = new Intent(getActivity(), BackupService.class);
                    startService(intent);
                    return false;
                }

            });
        }

        public void setUpAdvancedSettings()
        {
            final Context context = getActivity();
            final SharedPreferences sharedPrefs  = PreferenceManager.getDefaultSharedPreferences(getActivity());

            Preference deleteOld = (Preference) findPreference("delete_old");
            deleteOld.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

                @Override
                public boolean onPreferenceClick(Preference arg0) {
                    if (sharedPrefs.getBoolean("delete_old", false))
                    {
                        Intent deleteIntent = new Intent(context, DeleteOldService.class);
                        PendingIntent pintent = PendingIntent.getService(context, 0, deleteIntent, 0);
                        AlarmManager alarm = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
                        alarm.setRepeating(AlarmManager.RTC_WAKEUP, Calendar.getInstance().getTimeInMillis(), 6*60*60*1000, pintent);
                    }

                    return false;
                }

            });

            Preference backup = (Preference) findPreference("backup");
            backup.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

                @Override
                public boolean onPreferenceClick(Preference arg0) {
                    new AlertDialog.Builder(context)
                            .setTitle(context.getResources().getString(R.string.backup_settings_title))
                            .setMessage(context.getResources().getString(R.string.backup_settings_summary))
                            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    File des = new File(Environment.getExternalStorageDirectory() + "/SlidingMessaging/backup.prefs");
                                    saveSharedPreferencesToFile(des);

                                    Toast.makeText(context, context.getResources().getString(R.string.backup_success), Toast.LENGTH_LONG).show();
                                }
                            })
                            .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            })
                            .create()
                            .show();

                    return false;
                }

            });

            Preference restore = (Preference) findPreference("restore");
            restore.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

                @Override
                public boolean onPreferenceClick(Preference arg0) {
                    File des = new File(Environment.getExternalStorageDirectory() + "/SlidingMessaging/backup.prefs");
                    loadSharedPreferencesFromFile(des);

                    Toast.makeText(context, context.getResources().getString(R.string.restore_success), Toast.LENGTH_LONG).show();

                    return false;
                }

            });

            Preference smsToStore = (Preference) findPreference("sms_limit");
            smsToStore.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

                @Override
                public boolean onPreferenceClick(Preference arg0) {
                    if (sharedPrefs.getBoolean("delete_old", false))
                    {
                        NumberPickerDialog.OnNumberSetListener mSmsLimitListener =
                                new NumberPickerDialog.OnNumberSetListener() {
                                    public void onNumberSet(int limit) {
                                        SharedPreferences.Editor editor = sharedPrefs.edit();

                                        editor.putInt("sms_limit", limit);
                                        editor.commit();
                                    }
                                };

                        new NumberPickerDialog(context, mSmsLimitListener, sharedPrefs.getInt("sms_limit", 500), 100, 1000, R.string.sms_limit).show();
                    }

                    return false;
                }

            });

            Preference numCacheConversations = findPreference("num_cache_conversations");
            numCacheConversations.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

                @Override
                public boolean onPreferenceClick(Preference arg0) {
                    if (sharedPrefs.getBoolean("cache_conversations", false))
                    {
                        NumberPickerDialog.OnNumberSetListener numCacheListener =
                                new NumberPickerDialog.OnNumberSetListener() {
                                    public void onNumberSet(int limit) {
                                        SharedPreferences.Editor editor = sharedPrefs.edit();

                                        editor.putInt("num_cache_conversations", limit);
                                        editor.commit();

                                        if (sharedPrefs.getBoolean("cache_conversations", false)) {
                                            Intent cacheService = new Intent(context, CacheService.class);
                                            context.startService(cacheService);
                                        }
                                    }
                                };

                        new NumberPickerDialog(context, numCacheListener, sharedPrefs.getInt("num_cache_conversations", 5), 0, 15, R.string.num_cache_conversations).show();
                    }

                    return false;
                }

            });

//		Preference mmsToStore = (Preference) findPreference("mms_limit");
//		mmsToStore.setOnPreferenceClickListener(new OnPreferenceClickListener() {
//
//			@Override
//			public boolean onPreferenceClick(Preference arg0) {
//				if (sharedPrefs.getBoolean("delete_old", false))
//				{
//					NumberPickerDialog.OnNumberSetListener mMmsLimitListener =
//					        new NumberPickerDialog.OnNumberSetListener() {
//					            public void onNumberSet(int limit) {
//					            	SharedPreferences.Editor editor = sharedPrefs.edit();
//
//					                editor.putInt("mms_limit", limit);
//					                editor.commit();
//					            }
//					    };
//
//					new NumberPickerDialog(context2, android.R.style.Theme_Holo_Dialog, mMmsLimitListener, sharedPrefs.getInt("mms_limit", 100), 50, 500, R.string.mms_limit).show();
//				}
//
//				return false;
//			}
//
//		});
        }

        public void giveTestNotification()
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.test_notification);
            builder.setMessage(R.string.test_notification_summary);
            final AlertDialog dialog = builder.create();
            dialog.show();

            BroadcastReceiver screenOff = new BroadcastReceiver() {
                @Override
                public void onReceive(final Context context, Intent intent) {
                    final SharedPreferences sharedPrefs  = PreferenceManager.getDefaultSharedPreferences(getActivity());

                    NotificationCompat.Builder mBuilder =
                            new NotificationCompat.Builder(getActivity())
                                    .setSmallIcon(R.drawable.stat_notify_sms)
                                    .setContentTitle("Test")
                                    .setContentText("Test Notification")
                                    .setTicker("Test: Test Notification");

                    setIcon(mBuilder);

                    if (sharedPrefs.getBoolean("vibrate", true))
                    {
                        if (!sharedPrefs.getBoolean("custom_vibrate_pattern", false))
                        {
                            String vibPat = sharedPrefs.getString("vibrate_pattern", "2short");

                            if (vibPat.equals("short"))
                            {
                                long[] pattern = {0L, 400L};
                                mBuilder.setVibrate(pattern);
                            } else if (vibPat.equals("long"))
                            {
                                long[] pattern = {0L, 800L};
                                mBuilder.setVibrate(pattern);
                            } else if (vibPat.equals("2short"))
                            {
                                long[] pattern = {0L, 400L, 100L, 400L};
                                mBuilder.setVibrate(pattern);
                            } else if (vibPat.equals("2long"))
                            {
                                long[] pattern = {0L, 800L, 200L, 800L};
                                mBuilder.setVibrate(pattern);
                            } else if (vibPat.equals("3short"))
                            {
                                long[] pattern = {0L, 400L, 100L, 400L, 100L, 400L};
                                mBuilder.setVibrate(pattern);
                            } else if (vibPat.equals("3long"))
                            {
                                long[] pattern = {0L, 800L, 200L, 800L, 200L, 800L};
                                mBuilder.setVibrate(pattern);
                            }
                        } else
                        {
                            try
                            {
                                String[] vibPat = sharedPrefs.getString("set_custom_vibrate_pattern", "0, 400, 100, 400").replace("L", "").split(", ");
                                long[] pattern = new long[vibPat.length];

                                for (int i = 0; i < vibPat.length; i++)
                                {
                                    pattern[i] = Long.parseLong(vibPat[i]);
                                }

                                mBuilder.setVibrate(pattern);
                            } catch (Exception e)
                            {

                            }
                        }
                    }

                    if (sharedPrefs.getBoolean("led", true))
                    {
                        String ledColor = sharedPrefs.getString("led_color", "white");
                        int ledOn = sharedPrefs.getInt("led_on_time", 1000);
                        int ledOff = sharedPrefs.getInt("led_off_time", 2000);

                        if (ledColor.equalsIgnoreCase("white"))
                        {
                            mBuilder.setLights(0xFFFFFFFF, ledOn, ledOff);
                        } else if (ledColor.equalsIgnoreCase("blue"))
                        {
                            mBuilder.setLights(0xFF0099CC, ledOn, ledOff);
                        } else if (ledColor.equalsIgnoreCase("green"))
                        {
                            mBuilder.setLights(0xFF00FF00, ledOn, ledOff);
                        } else if (ledColor.equalsIgnoreCase("orange"))
                        {
                            mBuilder.setLights(0xFFFF8800, ledOn, ledOff);
                        } else if (ledColor.equalsIgnoreCase("red"))
                        {
                            mBuilder.setLights(0xFFCC0000, ledOn, ledOff);
                        } else if (ledColor.equalsIgnoreCase("purple"))
                        {
                            mBuilder.setLights(0xFFAA66CC, ledOn, ledOff);
                        } else
                        {
                            mBuilder.setLights(0xFFFFFFFF, ledOn, ledOff);
                        }
                    }

                    try
                    {
                        mBuilder.setSound(Uri.parse(sharedPrefs.getString("ringtone", "null")));
                    } catch(Exception e)
                    {
                        mBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
                    }

                    NotificationManager mNotificationManager =
                            (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);

                    Notification notification = new NotificationCompat.BigTextStyle(mBuilder).bigText("Test Notification").build();
                    Intent deleteIntent = new Intent(getActivity(), NotificationReceiver.class);
                    notification.deleteIntent = PendingIntent.getBroadcast(getActivity(), 0, deleteIntent, 0);
                    mNotificationManager.notify(1, notification);

                    context.unregisterReceiver(this);
                    dialog.dismiss();
                }
            };

            IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);

            getActivity().registerReceiver(screenOff, filter);
        }

        public void setIcon(NotificationCompat.Builder mBuilder)
        {
            final SharedPreferences sharedPrefs  = PreferenceManager.getDefaultSharedPreferences(getActivity());

            if (!sharedPrefs.getBoolean("breath", false))
            {
                String notIcon = sharedPrefs.getString("notification_icon", "white");
                int notImage = Integer.parseInt(sharedPrefs.getString("notification_image", "1"));

                switch (notImage)
                {
                    case 1:
                        if (notIcon.equals("white"))
                        {
                            mBuilder.setSmallIcon(R.drawable.stat_notify_sms);
                        } else if (notIcon.equals("blue"))
                        {
                            mBuilder.setSmallIcon(R.drawable.stat_notify_sms_blue);
                        } else if (notIcon.equals("green"))
                        {
                            mBuilder.setSmallIcon(R.drawable.stat_notify_sms_green);
                        } else if (notIcon.equals("orange"))
                        {
                            mBuilder.setSmallIcon(R.drawable.stat_notify_sms_orange);
                        } else if (notIcon.equals("purple"))
                        {
                            mBuilder.setSmallIcon(R.drawable.stat_notify_sms_purple);
                        } else if (notIcon.equals("red"))
                        {
                            mBuilder.setSmallIcon(R.drawable.stat_notify_sms_red);
                        } else if (notIcon.equals("icon"))
                        {
                            mBuilder.setSmallIcon(R.drawable.stat_notify_sms_icon);
                        }

                        break;
                    case 2:
                        if (notIcon.equals("white"))
                        {
                            mBuilder.setSmallIcon(R.drawable.stat_notify_bubble);
                        } else if (notIcon.equals("blue"))
                        {
                            mBuilder.setSmallIcon(R.drawable.stat_notify_bubble_blue);
                        } else if (notIcon.equals("green"))
                        {
                            mBuilder.setSmallIcon(R.drawable.stat_notify_bubble_green);
                        } else if (notIcon.equals("orange"))
                        {
                            mBuilder.setSmallIcon(R.drawable.stat_notify_bubble_orange);
                        } else if (notIcon.equals("purple"))
                        {
                            mBuilder.setSmallIcon(R.drawable.stat_notify_bubble_purple);
                        } else if (notIcon.equals("red"))
                        {
                            mBuilder.setSmallIcon(R.drawable.stat_notify_bubble_red);
                        } else if (notIcon.equals("icon"))
                        {
                            mBuilder.setSmallIcon(R.drawable.stat_notify_sms_icon);
                        }

                        break;
                    case 3:
                        if (notIcon.equals("white"))
                        {
                            mBuilder.setSmallIcon(R.drawable.stat_notify_point);
                        } else if (notIcon.equals("blue"))
                        {
                            mBuilder.setSmallIcon(R.drawable.stat_notify_point_blue);
                        } else if (notIcon.equals("green"))
                        {
                            mBuilder.setSmallIcon(R.drawable.stat_notify_point_green);
                        } else if (notIcon.equals("orange"))
                        {
                            mBuilder.setSmallIcon(R.drawable.stat_notify_point_orange);
                        } else if (notIcon.equals("purple"))
                        {
                            mBuilder.setSmallIcon(R.drawable.stat_notify_point_purple);
                        } else if (notIcon.equals("red"))
                        {
                            mBuilder.setSmallIcon(R.drawable.stat_notify_point_red);
                        } else if (notIcon.equals("icon"))
                        {
                            mBuilder.setSmallIcon(R.drawable.stat_notify_sms_icon);
                        }

                        break;
                    case 4:
                        if (notIcon.equals("white"))
                        {
                            mBuilder.setSmallIcon(R.drawable.stat_notify_airplane);
                        } else if (notIcon.equals("blue"))
                        {
                            mBuilder.setSmallIcon(R.drawable.stat_notify_airplane_blue);
                        } else if (notIcon.equals("green"))
                        {
                            mBuilder.setSmallIcon(R.drawable.stat_notify_airplane_green);
                        } else if (notIcon.equals("orange"))
                        {
                            mBuilder.setSmallIcon(R.drawable.stat_notify_airplane_orange);
                        } else if (notIcon.equals("purple"))
                        {
                            mBuilder.setSmallIcon(R.drawable.stat_notify_airplane_purple);
                        } else if (notIcon.equals("red"))
                        {
                            mBuilder.setSmallIcon(R.drawable.stat_notify_airplane_red);
                        } else if (notIcon.equals("icon"))
                        {
                            mBuilder.setSmallIcon(R.drawable.stat_notify_sms_icon);
                        }

                        break;
                    case 5:
                        if (notIcon.equals("white"))
                        {
                            mBuilder.setSmallIcon(R.drawable.stat_notify_cloud);
                        } else if (notIcon.equals("blue"))
                        {
                            mBuilder.setSmallIcon(R.drawable.stat_notify_cloud_blue);
                        } else if (notIcon.equals("green"))
                        {
                            mBuilder.setSmallIcon(R.drawable.stat_notify_cloud_green);
                        } else if (notIcon.equals("orange"))
                        {
                            mBuilder.setSmallIcon(R.drawable.stat_notify_cloud_orange);
                        } else if (notIcon.equals("purple"))
                        {
                            mBuilder.setSmallIcon(R.drawable.stat_notify_cloud_purple);
                        } else if (notIcon.equals("red"))
                        {
                            mBuilder.setSmallIcon(R.drawable.stat_notify_cloud_red);
                        } else if (notIcon.equals("icon"))
                        {
                            mBuilder.setSmallIcon(R.drawable.stat_notify_sms_icon);
                        }
                        break;
                }
            } else
            {
                mBuilder.setSmallIcon(R.drawable.stat_notify_sms_breath);
            }
        }

        public void deleteSMS(Context context) {
            ArrayList<String> threadIds = new ArrayList<String>();
            String[] projection = new String[]{"_id"};
            Uri uri = Uri.parse("content://mms-sms/conversations/?simple=true");
            Cursor query = context.getContentResolver().query(uri, projection, null, null, null);

            if (query.moveToFirst())
            {
                do
                {
                    threadIds.add(query.getString(query.getColumnIndex("_id")));
                } while (query.moveToNext());
            }

            try {
                for (int i = 0; i < threadIds.size(); i++)
                {
                    context.getContentResolver().delete(Uri.parse("content://mms-sms/conversations/" + threadIds.get(i) + "/"), null, null);
                }
            } catch (Exception e) {
            }
        }

        private void writeToFile(ArrayList<String> data, Context context) {
            try {
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("conversationList.txt", Context.MODE_PRIVATE));

                for (int i = 0; i < data.size(); i++)
                {
                    outputStreamWriter.write(data.get(i) + "\n");
                }

                outputStreamWriter.close();
            }
            catch (IOException e) {

            }

        }

        public void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
            super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

            if (requestCode == 1)
            {
                if(resultCode == RESULT_OK){
                    Uri selectedImage = imageReturnedIntent.getData();
                    String[] filePathColumn = {MediaStore.Images.Media.DATA};

                    Cursor cursor = getActivity().getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                    cursor.moveToFirst();

                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    String filePath = cursor.getString(columnIndex);
                    cursor.close();

                    SharedPreferences sharedPrefs  = PreferenceManager.getDefaultSharedPreferences(getActivity());
                    SharedPreferences.Editor editor = sharedPrefs.edit();

                    editor.putString("custom_background_location", filePath);
                    editor.commit();

                }
            } else if (requestCode == 2)
            {
                if(resultCode == RESULT_OK){
                    Uri selectedImage = imageReturnedIntent.getData();
                    String[] filePathColumn = {MediaStore.Images.Media.DATA};

                    Cursor cursor = getActivity().getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                    cursor.moveToFirst();

                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    String filePath = cursor.getString(columnIndex);
                    cursor.close();

                    SharedPreferences sharedPrefs  = PreferenceManager.getDefaultSharedPreferences(getActivity());
                    SharedPreferences.Editor editor = sharedPrefs.edit();

                    editor.putString("custom_background2_location", filePath);
                    editor.commit();

                }
            } else if (requestCode == REQ_CREATE_PATTERN)
            {
                if (resultCode == RESULT_OK) {

                } else {
                    SharedPreferences sharedPrefs  = PreferenceManager.getDefaultSharedPreferences(getActivity());
                    SharedPreferences.Editor editor = sharedPrefs.edit();
                    editor.putString("security_option", "none");
                    editor.commit();
                }
            }
        }

        private void writeToFile2(ArrayList<String> data, Context context) {
            try {
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("conversationList.txt", Context.MODE_PRIVATE));

                for (int i = 0; i < data.size(); i++)
                {
                    outputStreamWriter.write(data.get(i) + "\n");
                }

                outputStreamWriter.close();
            }
            catch (IOException e) {

            }

        }

        private boolean saveSharedPreferencesToFile(File dst)
        {
            boolean res = false;
            ObjectOutputStream output = null;

            try
            {
                output = new ObjectOutputStream(new FileOutputStream(dst));
                SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());

                output.writeObject(pref.getAll());

                res = true;
            } catch (Exception e)
            {

            } finally
            {
                try
                {
                    if (output != null)
                    {
                        output.flush();
                        output.close();
                    }
                } catch (Exception e)
                {

                }
            }

            return res;
        }

        private boolean loadSharedPreferencesFromFile(File src)
        {
            boolean res = false;
            ObjectInputStream input = null;

            try
            {
                input = new ObjectInputStream(new FileInputStream(src));
                SharedPreferences.Editor prefEdit = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
                prefEdit.clear();

                @SuppressWarnings("unchecked")
                Map<String, ?> entries = (Map<String, ?>) input.readObject();

                for (Map.Entry<String, ?> entry : entries.entrySet())
                {
                    Object v = entry.getValue();
                    String key = entry.getKey();

                    if (v instanceof Boolean)
                    {
                        prefEdit.putBoolean(key, ((Boolean) v).booleanValue());
                    } else if (v instanceof Float)
                    {
                        prefEdit.putFloat(key,  ((Float) v).floatValue());
                    } else if (v instanceof Integer)
                    {
                        prefEdit.putInt(key,  ((Integer) v).intValue());
                    } else if (v instanceof Long)
                    {
                        prefEdit.putLong(key,  ((Long) v).longValue());
                    } else if (v instanceof String)
                    {
                        prefEdit.putString(key, ((String) v));
                    }
                }

                prefEdit.commit();

                res = true;
            } catch (Exception e)
            {

            } finally
            {
                try
                {
                    if (input != null)
                    {
                        input.close();
                    }
                } catch (Exception e)
                {

                }
            }

            return res;
        }
    }

}