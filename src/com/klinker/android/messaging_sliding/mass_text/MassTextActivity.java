package com.klinker.android.messaging_sliding.mass_text;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListPopupWindow;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.*;

import com.klinker.android.messaging_donate.R;
import com.klinker.android.messaging_donate.settings.DrawerArrayAdapter;
import com.klinker.android.messaging_donate.settings.GetHelpSettingsActivity;
import com.klinker.android.messaging_donate.settings.OtherAppsSettingsActivity;
import com.klinker.android.messaging_donate.settings.SettingsPagerActivity;
import com.klinker.android.messaging_donate.utils.ContactUtil;
import com.klinker.android.messaging_donate.utils.IOUtil;
import com.klinker.android.messaging_donate.utils.SendUtil;
import com.klinker.android.messaging_sliding.ContactSearchArrayAdapter;
import com.klinker.android.messaging_sliding.developer_tips.MainActivity;
import com.klinker.android.messaging_sliding.emojis.EmojiAdapter;
import com.klinker.android.messaging_sliding.emojis.EmojiAdapter2;
import com.klinker.android.messaging_sliding.emojis.EmojiConverter;
import com.klinker.android.messaging_sliding.emojis.EmojiConverter2;
import com.klinker.android.messaging_sliding.scheduled.ScheduledSms;
import com.klinker.android.messaging_sliding.templates.TemplateActivity;
import com.klinker.android.messaging_sliding.templates.TemplateArrayAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MassTextActivity extends Activity {

    private Context context;
    private Activity activity;

    private ListPopupWindow lpw;

    private EditText contactSearch;
    private EditText mEditText;
    private CheckBox firstAndLast;
    private Button insertName;

    public SharedPreferences sharedPrefs;

    public boolean firstContactSearch = true;

    public ArrayList<String> contactNames, contactNumbers, contactTypes;
    public ArrayList<String[]> data;

    private String[] linkItems;
    private String[] otherItems;

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private LinearLayout mDrawer;
    private ActionBarDrawerToggle mDrawerToggle;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.personal_mass_sms);

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        context = this;

        mEditText = (EditText) findViewById(R.id.messageEntry2);
        contactSearch = (EditText) findViewById(R.id.contactEntry);
        firstAndLast = (CheckBox) findViewById(R.id.first_and_last);
        insertName = (Button) findViewById(R.id.insert_name);

        insertName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String currText = mEditText.getText().toString();

                mEditText.setText(currText + " [$NAME] ");
                mEditText.setSelection(mEditText.getText().toString().length());
                mEditText.requestFocus();
            }
        });

        if (!sharedPrefs.getBoolean("keyboard_type", true)) {
            mEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
            mEditText.setImeOptions(EditorInfo.IME_ACTION_NONE);
        }

        lpw = new ListPopupWindow(MassTextActivity.this);

        lpw.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1,
                                    int position, long arg3) {
                TextView view2 = (TextView) arg1.findViewById(R.id.receivedMessage);
                final TextView type = (TextView) arg1.findViewById(R.id.receivedDate);

                String[] t1 = contactSearch.getText().toString().split("; ");
                String string = "";

                for (int i = 0; i < t1.length - 1; i++) {
                    string += t1[i] + "; ";
                }

                if (!type.getText().toString().startsWith(getString(R.string.group))) {
                    contactSearch.setText(string + view2.getText() + "; ");
                } else {
                    final ProgressDialog dialog = new ProgressDialog(context);
                    dialog.setMessage(getString(R.string.getting_contacts));
                    dialog.show();

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            String contacts = "";
                            Uri groupURI = ContactsContract.Data.CONTENT_URI;

                            String[] projection = new String[]{
                                    ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID ,
                                    ContactsContract.CommonDataKinds.GroupMembership.CONTACT_ID,
                                    ContactsContract.Data.HAS_PHONE_NUMBER,
                                    ContactsContract.Data._ID,
                                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME};

                            Cursor c = context.getContentResolver().query(groupURI,
                                    projection,
                                    ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID + "=" + type.getText().toString().replaceAll("[^0-9\\+]", "")
                                            + " and " + ContactsContract.Data.HAS_PHONE_NUMBER + "=1",
                                    null, null);

                            if (c.moveToFirst()) {
                                do {
                                    Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
                                    String[] proj = new String[]{ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.Data._ID, ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                                            ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.LABEL};

                                    Cursor people = getContentResolver().query(uri, proj, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.GroupMembership.CONTACT_ID)), null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " asc");

                                    if (people.moveToFirst()) {
                                        do {
                                            int type = people.getInt(people.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
                                            int indexNumber = people.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);

                                            if (sharedPrefs.getBoolean("mobile_only", false)) {
                                                if (type == 2) {
                                                    contacts += people.getString(indexNumber).replaceAll("[^0-9\\+]", "") + "; ";
                                                }
                                            } else {
                                                contacts += people.getString(indexNumber).replaceAll("[^0-9\\+]", "") + "; ";
                                            }
                                        } while (people.moveToNext());
                                    }

                                    people.close();
                                } while (c.moveToNext());
                            }

                            c.close();

                            final String text = contacts;

                            ((Activity)context).findViewById(android.R.id.content).post(new Runnable() {
                                @Override
                                public void run() {
                                    dialog.dismiss();
                                    contactSearch.setText(text);
                                }
                            });
                        }
                    }).start();
                }
                contactSearch.setSelection(contactSearch.getText().length());
                lpw.dismiss();
                firstContactSearch = true;
            }

        });

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

                        String[] group_projection = new String[] {
                                ContactsContract.Groups._ID, ContactsContract.Groups.TITLE,
                                ContactsContract.Groups.SUMMARY_COUNT, ContactsContract.Groups.SUMMARY_WITH_PHONES };
                        people = context.getContentResolver().query(
                                ContactsContract.Groups.CONTENT_SUMMARY_URI,
                                group_projection,
                                ContactsContract.Groups.SUMMARY_WITH_PHONES + " > 0"
                                , null, ContactsContract.Groups.TITLE + " ASC");

                        if (people.moveToFirst()) {
                            do {
                                contactNames.add(people.getString(people.getColumnIndex(ContactsContract.Groups.TITLE)));
                                contactNumbers.add(people.getString(people.getColumnIndex(ContactsContract.Groups.SUMMARY_WITH_PHONES)) + " " + getString(R.string.people));
                                contactTypes.add(getString(R.string.group) + people.getLong(people.getColumnIndex(ContactsContract.Groups._ID)) + ")");
                            } while (people.moveToNext());
                        }

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

        linkItems = new String[]{getResources().getString(R.string.theme_settings),
                getResources().getString(R.string.notification_settings),
                getResources().getString(R.string.popup_settings),
                getResources().getString(R.string.slideover_settings),
                getResources().getString(R.string.text_settings),
                getResources().getString(R.string.conversation_settings),
                getResources().getString(R.string.mms_settings),
                getResources().getString(R.string.google_voice_settings),
                getResources().getString(R.string.security_settings),
                getResources().getString(R.string.advanced_settings)};

        otherItems = new String[]{getResources().getString(R.string.quick_templates),
                getResources().getString(R.string.scheduled_sms),
                getResources().getString(R.string.mass_sms),
                getResources().getString(R.string.get_help),
                getResources().getString(R.string.other_apps),
                getResources().getString(R.string.rate_it)};

        DrawerArrayAdapter.current = 2;
        SettingsPagerActivity.settingsLinksActive = false;
        SettingsPagerActivity.inOtherLinks = true;

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.links_list);
        mDrawer = (LinearLayout) findViewById(R.id.drawer);

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

        Spinner spinner = (Spinner) findViewById(R.id.spinner);

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.drawer_spinner_array, R.layout.drawer_spinner_item);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        spinner.setSelection(1);

        spinner.setOnItemSelectedListener(new SpinnerClickListener());

        // Set the adapter for the list view
        mDrawerList.setAdapter(new DrawerArrayAdapter(this,
                new ArrayList<String>(Arrays.asList(otherItems))));
        // Set the list's click listener
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        // Show the custom action bar view and hide the normal Home icon and title.
        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayOptions(
                ActionBar.DISPLAY_SHOW_CUSTOM,
                ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME
                        | ActionBar.DISPLAY_SHOW_TITLE);
        actionBar.setCustomView(customActionBarView, new ActionBar.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer icon to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description */
                R.string.drawer_close  /* "close drawer" description */
        );

        mDrawerLayout.setDrawerListener(mDrawerToggle);

        //getActionBar().setDisplayHomeAsUpEnabled(true);
        //getActionBar().setHomeButtonEnabled(true);

        activity = this;
    }

    // finishes the activity when the discard button is clicked, without making any changes or saving anything
    public boolean discardClick() {
        Intent i = new Intent(this, com.klinker.android.messaging_donate.MainActivity.class);
        startActivity(i);
        finish();
        overridePendingTransition(R.anim.activity_slide_in_left, R.anim.activity_slide_out_right);
        return true;
    }

    // this is where we will set everything up when the user has entered all the information
    // including the alarm manager and writing the files to the database to save them
    public boolean doneClick() {
        if (!contactSearch.getText().toString().equals("") && !mEditText.getText().toString().equals("")) {

            String[] names = contactSearch.getText().toString().split(" ");

            for (int i = 0; i < names.length; i++) {
                names[i] = names[i].replaceAll(";", "");
                names[i] = names[i].replaceAll("-", "");
                names[i] = names[i].replaceAll(" ", "");
            }

            String[] message = mEditText.getText().toString().split(" ");

            String[] messages = new String[names.length];

            for(int j = 0; j < names.length; j++) {
                messages[j] = "";
                for (int x = 0; x < message.length; x++) {
                    if (message[x].equals("[$NAME]")) {
                        if (firstAndLast.isChecked()) {
                            messages[j] += ContactUtil.findContactName(names[j], context) + " ";
                        } else {
                            messages[j] += ContactUtil.findContactName(names[j], context).split(" ")[0] + " ";
                        }
                    } else {
                        messages[j] += message[x] + " ";
                    }
                }

                Log.v("personal_sms", messages[j]);
            }

            for (int m = 0; m < messages.length; m++) {
                SendUtil.sendMessage(context, names[m], messages[m]);
            }

            Context context = getApplicationContext();
            CharSequence text = getResources().getString(R.string.send_success);
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();

            discardClick();

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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_scheduled, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (mDrawerToggle.onOptionsItemSelected(item)) {
                    return true;
                }
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

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {

            // TODO: Make this smoother
            // TODO: Add the other settings options for not switching viewpager
            final Context context = getApplicationContext();
            Intent intent;

            final int mPos = position;

            if (SettingsPagerActivity.settingsLinksActive) {
                mDrawerLayout.closeDrawer(mDrawer);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //onBackPressed();

                        Intent mIntent = new Intent(context, SettingsPagerActivity.class);
                        mIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        mIntent.putExtra("page_number", mPos);
                        startActivity(mIntent);
                        overridePendingTransition(0, 0);
                    }
                }, 200);
            } else {
                mDrawerLayout.closeDrawer(mDrawer);

                switch (position) {
                    case 0:
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                onBackPressed();

                                Intent mIntent = new Intent(context, TemplateActivity.class);
                                mIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(mIntent);
                                overridePendingTransition(0, 0);
                                //overridePendingTransition(R.anim.activity_slide_in_right, R.anim.activity_slide_out_left);
                            }
                        }, 200);
                        break;

                    case 1:

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Intent mIntent = new Intent(context, ScheduledSms.class);
                                mIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(mIntent);
                                overridePendingTransition(0, 0);
                                //overridePendingTransition(R.anim.activity_slide_in_right, R.anim.activity_slide_out_left);
                            }
                        }, 200);
                        break;

                    case 2:
                        /*new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Intent mIntent = new Intent(context, MassTextActivity.class);
                                mIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(mIntent);
                                //overridePendingTransition(R.anim.activity_slide_in_right, R.anim.activity_slide_out_left);
                                overridePendingTransition(0, 0);
                            }
                        }, 200);*/

                        break;

                    case 3:
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Intent mIntent = new Intent(context, GetHelpSettingsActivity.class);
                                mIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(mIntent);
                                overridePendingTransition(0, 0);
                                //overridePendingTransition(R.anim.activity_slide_in_right, R.anim.activity_slide_out_left);
                            }
                        }, 200);

                        break;

                    case 4:
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Intent mIntent = new Intent(context, OtherAppsSettingsActivity.class);
                                mIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(mIntent);
                                overridePendingTransition(0,0);
                                //overridePendingTransition(R.anim.activity_slide_in_right, R.anim.activity_slide_out_left);
                            }
                        }, 200);

                        break;

                    case 5:
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Uri uri = Uri.parse("market://details?id=" + context.getPackageName());
                                Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);

                                try {
                                    startActivity(goToMarket);
                                } catch (ActivityNotFoundException e) {
                                    Toast.makeText(context, "Couldn't launch the market", Toast.LENGTH_SHORT).show();
                                }
                                overridePendingTransition(R.anim.activity_slide_in_right, R.anim.activity_slide_out_left);
                            }
                        }, 200);

                        //overridePendingTransition(R.anim.activity_slide_in_right, R.anim.activity_slide_out_left);
                        //mDrawerLayout.closeDrawer(mDrawer);
                        break;
                }
            }
        }
    }

    private class SpinnerClickListener implements Spinner.OnItemSelectedListener {
        @Override
        // sets the string repetition to whatever is choosen from the spinner
        public void onItemSelected(AdapterView<?> parent, View view,
                                   int pos, long id) {
            // An item was selected. You can retrieve the selected item using
            // parent.getItemAtPosition(pos)
            String selected = parent.getItemAtPosition(pos).toString();

            if (selected.equals(getResources().getStringArray(R.array.drawer_spinner_array)[0])) {
                mDrawerList.setAdapter(new DrawerArrayAdapter(activity,
                        new ArrayList<String>(Arrays.asList(linkItems))));
                mDrawerList.invalidate();
                SettingsPagerActivity.settingsLinksActive = true;
            } else {
                mDrawerList.setAdapter(new DrawerArrayAdapter(activity,
                        new ArrayList<String>(Arrays.asList(otherItems))));
                mDrawerList.invalidate();
                SettingsPagerActivity.settingsLinksActive = false;
            }


        }

        public void onNothingSelected(AdapterView<?> parent) {

        }
    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent(this, com.klinker.android.messaging_donate.MainActivity.class);
        startActivity(i);
        finish();
        overridePendingTransition(R.anim.activity_slide_in_left, R.anim.activity_slide_out_right);
    }

}