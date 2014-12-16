package com.klinker.android.messaging_sliding.scheduled;

import android.annotation.SuppressLint;
import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.*;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.android.datetimepicker.time.RadialPickerLayout;
import com.klinker.android.messaging_donate.R;
import com.klinker.android.messaging_donate.settings.SettingsPagerActivity;
import com.klinker.android.messaging_donate.utils.ContactUtil;
import com.klinker.android.messaging_donate.utils.IOUtil;
import com.klinker.android.messaging_donate.wizardpager.ChangeLogMain;
import com.klinker.android.messaging_sliding.ContactSearchArrayAdapter;
import com.klinker.android.messaging_sliding.batch_delete.BatchDeleteAllActivity;
import com.klinker.android.messaging_sliding.batch_delete.BatchDeleteConversationActivity;
import com.klinker.android.messaging_sliding.emojis.EmojiAdapter;
import com.klinker.android.messaging_sliding.emojis.EmojiAdapter2;
import com.klinker.android.messaging_sliding.emojis.EmojiConverter;
import com.klinker.android.messaging_sliding.emojis.EmojiConverter2;
import com.klinker.android.messaging_sliding.quick_reply.QmMarkRead;
import com.klinker.android.messaging_sliding.receivers.VoiceReceiver;
import com.klinker.android.messaging_sliding.scheduled.scheduled.ScheduledDataSource;
import com.klinker.android.messaging_sliding.scheduled.scheduled.ScheduledMessage;
import com.klinker.android.messaging_sliding.search.SearchActivity;
import com.klinker.android.messaging_sliding.templates.TemplateActivity;
import com.klinker.android.messaging_sliding.templates.TemplateArrayAdapter;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NewScheduledSms extends Activity implements AdapterView.OnItemSelectedListener {

    public final static String EXTRA_NUMBER = "com.klinker.android.messaging_sliding.NUMBER";
    public final static String EXTRA_MESSAGE = "com.klinker.android.messaging_sliding.MESSAGE";

    private Context context;

    private ListPopupWindow lpw;

    private Date setDate;

    private int currentYear;
    private int currentMonth;
    private int currentDay;
    private int currentHour;
    private int currentMinute;

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

    private EditText contactSearch;
    private EditText mEditText;

    private String repetition;

    public SharedPreferences sharedPrefs;

    public boolean firstContactSearch = true;
    private boolean timeDone = false;

    public Date currentDate;

    public ArrayList<String> contactNames, contactNumbers, contactTypes;
    public ArrayList<ScheduledMessage> data;

    private String startNumber;
    private long startDate;
    private long startRepeat;
    private String startMessage;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scheduled_sms_activity);

        repetition = getString(R.string.never);

        Intent intent = getIntent();

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        context = this;

        data = IOUtil.readScheduled(context);

        mEditText = (EditText) findViewById(R.id.messageEntry2);

        if (!sharedPrefs.getBoolean("keyboard_type", true)) {
            mEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
            mEditText.setImeOptions(EditorInfo.IME_ACTION_NONE);
        }

        startNumber = intent.getStringExtra(ScheduledSms.EXTRA_NUMBER);
        startDate = intent.getLongExtra(ScheduledSms.EXTRA_DATE, 0);
        startRepeat = intent.getLongExtra(ScheduledSms.EXTRA_REPEAT, -1);
        startMessage = intent.getStringExtra(ScheduledSms.EXTRA_MESSAGE);

        int spinnerIndex;

        try {
            if (startRepeat == ScheduledMessage.REPEAT_NEVER)
                spinnerIndex = 0;
            else if (startRepeat == ScheduledMessage.REPEAT_DAILY)
                spinnerIndex = 1;
            else if (startRepeat == ScheduledMessage.REPEAT_WEEKLY)
                spinnerIndex = 2;
            else if (startRepeat == ScheduledMessage.REPEAT_MONTHLY)
                spinnerIndex = 3;
            else
                spinnerIndex = 4;
        } catch (Exception e) {
            spinnerIndex = 0;
        }

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

        contactSearch = (EditText) findViewById(R.id.contactEntry);
        contactSearch.requestFocus();

        if (startDate != 0) {
            setDate = new Date(startDate);
            timeDone = true;
            btTime.setEnabled(true);
        } else {
            btTime.setEnabled(false);
        }

        contactSearch.setText(startNumber);
        mEditText.setText(startMessage);

        if (startDate != 0) {
            Date startDateObj = new Date(startDate);
            if (sharedPrefs.getBoolean("hour_format", false)) {
                timeDisplay.setText(DateFormat.getTimeInstance(DateFormat.SHORT, Locale.GERMAN).format(startDateObj));
            } else {
                timeDisplay.setText(DateFormat.getTimeInstance(DateFormat.SHORT, Locale.US).format(startDateObj));
            }

            if (sharedPrefs.getBoolean("hour_format", false)) {
                dateDisplay.setText(DateFormat.getDateInstance(DateFormat.MEDIUM).format(startDateObj));
            } else {
                dateDisplay.setText(DateFormat.getDateInstance(DateFormat.MEDIUM).format(startDateObj));
            }
        }

        lpw = new ListPopupWindow(NewScheduledSms.this);

        lpw.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1,
                                    int position, long arg3) {
                TextView view2 = (TextView) arg1.findViewById(R.id.receivedMessage);

                String[] t1 = contactSearch.getText().toString().split("; ");
                String string = "";

                for (int i = 0; i < t1.length - 1; i++) {
                    string += t1[i] + "; ";
                }

                contactSearch.setText(string + view2.getText() + "; ");
                contactSearch.setSelection(contactSearch.getText().length());
                lpw.dismiss();
                firstContactSearch = true;
            }

        });

        // Creates the spinner for repetition of scheduled sms
        Spinner spinner = (Spinner) findViewById(R.id.repetitionSpinner);

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.repetition_array, android.R.layout.simple_spinner_item);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        spinner.setSelection(spinnerIndex);

        spinner.setOnItemSelectedListener(this);

        // brings up the pop up window for contact search
        contactSearch.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (firstContactSearch) {
                    try {
                        contactNames = new ArrayList<String>();
                        contactNumbers = new ArrayList<String>();
                        contactTypes = new ArrayList<String>();

                        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
                        String[] projection = new String[]{ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                                ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.LABEL};

                        Cursor people = getContentResolver().query(uri, projection, null, null, null);

                        int indexName = people.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                        int indexNumber = people.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);

                        people.moveToFirst();
                        do {
                            int type = people.getInt(people.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
                            String customLabel = people.getString(people.getColumnIndex(ContactsContract.CommonDataKinds.Phone.LABEL));

                            if (sharedPrefs.getBoolean("mobile_only", false)) {
                                if (type == 2) {
                                    contactNames.add(people.getString(indexName));
                                    contactNumbers.add(people.getString(indexNumber).replaceAll("[^0-9\\+]", ""));
                                    contactTypes.add(ContactsContract.CommonDataKinds.Phone.getTypeLabel(context.getResources(), type, customLabel).toString());
                                }
                            } else {
                                contactNames.add(people.getString(indexName));
                                contactNumbers.add(people.getString(indexNumber).replaceAll("[^0-9\\+]", ""));
                                contactTypes.add(ContactsContract.CommonDataKinds.Phone.getTypeLabel(context.getResources(), type, customLabel).toString());
                            }
                        } while (people.moveToNext());
                        people.close();
                    } catch (IllegalArgumentException e) {

                    }


                }
            }

            @SuppressLint("DefaultLocale")
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                ArrayList<String> searchedNames = new ArrayList<String>();
                ArrayList<String> searchedNumbers = new ArrayList<String>();
                ArrayList<String> searchedTypes = new ArrayList<String>();

                String text = contactSearch.getText().toString();

                String[] text2 = text.split("; ");

                text = text2[text2.length - 1];

                if (text.startsWith("+")) {
                    text = text.substring(1);
                }

                Pattern pattern;

                try {
                    pattern = Pattern.compile(text.toLowerCase());
                } catch (Exception e) {
                    pattern = Pattern.compile(text.toLowerCase().replace("(", "").replace(")", "").replace("?", "").replace("[", "").replace("{", "").replace("}", "").replace("\\", ""));
                }

                for (int i = 0; i < contactNames.size(); i++) {
                    try {
                        Long.parseLong(text);

                        if (text.length() <= contactNumbers.get(i).length()) {
                            Matcher matcher = pattern.matcher(contactNumbers.get(i));
                            if (matcher.find()) {
                                searchedNames.add(contactNames.get(i));
                                searchedNumbers.add(contactNumbers.get(i));
                                searchedTypes.add(contactTypes.get(i));
                            }
                        }
                    } catch (Exception e) {
                        if (contactNames == null) {
                            contactNames = new ArrayList<String>();
                            contactNumbers = new ArrayList<String>();
                            contactTypes = new ArrayList<String>();
                        }
                        if (text.length() <= contactNames.get(i).length()) {
                            Matcher matcher = pattern.matcher(contactNames.get(i).toLowerCase());
                            if (matcher.find()) {
                                searchedNames.add(contactNames.get(i));
                                searchedNumbers.add(contactNumbers.get(i));
                                searchedTypes.add(contactTypes.get(i));
                            }
                        }
                    }
                }

                Display display = getWindowManager().getDefaultDisplay();
                Point size = new Point();
                display.getSize(size);
                int width = size.x;
                int height = size.y;

                lpw.setAdapter(new ContactSearchArrayAdapter((Activity) context, searchedNames, searchedNumbers, searchedTypes));
                lpw.setAnchorView(findViewById(R.id.contactEntry));
                lpw.setWidth(width - 20); // TODO better sizing
                lpw.setHeight(height / 3);

                if (firstContactSearch) {
                    lpw.show();
                    firstContactSearch = false;
                }

                if (text.length() == 0) {
                    lpw.dismiss();
                    firstContactSearch = true;
                }


            }

            public void afterTextChanged(Editable s) {
            }
        });

        // sets up the emoji button and implements the listener
        ImageButton emojiButton = (ImageButton) findViewById(R.id.display_emoji);

        if (!sharedPrefs.getBoolean("emoji", false)) {
            emojiButton.setVisibility(View.GONE);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mEditText.getLayoutParams();
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            mEditText.setLayoutParams(params);
        } else {
            emojiButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Insert Emojis");
                    LayoutInflater inflater = ((Activity) context).getLayoutInflater();
                    View frame = inflater.inflate(R.layout.emoji_frame, null);

                    final EditText editText = (EditText) frame.findViewById(R.id.emoji_text);
                    ImageButton peopleButton = (ImageButton) frame.findViewById(R.id.peopleButton);
                    ImageButton objectsButton = (ImageButton) frame.findViewById(R.id.objectsButton);
                    ImageButton natureButton = (ImageButton) frame.findViewById(R.id.natureButton);
                    ImageButton placesButton = (ImageButton) frame.findViewById(R.id.placesButton);
                    ImageButton symbolsButton = (ImageButton) frame.findViewById(R.id.symbolsButton);

                    final GridView emojiGrid = (GridView) frame.findViewById(R.id.emojiGrid);
                    Button okButton = (Button) frame.findViewById(R.id.emoji_ok);

                    if (sharedPrefs.getBoolean("emoji_type", true)) {
                        emojiGrid.setAdapter(new EmojiAdapter2(context));
                        emojiGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                                editText.setText(EmojiConverter2.getSmiledText(context, editText.getText().toString() + EmojiAdapter2.mEmojiTexts[position]));
                                editText.setSelection(editText.getText().length());
                            }
                        });

                        peopleButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                emojiGrid.setSelection(0);
                            }
                        });

                        objectsButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                emojiGrid.setSelection(153 + (2 * 7));
                            }
                        });

                        natureButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                emojiGrid.setSelection(153 + 162 + (3 * 7));
                            }
                        });

                        placesButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                emojiGrid.setSelection(153 + 162 + 178 + (5 * 7));
                            }
                        });

                        symbolsButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                emojiGrid.setSelection(153 + 162 + 178 + 122 + (7 * 7));
                            }
                        });
                    } else {
                        emojiGrid.setAdapter(new EmojiAdapter(context));
                        emojiGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                                editText.setText(EmojiConverter.getSmiledText(context, editText.getText().toString() + EmojiAdapter.mEmojiTexts[position]));
                                editText.setSelection(editText.getText().length());
                            }
                        });

                        peopleButton.setMaxHeight(0);
                        objectsButton.setMaxHeight(0);
                        natureButton.setMaxHeight(0);
                        placesButton.setMaxHeight(0);
                        symbolsButton.setMaxHeight(0);

                        LinearLayout buttons = (LinearLayout) frame.findViewById(R.id.linearLayout);
                        buttons.setMinimumHeight(0);
                        buttons.setVisibility(View.GONE);
                    }

                    builder.setView(frame);
                    final AlertDialog dialog = builder.create();
                    dialog.show();

                    okButton.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            if (sharedPrefs.getBoolean("emoji_type", true)) {
                                mEditText.setText(EmojiConverter2.getSmiledText(context, mEditText.getText().toString() + editText.getText().toString()));
                                mEditText.setSelection(mEditText.getText().length());
                            } else {
                                mEditText.setText(EmojiConverter.getSmiledText(context, mEditText.getText().toString() + editText.getText().toString()));
                                mEditText.setSelection(mEditText.getText().length());
                            }

                            dialog.dismiss();
                        }

                    });
                }

            });
        }

        btDate.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                try {
                    com.android.datetimepicker.date.DatePickerDialog.newInstance(reservationDate, currentYear, currentMonth, currentDay, false)
                            .show(getFragmentManager(), "date_picker");
                    btTime.setEnabled(true);
                } catch (Exception e) {
                    showDialog(DATE_DIALOG_ID);
                }

            }
        });

        // sets the time button listener to call the dialog
        btTime.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                try {
                    com.android.datetimepicker.time.TimePickerDialog.newInstance(timeDate, currentHour, currentMinute, android.provider.Settings.System.getString(context.getContentResolver(), android.provider.Settings.System.TIME_12_24).equals("24"))
                            .show(getFragmentManager(), "time_picker");
                } catch (Exception e) {
                    try {
                        com.android.datetimepicker.time.TimePickerDialog.newInstance(timeDate, currentHour, currentMinute, sharedPrefs.getBoolean("hour_format", false))
                                .show(getFragmentManager(), "time_picker");
                    } catch (Exception x) {
                        showDialog(TIME_DIALOG_ID);
                    }
                }
            }
        });

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
    }

    // sets up the correct dialog (date vs. time)
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DATE_DIALOG_ID:
                DatePickerDialog dialog = new DatePickerDialog(this, reservationDate2, currentYear,
                        currentMonth, currentDay);
                dialog.getDatePicker().setCalendarViewShown(false);
                return dialog;
            case TIME_DIALOG_ID:
                return new TimePickerDialog(this, timeDate2, currentHour, currentMinute, false);
            }
        return null;
        }

    // To-do: make a date object to display in different time formats, check out the messageArrayAdapter class, it works with the dates
    // gets the date text from what is entered in the dialog and displays it
    private com.android.datetimepicker.date.DatePickerDialog.OnDateSetListener reservationDate = new com.android.datetimepicker.date.DatePickerDialog.OnDateSetListener() {
        public void onDateSet(com.android.datetimepicker.date.DatePickerDialog view, int year, int month, int day) {
            setYear = year;
            setMonth = month;
            setDay = day;

            if (setHour != -1 && setMinute != -1) {
                setDate = new Date(setYear - 1900, setMonth, setDay, setHour, setMinute);

                if (sharedPrefs.getBoolean("hour_format", false)) {
                    dateDisplay.setText(DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.GERMAN).format(setDate));
                } else {
                    dateDisplay.setText(DateFormat.getDateInstance(DateFormat.MEDIUM).format(setDate));
                }
            } else {
                setDate = new Date(setYear - 1900, setMonth, setDay);

                if (sharedPrefs.getBoolean("hour_format", false)) {
                    dateDisplay.setText(DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.GERMAN).format(setDate));
                } else {
                    dateDisplay.setText(DateFormat.getDateInstance(DateFormat.MEDIUM).format(setDate));
                }
            }
            //dateDisplay.setText((month + 1) + "/" + day + "/" + year);
        }

    };

    private DatePickerDialog.OnDateSetListener reservationDate2 = new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker view, int year, int month, int day) {
            setYear = year;
            setMonth = month;
            setDay = day;

            if (setHour != -1 && setMinute != -1) {
                setDate = new Date(setYear - 1900, setMonth, setDay, setHour, setMinute);

                if (sharedPrefs.getBoolean("hour_format", false)) {
                    dateDisplay.setText(DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.GERMAN).format(setDate));
                } else {
                    dateDisplay.setText(DateFormat.getDateInstance(DateFormat.MEDIUM).format(setDate));
                }
            } else {
                setDate = new Date(setYear - 1900, setMonth, setDay);

                if (sharedPrefs.getBoolean("hour_format", false)) {
                    dateDisplay.setText(DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.GERMAN).format(setDate));
                } else {
                    dateDisplay.setText(DateFormat.getDateInstance(DateFormat.MEDIUM).format(setDate));
                }
            }
            //dateDisplay.setText((month + 1) + "/" + day + "/" + year);
        }

    };

    // gets the time text from what is entered in the dialog and displays it
    private com.android.datetimepicker.time.TimePickerDialog.OnTimeSetListener timeDate = new com.android.datetimepicker.time.TimePickerDialog.OnTimeSetListener() {
        public void onTimeSet(RadialPickerLayout view, int hours, int minutes) {
            setHour = hours;
            setMinute = minutes;

            setDate.setHours(setHour);
            setDate.setMinutes(setMinute);

            currentDate.setYear(currentYear - 1900);

            if (!setDate.before(currentDate)) {
                if (sharedPrefs.getBoolean("hour_format", false)) {
                    timeDisplay.setText(DateFormat.getTimeInstance(DateFormat.SHORT, Locale.GERMAN).format(setDate));
                } else {
                    timeDisplay.setText(DateFormat.getTimeInstance(DateFormat.SHORT, Locale.US).format(setDate));
                }

                timeDone = true;
            } else {
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

    private TimePickerDialog.OnTimeSetListener timeDate2 = new TimePickerDialog.OnTimeSetListener() {
        public void onTimeSet(TimePicker view, int hours, int minutes) {
            setHour = hours;
            setMinute = minutes;

            setDate.setHours(setHour);
            setDate.setMinutes(setMinute);

            currentDate.setYear(currentYear - 1900);

            if (!setDate.before(currentDate)) {
                if (sharedPrefs.getBoolean("hour_format", false)) {
                    timeDisplay.setText(DateFormat.getTimeInstance(DateFormat.SHORT, Locale.GERMAN).format(setDate));
                } else {
                    timeDisplay.setText(DateFormat.getTimeInstance(DateFormat.SHORT, Locale.US).format(setDate));
                }

                timeDone = true;
            } else {
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

    // sets the string repetition to whatever is choosen from the spinner
    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        repetition = parent.getItemAtPosition(pos).toString();
    }

    public void onNothingSelected(AdapterView<?> parent) {

    }

    // finishes the activity when the discard button is clicked, without making any changes or saving anything
    public boolean discardClick() {
        finish();
        return true;
    }

    // this is where we will set everything up when the user has entered all the information
    // including the alarm manager and writing the files to the database to save them
    public boolean doneClick() {
        if (!contactSearch.getText().toString().equals("") && !mEditText.getText().toString().equals("") && timeDone) {
            String[] details = new String[4];
            details[0] = contactSearch.getText().toString();
            details[1] = "" + setDate.getTime();
            details[2] = repetition;
            details[3] = mEditText.getText().toString();

            ScheduledDataSource dataSource = new ScheduledDataSource(context);
            dataSource.open();
            ScheduledMessage message = ScheduledMessage.fromOldStringArray(context, details);
            if (!(details[0].equals(startNumber) && details[1].equals(startDate) && details[2].equals(startRepeat) && details[3].equals(startMessage))) {
                for (ScheduledMessage detail : data) {
                    if (detail.address.equals(startNumber) && detail.date == startDate && detail.repetition == startRepeat && detail.body.equals(startMessage)) {
                        dataSource.deleteMessage(detail);
                        break;
                    }
                }

            }
            dataSource.addMessage(message);
            dataSource.close();
            finish();

        } else {
            Context context = getApplicationContext();
            CharSequence text = "Please complete the form!";
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }
        return true;
    }

    @Override
    public void onStop() {
        super.onStop();
        ScheduledService.scheduleNextAlarm(this);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.activity_slide_in_left, R.anim.activity_slide_out_right);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_scheduled, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.insert_template:
                AlertDialog.Builder template = new AlertDialog.Builder(this);
                template.setTitle(context.getResources().getString(R.string.insert_template));

                ListView templates = new ListView(this);

                TextView footer = new TextView(this);
                footer.setText(context.getResources().getString(R.string.add_templates));
                int scale = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, context.getResources().getDisplayMetrics());
                footer.setPadding(scale, scale, scale, scale);

                templates.addFooterView(footer);

                final ArrayList<String> text = IOUtil.readTemplates(this);
                TemplateArrayAdapter adapter = new TemplateArrayAdapter(this, text);
                templates.setAdapter(adapter);

                template.setView(templates);
                final AlertDialog templateDialog = template.create();
                templateDialog.show();

                footer.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        templateDialog.dismiss();
                        Intent i = new Intent(view.getContext(), TemplateActivity.class);
                        startActivity(i);
                        overridePendingTransition(R.anim.activity_slide_in_right, R.anim.activity_slide_out_left);
                    }
                });

                templates.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> arg0, View arg1,
                                            int arg2, long arg3) {
                        mEditText.setText(text.get(arg2));
                        mEditText.setSelection(text.get(arg2).length());
                        templateDialog.cancel();
                    }

                });
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}