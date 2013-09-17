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
import android.widget.TextView;
import android.widget.Toast;
import com.klinker.android.messaging_donate.MainActivity;
import com.klinker.android.messaging_donate.R;

import java.util.Calendar;

public class PinActivity extends FragmentActivity {
    private SharedPreferences sharedPrefs;

    private Context context;

    private Intent fromIntent;

    private String password;
    private String text;

    private int numChar;

    private TextView passwordBox;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin);
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        fromIntent = getIntent();
        context = this;

        text = "";
        password = "";
        numChar = 0;

        Button unlock = (Button) findViewById(R.id.unlock_button);
        Button close = (Button) findViewById(R.id.close_button);

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

        close.setOnClickListener(new View.OnClickListener() {
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

                if (password.equals(sharedPrefs.getString("password", "0000")))
                {
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

                    if (fromIntent.getStringExtra("com.klinker.android.OPEN") != null)
                    {
                        flag = true;
                    }

                    final Intent intent = new Intent(context, MainActivity.class);
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

                    openActivity();
                } else
                {
                    incorrectPassword();
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
                if(!passwordBox.getText().toString().equals(""))
                {
                    password = password.substring(0, password.length()-1);

                    String currentText = passwordBox.getText().toString();
                    text = currentText.substring(0, currentText.length()-1);

                    passwordBox.setText(text);

                    numChar--;
                } else
                {
                    Context context = getApplicationContext();
                    CharSequence text = "Nothing to delete";
                    int duration = Toast.LENGTH_SHORT;

                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                }
            }
        });
    }

    public void updatePassword(String newNumber)
    {
        password = password + newNumber;

        text = "";

        for(int i = 0; i < numChar; i++)
        {
            text = text + "*";
        }

        numChar++;

        text = text + newNumber;

        passwordBox.setText(text);

        if(password.length() == sharedPrefs.getString("password", "0").length() && sharedPrefs.getBoolean("auto_unlock", true))
        {
            if (password.equals(sharedPrefs.getString("password", "0")))
                openActivity();
            else
            {
                incorrectPassword();
            }
        }
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

        final Intent intent = new Intent(context, MainActivity.class);
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

        text = "";
        password = "";
        numChar = 0;

        passwordBox.setText("");
    }
}
