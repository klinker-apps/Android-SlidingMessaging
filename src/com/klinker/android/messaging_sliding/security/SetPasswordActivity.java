package com.klinker.android.messaging_sliding.security;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
public class SetPasswordActivity extends FragmentActivity {
    private SharedPreferences sharedPrefs;

    private Intent fromIntent;

    private String password;
    private String text;

    private int numChar;
    private int numEntries;

    private TextView passwordBox;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password);
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        fromIntent = getIntent();
        final Context context = this;

        password = "";

        Button setPassword = (Button) findViewById(R.id.unlock_button);
        Button cancel = (Button) findViewById(R.id.close_button);

        setPassword.setText(getResources().getText(R.string.set_password));
        cancel.setText(getResources().getText(R.string.cancel));

        final EditText passwordText = (EditText)findViewById(R.id.passwordText);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Context context = getApplicationContext();
                CharSequence text = "Default password of 'password' set.";
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();

                SharedPreferences.Editor prefEdit = sharedPrefs.edit();
                prefEdit.putString("password", "password");
                prefEdit.commit();

                onBackPressed();
            }
        });

        setPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                password = passwordText.getText().toString();

                numEntries++;

                if (numEntries == 1)
                {
                    SharedPreferences.Editor prefEdit = sharedPrefs.edit();
                    prefEdit.putString("password", password);
                    prefEdit.commit();
                }


                if (password.equals(sharedPrefs.getString("password", "0000")) && numEntries == 2)
                {
                    SharedPreferences.Editor prefEdit = sharedPrefs.edit();
                    prefEdit.putLong("last_time", Calendar.getInstance().getTimeInMillis());
                    prefEdit.commit();

                    onBackPressed();
                } else
                {
                    if(password.equals(""))
                    {
                        CharSequence text = "Invalid password";
                        int duration = Toast.LENGTH_SHORT;

                        Toast toast = Toast.makeText(getApplicationContext(), text, duration);
                        toast.show();
                        numEntries = 0;

                    } else if(numEntries == 1)
                    {
                        passwordText.setText("");

                        TextView tv = (TextView) findViewById(R.id.title);
                        tv.setText("Re-Enter Password:");
                    } else if (numEntries == 2)
                    {
                        numEntries = 0;

                        CharSequence text = "Incorrect password";
                        int duration = Toast.LENGTH_SHORT;

                        Toast toast = Toast.makeText(getApplicationContext(), text, duration);
                        toast.show();

                        passwordText.setText("");

                        TextView tv = (TextView) findViewById(R.id.title);
                        tv.setText("Enter Password:");
                    }
                }
            }
        });
    }
}
