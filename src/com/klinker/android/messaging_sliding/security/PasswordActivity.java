package com.klinker.android.messaging_sliding.security;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.klinker.android.messaging_donate.R;

import java.util.Calendar;

/**
 * Created by Luke on 6/2/13.
 */
public class PasswordActivity extends FragmentActivity {
    private SharedPreferences sharedPrefs;

    private Context context;

    private Intent fromIntent;

    private String password;

    private EditText passwordText;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password);
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        fromIntent = getIntent();
        context = this;

        password = "";

        Button unlock = (Button) findViewById(R.id.unlock_button);
        Button cancel = (Button) findViewById(R.id.close_button);

        unlock.setText(getResources().getText(R.string.unlock));
        cancel.setText(getResources().getText(R.string.cancel));

        passwordText = (EditText)findViewById(R.id.passwordText);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent startMain = new Intent(Intent.ACTION_MAIN);
                startMain.addCategory(Intent.CATEGORY_HOME);
                startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(startMain);
            }
        });

        unlock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                password = passwordText.getText().toString();

                if (password.equals(sharedPrefs.getString("password", "0000"))) {
                    String version = "";

                    try {
                        version = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }

                    SharedPreferences.Editor prefEdit = sharedPrefs.edit();
                    prefEdit.putString("current_version", version);
                    prefEdit.putLong("last_time", Calendar.getInstance().getTimeInMillis());
                    prefEdit.commit();

                    boolean flag = false;

                    if (fromIntent.getStringExtra("com.klinker.android.OPEN") != null) {
                        flag = true;
                    }

                    final Intent intent = new Intent(context, com.klinker.android.messaging_sliding.MainActivity.class);
                    intent.setAction(fromIntent.getAction());
                    intent.setData(fromIntent.getData());

                    try {
                        intent.putExtras(fromIntent.getExtras());
                    } catch (Exception e) {

                    }

                    if (flag) {
                        intent.putExtra("com.klinker.android.OPEN", intent.getStringExtra("com.klinker.android.OPEN"));
                    }

                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(intent);
                    finish();

                    openActivity();
                } else {
                    incorrectPassword();
                }
            }
        });
    }

    public void openActivity()
    {
        SharedPreferences.Editor prefEdit = sharedPrefs.edit();
        prefEdit.putLong("last_time", System.currentTimeMillis());
        prefEdit.commit();

        boolean flag = false;

        if (fromIntent.getStringExtra("com.klinker.android.OPEN") != null)
        {
            flag = true;
        }

        final Intent intent = new Intent(context, com.klinker.android.messaging_sliding.MainActivity.class);
        intent.setAction(fromIntent.getAction());
        intent.setData(fromIntent.getData());

        try
        {
            intent.putExtras(fromIntent.getExtras());
        } catch (Exception e)
        {

        }

        if (flag)
        {
            intent.putExtra("com.klinker.android.OPEN", intent.getStringExtra("com.klinker.android.OPEN"));
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
        finish();
    }

    public void incorrectPassword()
    {
        CharSequence text = "Incorrect password";
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(getApplicationContext(), text, duration);
        toast.show();

        passwordText.setText("");
    }
}
