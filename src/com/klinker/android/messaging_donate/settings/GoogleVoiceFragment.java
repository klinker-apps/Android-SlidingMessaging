package com.klinker.android.messaging_donate.settings;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.klinker.android.messaging_donate.R;
import com.klinker.android.messaging_sliding.receivers.OnBootReceiver;
import com.klinker.android.messaging_sliding.receivers.VoiceReceiver;

import java.util.Calendar;

public class GoogleVoiceFragment extends Fragment {
    class AccountAdapter extends ArrayAdapter<Account> {
        AccountAdapter() {
            super(GoogleVoiceFragment.context, android.R.layout.simple_list_item_single_choice);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);

            CheckedTextView tv = (CheckedTextView) view.findViewById(android.R.id.text1);
            Account account = getItem(position);
            tv.setText(account.name);

            return view;
        }
    }

    Account NULL;

    ListView lv;
    AccountAdapter accountAdapter;
    SharedPreferences settings;

    static Activity context;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        context = activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View layout = inflater.inflate(R.layout.voice_setup, null);

        accountAdapter = new AccountAdapter();
        settings = PreferenceManager.getDefaultSharedPreferences(context);

        lv = (ListView) layout.findViewById(R.id.list);
        lv.setAdapter(accountAdapter = new AccountAdapter());

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Account account = accountAdapter.getItem(position);

                final String previousAccount = settings.getString("voice_account", null);
                new Thread() {
                    @Override
                    public void run() {
                        super.run();invalidateToken(previousAccount);
                    }
                }.start();

                if (account == NULL) {
                    settings.edit().remove("voice_account").remove("voice_rnrse").remove("voice_enabled").commit();

                    OnBootReceiver.stopVoiceReceiverManager(context);
                    return;
                }

                lv.clearChoices();
                lv.requestLayout();
                getToken(account, position);
            }
        });

        Button syncButton = (Button) layout.findViewById(R.id.syncButton);
        syncButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                context.startService(new Intent(context, VoiceReceiver.class));
            }
        });

        final CheckBox alwaysVoice = (CheckBox) layout.findViewById(R.id.alwaysUseVoice);
        alwaysVoice.setChecked(settings.getBoolean("always_use_voice", false));
        alwaysVoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (settings.getBoolean("always_use_voice", false)) {
                    alwaysVoice.setChecked(false);
                    settings.edit().putBoolean("always_use_voice", false).commit();
                } else {
                    alwaysVoice.setChecked(true);
                    settings.edit().putBoolean("always_use_voice", true).commit();
                }
            }
        });

        LinearLayout voiceReceivingOption1 = (LinearLayout) layout.findViewById(R.id.voiceReceivingOption1);
        voiceReceivingOption1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (android.os.Build.VERSION.SDK_INT >= 18) {
                    startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
                } else {
                    startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
                }
            }
        });

        LinearLayout voiceReceivingOption2 = (LinearLayout) layout.findViewById(R.id.voiceReceivingOption2);
        voiceReceivingOption2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String[] values = context.getResources().getStringArray(R.array.repeatingVoiceRefresh);
                final String[] set = context.getResources().getStringArray(R.array.repeatingVoiceRefreshSet);
                new AlertDialog.Builder(context)
                        .setTitle(R.string.select_interval)
                        .setSingleChoiceItems(values, settings.getInt("repeatingVoiceItem", 0), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                if (i == 1 || i == 2 || i == 3) {
                                    new AlertDialog.Builder(context)
                                            .setMessage(R.string.low_interval)
                                            .setPositiveButton(R.string.ok, null)
                                            .create()
                                            .show();
                                }

                                settings.edit()
                                        .putString("repeatingVoiceInterval", i != 0 ? set[i] : null)
                                        .putInt("repeatingVoiceItem", i)
                                        .commit();

                                if (i == 0) {
                                    OnBootReceiver.stopVoiceReceiverManager(context);
                                } else {
                                    OnBootReceiver.startVoiceReceiverManager(context, Calendar.getInstance().getTimeInMillis(), Long.parseLong(settings.getString("repeatingVoiceInterval", null)));
                                }
                            }
                        })
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        })
                        .create()
                        .show();
            }
        });

        Button tipsButton = (Button) layout.findViewById(R.id.tipsButton);
        tipsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(context, com.klinker.android.messaging_sliding.developer_tips.MainActivity.class));
            }
        });

        String selectedAccount = settings.getString("voice_account", null);

        NULL = new Account(getString(R.string.disable), "com.google");
        accountAdapter.add(NULL);
        int selected = 0;
        for (Account account : AccountManager.get(context).getAccountsByType("com.google")) {
            if (account.name.equals(selectedAccount))
                selected = accountAdapter.getCount();
            accountAdapter.add(account);
        }

        lv.setItemChecked(selected, true);
        lv.requestLayout();

        return layout;
    }

    void invalidateToken(String account) {
        if (account == null)
            return;

        try {
            Bundle bundle = AccountManager.get(context).getAuthToken(new Account(account, "com.google"), "grandcentral", true, null, null).getResult();
            String authToken = bundle.getString(AccountManager.KEY_AUTHTOKEN);
            AccountManager.get(context).invalidateAuthToken("com.google", authToken);
        } catch (Exception e) {

        }
    }

    void getToken(final Account account, final int position) {
        AccountManager am = AccountManager.get(context);
        if (am == null)
            return;

        am.getAuthToken(account, "grandcentral", null, context, new AccountManagerCallback<Bundle>() {
            @Override
            public void run(AccountManagerFuture<Bundle> future) {
                try {
                    settings.edit()
                            .putString("voice_account", account.name)
                            .putBoolean("voice_enabled", true)
                            .commit();

                    lv.setItemChecked(position, true);
                    lv.requestLayout();

                    OnBootReceiver.startVoiceReceiverManager(context, Calendar.getInstance().getTimeInMillis(), Long.parseLong(settings.getString("repeatingVoiceInterval", null)));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }, new Handler());
    }
}
