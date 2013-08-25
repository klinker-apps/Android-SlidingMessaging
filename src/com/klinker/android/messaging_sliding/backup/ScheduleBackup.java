package com.klinker.android.messaging_sliding.backup;

import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.klinker.android.messaging_donate.R;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by luke on 7/1/13.
 */
public class ScheduleBackup extends Activity implements AdapterView.OnItemSelectedListener {

    private Date setDate;

    private int currentYear;
    private int currentMonth;
    private int currentDay;
    private int currentHour;
    private int currentMinute;

    private long repetition;

    private int setYear = -1;
    private int setMonth = -1;
    private int setDay = -1;
    private int setHour = -1;
    private int setMinute = -1;

    static final int DATE_DIALOG_ID = 0;
    static final int TIME_DIALOG_ID = 1;

    private Button btDate;
    private Button btTime;

    private TextView timeDisplay;
    private TextView dateDisplay;

    public Date currentDate;

    public SharedPreferences sharedPrefs;

    public boolean timeDone = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scheduled_backup);

        // Inflate a "Done/Discard" custom action bar view.
        LayoutInflater inflater = (LayoutInflater) getActionBar().getThemedContext()
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        final View customActionBarView = inflater.inflate(
                R.layout.actionbar_custom_view_done_discard, null);

        customActionBarView.findViewById(R.id.actionbar_done).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        doneClick();
                    }
                });

        customActionBarView.findViewById(R.id.actionbar_discard).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        discardClick();
                    }
                });

        // Show the custom action bar view and hide the normal Home icon and title.
        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayOptions(
                ActionBar.DISPLAY_SHOW_CUSTOM,
                ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME
                        | ActionBar.DISPLAY_SHOW_TITLE);
        actionBar.setCustomView(customActionBarView, new ActionBar.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        sharedPrefs  = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        final Calendar c = Calendar.getInstance();
        currentYear = c.get(Calendar.YEAR);
        currentMonth = c.get(Calendar.MONTH);
        currentDay = c.get(Calendar.DAY_OF_MONTH);
        currentHour = c.get(Calendar.HOUR_OF_DAY);
        currentMinute = c.get(Calendar.MINUTE);

        currentDate = new Date(currentYear, currentMonth, currentDay, currentHour, currentMinute);

        timeDisplay = (TextView) findViewById(R.id.currentTime);
        dateDisplay = (TextView) findViewById(R.id.currentDate);
        btDate = (Button) findViewById(R.id.setDate);
        btTime = (Button) findViewById(R.id.setTime);

        btTime.setEnabled(false);

        if(!sharedPrefs.getString("speed_improvement_backup_date", "0").equals("0"))
            dateDisplay.setText(sharedPrefs.getString("speed_improvement_backup_date", "0"));

        if(!sharedPrefs.getString("speed_improvement_backup_time", "0").equals("0"))
        {
            timeDisplay.setText(sharedPrefs.getString("speed_improvement_backup_time", "0"));
            btTime.setEnabled(true);
        }

        int spinnerIndex = 0;

        if(sharedPrefs.getLong("speed_improvement_backup_repetition", 0) ==  0)
            spinnerIndex = 0;
        else if(sharedPrefs.getLong("speed_improvement_backup_repetition", 0) ==  AlarmManager.INTERVAL_DAY)
            spinnerIndex = 1;
        else if(sharedPrefs.getLong("speed_improvement_backup_repetition", 0) ==  AlarmManager.INTERVAL_DAY * 3)
            spinnerIndex = 2;
        else if(sharedPrefs.getLong("speed_improvement_backup_repetition", 0) ==  AlarmManager.INTERVAL_DAY * 7)
            spinnerIndex = 3;

        // Creates the spinner for repetition of scheduled sms
        Spinner spinner = (Spinner) findViewById(R.id.repetitionSpinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.backup_repetition_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
        spinner.setSelection(spinnerIndex);
        spinner.setOnItemSelectedListener(this);

        // sets the date button listener to call the dialog
        btDate.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                showDialog(DATE_DIALOG_ID);
                btTime.setEnabled(true);
            }
        });

        // sets the time button listener to call the dialog
        btTime.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                showDialog(TIME_DIALOG_ID);
            }
        });
    }


    // To-do: make a date object to display in different time formats, check out the messageArrayAdapter class, it works with the dates
    // gets the date text from what is entered in the dialog and displays it
    private DatePickerDialog.OnDateSetListener reservationDate = new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker view, int year, int month, int day) {
            setYear = year;
            setMonth = month;
            setDay = day;

            if (setHour != -1 && setMinute != -1)
            {
                setDate = new Date(setYear - 1900, setMonth, setDay, setHour, setMinute);

                if (sharedPrefs.getBoolean("hour_format", false))
                {
                    dateDisplay.setText(DateFormat.getDateInstance(DateFormat.SHORT, Locale.GERMAN).format(setDate));
                } else
                {
                    dateDisplay.setText(DateFormat.getDateInstance(DateFormat.SHORT, Locale.US).format(setDate));
                }
            } else
            {
                setDate = new Date(setYear - 1900, setMonth, setDay);

                if (sharedPrefs.getBoolean("hour_format", false))
                {
                    dateDisplay.setText(DateFormat.getDateInstance(DateFormat.SHORT, Locale.GERMAN).format(setDate));
                } else
                {
                    dateDisplay.setText(DateFormat.getDateInstance(DateFormat.SHORT, Locale.US).format(setDate));
                }
            }
            //dateDisplay.setText((month + 1) + "/" + day + "/" + year);
        }

    };

    // gets the time text from what is entered in the dialog and displays it
    private TimePickerDialog.OnTimeSetListener timeDate = new TimePickerDialog.OnTimeSetListener() {
        public void onTimeSet(TimePicker view, int hours, int minutes) {
            setHour = hours;
            setMinute = minutes;

            setDate.setHours(setHour);
            setDate.setMinutes(setMinute);

            currentDate.setYear(currentYear - 1900);

            if (!setDate.before(currentDate))
            {
                if (sharedPrefs.getBoolean("hour_format", false))
                {
                    timeDisplay.setText(DateFormat.getTimeInstance(DateFormat.SHORT, Locale.GERMAN).format(setDate));
                } else
                {
                    timeDisplay.setText(DateFormat.getTimeInstance(DateFormat.SHORT, Locale.US).format(setDate));
                }

                timeDone = true;
            } else
            {
                Context context = getApplicationContext();
                CharSequence text = "Date must be forward!";
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();

                btTime.setEnabled(false);

                timeDisplay.setText("");
                dateDisplay.setText("");

                timeDone = false;
            }

        }
    };

    // sets up the correct dialog (date vs. time)
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DATE_DIALOG_ID:
                DatePickerDialog dialog = new DatePickerDialog(this, reservationDate, currentYear,
                        currentMonth, currentDay);
                dialog.getDatePicker().setCalendarViewShown(false);
                return dialog;
            case TIME_DIALOG_ID:
                return new TimePickerDialog(this, timeDate, currentHour, currentMinute, false);
        }
        return null;
    }

    // sets the string repetition to whatever is choosen from the spinner
    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)
        String repeat = parent.getItemAtPosition(pos).toString();

        if (repeat.equals("Daily"))
            repetition = AlarmManager.INTERVAL_DAY;
        else if (repeat.equals("Every 3 Days"))
            repetition = AlarmManager.INTERVAL_DAY * 3;
        else if (repeat.equals("Weekly"))
            repetition = AlarmManager.INTERVAL_DAY * 7;
        else
            repetition = 0;
    }

    public void onNothingSelected(AdapterView<?> parent) {

    }

    // finishes the activity when the discard button is clicked, without making any changes or saving anything
    public boolean discardClick()
    {
        finish();
        return true;
    }

    // this is where we will set everything up when the user has entered all the information
    // including the alarm manager and writing the files to the database to save them
    public boolean doneClick()
    {
        if (timeDone)
        {
            SharedPreferences.Editor prefEdit = sharedPrefs.edit();
            prefEdit.putLong("speed_improvement_backup_repetition", repetition);
            prefEdit.putString("speed_improvement_backup_date", dateDisplay.getText().toString());
            prefEdit.putString("speed_improvement_backup_time", timeDisplay.getText().toString());
            prefEdit.commit();

            createAlarm();

            finish();
        } else
        {
            Context context = getApplicationContext();
            CharSequence text = "Please complete the form!";
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }

        return true;
    }

    public void createAlarm()
    {
        Intent serviceIntent = new Intent(getApplicationContext(), BackupService.class);

        PendingIntent pi = PendingIntent.getService(
                                this,               //context
                                1,                  //request id
                                serviceIntent,      //intent to be delivered
                                0);

        // Schedule the alarm!
        AlarmManager am = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);

        am.cancel(pi);

        if (repetition == 0)
        {
            am.set(AlarmManager.RTC_WAKEUP,
                    setDate.getTime(),
                    pi);
        } else
        {
            am.setRepeating(AlarmManager.RTC_WAKEUP,
                    setDate.getTime(),
                    repetition,
                    pi);
        }
    }
}
