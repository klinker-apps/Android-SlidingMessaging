package com.klinker.android.messaging_sliding.security;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.klinker.android.messaging_donate.R;

import java.util.Calendar;

/**
 * Created by Luke on 6/2/13.
 */
public class SetPinActivity extends FragmentActivity {
    private SharedPreferences sharedPrefs;

    private Intent fromIntent;

    private String password;
    private String text;

    private int numChar;
    private int numEntries;

    private TextView passwordBox;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin);
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        fromIntent = getIntent();
        final Context context = this;

        text = "";
        password = "";
        numChar = 0;
        numEntries = 0;

        Button setPassword = (Button) findViewById(R.id.unlock_button);
        Button cancel = (Button) findViewById(R.id.close_button);

        setPassword.setText(getResources().getText(R.string.set_password));
        cancel.setText(getResources().getText(R.string.cancel));

        Button one = (Button) findViewById(R.id.button1);
        Button two = (Button) findViewById(R.id.button2);
        Button three = (Button) findViewById(R.id.button3);
        Button four = (Button) findViewById(R.id.button4);
        Button five = (Button) findViewById(R.id.button5);
        Button six = (Button) findViewById(R.id.button6);
        Button seven = (Button) findViewById(R.id.button7);
        Button eight = (Button) findViewById(R.id.button8);
        Button nine = (Button) findViewById(R.id.button9);
        Button zero = (Button) findViewById(R.id.button0);
        Button delete = (Button) findViewById(R.id.delete);

        passwordBox = (TextView) findViewById(R.id.password);

        passwordBox.setText("");

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Context context = getApplicationContext();
                CharSequence text = "Default password of 0000 set.";
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();

                SharedPreferences.Editor prefEdit = sharedPrefs.edit();
                prefEdit.putString("password", "0000");
                prefEdit.commit();

                onBackPressed();
            }
        });

        setPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                numEntries++;

                if (numEntries == 1) {
                    SharedPreferences.Editor prefEdit = sharedPrefs.edit();
                    prefEdit.putString("password", password);
                    prefEdit.commit();
                }


                if (password.equals(sharedPrefs.getString("password", "0000")) && numEntries == 2) {
                    SharedPreferences.Editor prefEdit = sharedPrefs.edit();
                    prefEdit.putLong("last_time", Calendar.getInstance().getTimeInMillis());
                    prefEdit.commit();

                    onBackPressed();
                } else {
                    if (passwordBox.getText().toString().equals("")) {
                        CharSequence text = "Invalid password";
                        int duration = Toast.LENGTH_SHORT;

                        Toast toast = Toast.makeText(getApplicationContext(), text, duration);
                        toast.show();
                        numEntries = 0;

                    } else if (numEntries == 1) {
                        password = "";
                        text = "";
                        numChar = 0;

                        passwordBox.setText("");

                        TextView tv = (TextView) findViewById(R.id.title);
                        tv.setText("Re-Enter Password:");
                    } else if (numEntries == 2) {
                        password = "";
                        text = "";
                        numChar = 0;
                        numEntries = 0;

                        passwordBox.setText("");

                        CharSequence text = "Incorrect password";
                        int duration = Toast.LENGTH_SHORT;

                        Toast toast = Toast.makeText(getApplicationContext(), text, duration);
                        toast.show();

                        TextView tv = (TextView) findViewById(R.id.title);
                        tv.setText("Enter Password:");
                    }
                }
            }
        });

        one.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updatePassword("1");
            }
        });

        two.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updatePassword("2");
            }
        });

        three.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updatePassword("3");
            }
        });

        four.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updatePassword("4");
            }
        });

        five.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updatePassword("5");
            }
        });

        six.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updatePassword("6");
            }
        });

        seven.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updatePassword("7");
            }
        });

        eight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updatePassword("8");
            }
        });

        nine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updatePassword("9");
            }
        });

        zero.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updatePassword("0");
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!passwordBox.getText().toString().equals("")) {
                    password = password.substring(0, password.length() - 1);

                    String currentText = passwordBox.getText().toString();
                    text = currentText.substring(0, currentText.length() - 1);

                    passwordBox.setText(text);

                    numChar--;
                } else {
                    Context context = getApplicationContext();
                    CharSequence text = "Nothing to delete";
                    int duration = Toast.LENGTH_SHORT;

                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                }
            }
        });
    }

    public void updatePassword(String newNumber) {
        password = password + newNumber;

        text = "";

        for (int i = 0; i < numChar; i++) {
            text = text + "*";
        }

        numChar++;

        text = text + newNumber;

        passwordBox.setText(text);
    }
}
