package com.klinker.android.messaging_donate;

import android.annotation.SuppressLint;
import android.app.*;
import android.content.*;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.*;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.*;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Profile;
import android.provider.MediaStore;
import android.support.v4.app.*;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ListFragment;
import android.support.v4.view.PagerTitleStrip;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.ViewDragHelper;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsMessage;
import android.text.Editable;
import android.text.InputType;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.*;
import android.view.View.OnClickListener;
import android.view.animation.*;
import android.view.animation.Animation.AnimationListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.RelativeLayout.LayoutParams;
import com.android.mms.ui.ImageAttachmentView;
import com.astuetz.viewpager.extensions.PagerSlidingTabStrip;
import com.devspark.appmsg.AppMsg;
import com.google.android.mms.MMSPart;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.klinker.android.messaging_donate.receivers.UnlockReceiver;
import com.klinker.android.messaging_donate.settings.AppSettings;
import com.klinker.android.messaging_donate.settings.SettingsPagerActivity;
import com.klinker.android.messaging_donate.utils.ContactUtil;
import com.klinker.android.messaging_donate.utils.IOUtil;
import com.klinker.android.messaging_donate.utils.SendUtil;
import com.klinker.android.messaging_donate.wizardpager.ChangeLogMain;
import com.klinker.android.messaging_donate.wizardpager.InitialSetupMain;
import com.klinker.android.messaging_sliding.*;
import com.klinker.android.messaging_sliding.batch_delete.BatchDeleteAllActivity;
import com.klinker.android.messaging_sliding.batch_delete.BatchDeleteConversationActivity;
import com.klinker.android.messaging_sliding.emoji_pager.KeyboardFragment;
import com.klinker.android.messaging_sliding.emoji_pager.sqlite.EmojiDataSource;
import com.klinker.android.messaging_sliding.emoji_pager.sqlite.Recent;
import com.klinker.android.messaging_sliding.emojis.EmojiAdapter;
import com.klinker.android.messaging_sliding.emojis.EmojiAdapter2;
import com.klinker.android.messaging_sliding.emojis.EmojiConverter;
import com.klinker.android.messaging_sliding.emojis.EmojiConverter2;
import com.klinker.android.messaging_sliding.quick_reply.QmMarkRead;
import com.klinker.android.messaging_sliding.receivers.CacheService;
import com.klinker.android.messaging_sliding.receivers.NotificationRepeaterService;
import com.klinker.android.messaging_sliding.receivers.QuickTextService;
import com.klinker.android.messaging_sliding.receivers.VoiceReceiver;
import com.klinker.android.messaging_sliding.scheduled.NewScheduledSms;
import com.klinker.android.messaging_sliding.search.SearchActivity;
import com.klinker.android.messaging_sliding.security.PasswordActivity;
import com.klinker.android.messaging_sliding.security.PinActivity;
import com.klinker.android.messaging_sliding.slide_over.SlideOverService;
import com.klinker.android.messaging_sliding.templates.TemplateActivity;
import com.klinker.android.messaging_sliding.templates.TemplateArrayAdapter;
import com.klinker.android.messaging_sliding.views.ConversationFragment;
import com.klinker.android.messaging_sliding.views.ProgressAnimator;
import com.klinker.android.send_message.Message;
import com.klinker.android.send_message.Settings;
import com.klinker.android.send_message.StripAccents;
import com.klinker.android.send_message.Transaction;
import com.tonicartos.widget.stickygridheaders.StickyGridHeadersGridView;
import group.pals.android.lib.ui.lockpattern.LockPatternActivity;
import group.pals.android.lib.ui.lockpattern.prefs.SecurityPrefs;
import net.simonvt.messagebar.MessageBar;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher;

import java.io.File;
import java.lang.reflect.Field;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends FragmentActivity {

    public final static String EXTRA_NUMBER = "com.klinker.android.messaging_sliding.NUMBER";
    public final static String EXTRA_DATE = "com.klinker.android.messaging_sliding.DATE";
    public final static String EXTRA_REPEAT = "com.klinker.android.messaging_sliding.REPEAT";
    public final static String EXTRA_MESSAGE = "com.klinker.android.messaging_sliding.MESSAGE";
    private static final int SETTINGS_RESULT = 51324;

    protected static Context context;
    private ActionBar ab;
    protected Resources resources;

    public static DrawerLayout mDrawerLayout;
    public LinearLayout mDrawer;
    private ActionBarDrawerToggle mDrawerToggle;

    public static View newMessageView;

    private boolean unlocked = false;

    public static ViewPager mViewPager;
    public static SectionsPagerAdapter mSectionsPagerAdapter;
    public static Map<Integer, MessageCursorAdapter> cursorAdapters;

    public static String deviceType;
    public static boolean newMessage;

    public static Settings sendSettings;
    private Transaction sendTransaction;

    public static ArrayList<Long> threadsThroughVoice;
    public ArrayList<String> newMessages;

    public static boolean waitToLoad = false;
    public static boolean threadedLoad = true;
    public static boolean notChanged = true;

    public static String myPhoneNumber, myContactId;

    public static ArrayList<Conversation> conversations;
    private ArrayList<String> contactNames, contactNumbers, contactTypes;

    public static SlidingMenu menu;
    private MessageBar messageBar;
    public boolean firstRun = true;
    private boolean firstContactSearch = true;
    private boolean refreshMyContact = true;

    public static boolean animationOn = false;
    public static int animationReceived = 0;
    public static int animationThread = 0;

    private ProgressAnimator mmsProgressAnimation;

    public static int contactWidth;
    private boolean jump = true;

    private ArrayList<String> drafts;
    private ArrayList<Long> draftNames;
    private ArrayList<Boolean> draftChanged;
    private ArrayList<Long> draftsToDelete;
    private boolean fromDraft = false;
    private long newDraft = 0;
    private boolean deleteDraft = true;

    private ListView menuLayout;
    private MenuArrayAdapter menuAdapter;
    public static boolean messageRecieved = false;
    public static boolean sentMessage = false;
    public static boolean loadAll = false;
    public static int numToLoad = 20;

    private boolean sendTo = false;
    private String sendMessageTo;
    private String whatToSend = null;
    private boolean fromNotification = false;
    private String sendToThread = null;
    private String sendToMessage;

    public static EditText messageEntry;
    public static EditText messageEntry2;
    private TextView mTextView;
    private ImageButton sendButton;
    private ImageButton emojiButton;
    public ImageButton voiceButton;
    private ImageButton voiceButton2;
    private View v;
    protected PagerTitleStrip title;
    private View imageAttachBackground;
    private ImageAttachmentView imageAttach;
    public View imageAttachBackground2;
    public ImageAttachmentView imageAttach2;
    public Uri attachedImage;
    public Uri attachedImage2;
    private int attachedPosition;
    private ProgressBar mmsProgress;
    private View subjectLine;
    private EditText subjectEntry;
    private View subjectLine2;
    private EditText subjectEntry2;
    private ImageView subjectDelete;
    private ImageView subjectDelete2;

    private Uri capturedPhotoUri;
    private boolean fromCamera = false;
    private boolean multipleAttachments = false;

    private Typeface font;

    private PullToRefreshAttacher mPullToRefreshAttacher;
    public static int pullToRefreshPosition = -1;

    private AppMsg appMsg = null;
    private int appMsgConversations = 0;
    private boolean dismissCrouton = true;
    private boolean dismissNotification = true;

    protected boolean isPopup = false;
    protected boolean attachOnSend = false;
    protected boolean popupAttaching = false;
    protected boolean unlockDevice = false;

    public static boolean limitConversations = true;

    private static final int REQ_ENTER_PATTERN = 7;

    private boolean emojiOpen = false;
    private boolean emoji2Open = false;
    private ViewPager vp;
    private PagerSlidingTabStrip tabs;
    private LinearLayout messageScreen;
    private LinearLayout messageScreen2;
    private LayoutParams SlidingTabParams;
    private LayoutParams viewPagerParams;

    private int currentVoiceTutorial = 0;

    private boolean haloPopup = false;

    public static AppSettings settings;
    protected SharedPreferences sharedPrefs;

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = this;
        resources = getResources();

        if (getPackageName().equals("com.klinker.android.messaging_donate")) {
            unlocked = true;
        } else {
            unlocked = IOUtil.checkUnlocked();
        }

        settings = AppSettings.init(this);
        initialSetup();

        if (settings.voiceAccount != null) {
            String[] threads = settings.voiceThreads.split("-");
            threadsThroughVoice = new ArrayList<Long>();

            for (int i = 0; i < threads.length; i++) {
                try {
                    threadsThroughVoice.add(Long.parseLong(threads[i]));
                } catch (Exception e) {
                }
            }
        }

        setUpWindow();
        setUpSendSettings();

        if (settings.emojiKeyboard && settings.emoji) {
            vp = new ViewPager(this);
            tabs = new PagerSlidingTabStrip(this);
            vp.setBackgroundColor(resources.getColor(R.color.light_silver));
            tabs.setBackgroundColor(resources.getColor(R.color.light_silver));

            vp.setId(555555);

            Display d = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
            int keyboardHeight = (int) (d.getHeight() / 3.0);

            SlidingTabParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, resources.getDisplayMetrics()));
            viewPagerParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, keyboardHeight);

            tabs.setIndicatorColor(settings.titleBarColor);

            emojiAdapter = new MyPagerAdapter(getSupportFragmentManager());
            vp.setAdapter(emojiAdapter);

            tabs.setViewPager(vp);

            vp.setCurrentItem(1);

            messageScreen = (LinearLayout) findViewById(R.id.messageScreen);
            messageScreen2 = (LinearLayout) findViewById(R.id.messageScreen2);
        }

        Intent fromIntent = getIntent();
        fromIntent.getFlags();

        if (fromIntent.getBooleanExtra("halo_popup", false))
            haloPopup = true;

        if (fromIntent.getBooleanExtra("initial_run", false)) {
            try { // try catch so if they change to landscape, which uses a linear layout instead, everything won't force close
                final WindowManager.LayoutParams arcParams = new WindowManager.LayoutParams(
                        WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                                | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                        PixelFormat.TRANSLUCENT);

                final WindowManager arcWindow = (WindowManager) getSystemService(WINDOW_SERVICE);

                final View conversationSwipe = getLayoutInflater().inflate(R.layout.conversation_swipe, null);
                final View newMessageSwipe = getLayoutInflater().inflate(R.layout.new_message_swipe, null);
                final View messagesSwipe = getLayoutInflater().inflate(R.layout.messages_swipe, null);

                arcWindow.addView(conversationSwipe, arcParams);

                conversationSwipe.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent event) {
                        arcWindow.removeViewImmediate(conversationSwipe);
                        arcWindow.addView(newMessageSwipe, arcParams);
                        return false;
                    }
                });

                newMessageSwipe.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent event) {
                        arcWindow.removeViewImmediate(newMessageSwipe);
                        arcWindow.addView(messagesSwipe, arcParams);
                        return false;
                    }
                });

                messagesSwipe.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent event) {
                        arcWindow.removeViewImmediate(messagesSwipe);
                        return false;
                    }
                });

            } catch (ClassCastException e) {

            }
        }

        mPullToRefreshAttacher = new PullToRefreshAttacher(this, settings.lightActionBar, true);
        appMsg = AppMsg.makeText(this, "", AppMsg.STYLE_ALERT);

        if (settings.limitConversationsAtStart) {
            limitConversations = true;
        } else {
            limitConversations = false;
        }

        MainActivity.notChanged = true;
        setUpIntentStuff();

        if (settings.customFont) {
            try {
                font = Typeface.createFromFile(settings.customFontPath);
            } catch (Throwable e) {
                Editor edit = PreferenceManager.getDefaultSharedPreferences(this).edit();
                edit.putBoolean("custom_font", false);
                edit.commit();
                settings.customFont = false;
            }
        }

        if (settings.quickText) {
            Intent mIntent = new Intent(this, QuickTextService.class);
            this.startService(mIntent);
        } else {
            NotificationManager mNotificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.cancel(3);
        }

        if (settings.overrideLang) {
            String languageToLoad = "en";
            Locale locale = new Locale(languageToLoad);
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            resources.updateConfiguration(config, resources.getDisplayMetrics());
        }

        setUpTitleBar();

        menuLayout = new ListView(this);
        myPhoneNumber = ContactUtil.getMyPhoneNumber(this);

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        final Activity activity = this;

        mmsProgressReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int progress = intent.getIntExtra("progress", 0);

                if (progress == -1) {
                    mmsProgress.setVisibility(View.VISIBLE);
                    mmsProgress.setProgress(0);

                    try {
                        if (!mmsProgressAnimation.alreadyRunning) {
                            mmsProgressAnimation = new ProgressAnimator();
                            mmsProgressAnimation.setContext(activity);
                            mmsProgressAnimation.setCurrentProgress(0);
                            mmsProgressAnimation.setMmsProgress(mmsProgress);
                            mmsProgressAnimation.start();
                            mmsProgressAnimation.alreadyRunning = true;
                        }
                    } catch (Exception e) {
                        // animation already started
                    }
                } else if (progress == 100) {
                    mmsProgressAnimation.alreadyRunning = false;
                    mmsProgressAnimation.setMaxProgress(100);

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mmsProgress.setVisibility(View.GONE);
                        }
                    }, 250);
                } else {
                    mmsProgressAnimation.setMaxProgress(progress);
                }
            }
        };

        mmsProgressAnimation = new ProgressAnimator();
        mmsProgressAnimation.setContext(activity);
        mmsProgressAnimation.setCurrentProgress(0);

        final float scale = resources.getDisplayMetrics().density;
        MainActivity.contactWidth = (int) (64 * scale + 0.5f);

        try {
            ab.setDisplayHomeAsUpEnabled(true);

            if (!settings.lightActionBar) {
                ab.setBackgroundDrawable(new ColorDrawable(resources.getColor(R.color.black)));

                if (settings.ctConversationListBackground == resources.getColor(R.color.pitch_black)) {
                    if (!settings.useTitleBar) {
                        ab.setBackgroundDrawable(resources.getDrawable(R.drawable.pitch_black_action_bar_blue));
                    } else {
                        ab.setBackgroundDrawable(new ColorDrawable(resources.getColor(R.color.pitch_black)));
                    }
                }
            } else {
                ab.setBackgroundDrawable(resources.getDrawable(R.drawable.ab_hangouts));
            }
        } catch (Exception e) {
            // no action bar, dialog theme
        }

        View v = findViewById(R.id.newMessageGlow);
        v.setVisibility(View.GONE);

        setUpSendbar();

        if (!unlocked) {
            showUnlockFullDialog();
        }

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawer = (LinearLayout) findViewById(R.id.drawer);

        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, Gravity.RIGHT);
        setUpDrawer();

        final String menuOption = sharedPrefs.getString("page_or_menu2", "2");

        if (menuOption.equals("1")) {
            try {
                Field mDragger = mDrawerLayout.getClass().getDeclaredField("mRightDragger");//mRightDragger for right obviously
                mDragger.setAccessible(true);
                ViewDragHelper draggerObj = (ViewDragHelper) mDragger.get(mDrawerLayout);

                Field mEdgeSize = draggerObj.getClass().getDeclaredField(
                        "mEdgeSize");
                mEdgeSize.setAccessible(true);
                int edge = mEdgeSize.getInt(draggerObj);

                Display display = getWindowManager().getDefaultDisplay();
                int width = display.getWidth();  // deprecated

                mEdgeSize.setInt(draggerObj, edge * 5);
            } catch (Exception e) {
                // couldn't get the correct drawer i guess
            }
        }

        final String newMessage = resources.getString(R.string.new_message);
        final String messaging = resources.getString(R.string.app_name_in_app);

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close) {

            public void onDrawerClosed(View view) {
                try {
                    if (!settings.useTitleBar || settings.alwaysShowContactInfo || settings.titleContactImages) {
                        if (ab != null) {
                            try {
                                new Thread(new Runnable() {

                                    @Override
                                    public void run() {
                                        if (conversations.size() != 0) {
                                            final String title = ContactUtil.findContactName(ContactUtil.findContactNumber(conversations.get(mViewPager.getCurrentItem()).getNumber(), context), context);
                                            int scale = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, resources.getDisplayMetrics());
                                            Bitmap image = Bitmap.createScaledBitmap(ContactUtil.getFacebookPhoto(ContactUtil.findContactNumber(conversations.get(mViewPager.getCurrentItem()).getNumber(), context), context), scale, scale, true);
                                            final BitmapDrawable image2 = new BitmapDrawable(image);

                                            ((MainActivity) context).getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {

                                                @Override
                                                public void run() {
                                                    if (!settings.useTitleBar || settings.alwaysShowContactInfo) {
                                                        ab.setTitle(title);

                                                        Locale sCachedLocale = Locale.getDefault();
                                                        int sFormatType = PhoneNumberUtils.getFormatTypeForLocale(sCachedLocale);
                                                        Editable editable = new SpannableStringBuilder(ContactUtil.findContactNumber(conversations.get(mViewPager.getCurrentItem()).getNumber(), context));
                                                        PhoneNumberUtils.formatNumber(editable, sFormatType);
                                                        ab.setSubtitle(editable.toString());

                                                        if (ab.getTitle().equals(ab.getSubtitle())) {
                                                            ab.setSubtitle(null);
                                                        }

                                                        if (conversations.get(mViewPager.getCurrentItem()).getGroup()) {
                                                            ab.setTitle("Group MMS");
                                                            ab.setSubtitle(null);
                                                        }
                                                    } else {
                                                        ab.setTitle(getString(R.string.app_name_in_app));
                                                    }

                                                    if (settings.titleContactImages) {
                                                        ab.setIcon(image2);
                                                    }
                                                }

                                            });
                                        }
                                    }

                                }).start();
                            } catch (Exception e) {
                                ab.setTitle(R.string.app_name_in_app);
                                ab.setIcon(R.drawable.ic_launcher);
                            }
                        }

                        if (conversations.size() == 0 && ab != null) {
                            ab.setIcon(R.drawable.ic_launcher);
                        }
                    } else {
                        ab.setTitle(R.string.app_name_in_app);
                    }

                    ab.setDisplayHomeAsUpEnabled(true);
                } catch (Exception e) {
                    // no action bar, dialog theme
                }

                invalidateOptionsMenu();
                messageEntry.requestFocusFromTouch();

                if (emoji2Open) {
                    messageScreen2.removeView(tabs);
                    messageScreen2.removeView(vp);
                }

                mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);

                if (menuOption.equals("1") && menu != null) {
                    menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
                }
            }

            public void onDrawerOpened(View drawerView) {
                try {
                    ab.setTitle(newMessage);
                    ab.setSubtitle(null);
                    ab.setIcon(R.drawable.ic_launcher);
                } catch (Exception e) {
                }

                invalidateOptionsMenu();

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        EditText contactEntry = (EditText) newMessageView.findViewById(R.id.contactEntry);
                        contactEntry.requestFocusFromTouch();
                        InputMethodManager keyboard = (InputMethodManager)
                                getSystemService(Context.INPUT_METHOD_SERVICE);
                        keyboard.showSoftInput(contactEntry, 0);
                    }
                }, 350);


                if (emojiOpen) {
                    messageScreen.removeView(tabs);
                    messageScreen.removeView(vp);
                }

                mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);

                if (menuOption.equals("1") && menu != null) {
                    menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
                }
            }
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    private void setUpDrawer() {
        LayoutInflater inflater2 = (LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        newMessageView = inflater2.inflate(R.layout.new_message_frame, (ViewGroup) this.getWindow().getDecorView(), false);

        final TextView mTextView = (TextView) newMessageView.findViewById(R.id.charsRemaining2);
        final EditText mEditText = (EditText) newMessageView.findViewById(R.id.messageEntry2);
        messageEntry2 = mEditText;
        final ImageButton sendButton = (ImageButton) newMessageView.findViewById(R.id.sendButton);
        imageAttachBackground2 = newMessageView.findViewById(R.id.image_attachment_view_background);
        imageAttach2 = (ImageAttachmentView) newMessageView.findViewById(R.id.image_attachment_view);
        ImageButton contactLister = (ImageButton) newMessageView.findViewById(R.id.contactLister);
        subjectLine2 = newMessageView.findViewById(R.id.subjectBar);
        subjectEntry2 = (EditText) newMessageView.findViewById(R.id.subjectEntry);
        subjectDelete2 = (ImageButton) newMessageView.findViewById(R.id.subjectDelete);

        mDrawer.addView(newMessageView);

        mTextView.setVisibility(View.GONE);
        mEditText.requestFocus();

        mEditText.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (mEditText.getError() != null) {
                    mEditText.setError(null);
                }

                if (!settings.signature.equals("")) {
                    s = s + "\n" + settings.signature;
                }

                int[] data = SmsMessage.calculateLength(s, false);
                mTextView.setText(data[0] + "/" + data[2]);

                if ((data[0] < 2 && data[2] > 30) ||
                        (imageAttach2.getVisibility() == View.VISIBLE) ||
                        (data[0] > settings.mmsAfter && settings.sendAsMMS)) {
                    mTextView.setVisibility(View.INVISIBLE);
                } else {
                    mTextView.setVisibility(View.VISIBLE);
                }

                if (settings.sendWithReturn) {
                    if (mEditText.getText().toString().endsWith("\n")) {
                        mEditText.setText(mEditText.getText().toString().substring(0, mEditText.getText().toString().length() - 1));
                        sendButton.performClick();
                    }
                }
            }

            public void afterTextChanged(Editable s) {
            }
        });

        if (!settings.keyboardType) {
            mEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
            mEditText.setImeOptions(EditorInfo.IME_ACTION_NONE);
        }

        final EditText contact = (EditText) newMessageView.findViewById(R.id.contactEntry);

        final ListPopupWindow lpw = new ListPopupWindow(this);
        lpw.setBackgroundDrawable(new ColorDrawable(settings.ctConversationListBackground));

        lpw.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                final ArrayList<String> currentNames = new ArrayList<String>(), currentNumbers = new ArrayList<String>(), currentTypes = new ArrayList<String>();

                String[] numbers = contact.getText().toString().split("; ");

                for (int i = 0; i < numbers.length; i++) {
                    currentNumbers.add(numbers[i]);
                    currentTypes.add("");
                    currentNames.add(ContactUtil.findContactName(numbers[i], context));
                }

                getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {

                    @Override
                    public void run() {
                        ListView current = (ListView) newMessageView.findViewById(R.id.contactSearch);

                        if (!currentNames.get(0).equals("No Information")) {
                            current.setAdapter(new ContactSearchArrayAdapter((Activity) context, currentNames, currentNumbers, currentTypes));
                        } else {
                            current.setAdapter(new ContactSearchArrayAdapter((Activity) context, new ArrayList<String>(), new ArrayList<String>(), new ArrayList<String>()));
                        }
                    }
                });
            }
        });

        lpw.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1,
                                    int position, long arg3) {

                TextView view2 = (TextView) arg1.findViewById(R.id.receivedMessage);

                String[] t1 = contact.getText().toString().split("; ");
                String string = "";

                for (int i = 0; i < t1.length - 1; i++) {
                    string += t1[i] + "; ";
                }

                contact.setText(string + view2.getText() + "; ");
                contact.setSelection(contact.getText().length());
                lpw.dismiss();
                firstContactSearch = true;

                if (contact.getText().length() <= 13) {
                    mEditText.requestFocus();
                }
            }

        });

        contactLister.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                contactNames = new ArrayList<String>();
                contactNumbers = new ArrayList<String>();
                contactTypes = new ArrayList<String>();

                Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
                String[] projection = new String[]{ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                        ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.LABEL};

                Cursor people = getContentResolver().query(uri, projection, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " asc");
                int indexName = people.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                int indexNumber = people.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);

                if (people.moveToFirst()) {
                    do {
                        int type = people.getInt(people.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
                        String customLabel = people.getString(people.getColumnIndex(ContactsContract.CommonDataKinds.Phone.LABEL));

                        if (settings.mobileOnly) {
                            if (type == 2) {
                                contactNames.add(people.getString(indexName));
                                contactNumbers.add(people.getString(indexNumber).replaceAll("[^0-9\\+]", ""));
                                contactTypes.add(ContactsContract.CommonDataKinds.Phone.getTypeLabel(resources, type, customLabel).toString());
                            }
                        } else {
                            contactNames.add(people.getString(indexName));
                            contactNumbers.add(people.getString(indexNumber).replaceAll("[^0-9\\+]", ""));
                            contactTypes.add(ContactsContract.CommonDataKinds.Phone.getTypeLabel(resources, type, customLabel).toString());
                        }
                    } while (people.moveToNext());
                }

                people.close();

                Display display = getWindowManager().getDefaultDisplay();
                Point size = new Point();
                display.getSize(size);
                final int height = size.y;

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            lpw.setAdapter(new ContactSearchArrayAdapter((Activity) context, contactNames, contactNumbers, contactTypes));
                            lpw.setAnchorView(contact);
                            lpw.setWidth(ListPopupWindow.WRAP_CONTENT);
                            lpw.setHeight(height / 3);
                            lpw.show();
                        } catch (Exception e) {
                            // window is null
                        }
                    }
                }, 500);
            }
        });

        contact.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (firstContactSearch) {
                    try {
                        contactNames = new ArrayList<String>();
                        contactNumbers = new ArrayList<String>();
                        contactTypes = new ArrayList<String>();

                        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
                        String[] projection = new String[]{ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                                ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.LABEL};

                        Cursor people = getContentResolver().query(uri, projection, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " asc");

                        int indexName = people.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                        int indexNumber = people.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);

                        if (people.moveToFirst()) {
                            do {
                                int type = people.getInt(people.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
                                String customLabel = people.getString(people.getColumnIndex(ContactsContract.CommonDataKinds.Phone.LABEL));

                                try {
                                    if (settings.mobileOnly) {
                                        if (type == 2) {
                                            contactNames.add(people.getString(indexName));
                                            contactNumbers.add(people.getString(indexNumber).replaceAll("[^0-9\\+]", ""));
                                            contactTypes.add(ContactsContract.CommonDataKinds.Phone.getTypeLabel(resources, type, customLabel).toString());
                                        }
                                    } else {
                                        contactNames.add(people.getString(indexName));
                                        contactNumbers.add(people.getString(indexNumber).replaceAll("[^0-9\\+]", ""));
                                        contactTypes.add(ContactsContract.CommonDataKinds.Phone.getTypeLabel(resources, type, customLabel).toString());
                                    }
                                } catch (Exception e) {
                                    contactNames.add(people.getString(indexName));
                                    contactNumbers.add(people.getString(indexName));
                                    contactTypes.add(ContactsContract.CommonDataKinds.Phone.getTypeLabel(resources, type, customLabel).toString());
                                }
                            } while (people.moveToNext());
                        }
                        people.close();
                    } catch (IllegalArgumentException e) {

                    }
                }
            }

            @SuppressLint("DefaultLocale")
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                final ArrayList<String> searchedNames = new ArrayList<String>();
                final ArrayList<String> searchedNumbers = new ArrayList<String>();
                final ArrayList<String> searchedTypes = new ArrayList<String>();

                String text = contact.getText().toString();

                String[] text2 = text.split("; ");

                text = text2[text2.length - 1].trim();

                if (text.startsWith("+")) {
                    text = text.substring(1);
                }

                Pattern pattern;

                try {
                    pattern = Pattern.compile(text.toLowerCase());
                } catch (Exception e) {
                    pattern = Pattern.compile(text.toLowerCase().replace("(", "").replace(")", "").replace("?", "").replace("[", "").replace("{", "").replace("}", "").replace("\\", "").replace("*", ""));
                }

                try {
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
                } catch (Exception e) {
                }

                Display display = getWindowManager().getDefaultDisplay();
                Point size = new Point();
                display.getSize(size);
                final int height = size.y;

                if (sendTo) {
                    final String textF = text;
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            lpw.setAdapter(new ContactSearchArrayAdapter((Activity) context, searchedNames, searchedNumbers, searchedTypes));
                            lpw.setAnchorView(findViewById(R.id.contactEntry));
                            lpw.setWidth(ListPopupWindow.WRAP_CONTENT);
                            lpw.setHeight(height / 3);


                            if (firstContactSearch) {
                                lpw.show();
                                firstContactSearch = false;
                            }

                            if (textF.length() == 0) {
                                lpw.dismiss();
                                firstContactSearch = true;
                            }
                        }
                    }, 500);
                } else {
                    lpw.setAdapter(new ContactSearchArrayAdapter((Activity) context, searchedNames, searchedNumbers, searchedTypes));
                    lpw.setAnchorView(findViewById(R.id.contactEntry));
                    lpw.setWidth(ListPopupWindow.WRAP_CONTENT);
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

                contact.setError(null);
            }

            public void afterTextChanged(Editable s) {
            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                MainActivity.sentMessage = true;

                if (contact.getText().toString().equals("")) {
                    contact.setError("No Recipients");
                } else if (mEditText.getText().toString().equals("") && imageAttach2.getVisibility() == View.GONE) {
                    mEditText.setError("Nothing to Send");
                } else {
                    mDrawerLayout.closeDrawer(Gravity.RIGHT);

                    MainActivity.animationOn = true;
                    final boolean image;

                    if (imageAttach2.getVisibility() == View.VISIBLE) {
                        MainActivity.animationOn = false;
                        image = true;
                        imageAttach2.setVisibility(false);
                        imageAttachBackground2.setVisibility(View.GONE);
                    } else {
                        image = false;
                    }

                    final String text = mEditText.getText().toString();
                    mEditText.setText("");

                    if (settings.hideKeyboard) {
                        new Thread(new Runnable() {

                            @Override
                            public void run() {
                                try {
                                    Thread.sleep(2000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                } finally {
                                    InputMethodManager keyboard = (InputMethodManager)
                                            getSystemService(Context.INPUT_METHOD_SERVICE);
                                    keyboard.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
                                }

                            }

                        }).start();
                    }

                    Intent updateWidget = new Intent("com.klinker.android.messaging.UPDATE_WIDGET");
                    context.sendBroadcast(updateWidget);

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            final Message message = new Message(text, contact.getText().toString().replace(";", ""));

                            if (image) {
                                ArrayList<Bitmap> bitmaps = new ArrayList<Bitmap>();

                                if (!multipleAttachments) {
                                    if (!fromCamera) {
                                        try {
                                            bitmaps.add(SendUtil.getImage(context, attachedImage2, 600));
                                        } catch (Exception e) {
                                            bitmaps.add(IOUtil.decodeFileWithExif2(new File(IOUtil.getPath(attachedImage2, context))));
                                        }
                                    } else {
                                        try {
                                            bitmaps.add(SendUtil.getImage(context, Uri.fromFile(new File(Environment.getExternalStorageDirectory() + "/SlidingMessaging/", "photoToSend.png")), 600));
                                        } catch (Exception e) {
                                            bitmaps.add(IOUtil.decodeFileWithExif2(new File(Environment.getExternalStorageDirectory() + "/SlidingMessaging/", "photoToSend.png")));
                                        }
                                    }
                                } else {
                                    bitmaps = AttachMore.images;
                                    AttachMore.images = new ArrayList<Bitmap>();
                                    AttachMore.data = new ArrayList<MMSPart>();
                                }

                                Bitmap[] images = new Bitmap[bitmaps.size()];

                                for (int i = 0; i < bitmaps.size(); i++) {
                                    images[i] = bitmaps.get(i);
                                }

                                message.setImages(images);
                            }

                            if (subjectLine2.getVisibility() != View.GONE && !subjectEntry2.getText().toString().equals("")) {
                                message.setSubject(subjectEntry2.getText().toString());
                            }

                            if (!settings.sendWithStock) {
                                if (sendTransaction.checkMMS(message)) {
                                    ((Activity) context).getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {
                                        @Override
                                        public void run() {
                                            Log.v("sending_mms_library", "sending new mms, posted to UI thread");
                                            sendTransaction.sendNewMessage(message, Transaction.NO_THREAD_ID);
                                        }
                                    });
                                } else {
                                    sendTransaction.sendNewMessage(message, Transaction.NO_THREAD_ID);
                                }
                            } else {
                                if (sendTransaction.checkMMS(message)) {
                                    if (!multipleAttachments) {
                                        Intent sendIntent = new Intent(Intent.ACTION_SEND);
                                        sendIntent.putExtra("address", contact.getText().toString().replace(";", ""));
                                        sendIntent.putExtra("sms_body", text);
                                        sendIntent.putExtra(Intent.EXTRA_STREAM, attachedImage);
                                        sendIntent.setType("image/png");

                                        Intent htcIntent = new Intent("android.intent.action.SEND_MSG");
                                        htcIntent.putExtra("address", contact.getText().toString().replace(";", ""));
                                        htcIntent.putExtra("sms_body", text);
                                        htcIntent.putExtra(Intent.EXTRA_STREAM, attachedImage);
                                        htcIntent.setType("image/png");

                                        Intent chooser = Intent.createChooser(sendIntent, "Send Message:");
                                        chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{htcIntent});
                                        startActivity(chooser);

                                        MainActivity.messageRecieved = true;
                                    } else {
                                        Toast.makeText(context, "Cannot send multiple images through stock", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    sendTransaction.sendNewMessage(message, Transaction.NO_THREAD_ID);
                                }
                            }

                            ((Activity) context).getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {

                                @Override
                                public void run() {
                                    contact.setText("");
                                    if (menu != null)
                                        menu.showContent();
                                    refreshViewPager();
                                    mViewPager.setCurrentItem(0);

                                    subjectLine2.setVisibility(View.GONE);
                                    subjectEntry2.setText("");
                                }

                            });
                        }
                    }).start();
                }

                if (haloPopup && settings.closeHaloAfterSend)
                    finish();
            }

        });

        ImageButton emojiButton = (ImageButton) newMessageView.findViewById(R.id.display_emoji);
        voiceButton2 = (ImageButton) newMessageView.findViewById(R.id.voiceButton);

        if (!settings.emoji) {
            emojiButton.setVisibility(View.GONE);
        } else {
            emojiButton.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {

                    final String menuOption = sharedPrefs.getString("page_or_menu2", "2");

                    if (settings.emojiKeyboard && settings.emojiType) {
                        if (!emoji2Open) {
                            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_OPEN);
                            messageScreen2 = (LinearLayout) findViewById(R.id.messageScreen2);

                            messageScreen2.addView(tabs, SlidingTabParams);
                            messageScreen2.addView(vp, viewPagerParams);

                            if (menu != null) {
                                if (menuOption.equals("1")) {
                                    menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
                                }
                            }

                            emoji2Open = true;
                            messageEntry2.requestFocus();

                            InputMethodManager keyboard = (InputMethodManager)
                                    getSystemService(Context.INPUT_METHOD_SERVICE);
                            keyboard.hideSoftInputFromWindow(messageEntry2.getWindowToken(), 0);

                            final EditText contactEntry = (EditText) findViewById(R.id.contactEntry);
                            contactEntry.setOnTouchListener(new View.OnTouchListener() {
                                @Override
                                public boolean onTouch(View v, MotionEvent event) {
                                    messageScreen2.removeView(tabs);
                                    messageScreen2.removeView(vp);

                                    if (menu != null) {
                                        if (menuOption.equals("1")) {
                                            menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
                                        }
                                    }

                                    mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                                    emoji2Open = false;
                                    return false;
                                }
                            });

                            messageEntry2.setOnTouchListener(new View.OnTouchListener() {
                                @Override
                                public boolean onTouch(View v, MotionEvent event) {
                                    if (emoji2Open) {
                                        messageScreen2.removeView(tabs);
                                        messageScreen2.removeView(vp);

                                        if (menu != null) {
                                            if (menuOption.equals("1")) {
                                                menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
                                            }
                                        }

                                        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                                        emoji2Open = false;
                                    }
                                    return false;
                                }
                            });
                        } else {
                            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                            emoji2Open = false;
                            messageScreen2.removeView(tabs);
                            messageScreen2.removeView(vp);

                            if (menu != null) {
                                if (menuOption.equals("1")) {
                                    menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
                                }
                            }
                        }
                    } else {
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

                        if (settings.emojiType) {
                            emojiGrid.setAdapter(new EmojiAdapter2(context));
                            emojiGrid.setOnItemClickListener(new OnItemClickListener() {

                                public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                                    editText.setText(EmojiConverter2.getSmiledText(context, editText.getText().toString() + EmojiAdapter2.mEmojiTexts[position]));
                                    editText.setSelection(editText.getText().length());
                                }
                            });

                            peopleButton.setOnClickListener(new OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    emojiGrid.setSelection(0);
                                }
                            });

                            objectsButton.setOnClickListener(new OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    emojiGrid.setSelection(153 + (2 * 7));
                                }
                            });

                            natureButton.setOnClickListener(new OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    emojiGrid.setSelection(153 + 162 + (3 * 7));
                                }
                            });

                            placesButton.setOnClickListener(new OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    emojiGrid.setSelection(153 + 162 + 178 + (5 * 7));
                                }
                            });

                            symbolsButton.setOnClickListener(new OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    emojiGrid.setSelection(153 + 162 + 178 + 122 + (7 * 7));
                                }
                            });
                        } else {
                            emojiGrid.setAdapter(new EmojiAdapter(context));
                            emojiGrid.setOnItemClickListener(new OnItemClickListener() {

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

                        okButton.setOnClickListener(new OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                if (settings.emojiType) {
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
                }

            });
        }

        if (settings.voiceAccount != null) {
            if (settings.voiceEnabled) {
                voiceButton2.setImageResource(R.drawable.voice_enabled);
            } else {
                voiceButton2.setImageResource(R.drawable.voice_disabled);
            }

            voiceButton2.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (settings.voiceEnabled) {
                        settings.voiceEnabled = false;
                        sharedPrefs.edit().putBoolean("voice_enabled", false).commit();
                        voiceButton2.setImageResource(R.drawable.voice_disabled);
                        voiceButton.setImageResource(R.drawable.voice_disabled);
                        sendSettings.setPreferVoice(false);
                        sendTransaction.settings = sendSettings;
                    } else {
                        settings.voiceEnabled = true;
                        sharedPrefs.edit().putBoolean("voice_enabled", true).commit();
                        voiceButton2.setImageResource(R.drawable.voice_enabled);
                        voiceButton.setImageResource(R.drawable.voice_enabled);
                        sendSettings.setPreferVoice(true);
                        sendTransaction.settings = sendSettings;
                    }
                }
            });
        } else {
            voiceButton2.setVisibility(View.GONE);
        }

        ListView searchView = (ListView) newMessageView.findViewById(R.id.contactSearch);

        try {
            mEditText.setTextSize(Integer.parseInt(settings.textSize.substring(0, 2)));
        } catch (Exception e) {
            mEditText.setTextSize(Integer.parseInt(settings.textSize.substring(0, 1)));
        }

        View v1 = newMessageView.findViewById(R.id.view1);
        View v2 = newMessageView.findViewById(R.id.sentBackground);

        mTextView.setTextColor(settings.ctSendButtonColor);
        v1.setBackgroundColor(settings.ctSendBarBackground);
        v2.setBackgroundColor(settings.ctSendBarBackground);
        sendButton.setBackgroundResource(R.drawable.pitch_black_send_button);
        sendButton.setImageResource(R.drawable.ic_action_send_white);
        sendButton.setColorFilter(settings.ctSendButtonColor);
        searchView.setBackgroundColor(settings.ctConversationListBackground);
        emojiButton.setBackgroundResource(R.drawable.pitch_black_send_button);
        emojiButton.setColorFilter(settings.emojiButtonColor);
        voiceButton2.setColorFilter(settings.emojiButtonColor);
        mEditText.setTextColor(settings.draftTextColor);
        contact.setTextColor(settings.draftTextColor);
        contactLister.setColorFilter(settings.ctSendButtonColor);

        imageAttachBackground2.setBackgroundColor(settings.ctMessageListBackground);
        Drawable attachBack = resources.getDrawable(R.drawable.attachment_editor_bg);
        attachBack.setColorFilter(settings.ctSentMessageBackground, Mode.MULTIPLY);
        imageAttach2.setBackgroundDrawable(attachBack);
        imageAttachBackground2.setVisibility(View.GONE);
        imageAttach2.setVisibility(false);

        subjectEntry2.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        subjectEntry2.setTextColor(settings.draftTextColor);
        subjectLine2.setVisibility(View.GONE);
        subjectLine2.setBackgroundColor(settings.ctSendBarBackground);
        subjectDelete2.setColorFilter(settings.ctSendButtonColor);

        if (settings.customFont) {
            mTextView.setTypeface(font);
            mEditText.setTypeface(font);
            contact.setTypeface(font);
        }

        if (settings.runAs.equals("hangout") || settings.runAs.equals("card2") || settings.runAs.equals("card+")) {
            emojiButton.setImageResource(R.drawable.ic_emoji_dark);
        }

        if (settings.runAs.equals("sliding")) {
            voiceButton2.setAlpha(255);
        }

        if (settings.customBackground) {
            try {
                BitmapFactory.Options options = new BitmapFactory.Options();

                options.inSampleSize = 2;
                Bitmap myBitmap = BitmapFactory.decodeFile(Uri.parse(settings.customBackgroundLocation).getPath(), options);
                Drawable d = new BitmapDrawable(Resources.getSystem(), myBitmap);
                searchView.setBackgroundDrawable(d);
            } catch (Error e) {
                try {
                    BitmapFactory.Options options = new BitmapFactory.Options();

                    options.inSampleSize = 4;
                    Bitmap myBitmap = BitmapFactory.decodeFile(Uri.parse(settings.customBackgroundLocation).getPath(), options);
                    Drawable d = new BitmapDrawable(Resources.getSystem(), myBitmap);
                    searchView.setBackgroundDrawable(d);
                } catch (Error f) {

                }
            }
        }
    }

    private void initialSetup() {
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        try {
            if (getIntent().getAction().equals("OPEN_APP")) {
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
            }
        } catch (Exception e) {
        }

        if (sharedPrefs.getBoolean("slideover_enabled", false)) {
            if (!isSlideOverRunning()) {
                Intent service = new Intent(getApplicationContext(), com.klinker.android.messaging_sliding.slide_over.SlideOverService.class);
                startService(service);
            }
        }

        UnlockReceiver.openApp = false;

        Intent fromIntent = getIntent();

        String version = "";

        try {
            version = this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        if (!sharedPrefs.getString("current_version", "0").equals(version)) {
            if (sharedPrefs.getString("current_version", "0").equals("0")) {
                Intent initialSetupIntent = new Intent(getApplicationContext(), InitialSetupMain.class);
                initialSetupIntent.setAction(fromIntent.getAction());
                initialSetupIntent.setData(fromIntent.getData());
                startActivity(initialSetupIntent);
            } else {
                Intent changeLogIntent = new Intent(getApplicationContext(), ChangeLogMain.class);
                changeLogIntent.setAction(fromIntent.getAction());
                changeLogIntent.setData(fromIntent.getData());
                changeLogIntent.putExtra("version", version);
                startActivity(changeLogIntent);
            }

            finish();
            overridePendingTransition(0, 0);
        }
    }

    private boolean isSlideOverRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (SlideOverService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public void setUpSendSettings() {
        sendSettings = SendUtil.getSendSettings(this);
        sendTransaction = new Transaction(this, sendSettings);
    }

    public void setUpWindow() {
        if (settings.lightActionBar) {
            setTheme(R.style.HangoutsTheme);
        }

        try {
            ab = getActionBar();
        } catch (Exception e) {
            // just in case there is no action bar and slideover for some reason called this
        }

        if (!settings.pinType.equals("1")) {
            if (settings.pinType.equals("2")) {
                setContentView(R.layout.activity_main_phone);
            } else if (settings.pinType.equals("3")) {
                if (resources.getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                    setContentView(R.layout.activity_main_phablet2);
                } else {
                    setContentView(R.layout.activity_main_phablet);
                }
            } else {
                setContentView(R.layout.activity_main_tablet);
            }
        } else {
            setContentView(R.layout.activity_main);
        }

        setTitle(R.string.app_name_in_app);
        getWindow().setBackgroundDrawable(new ColorDrawable(resources.getColor(android.R.color.transparent)));
    }

    public void setUpIntentStuff() {
        Intent intent = getIntent();
        String action = intent.getAction();

        try {
            if (action != null) {
                if (action.equals(Intent.ACTION_SENDTO)) {
                    sendTo = true;

                    try {
                        if (intent.getDataString().startsWith("smsto:")) {
                            sendMessageTo = Uri.decode(intent.getDataString()).substring("smsto:".length()).replace("(", "").replace(")", "").replace("-", "").replace(" ", "");
                            fromNotification = false;
                        } else {
                            sendMessageTo = Uri.decode(intent.getDataString()).substring("sms:".length()).replace("(", "").replace(")", "").replace("-", "").replace(" ", "");
                            fromNotification = false;
                        }
                    } catch (Exception e) {
                        sendMessageTo = intent.getStringExtra("com.klinker.android.OPEN").replace("(", "").replace(")", "").replace("-", "").replace(" ", "");
                        fromNotification = true;
                    }
                } else if (action.equals(Intent.ACTION_SEND)) {
                    Bundle extras = intent.getExtras();

                    if (extras != null) {
                        if (extras.containsKey(Intent.EXTRA_TEXT)) {
                            whatToSend = (String) extras.getCharSequence(Intent.EXTRA_TEXT);
                        }

                        if (extras.containsKey(Intent.EXTRA_STREAM)) {
                            sendTo = true;
                            sendMessageTo = "";
                            fromNotification = false;
                            attachedImage2 = intent.getParcelableExtra(Intent.EXTRA_STREAM);
                        }
                    }
                }
            } else {
                Bundle extras = intent.getExtras();

                if (extras != null) {
                    if (extras.containsKey("com.klinker.android.OPEN_THREAD")) {
                        sendToThread = extras.getString("com.klinker.android.OPEN_THREAD");
                        sendToMessage = extras.getString("com.klinker.android.CURRENT_TEXT");
                    }
                }
            }
        } catch (Exception e) {
        }
    }

    public void setUpTitleBar() {
        title = (PagerTitleStrip) findViewById(R.id.pager_title_strip);

        if (settings.pageorMenu2) {
            title.setTextSpacing(5000);
        }

        if (!settings.customTheme) {
            if (settings.titleTextColor) {
                title.setTextColor(resources.getColor(R.color.black));
            }
        } else {
            title.setTextColor(settings.titleBarTextColor);
        }

        if (!settings.titleCaps) {
            title.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        }

        if (settings.useTitleBar) {
            if (!settings.customTheme) {
                String titleColor = sharedPrefs.getString("title_color", "blue");

                if (titleColor.equals("blue")) {
                    title.setBackgroundColor(resources.getColor(R.color.holo_blue));
                } else if (titleColor.equals("orange")) {
                    title.setBackgroundColor(resources.getColor(R.color.holo_orange));
                } else if (titleColor.equals("red")) {
                    title.setBackgroundColor(resources.getColor(R.color.holo_red));
                } else if (titleColor.equals("green")) {
                    title.setBackgroundColor(resources.getColor(R.color.holo_green));
                } else if (titleColor.equals("purple")) {
                    title.setBackgroundColor(resources.getColor(R.color.holo_purple));
                } else if (titleColor.equals("grey")) {
                    title.setBackgroundColor(resources.getColor(R.color.grey));
                } else if (titleColor.equals("black")) {
                    title.setBackgroundColor(resources.getColor(R.color.pitch_black));
                } else if (titleColor.equals("darkgrey")) {
                    title.setBackgroundColor(resources.getColor(R.color.darkgrey));
                }
            } else {
                title.setBackgroundColor(settings.titleBarColor);
            }
        } else {
            title.setVisibility(View.GONE);
        }
    }

    public void showUnlockFullDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.trial_expired)
                .setMessage(R.string.trial_expired_message)
                .setPositiveButton(R.string.upgrade_to_pro, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=com.klinker.android.messaging_donate"));
                        startActivity(intent);
                    }
                })
                .create()
                .show();
    }

    public void refreshMessages() {
        conversations = new ArrayList<Conversation>();
        ContentResolver contentResolver = getContentResolver();

        try {
            String[] projection = new String[]{"_id", "date", "message_count", "recipient_ids", "snippet", "read"};
            Uri uri = Uri.parse("content://mms-sms/conversations/?simple=true");
            Cursor query = contentResolver.query(uri, projection, null, null, "date desc");

            if (query.moveToFirst()) {
                do {
                    String snippet = " ";
                    try {
                        snippet = query.getString(query.getColumnIndex("snippet")).replaceAll("\\\n", " ");
                    } catch (Exception e) {
                    }

                    conversations.add(new Conversation(
                            query.getLong(query.getColumnIndex("_id")),
                            query.getInt(query.getColumnIndex("message_count")),
                            query.getString(query.getColumnIndex("read")),
                            snippet,
                            query.getLong(query.getColumnIndex("date")),
                            query.getString(query.getColumnIndex("recipient_ids"))
                    ));
                } while (query.moveToNext());
            }

            query.close();
        } catch (Exception e) {
        }

        if (settings.alwaysUseVoice) {
            for (Conversation i : conversations) {
                threadsThroughVoice.add(i.getThreadId());
            }
        }

        if (conversations.size() > 0) {
            messageEntry.setVisibility(View.VISIBLE);
            sendButton.setVisibility(View.VISIBLE);
            v.setVisibility(View.VISIBLE);

            if (settings.useTitleBar) {
                title.setVisibility(View.VISIBLE);
            }
        } else {
            messageEntry.setVisibility(View.GONE);
            sendButton.setVisibility(View.GONE);
            v.setVisibility(View.GONE);
            title.setVisibility(View.GONE);
            emojiButton.setVisibility(View.GONE);
            voiceButton.setVisibility(View.GONE);

            getWindow().getDecorView().setBackgroundColor(settings.ctMessageListBackground);
        }

        if (refreshMyContact) {
            String[] mProjection = new String[]
                    {
                            Profile._ID
                    };

            Cursor mProfileCursor =
                    getContentResolver().query(
                            Profile.CONTENT_URI,
                            mProjection,
                            null,
                            null,
                            null);

            try {
                if (mProfileCursor.moveToFirst()) {
                    myContactId = mProfileCursor.getString(mProfileCursor.getColumnIndex(Profile._ID));
                }
            } catch (Exception e) {
                myContactId = myPhoneNumber;
            } finally {
                mProfileCursor.close();
            }

            refreshMyContact = false;
        }
    }

    @SuppressWarnings("deprecation")
    public void setUpSendbar() {
        mTextView = (TextView) findViewById(R.id.charsRemaining2);
        messageEntry = (EditText) findViewById(R.id.messageEntry);
        sendButton = (ImageButton) findViewById(R.id.sendButton);
        emojiButton = (ImageButton) findViewById(R.id.display_emoji);
        voiceButton = (ImageButton) findViewById(R.id.voiceButton);
        mmsProgress = (ProgressBar) findViewById(R.id.mmsProgress);
        mmsProgressAnimation.setMmsProgress(mmsProgress);
        subjectLine = findViewById(R.id.subjectBar);
        subjectEntry = (EditText) findViewById(R.id.subjectEntry);
        subjectDelete = (ImageButton) findViewById(R.id.subjectDelete);

        v = findViewById(R.id.view1);
        imageAttachBackground = findViewById(R.id.image_attachment_view_background2);
        imageAttach = (ImageAttachmentView) findViewById(R.id.image_attachment_view);

        deviceType = messageEntry.getTag().toString();

        try {
            if (deviceType.equals("phablet") || deviceType.equals("tablet")) {
                ab.setDisplayHomeAsUpEnabled(false);
            }
        } catch (Exception e) {
            // no action bar, dialog theme
        }

        if (!settings.keyboardType) {
            messageEntry.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
            messageEntry.setImeOptions(EditorInfo.IME_ACTION_NONE);
        }

        if (deviceType.equals("phablet") || deviceType.equals("tablet")) {
            if (!settings.keyboardType) {
                messageEntry.setImeOptions(EditorInfo.IME_ACTION_NONE | EditorInfo.IME_FLAG_NO_EXTRACT_UI);
            } else {
                messageEntry.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
            }
        }

        mTextView.setVisibility(View.GONE);

        messageEntry.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (isPopup) {
                    if (attachOnSend && s.length() != 0) {
                        sendButton.setImageResource(R.drawable.ic_action_send_white);
                        attachOnSend = false;
                        return;
                    } else if (!attachOnSend && s.length() == 0) {
                        sendButton.setImageResource(R.drawable.ic_attach);
                        attachOnSend = true;
                        return;
                    }
                }

                if (messageEntry.getError() != null) {
                    messageEntry.setError(null);
                }

                if (!settings.signature.equals("")) {
                    s = s + "\n" + settings.signature;
                }

                int[] data = SmsMessage.calculateLength(s, false);
                mTextView.setText(data[0] + "/" + data[2]);

                if ((data[0] < 2 && data[2] > 30) ||
                        (imageAttach.getVisibility() == View.VISIBLE) ||
                        (data[0] > settings.mmsAfter && settings.sendAsMMS) ||
                        (conversations.get(mViewPager.getCurrentItem()).getGroup())) {
                    mTextView.setVisibility(View.INVISIBLE);
                } else {
                    mTextView.setVisibility(View.VISIBLE);
                }

                if (settings.sendWithReturn) {
                    if (messageEntry.getText().toString().endsWith("\n")) {
                        messageEntry.setText(messageEntry.getText().toString().substring(0, messageEntry.getText().toString().length() - 1));
                        sendButton.performClick();
                    }
                }
            }

            public void afterTextChanged(Editable s) {
                if (settings.enableDrafts) {
                    if (newDraft == 0) {
                        newDraft = conversations.get(mViewPager.getCurrentItem()).getThreadId();
                    }
                }
            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                final String recipientId = conversations.get(mViewPager.getCurrentItem()).getNumber();
                final long threadId = conversations.get(mViewPager.getCurrentItem()).getThreadId();

                if (!unlocked) {
                    messageEntry.setError(getString(R.string.trial_expired));
                    return;
                }

                if (isPopup && attachOnSend) {
                    popupAttaching = true;
                    menuAttachImage();
                    return;
                }

                if (messageEntry.getText().toString().equals("") && imageAttach.getVisibility() == View.GONE) {
                    messageEntry.setError("Nothing to send");
                } else {
                    MainActivity.animationOn = true;
                    final boolean image;

                    if (imageAttach.getVisibility() == View.VISIBLE) {
                        MainActivity.animationOn = false;
                        image = true;
                        imageAttach.setVisibility(false);
                        imageAttachBackground.setVisibility(View.GONE);
                    } else {
                        image = false;
                    }

                    final String text = messageEntry.getText().toString();
                    messageEntry.setText("");
                    mTextView.setVisibility(View.GONE);

                    if (settings.hideKeyboard) {
                        new Thread(new Runnable() {

                            @Override
                            public void run() {
                                try {
                                    Thread.sleep(2000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                } finally {
                                    InputMethodManager keyboard = (InputMethodManager)
                                            getSystemService(Context.INPUT_METHOD_SERVICE);
                                    keyboard.hideSoftInputFromWindow(messageEntry.getWindowToken(), 0);
                                }

                            }

                        }).start();
                    }

                    Intent updateWidget = new Intent("com.klinker.android.messaging.UPDATE_WIDGET");
                    context.sendBroadcast(updateWidget);

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            final Message message = new Message(text, ContactUtil.findContactNumber(recipientId, context).replace(";", ""));

                            if (image) {
                                ArrayList<Bitmap> bitmaps = new ArrayList<Bitmap>();

                                if (!multipleAttachments) {
                                    if (!fromCamera) {
                                        try {
                                            bitmaps.add(SendUtil.getImage(context, attachedImage, 600));
                                        } catch (Exception e) {
                                            bitmaps.add(IOUtil.decodeFileWithExif2(new File(IOUtil.getPath(attachedImage, context))));
                                        }
                                    } else {
                                        try {
                                            bitmaps.add(SendUtil.getImage(context, Uri.fromFile(new File(Environment.getExternalStorageDirectory() + "/SlidingMessaging/", "photoToSend.png")), 600));
                                        } catch (Exception e) {
                                            bitmaps.add(IOUtil.decodeFileWithExif2(new File(Environment.getExternalStorageDirectory() + "/SlidingMessaging/", "photoToSend.png")));
                                        }
                                    }
                                } else {
                                    bitmaps = AttachMore.images;
                                    AttachMore.images = new ArrayList<Bitmap>();
                                    AttachMore.data = new ArrayList<MMSPart>();
                                }

                                Bitmap[] images = new Bitmap[bitmaps.size()];

                                for (int i = 0; i < bitmaps.size(); i++) {
                                    images[i] = bitmaps.get(i);
                                }

                                message.setImages(images);
                            }

                            if (subjectLine.getVisibility() != View.GONE && !subjectEntry.getText().toString().equals("")) {
                                message.setSubject(subjectEntry.getText().toString());
                            }

                            if (!settings.sendWithStock) {
                                if (sendTransaction.checkMMS(message)) {
                                    ((Activity) context).getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {
                                        @Override
                                        public void run() {
                                            Log.v("sending_mms_library", "sending new mms, posted to UI thread");
                                            sendTransaction.sendNewMessage(message, threadId);
                                        }
                                    });
                                } else {
                                    sendTransaction.sendNewMessage(message, threadId);
                                }
                            } else {
                                if (sendTransaction.checkMMS(message)) {
                                    if (!multipleAttachments) {
                                        Intent sendIntent = new Intent(Intent.ACTION_SEND);
                                        sendIntent.putExtra("address", ContactUtil.findContactNumber(recipientId, context).replace(";", ""));
                                        sendIntent.putExtra("sms_body", text);
                                        sendIntent.putExtra(Intent.EXTRA_STREAM, attachedImage);
                                        sendIntent.setType("image/png");

                                        Intent htcIntent = new Intent("android.intent.action.SEND_MSG");
                                        htcIntent.putExtra("address", ContactUtil.findContactNumber(recipientId, context).replace(";", ""));
                                        htcIntent.putExtra("sms_body", text);
                                        htcIntent.putExtra(Intent.EXTRA_STREAM, attachedImage);
                                        htcIntent.setType("image/png");

                                        Intent chooser = Intent.createChooser(sendIntent, "Send Message:");
                                        chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{htcIntent});
                                        startActivity(chooser);

                                        MainActivity.messageRecieved = true;
                                    } else {
                                        Toast.makeText(context, "Cannot send multiple images through stock", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    sendTransaction.sendNewMessage(message, threadId);
                                }
                            }

                            ((Activity) context).getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {

                                @Override
                                public void run() {
                                    if (settings.enableDrafts) {
                                        if (fromDraft) {
                                            try {
                                                for (int i = 0; i < draftNames.size(); i++) {
                                                    if (draftNames.get(i) == (threadId)) {
                                                        draftsToDelete.add(draftNames.get(i));
                                                        draftNames.remove(i);
                                                        drafts.remove(i);
                                                        draftChanged.remove(i);
                                                        break;
                                                    }
                                                }
                                            } catch (Exception e) {
                                            }
                                        }
                                    }

                                    MainActivity.sentMessage = true;
                                    MainActivity.threadedLoad = false;
                                    MainActivity.notChanged = false;
                                    refreshViewPager4(recipientId, StripAccents.stripAccents(message.getText()), Calendar.getInstance().getTimeInMillis() + "");

                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            mViewPager.setCurrentItem(0);
                                        }
                                    }, 200);

                                    if (isPopup && settings.fullAppPopupClose && !sendTransaction.checkMMS(message)) {
                                        if (!settings.voiceEnabled) {
                                            Intent intent = new Intent("com.klinker.android.messaging.CLOSE_POPUP");
                                            sendBroadcast(intent);
                                        } else {
                                            registerReceiver(new BroadcastReceiver() {
                                                @Override
                                                public void onReceive(Context context, Intent intent) {
                                                    unregisterReceiver(this);
                                                    Intent close = new Intent("com.klinker.android.messaging.CLOSE_POPUP");
                                                    sendBroadcast(close);
                                                }
                                            }, new IntentFilter(Transaction.REFRESH));
                                        }
                                    }

                                    subjectEntry.setText("");
                                    subjectLine.setVisibility(View.GONE);
                                }

                            });
                        }
                    }).start();
                }

                if (haloPopup && settings.closeHaloAfterSend) {
                    finish();
                }
            }

        });

        try {
            messageEntry.setTextSize(Integer.parseInt(settings.textSize.substring(0, 2)));
        } catch (Exception e) {
            messageEntry.setTextSize(Integer.parseInt(settings.textSize.substring(0, 1)));
        }

        if (!settings.emoji) {
            emojiButton.setVisibility(View.GONE);
        } else {
            emojiButton.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {

                    final String menuOption = sharedPrefs.getString("page_or_menu2", "2");

                    if (settings.emojiKeyboard && settings.emojiType) {
                        if (!emojiOpen) {
                            emojiOpen = true;

                            messageScreen.addView(tabs, SlidingTabParams);
                            messageScreen.addView(vp, viewPagerParams);

                            if (menu != null) {
                                if (menuOption.equals("1")) {
                                    menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
                                }
                            }

                            InputMethodManager keyboard = (InputMethodManager)
                                    getSystemService(Context.INPUT_METHOD_SERVICE);
                            keyboard.hideSoftInputFromWindow(messageEntry.getWindowToken(), 0);

                            messageEntry.setOnTouchListener(new View.OnTouchListener() {
                                @Override
                                public boolean onTouch(View v, MotionEvent event) {
                                    if (emojiOpen) {
                                        messageScreen.removeView(tabs);
                                        messageScreen.removeView(vp);

                                        if (menu != null) {
                                            if (menuOption.equals("1")) {
                                                menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
                                            }
                                        }

                                        emojiOpen = false;
                                    }

                                    return false;
                                }
                            });
                        } else {
                            emojiOpen = false;

                            if (menu != null) {
                                if (menuOption.equals("1")) {
                                    menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
                                }
                            }

                            messageScreen.removeView(tabs);
                            messageScreen.removeView(vp);
                        }
                    } else {

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

                        final StickyGridHeadersGridView emojiGrid = (StickyGridHeadersGridView) frame.findViewById(R.id.emojiGrid);
                        Button okButton = (Button) frame.findViewById(R.id.emoji_ok);

                        if (settings.emojiType) {
                            emojiGrid.setAdapter(new EmojiAdapter2(context));
                            emojiGrid.setOnItemClickListener(new OnItemClickListener() {

                                public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                                    editText.setText(EmojiConverter2.getSmiledText(context, editText.getText().toString() + EmojiAdapter2.mEmojiTexts[position]));
                                    editText.setSelection(editText.getText().length());
                                }
                            });

                            peopleButton.setOnClickListener(new OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    emojiGrid.setSelection(0);
                                }
                            });

                            objectsButton.setOnClickListener(new OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    emojiGrid.setSelection(153 + (2 * 7));
                                }
                            });

                            natureButton.setOnClickListener(new OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    emojiGrid.setSelection(153 + 162 + (3 * 7));
                                }
                            });

                            placesButton.setOnClickListener(new OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    emojiGrid.setSelection(153 + 162 + 178 + (5 * 7));
                                }
                            });

                            symbolsButton.setOnClickListener(new OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    emojiGrid.setSelection(153 + 162 + 178 + 122 + (7 * 7));
                                }
                            });
                        } else {
                            emojiGrid.setAdapter(new EmojiAdapter(context));
                            emojiGrid.setOnItemClickListener(new OnItemClickListener() {

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

                        okButton.setOnClickListener(new OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                if (settings.emojiType) {
                                    messageEntry.setText(EmojiConverter2.getSmiledText(context, messageEntry.getText().toString() + editText.getText().toString()));
                                    messageEntry.setSelection(messageEntry.getText().length());
                                } else {
                                    messageEntry.setText(EmojiConverter.getSmiledText(context, messageEntry.getText().toString() + editText.getText().toString()));
                                    messageEntry.setSelection(messageEntry.getText().length());
                                }

                                dialog.dismiss();
                            }

                        });
                    }
                }

            });
        }

        if (settings.voiceAccount != null) {
            if (settings.voiceEnabled) {
                voiceButton.setImageResource(R.drawable.voice_enabled);
            } else {
                voiceButton.setImageResource(R.drawable.voice_disabled);
            }

            voiceButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (settings.voiceEnabled) {
                        settings.voiceEnabled = false;
                        sharedPrefs.edit().putBoolean("voice_enabled", false).commit();
                        voiceButton2.setImageResource(R.drawable.voice_disabled);
                        voiceButton.setImageResource(R.drawable.voice_disabled);
                        sendSettings.setPreferVoice(false);
                        sendTransaction.settings = sendSettings;

                        if (threadsThroughVoice.contains(conversations.get(mViewPager.getCurrentItem()).getThreadId())) {
                            boolean res = threadsThroughVoice.remove(conversations.get(mViewPager.getCurrentItem()).getThreadId());
                            Log.v("threads_through_voice", "removed: " + res);
                        }
                    } else {
                        settings.voiceEnabled = true;
                        sharedPrefs.edit().putBoolean("voice_enabled", true).commit();
                        voiceButton2.setImageResource(R.drawable.voice_enabled);
                        voiceButton.setImageResource(R.drawable.voice_enabled);
                        sendSettings.setPreferVoice(true);
                        sendTransaction.settings = sendSettings;

                        if (!threadsThroughVoice.contains(conversations.get(mViewPager.getCurrentItem()).getThreadId())) {
                            boolean res = threadsThroughVoice.add(conversations.get(mViewPager.getCurrentItem()).getThreadId());
                            Log.v("threads_through_voice", "added: " + res);
                        }
                    }
                }
            });
        } else {
            voiceButton.setVisibility(View.GONE);
        }

        if (!isPopup) {
            sendButton.setImageResource(R.drawable.ic_action_send_white);
        } else {
            sendButton.setImageResource(R.drawable.ic_attach);
        }

        mTextView.setTextColor(settings.ctSendButtonColor);
        v.setBackgroundColor(settings.ctSendBarBackground);
        sendButton.setBackgroundResource(R.drawable.pitch_black_send_button);
        sendButton.setColorFilter(settings.ctSendButtonColor);
        emojiButton.setBackgroundResource(R.drawable.pitch_black_send_button);
        emojiButton.setColorFilter(settings.emojiButtonColor);
        voiceButton.setColorFilter(settings.emojiButtonColor);
        messageEntry.setTextColor(settings.draftTextColor);
        imageAttachBackground.setBackgroundColor(settings.ctMessageListBackground);
        Drawable attachBack = resources.getDrawable(R.drawable.attachment_editor_bg);
        attachBack.setColorFilter(settings.ctSentMessageBackground, Mode.MULTIPLY);
        imageAttach.setBackgroundDrawable(attachBack);
        imageAttachBackground.setVisibility(View.GONE);
        imageAttach.setVisibility(false);

        Drawable mmsProgressDrawable = resources.getDrawable(R.drawable.progress_horizontal_holo_light);
        mmsProgressDrawable.setColorFilter(settings.ctSendButtonColor, Mode.MULTIPLY);
        mmsProgress.setProgressDrawable(mmsProgressDrawable);

        subjectEntry.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        subjectEntry.setTextColor(settings.draftTextColor);
        subjectLine.setVisibility(View.GONE);
        subjectLine.setBackgroundColor(settings.ctSendBarBackground);
        subjectDelete.setColorFilter(settings.ctSendButtonColor);

        if (settings.customFont) {
            mTextView.setTypeface(font);
            messageEntry.setTypeface(font);
            subjectEntry.setTypeface(font);
        }

        if (settings.runAs.equals("hangout") || settings.runAs.equals("card2") || settings.runAs.equals("card+")) {
            emojiButton.setImageResource(R.drawable.ic_emoji_dark);
        }

        if (settings.runAs.equals("sliding")) {
            voiceButton.setAlpha(255);
        }
    }

    private static EmojiDataSource dataSource;
    public static ArrayList<Recent> recents;

    public static void insertEmoji(String emoji, int icon) {
        EditText input;

        if (mDrawerLayout.isDrawerOpen(Gravity.RIGHT)) {
            input = messageEntry2;
        } else {
            input = messageEntry;
        }

        input.setEnabled(false);
        int beforeSelectionStart = input.getSelectionStart();
        int beforeLength = input.getText().toString().length();
        CharSequence before = input.getText().subSequence(0, beforeSelectionStart);
        CharSequence after = input.getText().subSequence(input.getSelectionEnd(), beforeLength);
        input.setText(android.text.TextUtils.concat(before, EmojiConverter2.getSmiledText(context, emoji), after));
        input.setEnabled(true);
        input.setSelection(beforeSelectionStart + (input.getText().toString().length() - beforeLength));

        for (Recent recent : recents) {
            if (recent.text.equals(emoji)) {
                dataSource.updateRecent(icon + "");
                recent.count++;
                return;
            }
        }

        Recent recent = dataSource.createRecent(emoji, icon + "");

        if (recent != null) {
            recents.add(recent);
        }
    }

    private static MyPagerAdapter emojiAdapter;

    public static void removeRecent(int position) {
        dataSource.deleteRecent(recents.get(position).id);
        recents.remove(position);
        emojiAdapter.notifyDataSetChanged();
    }

    public class MyPagerAdapter extends FragmentPagerAdapter {

        private final String[] TITLES = {getString(R.string.favorites), getString(R.string.people), getString(R.string.things), getString(R.string.nature), getString(R.string.places), getString(R.string.symbols)};

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);

            dataSource = new EmojiDataSource(MainActivity.this);
            dataSource.open();
            recents = (ArrayList<Recent>) dataSource.getAllRecents();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return TITLES[position];
        }

        @Override
        public int getCount() {
            return TITLES.length;
        }

        @Override
        public KeyboardFragment getItem(int position) {
            return KeyboardFragment.newInstance(position);
        }

    }

    @SuppressWarnings("deprecation")
    public void createMenu() {
        if (deviceType.equals("phablet") || deviceType.equals("tablet")) {
            ListFragment newFragment = (ListFragment) getSupportFragmentManager().findFragmentById(R.id.menuList);
            final ListView menuLayout = newFragment.getListView();
            if (settings.limitConversationsAtStart && conversations.size() > 10) {
                final Button footer = new Button(this);
                footer.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        menuLayout.removeFooterView(footer);
                        limitConversations = false;
                        refreshViewPager();
                    }
                });
                footer.setText(resources.getString(R.string.load_all));
                footer.setTypeface(font);
                footer.setBackgroundResource(android.R.color.transparent);
                footer.setTextColor(settings.ctNameTextColor);
                menuLayout.addFooterView(footer);
            }

            newFragment.setListAdapter(new MenuArrayAdapter(this, conversations, MainActivity.mViewPager));

            if (settings.customBackground) {
                try {
                    BitmapFactory.Options options = new BitmapFactory.Options();

                    options.inSampleSize = 2;
                    Bitmap myBitmap = BitmapFactory.decodeFile(Uri.parse(settings.customBackgroundLocation).getPath(), options);
                    Drawable d = new BitmapDrawable(Resources.getSystem(), myBitmap);
                    newFragment.getView().setBackgroundDrawable(d);
                } catch (Error e) {

                }
            } else {
                newFragment.getView().setBackgroundColor(settings.ctConversationListBackground);
            }

            menuLayout.setDivider(new ColorDrawable(settings.ctConversationDividerColor));

            if (settings.messageDividerVisible) {
                menuLayout.setDividerHeight(1);
            } else {
                menuLayout.setDividerHeight(0);
            }
        } else {
            if (settings.limitConversationsAtStart && conversations.size() > 10) {
                final Button footer = new Button(this);
                footer.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        menuLayout.removeFooterView(footer);
                        limitConversations = false;
                        refreshViewPager();
                    }
                });
                footer.setText(resources.getString(R.string.load_all));
                footer.setTypeface(font);
                footer.setBackgroundResource(android.R.color.transparent);
                footer.setTextColor(settings.ctNameTextColor);
                menuLayout.addFooterView(footer);
            }

            if (settings.customBackground) {
                try {
                    BitmapFactory.Options options = new BitmapFactory.Options();

                    options.inSampleSize = 2;
                    Bitmap myBitmap = BitmapFactory.decodeFile(Uri.parse(settings.customBackgroundLocation).getPath(), options);
                    Drawable d = new BitmapDrawable(Resources.getSystem(), myBitmap);
                    menuLayout.setBackgroundDrawable(d);
                } catch (Error e) {

                }
            } else {
                menuLayout.setBackgroundColor(settings.ctConversationListBackground);
            }

            final Activity activity = this;

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    menuAdapter = new MenuArrayAdapter(activity, conversations, MainActivity.mViewPager);
                    menuLayout.setAdapter(menuAdapter);
                    menuLayout.setDivider(new ColorDrawable(settings.ctConversationDividerColor));

                    if (settings.messageDividerVisible) {
                        menuLayout.setDividerHeight(1);
                    } else {
                        menuLayout.setDividerHeight(0);
                    }
                }
            }, 50);
        }

        if (deviceType.equals("phone") || deviceType.equals("phablet2")) {
            menu = new SlidingMenu(this);
            menu.setMode(SlidingMenu.LEFT);
            menu.setShadowDrawable(R.drawable.shadow);
            menu.setShadowWidthRes(R.dimen.shadow_width);

            if (!settings.slideMessages) {
                menu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
            } else {
                menu.setBehindOffset(0);
            }

            menu.setFadeDegree(0.35f);
            menu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
            menu.getContent().setBackgroundDrawable(new ColorDrawable(settings.ctMessageListBackground));
            menu.setMenu(menuLayout);

            menu.setOnOpenedListener(new SlidingMenu.OnOpenedListener() {

                @Override
                public void onOpened() {
                    invalidateOptionsMenu();

                    if (mDrawerLayout.isDrawerOpen(Gravity.RIGHT))
                        mDrawerLayout.closeDrawer(Gravity.RIGHT);

                    if (emojiOpen) {
                        messageScreen = (LinearLayout) findViewById(R.id.messageScreen);

                        messageScreen.removeView(tabs);
                        messageScreen.removeView(vp);
                        emojiOpen = false;
                    }

                    if (emoji2Open) {
                        messageScreen2 = (LinearLayout) findViewById(R.id.messageScreen2);

                        messageScreen2.removeView(tabs);
                        messageScreen2.removeView(vp);
                        emoji2Open = false;
                    }

                    try {
                        ab.setTitle(R.string.app_name_in_app);
                        ab.setSubtitle(null);
                        ab.setIcon(R.drawable.ic_launcher);

                        ab.setDisplayHomeAsUpEnabled(false);
                    } catch (Exception e) {
                        // no action bar, dialog theme
                    }

                    if (menu.isMenuShowing()) {
                        InputMethodManager imm = (InputMethodManager) getSystemService(
                                Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(messageEntry.getWindowToken(), 0);
                    }
                }

            });

            menu.setOnClosedListener(new SlidingMenu.OnClosedListener() {

                @Override
                public void onClosed() {

                    invalidateOptionsMenu();

                    if (emojiOpen) {
                        messageScreen = (LinearLayout) findViewById(R.id.messageScreen);

                        messageScreen.removeView(tabs);
                        messageScreen.removeView(vp);
                        emojiOpen = false;
                    }

                    if (emoji2Open) {
                        messageScreen2 = (LinearLayout) findViewById(R.id.messageScreen2);

                        messageScreen2.removeView(tabs);
                        messageScreen2.removeView(vp);
                        emoji2Open = false;
                    }

                    try {
                        if (!settings.useTitleBar || settings.alwaysShowContactInfo || settings.titleContactImages) {
                            if (ab != null) {
                                try {
                                    new Thread(new Runnable() {

                                        @Override
                                        public void run() {
                                            if (conversations.size() != 0) {
                                                final String title = ContactUtil.findContactName(ContactUtil.findContactNumber(conversations.get(mViewPager.getCurrentItem()).getNumber(), context), context);
                                                int scale = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, resources.getDisplayMetrics());
                                                Bitmap image = Bitmap.createScaledBitmap(ContactUtil.getFacebookPhoto(ContactUtil.findContactNumber(conversations.get(mViewPager.getCurrentItem()).getNumber(), context), context), scale, scale, true);
                                                final BitmapDrawable image2 = new BitmapDrawable(image);

                                                ((MainActivity) context).getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {

                                                    @Override
                                                    public void run() {
                                                        if (!settings.useTitleBar || settings.alwaysShowContactInfo) {
                                                            ab.setTitle(title);

                                                            Locale sCachedLocale = Locale.getDefault();
                                                            int sFormatType = PhoneNumberUtils.getFormatTypeForLocale(sCachedLocale);
                                                            Editable editable = new SpannableStringBuilder(ContactUtil.findContactNumber(conversations.get(mViewPager.getCurrentItem()).getNumber(), context));
                                                            PhoneNumberUtils.formatNumber(editable, sFormatType);
                                                            ab.setSubtitle(editable.toString());

                                                            if (ab.getTitle().equals(ab.getSubtitle())) {
                                                                ab.setSubtitle(null);
                                                            }

                                                            if (conversations.get(mViewPager.getCurrentItem()).getGroup()) {
                                                                ab.setTitle("Group MMS");
                                                                ab.setSubtitle(null);
                                                            }
                                                        }

                                                        if (settings.titleContactImages) {
                                                            ab.setIcon(image2);
                                                        }
                                                    }

                                                });
                                            }
                                        }

                                    }).start();
                                } catch (Exception e) {
                                    ab.setTitle(R.string.app_name_in_app);
                                    ab.setIcon(R.drawable.ic_launcher);
                                }
                            }

                            if (conversations.size() == 0 && ab != null) {
                                ab.setIcon(R.drawable.ic_launcher);
                            }
                        }

                        ab.setDisplayHomeAsUpEnabled(true);
                    } catch (Exception e) {
                        // no action bar, dialog theme
                    }

                    messageEntry.requestFocus();
                }

            });
        } else {
            mViewPager.setBackgroundDrawable(new ColorDrawable(settings.ctMessageListBackground));
        }

        messageBar = new MessageBar(this);

        drafts = new ArrayList<String>();
        draftNames = new ArrayList<Long>();
        draftChanged = new ArrayList<Boolean>();
        draftsToDelete = new ArrayList<Long>();

        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int arg0) {

                if (mViewPager.getCurrentItem() < appMsgConversations && appMsg.isShowing() && dismissCrouton) {
                    appMsg.cancel();
                    appMsgConversations = 0;
                }

                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        String title = "";
                        String subtitle = "";

                        if (!settings.useTitleBar || settings.alwaysShowContactInfo) {
                            if (conversations.get(mViewPager.getCurrentItem()).getGroup()) {
                                title = "Group MMS";
                                subtitle = null;
                            } else {
                                title = ContactUtil.findContactName(ContactUtil.findContactNumber(conversations.get(mViewPager.getCurrentItem()).getNumber(), context), context);

                                Locale sCachedLocale = Locale.getDefault();
                                int sFormatType = PhoneNumberUtils.getFormatTypeForLocale(sCachedLocale);
                                Editable editable = new SpannableStringBuilder(ContactUtil.findContactNumber(conversations.get(mViewPager.getCurrentItem()).getNumber(), context));
                                PhoneNumberUtils.formatNumber(editable, sFormatType);
                                subtitle = editable.toString();

                                if (title.equals(subtitle)) {
                                    subtitle = null;
                                }
                            }
                        }

                        final String titleF = title, subtitleF = subtitle;

                        BitmapDrawable image2 = null;

                        if (settings.titleContactImages) {
                            int scale = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, resources.getDisplayMetrics());
                            Bitmap image = Bitmap.createScaledBitmap(ContactUtil.getFacebookPhoto(ContactUtil.findContactNumber(conversations.get(mViewPager.getCurrentItem()).getNumber(), context), context), scale, scale, true);
                            image2 = new BitmapDrawable(image);
                        }

                        final BitmapDrawable icon = image2;

                        boolean contains = false;
                        int where = -1;
                        int index = -1;

                        if (settings.enableDrafts) {
                            try {
                                for (int i = 0; i < draftNames.size(); i++) {
                                    if (draftNames.get(i) == (newDraft)) {
                                        contains = true;
                                        where = i;
                                        break;
                                    }
                                }

                                if (!contains && messageEntry.getText().toString().trim().length() > 0) {
                                    draftNames.add(newDraft);
                                    drafts.add(messageEntry.getText().toString());
                                    draftChanged.add(true);
                                } else if (contains && messageEntry.getText().toString().trim().length() > 0) {
                                    drafts.set(where, messageEntry.getText().toString());
                                    draftChanged.set(where, true);
                                } else if (contains && messageEntry.getText().toString().trim().length() == 0 && fromDraft) {
                                    draftsToDelete.add(draftNames.get(where));
                                    draftNames.remove(where);
                                    drafts.remove(where);
                                    draftChanged.remove(where);
                                }
                            } catch (Exception e) {
                            }

                            newDraft = 0;

                            try {
                                for (int i = 0; i < draftNames.size(); i++) {
                                    if (draftNames.get(i).equals(conversations.get(mViewPager.getCurrentItem()).getThreadId())) {
                                        index = i;
                                        break;
                                    }
                                }
                            } catch (Exception e) {
                            }
                        }

                        final int indexF = index;

                        boolean voicePerformClick = false;

                        if (settings.voiceAccount != null) {
                            if (threadsThroughVoice.contains(conversations.get(mViewPager.getCurrentItem()).getThreadId())) {
                                if (!settings.voiceEnabled) {
                                    voicePerformClick = true;
                                }
                            } else {
                                if (settings.voiceEnabled) {
                                    voicePerformClick = true;
                                }
                            }
                        }

                        final boolean performClick = voicePerformClick;

                        ((Activity) context).getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {

                            @Override
                            public void run() {
                                try {
                                    View row = menuLayout.getChildAt(mViewPager.getCurrentItem());
                                    if (!settings.customBackground) {
                                        row.setBackgroundColor(settings.ctConversationListBackground);
                                    }
                                } catch (Exception e) {
                                }

                                if ((!settings.useTitleBar || settings.alwaysShowContactInfo) && ab != null) {
                                    ab.setTitle(titleF);
                                    ab.setSubtitle(subtitleF);
                                }

                                if (settings.titleContactImages && ab != null) {
                                    ab.setIcon(icon);
                                }

                                if (settings.enableDrafts) {
                                    if (deleteDraft) {
                                        if (!messageEntry.getText().toString().equals("")) {
                                            messageEntry.setText("");
                                        }

                                        fromDraft = false;

                                        if (indexF != -1) {
                                            if (settings.autoInsertDrafts) {
                                                fromDraft = true;
                                                messageEntry.setText(drafts.get(indexF));
                                                messageEntry.setSelection(drafts.get(indexF).length());
                                            } else {
                                                messageBar.setOnClickListener(new MessageBar.OnMessageClickListener() {
                                                    @Override
                                                    public void onMessageClick(Parcelable token) {
                                                        fromDraft = true;
                                                        messageEntry.setText(drafts.get(indexF));
                                                        messageEntry.setSelection(drafts.get(indexF).length());
                                                    }
                                                });

                                                if (!menu.isMenuShowing()) {
                                                    messageBar.show(getString(R.string.draft_found), getString(R.string.apply_draft));
                                                }
                                            }
                                        }
                                    } else {
                                        fromDraft = true;
                                    }
                                }

                                if (performClick) {
                                    voiceButton.performClick();
                                }
                            }

                        });

                        for (int j = 0; j < newMessages.size(); j++) {
                            if (newMessages.get(j).replaceAll("-", "").endsWith(ContactUtil.findContactName(ContactUtil.findContactNumber(conversations.get(mViewPager.getCurrentItem()).getNumber(), context), context).replace("-", ""))) {
                                newMessages.remove(j);
                            }
                        }
                    }

                }).start();
            }
        });

        mViewPager.setOffscreenPageLimit(1);

        if (settings.runAs.equals("card2") || settings.runAs.equals("card+")) {
            mViewPager.setOffscreenPageLimit(2);
            int scale = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -26, resources.getDisplayMetrics());
            mViewPager.setPageMargin(scale);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // Associate searchable configuration with the SearchView
        final SearchView searchView =
                (SearchView) menu.findItem(R.id.menu_search).getActionView();
        searchView.setQueryHint("Search");
        searchView.setIconifiedByDefault(true);
        searchView.setSubmitButtonEnabled(false);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                String query = searchView.getQuery().toString();

                Intent intent = new Intent(getBaseContext(), SearchActivity.class);
                intent.putExtra("query", query);
                startActivity(intent);
                overridePendingTransition(R.anim.activity_slide_in_right, R.anim.activity_slide_out_left);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });

        final int MENU_CALL = 0;
        final int MENU_SCHEDULED = 1;
        final int MENU_ATTACH = 2;
        final int MENU_SEARCH = 3;
        final int MENU_NEW_MESSAGE = 4;
        final int MENU_DELETE = 5;
        final int MENU_TEMPLATE = 6;
        final int MENU_SUBJECT = 7;
        final int DELETE_CONVERSATION = 8;
        final int MENU_MARK_ALL_READ = 9;
        final int COPY_SENDER = 10;
        final int MENU_REFRESH_VOICE = 11;
        final int MENU_SETTINGS = 12;
        final int MENU_ABOUT = 13;

        try {
            if (menu != null) {
                if (conversations.size() == 0 || MainActivity.menu.isMenuShowing() || mDrawerLayout.isDrawerOpen(Gravity.RIGHT)) {
                    if (MainActivity.menu.isMenuShowing()) {
                        menu.getItem(MENU_CALL).setVisible(false);
                        menu.getItem(MENU_SCHEDULED).setVisible(false);
                        menu.getItem(MENU_ATTACH).setVisible(false);
                        menu.getItem(MENU_SEARCH).setVisible(true);
                        menu.getItem(MENU_NEW_MESSAGE).setVisible(true);
                        menu.getItem(MENU_DELETE).setVisible(true);
                        menu.getItem(MENU_TEMPLATE).setVisible(false);
                        menu.getItem(MENU_SUBJECT).setVisible(false);
                        menu.getItem(DELETE_CONVERSATION).setVisible(false);
                        menu.getItem(MENU_MARK_ALL_READ).setVisible(true);
                        menu.getItem(COPY_SENDER).setVisible(false);
                    } else if (mDrawerLayout.isDrawerOpen(Gravity.RIGHT)) {
                        menu.getItem(MENU_CALL).setVisible(false);
                        menu.getItem(MENU_SCHEDULED).setVisible(true);
                        menu.getItem(MENU_ATTACH).setVisible(true);
                        menu.getItem(MENU_SEARCH).setVisible(false);
                        menu.getItem(MENU_NEW_MESSAGE).setVisible(false);
                        menu.getItem(MENU_DELETE).setVisible(false);
                        menu.getItem(MENU_TEMPLATE).setVisible(true);
                        menu.getItem(MENU_SUBJECT).setVisible(true);
                        menu.getItem(DELETE_CONVERSATION).setVisible(false);
                        menu.getItem(MENU_MARK_ALL_READ).setVisible(false);
                        menu.getItem(COPY_SENDER).setVisible(false);
                    }
                } else {
                    menu.getItem(MENU_CALL).setVisible(true);
                    menu.getItem(MENU_SCHEDULED).setVisible(false);
                    menu.getItem(MENU_ATTACH).setVisible(true);
                    menu.getItem(MENU_SEARCH).setVisible(false);
                    menu.getItem(MENU_NEW_MESSAGE).setVisible(true);
                    menu.getItem(MENU_DELETE).setVisible(true);
                    menu.getItem(MENU_TEMPLATE).setVisible(true);
                    menu.getItem(MENU_SUBJECT).setVisible(true);
                    menu.getItem(DELETE_CONVERSATION).setVisible(true);
                    menu.getItem(MENU_MARK_ALL_READ).setVisible(true);
                    menu.getItem(COPY_SENDER).setVisible(true);

                    if (conversations.get(mViewPager.getCurrentItem()).getGroup()) {
                        menu.getItem(MENU_CALL).setVisible(false);
                        menu.getItem(COPY_SENDER).setVisible(false);
                    }
                }
            } else {
                if (conversations.size() == 0 || MainActivity.menu.isMenuShowing()) {
                    menu.getItem(MENU_CALL).setVisible(false);
                    menu.getItem(MENU_NEW_MESSAGE).setVisible(false);
                    menu.getItem(MENU_DELETE).setVisible(false);
                    menu.getItem(DELETE_CONVERSATION).setVisible(false);
                    menu.getItem(MENU_MARK_ALL_READ).setVisible(false);
                    menu.getItem(COPY_SENDER).setVisible(false);
                } else {
                    menu.getItem(MENU_CALL).setVisible(true);
                    menu.getItem(MENU_NEW_MESSAGE).setVisible(true);
                    menu.getItem(MENU_DELETE).setVisible(true);
                    menu.getItem(DELETE_CONVERSATION).setVisible(true);
                    menu.getItem(MENU_MARK_ALL_READ).setVisible(true);
                    menu.getItem(COPY_SENDER).setVisible(true);

                    if (conversations.get(mViewPager.getCurrentItem()).getGroup()) {
                        menu.getItem(MENU_CALL).setVisible(false);
                        menu.getItem(COPY_SENDER).setVisible(false);
                    }
                }
            }
        } catch (Exception e) {
        }

        if (settings.voiceAccount != null) {
            menu.getItem(MENU_REFRESH_VOICE).setVisible(true);
        } else {
            menu.getItem(MENU_REFRESH_VOICE).setVisible(false);
        }

        if (settings.lightActionBar) {
            Drawable callButton = resources.getDrawable(R.drawable.ic_menu_call);
            callButton.setColorFilter(resources.getColor(R.color.hangouts_ab_icon), Mode.MULTIPLY);
            menu.getItem(MENU_CALL).setIcon(callButton);

            Drawable scheduledButton = resources.getDrawable(R.drawable.ic_scheduled);
            scheduledButton.setColorFilter(resources.getColor(R.color.hangouts_ab_icon), Mode.MULTIPLY);
            menu.getItem(MENU_SCHEDULED).setIcon(scheduledButton);

            Drawable attachButton = resources.getDrawable(R.drawable.ic_attach);
            attachButton.setColorFilter(resources.getColor(R.color.hangouts_ab_icon), Mode.MULTIPLY);
            menu.getItem(MENU_ATTACH).setIcon(attachButton);

            Drawable searchButton = resources.getDrawable(R.drawable.ic_search);
            searchButton.setColorFilter(resources.getColor(R.color.hangouts_ab_icon), Mode.MULTIPLY);
            menu.getItem(MENU_SEARCH).setIcon(searchButton);

            Drawable replyButton = resources.getDrawable(R.drawable.ic_reply);
            replyButton.setColorFilter(resources.getColor(R.color.hangouts_ab_icon), Mode.MULTIPLY);
            menu.getItem(MENU_NEW_MESSAGE).setIcon(replyButton);
        } else {
            Drawable callButton = resources.getDrawable(R.drawable.ic_menu_call);
            callButton.setColorFilter(resources.getColor(R.color.white), Mode.MULTIPLY);
            menu.getItem(MENU_CALL).setIcon(callButton);

            Drawable scheduledButton = resources.getDrawable(R.drawable.ic_scheduled);
            scheduledButton.setColorFilter(resources.getColor(R.color.white), Mode.MULTIPLY);
            menu.getItem(MENU_SCHEDULED).setIcon(scheduledButton);

            Drawable attachButton = resources.getDrawable(R.drawable.ic_attach);
            attachButton.setColorFilter(resources.getColor(R.color.white), Mode.MULTIPLY);
            menu.getItem(MENU_ATTACH).setIcon(attachButton);

            Drawable searchButton = resources.getDrawable(R.drawable.ic_search);
            searchButton.setColorFilter(resources.getColor(R.color.white), Mode.MULTIPLY);
            menu.getItem(MENU_SEARCH).setIcon(searchButton);

            Drawable replyButton = resources.getDrawable(R.drawable.ic_reply);
            replyButton.setColorFilter(resources.getColor(R.color.white), Mode.MULTIPLY);
            menu.getItem(MENU_NEW_MESSAGE).setIcon(replyButton);
        }

        return true;
    }

    public String version;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_new_message:
                if (mDrawerLayout.isDrawerOpen(Gravity.RIGHT)) {
                    mDrawerLayout.closeDrawer(Gravity.RIGHT);
                } else {
                    if (menu != null) {
                        if (menu.isMenuShowing())
                            menu.toggle();
                    }
                    mDrawerLayout.openDrawer(Gravity.RIGHT);
                }

                return true;
            case R.id.menu_settings:
                startActivityForResult(new Intent(this, SettingsPagerActivity.class), SETTINGS_RESULT);
                //finish();
                overridePendingTransition(R.anim.activity_slide_in_right, R.anim.activity_slide_out_left);
                return true;
            case R.id.menu_scheduled:
                Intent scheduled = new Intent(this, NewScheduledSms.class);

                scheduled.putExtra(EXTRA_NUMBER, "");
                scheduled.putExtra(EXTRA_DATE, "");
                scheduled.putExtra(EXTRA_REPEAT, "0");
                scheduled.putExtra(EXTRA_MESSAGE, "");

                startActivity(scheduled);

                overridePendingTransition(R.anim.activity_slide_in_right, R.anim.activity_slide_out_left);
                return true;
            case R.id.menu_about:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.menu_about);
                version = "";

                try {
                    version = this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName;
                } catch (NameNotFoundException e) {
                    e.printStackTrace();
                }

                builder.setMessage(resources.getString(R.string.version) + ": " + version +
                        "\n\n" + resources.getString(R.string.about_expanded) + "\n\n© 2013 Jacob Klinker and Luke Klinker")
                        .setPositiveButton(resources.getString(R.string.changelog), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent wizardintent = new Intent(getApplicationContext(), ChangeLogMain.class);
                                wizardintent.putExtra("version", version);
                                startActivity(wizardintent);
                            }
                        })
                        .setNegativeButton(resources.getString(R.string.tweet_us), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                                sharingIntent.setType("text/plain");
                                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, "@slidingSMS ");
                                startActivity(Intent.createChooser(sharingIntent, "Share using"));
                            }
                        });

                AlertDialog dialog = builder.create();
                dialog.show();
                return true;
            case android.R.id.home:
                if (menu != null) {
                    menu.showMenu();
                }

                return true;
            case R.id.menu_attach:
                menuAttachImage();

                return true;
            case R.id.menu_call:
                try {
                    Intent callIntent = new Intent(Intent.ACTION_CALL);
                    callIntent.setData(Uri.parse("tel:" + ContactUtil.findContactNumber(conversations.get(mViewPager.getCurrentItem()).getNumber(), this)));
                    callIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(callIntent);
                } catch (Exception e) {
                    Toast.makeText(this, "No contact to call", Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.menu_delete:
                if (menu != null) {
                    if (menu.isMenuShowing()) {
                        Intent intent = new Intent(this, BatchDeleteAllActivity.class);
                        startActivity(intent);
                        overridePendingTransition(R.anim.activity_slide_in_right, R.anim.activity_slide_out_left);
                    } else {
                        try {
                            Intent intent = new Intent(this, BatchDeleteConversationActivity.class);
                            intent.putExtra("threadId", conversations.get(mViewPager.getCurrentItem()).getThreadId());
                            startActivityForResult(intent, SETTINGS_RESULT);
                            overridePendingTransition(R.anim.activity_slide_in_right, R.anim.activity_slide_out_left);
                        } catch (Exception e) {
                            Toast.makeText(this, "No Messages", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    Intent intent = new Intent(this, BatchDeleteAllActivity.class);
                    startActivityForResult(intent, SETTINGS_RESULT);
                    overridePendingTransition(R.anim.activity_slide_in_right, R.anim.activity_slide_out_left);
                }

                return true;
            case R.id.menu_template:
                AlertDialog.Builder template = new AlertDialog.Builder(this);
                template.setTitle(resources.getString(R.string.insert_template));

                ListView templates = new ListView(this);

                TextView footer = new TextView(this);
                footer.setText(resources.getString(R.string.add_templates));
                int scale = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, resources.getDisplayMetrics());
                footer.setPadding(scale, scale, scale, scale);

                templates.addFooterView(footer);

                final ArrayList<String> text = IOUtil.readTemplates(this);
                TemplateArrayAdapter adapter = new TemplateArrayAdapter(this, text);
                templates.setAdapter(adapter);

                template.setView(templates);
                final AlertDialog templateDialog = template.create();
                templateDialog.show();

                footer.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        templateDialog.dismiss();
                        Intent i = new Intent(view.getContext(), TemplateActivity.class);
                        startActivity(i);
                        overridePendingTransition(R.anim.activity_slide_in_right, R.anim.activity_slide_out_left);
                    }
                });

                templates.setOnItemClickListener(new OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> arg0, View arg1,
                                            int arg2, long arg3) {
                        try {
                            boolean newMessage = mDrawerLayout.isDrawerOpen(Gravity.RIGHT);

                            if (newMessage) {
                                messageEntry2.setText(text.get(arg2));
                                templateDialog.cancel();
                            } else {
                                messageEntry.setText(text.get(arg2));
                                messageEntry.setSelection(text.get(arg2).length());
                                templateDialog.cancel();
                            }
                        } catch (Exception e) {
                        }

                    }

                });

                return true;
            case R.id.copy_sender:
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Copied Address", ContactUtil.findContactNumber(conversations.get(mViewPager.getCurrentItem()).getNumber(), this));
                clipboard.setPrimaryClip(clip);

                Toast.makeText(this, R.string.text_saved, Toast.LENGTH_SHORT).show();
                return true;
            case R.id.delete_conversation:
                AlertDialog.Builder deleteDialog = new AlertDialog.Builder(this);
                deleteDialog.setTitle(resources.getString(R.string.delete_conversation));
                deleteDialog.setMessage(resources.getString(R.string.delete_conversation_message));
                deleteDialog.setPositiveButton(resources.getString(R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        final ProgressDialog progDialog = new ProgressDialog(context);
                        progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                        progDialog.setMessage(resources.getString(R.string.deleting));
                        progDialog.show();

                        new Thread(new Runnable() {

                            @Override
                            public void run() {
                                Looper.prepare();
                                deleteSMS(context, conversations.get(mViewPager.getCurrentItem()).getThreadId(), progDialog);
                            }

                        }).start();
                    }
                });
                deleteDialog.setNegativeButton(resources.getString(R.string.no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });

                deleteDialog.create().show();

                return true;
            case R.id.menu_mark_all_read:
                startService(new Intent(getBaseContext(), QmMarkRead.class));
                return true;
            case R.id.menu_refreshVoice:
                startService(new Intent(this, VoiceReceiver.class));
                return true;
            case R.id.menu_subject:
                Toast.makeText(this, getString(R.string.converting_mms), Toast.LENGTH_SHORT).show();

                if (mDrawerLayout.isDrawerOpen(Gravity.RIGHT)) {
                    if (subjectLine2.getVisibility() == View.GONE) {
                        subjectLine2.setVisibility(View.VISIBLE);
                        subjectEntry2.requestFocusFromTouch();
                        final InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputManager.showSoftInput(subjectLine2, 0);

                        subjectDelete2.setColorFilter(settings.ctSendButtonColor);
                        subjectDelete2.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                subjectLine2.setVisibility(View.GONE);
                                subjectEntry2.setText("");
                                messageEntry2.requestFocusFromTouch();
                                inputManager.showSoftInput(messageEntry2, 0);
                            }
                        });
                    }
                } else {
                    if (subjectLine.getVisibility() == View.GONE) {
                        subjectLine.setVisibility(View.VISIBLE);
                        subjectEntry.requestFocusFromTouch();
                        final InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputManager.showSoftInput(subjectLine, 0);

                        subjectDelete.setColorFilter(settings.ctSendButtonColor);
                        subjectDelete.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                subjectLine.setVisibility(View.GONE);
                                subjectEntry.setText("");
                                messageEntry.requestFocusFromTouch();
                                inputManager.showSoftInput(messageEntry, 0);
                            }
                        });
                    }

                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void menuAttachImage() {
        multipleAttachments = false;
        AttachMore.data = new ArrayList<MMSPart>();

        if (menu != null) {
            newMessage = mDrawerLayout.isDrawerOpen(Gravity.RIGHT);
        } else {
            newMessage = mDrawerLayout.isDrawerOpen(Gravity.RIGHT);
        }

        if (newMessage) {
            AlertDialog.Builder attachBuilder = new AlertDialog.Builder(this);
            attachBuilder.setItems(R.array.selectImage, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface arg0, int arg1) {
                    switch (arg1) {
                        case 0:
                            Intent intent = new Intent();
                            intent.setType("image/*");
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            startActivityForResult(Intent.createChooser(intent, resources.getString(R.string.select_picture)), 2);

                            break;
                        case 1:
                            Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            File f = new File(Environment.getExternalStorageDirectory() + "/SlidingMessaging/", "photoToSend.png");
                            capturedPhotoUri = Uri.fromFile(f);
                            captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, capturedPhotoUri);
                            startActivityForResult(captureIntent, 4);
                            break;
                        case 2:
                            Intent attachMore = new Intent(context, AttachMore.class);
                            startActivityForResult(attachMore, 6);
                            break;
                    }

                }

            });

            attachBuilder.create().show();
        } else {
            attachedPosition = mViewPager.getCurrentItem();

            AlertDialog.Builder attachBuilder = new AlertDialog.Builder(this);
            attachBuilder.setItems(R.array.selectImage, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface arg0, int arg1) {
                    switch (arg1) {
                        case 0:
                            Intent intent = new Intent();
                            intent.setType("image/*");
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            startActivityForResult(Intent.createChooser(intent, resources.getString(R.string.select_picture)), 1);

                            break;
                        case 1:
                            Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            File f = new File(Environment.getExternalStorageDirectory() + "/SlidingMessaging/", "photoToSend.png");
                            capturedPhotoUri = Uri.fromFile(f);
                            captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, capturedPhotoUri);
                            startActivityForResult(captureIntent, 3);
                            break;
                        case 2:
                            Intent attachMore = new Intent(context, AttachMore.class);
                            startActivityForResult(attachMore, 5);
                            break;
                    }

                }

            });

            attachBuilder.create().show();
        }
    }

    public void deleteSMS(final Context context, final long id, final ProgressDialog progDialog) {
        if (checkLocked(context, id)) {
            ((Activity) context).getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {

                @Override
                public void run() {
                    new AlertDialog.Builder(context)
                            .setTitle(R.string.locked_messages)
                            .setMessage(R.string.locked_messages_summary)
                            .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            deleteLocked(context, id);

                                            ((Activity) context).getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {

                                                @Override
                                                public void run() {
                                                    ((MainActivity) context).refreshViewPager();
                                                    progDialog.dismiss();
                                                }

                                            });
                                        }
                                    }).start();
                                }
                            })
                            .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            dontDeleteLocked(context, id);

                                            ((Activity) context).getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {

                                                @Override
                                                public void run() {
                                                    ((MainActivity) context).refreshViewPager();
                                                    progDialog.dismiss();
                                                }

                                            });
                                        }
                                    }).start();
                                }
                            })
                            .create()
                            .show();
                }

            });
        } else {
            deleteLocked(context, id);

            ((Activity) context).getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {

                @Override
                public void run() {
                    ((MainActivity) context).refreshViewPager();
                    progDialog.dismiss();
                }

            });
        }
    }

    public boolean checkLocked(Context context, long id) {
        try {
            return context.getContentResolver().query(Uri.parse("content://mms-sms/locked/" + id + "/"), new String[]{"_id"}, null, null, null).moveToFirst();
        } catch (Exception e) {
            return false;
        }
    }

    public void deleteLocked(Context context, long id) {
        context.getContentResolver().delete(Uri.parse("content://mms-sms/conversations/" + id + "/"), null, null);
        context.getContentResolver().delete(Uri.parse("content://mms-sms/conversations/"), "_id=?", new String[]{id + ""});
    }

    public void dontDeleteLocked(Context context, long id) {
        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
        ops.add(ContentProviderOperation.newDelete(Uri.parse("content://mms-sms/conversations/" + id + "/"))
                .withSelection("locked=?", new String[]{"0"})
                .build());
        try {
            context.getContentResolver().applyBatch("mms-sms", ops);
        } catch (RemoteException e) {
        } catch (OperationApplicationException e) {
        }
    }

    @SuppressWarnings("deprecation")
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        popupAttaching = false;
        attachOnSend = false;
        sendButton.setImageResource(R.drawable.ic_action_send_white);

        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                final Uri selectedImage = imageReturnedIntent.getData();
                attachedImage = selectedImage;
                fromCamera = false;

                imageAttachBackground.setBackgroundColor(settings.ctMessageListBackground);
                Drawable attachBack = resources.getDrawable(R.drawable.attachment_editor_bg);
                attachBack.setColorFilter(settings.ctSentMessageBackground, Mode.MULTIPLY);
                imageAttach.setBackgroundDrawable(attachBack);
                imageAttachBackground.setVisibility(View.VISIBLE);
                imageAttach.setVisibility(true);

                try {
                    imageAttach.setImage("send_image", SendUtil.getThumbnail(this, selectedImage));
                } catch (Exception e) {
                    Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
                    imageAttach.setVisibility(false);
                    imageAttachBackground.setVisibility(View.GONE);
                }

                Button viewImage = (Button) findViewById(R.id.view_image_button);
                Button replaceImage = (Button) findViewById(R.id.replace_image_button);
                Button removeImage = (Button) findViewById(R.id.remove_image_button);

                viewImage.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        context.startActivity(new Intent(Intent.ACTION_VIEW, selectedImage));

                    }

                });

                replaceImage.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent();
                        intent.setType("image/*");
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        startActivityForResult(Intent.createChooser(intent, resources.getString(R.string.select_picture)), 1);

                    }

                });

                removeImage.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        imageAttach.setVisibility(false);
                        imageAttachBackground.setVisibility(View.GONE);

                    }

                });

                if (menu != null) {
                    MainActivity.menu.showContent();
                }
                mViewPager.setCurrentItem(attachedPosition);

            }
        } else if (requestCode == 2) {
            if (resultCode == RESULT_OK) {
                final Uri selectedImage = imageReturnedIntent.getData();
                attachedImage2 = selectedImage;
                fromCamera = false;

                imageAttachBackground2.setBackgroundColor(settings.ctConversationListBackground);
                Drawable attachBack = resources.getDrawable(R.drawable.attachment_editor_bg);
                attachBack.setColorFilter(settings.ctSentMessageBackground, Mode.MULTIPLY);
                imageAttach2.setBackgroundDrawable(attachBack);
                imageAttachBackground2.setVisibility(View.VISIBLE);
                imageAttach2.setVisibility(true);

                try {
                    imageAttach2.setImage("send_image", SendUtil.getThumbnail(this, selectedImage));
                } catch (Exception e) {
                    Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
                    imageAttach2.setVisibility(false);
                    imageAttachBackground2.setVisibility(View.GONE);
                }

                Button viewImage = (Button) findViewById(R.id.view_image_button2);
                Button replaceImage = (Button) findViewById(R.id.replace_image_button2);
                Button removeImage = (Button) findViewById(R.id.remove_image_button2);

                viewImage.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        context.startActivity(new Intent(Intent.ACTION_VIEW, selectedImage));

                    }

                });

                replaceImage.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent();
                        intent.setType("image/*");
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        startActivityForResult(Intent.createChooser(intent, resources.getString(R.string.select_picture)), 2);

                    }

                });

                removeImage.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        imageAttach2.setVisibility(false);
                        imageAttachBackground2.setVisibility(View.GONE);

                    }

                });
                mDrawerLayout.openDrawer(Gravity.RIGHT);
            }
        } else if (requestCode == 3) {
            if (resultCode == Activity.RESULT_OK) {
                getContentResolver().notifyChange(capturedPhotoUri, null);
                attachedImage = Uri.fromFile(new File(Environment.getExternalStorageDirectory() + "/SlidingMessaging/", "photoToSend.png"));
                fromCamera = true;

                imageAttachBackground.setBackgroundColor(settings.ctConversationListBackground);
                Drawable attachBack = resources.getDrawable(R.drawable.attachment_editor_bg);
                attachBack.setColorFilter(settings.ctSentMessageBackground, Mode.MULTIPLY);
                imageAttach.setBackgroundDrawable(attachBack);
                imageAttachBackground.setVisibility(View.VISIBLE);
                imageAttach.setVisibility(true);

                try {
                    Bitmap image = SendUtil.getThumbnail(this, capturedPhotoUri);
                    imageAttach.setImage("send_image", image);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
                    imageAttach.setVisibility(false);
                    imageAttachBackground.setVisibility(View.GONE);
                }

                Button viewImage = (Button) findViewById(R.id.view_image_button);
                Button replaceImage = (Button) findViewById(R.id.replace_image_button);
                Button removeImage = (Button) findViewById(R.id.remove_image_button);

                viewImage.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_VIEW);
                        intent.setDataAndType(Uri.fromFile(new File(Environment.getExternalStorageDirectory() + "/SlidingMessaging/", "photoToSend.png")), "image/*");
                        context.startActivity(intent);

                    }

                });

                replaceImage.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent();
                        intent.setType("image/*");
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        startActivityForResult(Intent.createChooser(intent, resources.getString(R.string.select_picture)), 1);

                    }

                });

                removeImage.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        imageAttach.setVisibility(false);
                        imageAttachBackground.setVisibility(View.GONE);

                    }

                });
            }
        } else if (requestCode == 4) {
            if (resultCode == Activity.RESULT_OK) {
                getContentResolver().notifyChange(capturedPhotoUri, null);
                attachedImage2 = Uri.fromFile(new File(Environment.getExternalStorageDirectory() + "/SlidingMessaging/", "photoToSend.png"));
                fromCamera = true;

                imageAttachBackground2.setBackgroundColor(settings.ctConversationListBackground);
                Drawable attachBack = resources.getDrawable(R.drawable.attachment_editor_bg);
                attachBack.setColorFilter(settings.ctSentMessageBackground, Mode.MULTIPLY);
                imageAttach2.setBackgroundDrawable(attachBack);
                imageAttachBackground2.setVisibility(View.VISIBLE);
                imageAttach2.setVisibility(true);

                try {
                    Bitmap image = SendUtil.getThumbnail(this, capturedPhotoUri);
                    imageAttach2.setImage("send_image", image);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
                    imageAttach2.setVisibility(false);
                    imageAttachBackground2.setVisibility(View.GONE);
                }

                Button viewImage = (Button) findViewById(R.id.view_image_button2);
                Button replaceImage = (Button) findViewById(R.id.replace_image_button2);
                Button removeImage = (Button) findViewById(R.id.remove_image_button2);

                viewImage.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_VIEW);
                        intent.setDataAndType(Uri.fromFile(new File(Environment.getExternalStorageDirectory() + "/SlidingMessaging/", "photoToSend.png")), "image/*");
                        context.startActivity(intent);

                    }

                });

                replaceImage.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent();
                        intent.setType("image/*");
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        startActivityForResult(Intent.createChooser(intent, resources.getString(R.string.select_picture)), 1);

                    }

                });

                removeImage.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        imageAttach2.setVisibility(false);
                        imageAttachBackground2.setVisibility(View.GONE);

                    }

                });
                mDrawerLayout.openDrawer(Gravity.RIGHT);
            }
        } else if (requestCode == 5) {
            if (resultCode == Activity.RESULT_OK) {
                multipleAttachments = true;

                imageAttachBackground.setBackgroundColor(settings.ctConversationListBackground);
                Drawable attachBack = resources.getDrawable(R.drawable.attachment_editor_bg);
                attachBack.setColorFilter(settings.ctSentMessageBackground, Mode.MULTIPLY);
                imageAttach.setBackgroundDrawable(attachBack);
                imageAttachBackground.setVisibility(View.VISIBLE);
                imageAttach.setVisibility(true);

                try {
                    Bitmap bmp = BitmapFactory.decodeByteArray(AttachMore.data.get(0).Data, 0, AttachMore.data.get(0).Data.length);
                    Bitmap mutableBitmap = bmp.copy(Bitmap.Config.ARGB_8888, true);
                    imageAttach.setImage("send_image", mutableBitmap);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Error loading MMS", Toast.LENGTH_SHORT).show();
                    imageAttach.setVisibility(false);
                    imageAttachBackground.setVisibility(View.GONE);
                }

                Button viewImage = (Button) findViewById(R.id.view_image_button);
                Button replaceImage = (Button) findViewById(R.id.replace_image_button);
                Button removeImage = (Button) findViewById(R.id.remove_image_button);

                viewImage.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        Intent attachMore = new Intent(context, AttachMore.class);
                        startActivityForResult(attachMore, 5);

                    }

                });

                replaceImage.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        Intent attachMore = new Intent(context, AttachMore.class);
                        startActivityForResult(attachMore, 5);

                    }

                });

                removeImage.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        imageAttach.setVisibility(false);
                        imageAttachBackground.setVisibility(View.GONE);
                        multipleAttachments = false;
                        AttachMore.data = new ArrayList<MMSPart>();

                    }

                });
            }
        } else if (requestCode == 6) {
            if (resultCode == Activity.RESULT_OK) {
                multipleAttachments = true;

                imageAttachBackground2.setBackgroundColor(settings.ctConversationListBackground);
                Drawable attachBack = resources.getDrawable(R.drawable.attachment_editor_bg);
                attachBack.setColorFilter(settings.ctSentMessageBackground, Mode.MULTIPLY);
                imageAttach2.setBackgroundDrawable(attachBack);
                imageAttachBackground2.setVisibility(View.VISIBLE);
                imageAttach2.setVisibility(true);

                try {
                    Bitmap bmp = BitmapFactory.decodeByteArray(AttachMore.data.get(0).Data, 0, AttachMore.data.get(0).Data.length);
                    Bitmap mutableBitmap = bmp.copy(Bitmap.Config.ARGB_8888, true);
                    imageAttach2.setImage("send_image", mutableBitmap);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Error loading MMS", Toast.LENGTH_SHORT).show();
                    imageAttach2.setVisibility(false);
                    imageAttachBackground2.setVisibility(View.GONE);
                }

                Button viewImage = (Button) findViewById(R.id.view_image_button);
                Button replaceImage = (Button) findViewById(R.id.replace_image_button);
                Button removeImage = (Button) findViewById(R.id.remove_image_button);

                viewImage.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        Intent attachMore = new Intent(context, AttachMore.class);
                        startActivityForResult(attachMore, 6);

                    }

                });

                replaceImage.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        Intent attachMore = new Intent(context, AttachMore.class);
                        startActivityForResult(attachMore, 6);

                    }

                });

                removeImage.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        imageAttach2.setVisibility(false);
                        imageAttachBackground2.setVisibility(View.GONE);
                        multipleAttachments = false;
                        AttachMore.data = new ArrayList<MMSPart>();

                    }

                });

                mDrawerLayout.openDrawer(Gravity.RIGHT);
            }
        } else if (requestCode == REQ_ENTER_PATTERN) {
            switch (resultCode) {
                case RESULT_OK:
                    SharedPreferences.Editor prefEdit = sharedPrefs.edit();
                    prefEdit.putLong("last_time", Calendar.getInstance().getTimeInMillis());
                    prefEdit.commit();
                    break;
                case RESULT_CANCELED:
                    finish();
                    break;
                case LockPatternActivity.RESULT_FAILED:
                    Context context = getApplicationContext();
                    CharSequence text = "Incorrect Pattern!";
                    int duration = Toast.LENGTH_SHORT;

                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();

                    finish();

                    break;
            }
        } else if (requestCode == SETTINGS_RESULT) {
            mSectionsPagerAdapter.notifyDataSetChanged();
            recreate();
        }

        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
    }

    @Override
    public void onBackPressed() {
        if (emojiOpen || emoji2Open) {
            if (mDrawerLayout.isDrawerOpen(Gravity.RIGHT)) {
                if (emoji2Open) {
                    messageScreen2 = (LinearLayout) findViewById(R.id.messageScreen2);

                    emoji2Open = false;
                    messageScreen2.removeView(tabs);
                    messageScreen2.removeView(vp);

                    mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                }
            } else {
                if (emojiOpen) {
                    messageScreen = (LinearLayout) findViewById(R.id.messageScreen);

                    emojiOpen = false;
                    messageScreen.removeView(tabs);
                    messageScreen.removeView(vp);
                }
            }
        } else if (menu != null) {
            if (mDrawerLayout.isDrawerOpen(Gravity.RIGHT)) {
                if (mDrawerLayout.isDrawerOpen(Gravity.RIGHT)) {
                    mDrawerLayout.closeDrawer(Gravity.RIGHT);
                }
                if (!settings.openContactMenu) {
                    menu.showContent();
                } else {
                    menu.showMenu();
                }
            } else {
                if (menu.isMenuShowing() && !settings.openContactMenu) {
                    menu.showContent();
                } else if (!menu.isMenuShowing() && settings.openContactMenu) {
                    menu.showMenu();
                } else {
                    super.onBackPressed();
                }
            }
        } else {
            if (mDrawerLayout.isDrawerOpen(Gravity.RIGHT)) {
                mDrawerLayout.closeDrawer(Gravity.RIGHT);
            } else {
                super.onBackPressed();
            }
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // do nothing to save state...
    }

    @Override
    public void onResume() {
        super.onResume();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                int drawerWidth = (int) (mViewPager.getWidth() * 3 / 4.0);

                if (menu == null && getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
                    ListFragment newFragment = (ListFragment) getSupportFragmentManager().findFragmentById(R.id.menuList);
                    drawerWidth += (int) (newFragment.getListView().getWidth() * 3 / 4.0);
                }

                DrawerLayout.LayoutParams params = (DrawerLayout.LayoutParams) mDrawer.getLayoutParams();
                params.width = drawerWidth;
                mDrawer.setLayoutParams(params);
            }
        }, 1000);

        SlideOverService.restartHalo(this);

        Intent clearMessages = new Intent("com.klinker.android.messaging.CLEAR_MESSAGES");
        getApplicationContext().sendBroadcast(clearMessages);

        IntentFilter filter = new IntentFilter(Transaction.REFRESH);
        registerReceiver(refreshReceiver, filter);

        filter = new IntentFilter(Transaction.MMS_ERROR);
        registerReceiver(mmsError, filter);

        filter = new IntentFilter(Transaction.VOICE_FAILED);
        registerReceiver(voiceError, filter);

        filter = new IntentFilter(Transaction.MMS_PROGRESS);
        registerReceiver(mmsProgressReceiver, filter);

        filter = new IntentFilter("com.klinker.android.messaging.NEW_MMS");
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        filter.setPriority(3);
        registerReceiver(messageReceiver, filter);

        filter = new IntentFilter("com.klinker.android.messaging_donate.KILL_FOR_HALO");
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        registerReceiver(killReceiver, filter);

        if (isPopup && messageEntry.getText().toString().equals("")) {
            sendButton.setImageResource(R.drawable.ic_attach);
            attachOnSend = true;
        }

        if (menu != null) {
            String menuOption = sharedPrefs.getString("page_or_menu2", "2");

            if (menuOption.equals("2")) {
                menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
            } else if (menuOption.equals("1")) {
                menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
            } else {
                menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
            }

            if (imageAttach.getVisibility() == View.VISIBLE) {
                menu.showContent();
            }
        }

        if (whatToSend != null) {
            messageEntry.setText(whatToSend);
            whatToSend = null;
        }

        Intent updateWidget = new Intent("com.klinker.android.messaging.UPDATE_WIDGET");
        sendBroadcast(updateWidget);

        if (!settings.securityOption.equals("none")) {
            long currentTime = Calendar.getInstance().getTimeInMillis();
            long lastTime = sharedPrefs.getLong("last_time", 0);

            if (currentTime - lastTime > Long.parseLong(sharedPrefs.getString("timeout_settings", "300000"))) {
                if (sharedPrefs.getString("security_option", "none").equals("pin")) {
                    Intent passwordIntent = new Intent(getApplicationContext(), PinActivity.class);
                    startActivity(passwordIntent);
                    finish();
                } else if (sharedPrefs.getString("security_option", "none").equals("password")) {
                    Intent passwordIntent = new Intent(getApplicationContext(), PasswordActivity.class);
                    startActivity(passwordIntent);
                    finish();
                } else if (sharedPrefs.getString("security_option", "none").equals("pattern")) {
                    SecurityPrefs.setAutoSavePattern(this, true);
                    Intent intent = new Intent(LockPatternActivity.ACTION_COMPARE_PATTERN, null,
                            this, LockPatternActivity.class);
                    startActivityForResult(intent, REQ_ENTER_PATTERN);
                }
            }
        }

        if (sharedPrefs.getBoolean("run_voice_tutorial", true) && sharedPrefs.getString("voice_account", null) != (null)) {
            sharedPrefs.edit().putBoolean("run_voice_tutorial", false).commit();

            try { // try catch so if they change to landscape, which uses a linear layout instead, everything won't force close
                final WindowManager.LayoutParams arcParams = new WindowManager.LayoutParams(
                        WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                                | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                        PixelFormat.TRANSLUCENT);

                final WindowManager arcWindow = (WindowManager) getSystemService(WINDOW_SERVICE);
                final View voiceTutorial = getLayoutInflater().inflate(R.layout.google_voice_tutorial, null);

                final Button next = (Button) voiceTutorial.findViewById(R.id.next_button);
                final Button finish = (Button) voiceTutorial.findViewById(R.id.finish_button);
                final Button voice = (Button) voiceTutorial.findViewById(R.id.voice_button);
                final TextView text = (TextView) voiceTutorial.findViewById(R.id.voice_tutorial_text);

                currentVoiceTutorial = 0;

                voice.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        voiceButton.performClick();
                    }
                });

                next.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (currentVoiceTutorial == 0) {
                            text.setText(resources.getString(R.string.google_voice_tutorial_2));
                        } else if (currentVoiceTutorial == 1) {
                            text.setText(resources.getString(R.string.google_voice_tutorial_3));
                        } else if (currentVoiceTutorial == 2) {
                            text.setText(resources.getString(R.string.google_voice_tutorial_4));

                            next.setVisibility(View.GONE);
                            finish.setVisibility(View.VISIBLE);
                        }

                        currentVoiceTutorial++;
                    }
                });

                finish.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        arcWindow.removeViewImmediate(voiceTutorial);
                    }
                });

                arcWindow.addView(voiceTutorial, arcParams);

            } catch (ClassCastException e) {
            }
        }

        if (settings.enableDrafts) {
            try {
                drafts = new ArrayList<String>();
                draftNames = new ArrayList<Long>();
                draftChanged = new ArrayList<Boolean>();
                draftsToDelete = new ArrayList<Long>();

                Cursor query = getContentResolver().query(Uri.parse("content://sms/draft/"), new String[]{"thread_id", "body"}, null, null, null);

                if (query.moveToFirst()) {
                    do {
                        drafts.add(query.getString(query.getColumnIndex("body")));
                        draftNames.add(Long.parseLong(query.getString(query.getColumnIndex("thread_id"))));
                        draftChanged.add(false);
                    } while (query.moveToNext());
                }

                query.close();
            } catch (Exception e) {
            }

            int index = -1;

            try {
                for (int i = 0; i < draftNames.size(); i++) {
                    if (draftNames.get(i) == conversations.get(mViewPager.getCurrentItem()).getThreadId()) {
                        index = i;
                        break;
                    }
                }
            } catch (Exception e) {
            }

            fromDraft = false;

            if (index != -1) {
                if (settings.autoInsertDrafts) {
                    fromDraft = true;
                    messageEntry.setText(drafts.get(index));
                    messageEntry.setSelection(drafts.get(index).length());
                } else {
                    final int indexF = index;

                    messageBar.setOnClickListener(new MessageBar.OnMessageClickListener() {
                        @Override
                        public void onMessageClick(Parcelable token) {
                            fromDraft = true;
                            messageEntry.setText(drafts.get(indexF));
                            messageEntry.setSelection(drafts.get(indexF).length());
                        }
                    });

                    if (!menu.isMenuShowing()) {
                        messageBar.show(getString(R.string.draft_found), getString(R.string.apply_draft));
                    }
                }
            }
        }

        if (settings.voiceAccount != null && conversations.size() > 0) {
            if (threadsThroughVoice.contains(conversations.get(mViewPager.getCurrentItem()).getThreadId())) {
                if (!settings.voiceEnabled) {
                    voiceButton.performClick();
                }
            } else {
                if (settings.voiceEnabled) {
                    voiceButton.performClick();
                }
            }
        }

        messageEntry.requestFocus();
    }

    @Override
    public void onPause() {
        super.onPause();

        try {
            unregisterReceiver(refreshReceiver);
            unregisterReceiver(mmsError);
            unregisterReceiver(voiceError);
            unregisterReceiver(mmsProgressReceiver);
            unregisterReceiver(messageReceiver);
            unregisterReceiver(killReceiver);
        } catch (Exception e) {
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        Intent updateWidget = new Intent("com.klinker.android.messaging.UPDATE_WIDGET");
        sendBroadcast(updateWidget);

        if (settings.enableDrafts) {
            if (messageEntry.getText().toString().length() != 0) {
                draftChanged.add(true);
                draftNames.add(conversations.get(mViewPager.getCurrentItem()).getThreadId());
                drafts.add(messageEntry.getText().toString());
                messageEntry.setText("");
            }

            new Thread(new Runnable() {
                @Override
                public void run() {

                    try {
                        for (int i = 0; i < draftChanged.size(); i++) {
                            if (!draftChanged.get(i)) {
                                draftChanged.remove(i);
                                draftNames.remove(i);
                                drafts.remove(i);
                                i--;
                            }
                        }

                        ArrayList<Long> ids = new ArrayList<Long>();

                        Cursor query = context.getContentResolver().query(Uri.parse("content://sms/draft/"), new String[]{"_id", "thread_id"}, null, null, null);

                        if (query != null) {
                            if (query.moveToFirst()) {
                                do {
                                    for (Long draft : draftsToDelete) {
                                        if (query.getLong(query.getColumnIndex("thread_id")) == draft) {
                                            ids.add(query.getLong(query.getColumnIndex("_id")));
                                            break;
                                        }
                                    }

                                    for (Long name : draftNames) {
                                        if (name == query.getLong(query.getColumnIndex("thread_id"))) {
                                            context.getContentResolver().delete(Uri.parse("content://sms/" + query.getString(query.getColumnIndex("_id"))), null, null);
                                            break;
                                        }
                                    }
                                } while (query.moveToNext());

                                for (Long id : ids) {
                                    context.getContentResolver().delete(Uri.parse("content://sms/" + id), null, null);
                                }
                            }

                            query.close();
                        }

                        for (int i = 0; i < draftNames.size(); i++) {
                            String address = "";

                            for (Conversation conversation : conversations) {
                                if (conversation.getThreadId() == draftNames.get(i)) {
                                    address = ContactUtil.findContactNumber(conversation.getNumber(), context);
                                    break;
                                }
                            }

                            ContentValues values = new ContentValues();
                            values.put("address", address);
                            values.put("thread_id", draftNames.get(i));
                            values.put("body", drafts.get(i));
                            values.put("type", "3");
                            context.getContentResolver().insert(Uri.parse("content://sms/"), values);
                        }
                    } catch (Exception e) {
                    }
                }
            }).start();
        }

        if (settings.cacheConversations) {
            Intent cacheService = new Intent(context, CacheService.class);
            context.startService(cacheService);
        }

        if (isPopup && !unlockDevice) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    NotificationManager mNotificationManager =
                            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    mNotificationManager.cancel(1);
                    mNotificationManager.cancel(2);
                    mNotificationManager.cancel(4);
                    IOUtil.writeNotifications(new ArrayList<String>(), context);

                    Intent intent = new Intent("com.klinker.android.messaging.CLEARED_NOTIFICATION");
                    context.sendBroadcast(intent);

                    Intent stopRepeating = new Intent(context, NotificationRepeaterService.class);
                    PendingIntent pStopRepeating = PendingIntent.getService(context, 0, stopRepeating, 0);
                    AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                    alarm.cancel(pStopRepeating);
                }
            }, 500);

            if (!popupAttaching) {
                MainActivity.newMessage = true;
                finish();
            }
        }

        unlockDevice = false;

        if (settings.voiceAccount != null) {
            String voiceThreads = "";

            for (Long s : threadsThroughVoice) {
                voiceThreads += s + "-";
            }

            try {
                voiceThreads = voiceThreads.substring(0, voiceThreads.length() - 1);
            } catch (Exception e) {
            }

            sharedPrefs.edit().putString("voice_threads", voiceThreads).commit();
        }

        IOUtil.writeNewMessages(newMessages, this);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (deviceType.startsWith("phablet") && !isPopup) {
            recreate();
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onStart() {
        super.onStart();

        if (firstRun) {
            try {
                refreshViewPager();
                createMenu();

                if (settings.openContactMenu && menu != null) {
                    menu.showMenu();
                }

                if (sendTo && !fromNotification) {
                    try {
                        boolean flag = false;

                        for (int i = 0; i < conversations.size(); i++) {
                            if (ContactUtil.findContactNumber(conversations.get(i).getNumber(), this).replace("-", "").replace("+", "").equals(sendMessageTo.replace("-", "").replace("+1", ""))) {
                                if (i < 10 || (!limitConversations && i > 10)) {
                                    mViewPager.setCurrentItem(i);
                                    if (menu != null) {
                                        menu.showContent();
                                    }
                                    flag = true;
                                    break;
                                }
                            }
                        }

                        if (!flag) {
                            String name = ContactUtil.findContactName(sendMessageTo, this);

                            for (int i = 0; i < conversations.size(); i++) {
                                if (ContactUtil.findContactName(ContactUtil.findContactNumber(conversations.get(i).getNumber(), this), this).equals(name)) {
                                    if (i < 10 || (!limitConversations && i > 10)) {
                                        mViewPager.setCurrentItem(i);
                                        if (menu != null) {
                                            menu.showContent();
                                        }
                                        flag = true;
                                        break;
                                    }
                                }
                            }
                        }

                        if (!flag) {
                            mDrawerLayout.openDrawer(Gravity.RIGHT);

                            EditText contact = (EditText) newMessageView.findViewById(R.id.contactEntry);
                            contact.setText(sendMessageTo);

                            if (attachedImage2 != null) {
                                imageAttachBackground2.setBackgroundColor(settings.ctConversationListBackground);
                                Drawable attachBack = resources.getDrawable(R.drawable.attachment_editor_bg);
                                attachBack.setColorFilter(settings.ctSentMessageBackground, Mode.MULTIPLY);
                                imageAttach2.setBackgroundDrawable(attachBack);
                                imageAttachBackground2.setVisibility(View.VISIBLE);
                                imageAttach2.setVisibility(true);

                                try {
                                    imageAttach2.setImage("send_image", IOUtil.decodeFileWithExif(new File(IOUtil.getPath(attachedImage2, this))));
                                } catch (Exception e) {
                                    Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
                                    imageAttach2.setVisibility(false);
                                    imageAttachBackground2.setVisibility(View.GONE);
                                }

                                Button viewImage = (Button) findViewById(R.id.view_image_button2);
                                Button replaceImage = (Button) findViewById(R.id.replace_image_button2);
                                Button removeImage = (Button) findViewById(R.id.remove_image_button2);

                                viewImage.setOnClickListener(new OnClickListener() {

                                    @Override
                                    public void onClick(View arg0) {
                                        context.startActivity(new Intent(Intent.ACTION_VIEW, attachedImage2));

                                    }

                                });

                                replaceImage.setOnClickListener(new OnClickListener() {

                                    @Override
                                    public void onClick(View v) {
                                        Intent intent = new Intent();
                                        intent.setType("image/*");
                                        intent.setAction(Intent.ACTION_GET_CONTENT);
                                        startActivityForResult(Intent.createChooser(intent, resources.getString(R.string.select_picture)), 2);

                                    }

                                });

                                removeImage.setOnClickListener(new OnClickListener() {

                                    @Override
                                    public void onClick(View v) {
                                        imageAttach2.setVisibility(false);
                                        imageAttachBackground2.setVisibility(View.GONE);

                                    }

                                });
                            }
                        }

                        sendTo = false;
                    } catch (Exception e) {
                    }
                }

                if (sendToThread != null) {
                    for (int i = 0; i < conversations.size(); i++) {
                        if (("" + conversations.get(i).getThreadId()).equals(sendToThread)) {
                            mViewPager.setCurrentItem(i);
                            sendToThread = null;
                            break;
                        }
                    }

                    messageEntry.setText(sendToMessage);

                    try {
                        messageEntry.setSelection(sendToMessage.length());
                    } catch (Exception e) {
                    }
                }

                long threadId = getIntent().getLongExtra("thread_id", 0);
                if (threadId > 0) {
                    for (int i = 0; i < conversations.size(); i++) {
                        if (threadId == conversations.get(i).getThreadId()) {
                            if ((i < 10 && limitConversations) || !limitConversations) {
                                mViewPager.setCurrentItem(i);
                            }

                            break;
                        }
                    }
                }

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        firstRun = false;
                    }
                }, 3000);
            } catch (Exception e) {
                // something went wrong setting everything up, but lets continue anyways just for the hell of it.
                // This was something coming for the lockscreen widget NiLS, assuming setting everything up and navagating to correct conversation
            }
        } else {
            if (messageRecieved) {
                refreshViewPager();
                messageRecieved = false;
            }

            if (menu != null) {
                if (sendTo) {
                    menu.showContent();
                } else {
                    if (settings.openContactMenu && (imageAttach.getVisibility() != View.VISIBLE && imageAttach2.getVisibility() != View.VISIBLE)) {
                        menu.showMenu();
                    } else if (imageAttach.getVisibility() == View.VISIBLE) {
                        menu.showContent();
                    }
                }
            }
        }

        if (fromNotification) {
            if (menu != null) {
                menu.showContent();
            }
            fromNotification = false;
        }
    }

    @SuppressWarnings("deprecation")
    public void refreshViewPager() {
        Log.v("refreshViewPager", "full refresh");

        pullToRefreshPosition = -1;
        String threadTitle = "0";

        if (!firstRun && conversations.size() != 0) {
            MainActivity.notChanged = false;
            threadTitle = ContactUtil.findContactName(ContactUtil.findContactNumber(conversations.get(mViewPager.getCurrentItem()).getNumber(), this), this);
        }

        refreshMessages();

        mSectionsPagerAdapter = new SectionsPagerAdapter(
                getFragmentManager());

        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setBackgroundDrawable(null);
        if (settings.customBackground2) {
            try {
                BitmapFactory.Options options = new BitmapFactory.Options();

                options.inSampleSize = 2;
                Bitmap myBitmap = BitmapFactory.decodeFile(Uri.parse(settings.customBackground2Location).getPath(), options);
                Drawable d = new BitmapDrawable(Resources.getSystem(), myBitmap);
                mViewPager.setBackgroundDrawable(d);
            } catch (Error e) {
                try {
                    BitmapFactory.Options options = new BitmapFactory.Options();

                    options.inSampleSize = 4;
                    Bitmap myBitmap = BitmapFactory.decodeFile(Uri.parse(settings.customBackground2Location).getPath(), options);
                    Drawable d = new BitmapDrawable(Resources.getSystem(), myBitmap);
                    mViewPager.setBackgroundDrawable(d);
                } catch (Error f) {

                }
            }
        }

        mSectionsPagerAdapter.notifyDataSetChanged();
        mViewPager.setAdapter(mSectionsPagerAdapter);

        if ((messageRecieved && jump) || sentMessage) {
            threadTitle = "0";
            sentMessage = false;
        }

        if (!isPopup) {
            if (dismissNotification) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        NotificationManager mNotificationManager =
                                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                        mNotificationManager.cancel(1);
                        mNotificationManager.cancel(2);
                        mNotificationManager.cancel(4);

                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                IOUtil.writeNotifications(new ArrayList<String>(), context);

                                Intent intent = new Intent("com.klinker.android.messaging.CLEARED_NOTIFICATION");
                                context.sendBroadcast(intent);

                                Intent stopRepeating = new Intent(context, NotificationRepeaterService.class);
                                PendingIntent pStopRepeating = PendingIntent.getService(context, 0, stopRepeating, 0);
                                AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                                alarm.cancel(pStopRepeating);
                            }
                        }).start();

                        Map<Long, String[]> fnMessages = com.klinker.android.messaging_donate.floating_notifications.FNReceiver.messages;

                        if (fnMessages != null) {
                            if (fnMessages.size() > 0) {
                                Set<Long> keys = fnMessages.keySet();

                                for (Long ii : keys) {
                                    robj.floating.notifications.Extension.remove(ii, context);
                                }
                            }
                        }
                    }
                }, 500);
            }
        }

        if (!firstRun) {
            if (menu != null) {
                menuAdapter = new MenuArrayAdapter(this, conversations, MainActivity.mViewPager);
                menuLayout.setAdapter(menuAdapter);
            } else {
                ListFragment newFragment = (ListFragment) getSupportFragmentManager().findFragmentById(R.id.menuList);
                newFragment.setListAdapter(new MenuArrayAdapter(this, conversations, MainActivity.mViewPager));
            }
        } else {
            new Thread(new Runnable() {

                @Override
                public void run() {
                    newMessages = IOUtil.readNewMessages(context);

                    if (conversations.size() > 0) {
                        if (newMessages.size() != 0)
                            newMessages.remove(newMessages.size() - 1);
                    } else {
                        newMessages = new ArrayList<String>();
                    }
                }

            }).start();
        }

        if (threadTitle.equals("0")) {
            mViewPager.setCurrentItem(0);
        } else {
            final String threadT = threadTitle;
            new Thread(new Runnable() {

                @Override
                public void run() {
                    boolean flag = false;

                    for (int i = 0; i < conversations.size(); i++) {
                        if (threadT.equals(ContactUtil.findContactName(ContactUtil.findContactNumber(conversations.get(i).getNumber(), context), context))) {
                            final int index = i;

                            ((Activity) context).getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {

                                @Override
                                public void run() {
                                    mViewPager.setCurrentItem(index);
                                }
                            });

                            flag = true;
                            break;
                        }
                    }

                    if (!flag) {
                        ((Activity) context).getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {

                            @Override
                            public void run() {
                                mViewPager.setCurrentItem(0);
                            }
                        });
                    }

                }

            }).start();
        }

        if (!settings.runAs.equals("card+")) {
            if (!settings.useTitleBar || settings.alwaysShowContactInfo) {
                if (ab != null) {
                    try {
                        ab.setTitle(ContactUtil.findContactName(ContactUtil.findContactNumber(conversations.get(mViewPager.getCurrentItem()).getNumber(), context), context));

                        Locale sCachedLocale = Locale.getDefault();
                        int sFormatType = PhoneNumberUtils.getFormatTypeForLocale(sCachedLocale);
                        Editable editable = new SpannableStringBuilder(ContactUtil.findContactNumber(conversations.get(mViewPager.getCurrentItem()).getNumber(), context));
                        PhoneNumberUtils.formatNumber(editable, sFormatType);
                        ab.setSubtitle(editable.toString());

                        if (ab.getTitle().equals(ab.getSubtitle())) {
                            ab.setSubtitle(null);
                        }

                        if (conversations.get(mViewPager.getCurrentItem()).getGroup()) {
                            ab.setTitle("Group MMS");
                            ab.setSubtitle(null);
                        }
                    } catch (Exception e) {
                        ab.setTitle(R.string.app_name_in_app);
                        ab.setSubtitle(null);
                        ab.setIcon(R.drawable.ic_launcher);
                    }
                }
            }
        }

        if (settings.titleContactImages) {
            try {
                int scale = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, resources.getDisplayMetrics());
                Bitmap image = Bitmap.createScaledBitmap(ContactUtil.getFacebookPhoto(ContactUtil.findContactNumber(conversations.get(mViewPager.getCurrentItem()).getNumber(), context), context), scale, scale, true);
                ab.setIcon(new BitmapDrawable(image));
            } catch (Exception e) {
            }
        }

        MainActivity.loadAll = false;

        if (MainActivity.animationReceived == 2) {
            final ImageView glow = (ImageView) findViewById(R.id.newMessageGlow);

            glow.setVisibility(View.VISIBLE);
            glow.setAlpha((float) 1);

            Animation fadeIn = new AlphaAnimation(0, (float) .9);
            fadeIn.setInterpolator(new DecelerateInterpolator());
            fadeIn.setDuration(1000);

            Animation fadeOut = new AlphaAnimation((float) .9, 0);
            fadeOut.setInterpolator(new AccelerateInterpolator());
            fadeOut.setStartOffset(1000);
            fadeOut.setDuration(1000);

            AnimationSet animation = new AnimationSet(false);
            animation.addAnimation(fadeIn);
            animation.addAnimation(fadeOut);

            animation.setAnimationListener(new AnimationListener() {

                @Override
                public void onAnimationEnd(Animation arg0) {
                    glow.setAlpha((float) 0);

                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }

                @Override
                public void onAnimationStart(Animation animation) {

                }

            });

            glow.startAnimation(animation);

            MainActivity.animationReceived = 0;
        } else {
            final ImageView glow = (ImageView) findViewById(R.id.newMessageGlow);
            glow.setAlpha((float) 0);
            glow.setVisibility(View.GONE);
        }

        try {
            invalidateOptionsMenu();
        } catch (Exception e) {
        }
    }

    public void refreshViewPager3() {
        Log.v("refreshViewPager", "quick refresh");

        if (MainActivity.animationOn) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    refreshViewPager3();
                }
            }, 1000);
            return;
        }

        pullToRefreshPosition = -1;
        MainActivity.notChanged = false;
        MainActivity.threadedLoad = false;

        mSectionsPagerAdapter.notifyDataSetChanged();

        try {
            invalidateOptionsMenu();
        } catch (Exception e) {
        }
    }

    public void refreshViewPager4(String number, String body, String date) {
        pullToRefreshPosition = -1;
        MainActivity.notChanged = false;
        MainActivity.threadedLoad = false;
        long currentThread = conversations.get(mViewPager.getCurrentItem()).getThreadId();

        if (conversations.size() == 0) {
            refreshViewPager();
            return;
        }

        boolean flag = false;

        for (int i = 0; i < conversations.size(); i++) {
            String convNumber = conversations.get(i).getNumber();
            if (number.equals(convNumber)) {
                conversations.add(0, new Conversation(conversations.get(i).getThreadId(), conversations.get(i).getCount() + 1, "0", body, Long.parseLong(date), conversations.get(i).getNumber()));
                conversations.remove(i + 1);

                flag = true;
                break;
            }
        }

        if (!flag) {
            refreshMessages();

            if (menu != null) {
                menuAdapter =  new MenuArrayAdapter(this, conversations, MainActivity.mViewPager);
                menuLayout.setAdapter(menuAdapter);
            } else {
                ListFragment newFragment = (ListFragment) getSupportFragmentManager().findFragmentById(R.id.menuList);
                newFragment.setListAdapter(new MenuArrayAdapter(this, conversations, MainActivity.mViewPager));
            }
        } else {
            if (menu != null) {
                menuAdapter.notifyDataSetChanged();
            } else {
                ListFragment newFragment = (ListFragment) getSupportFragmentManager().findFragmentById(R.id.menuList);
                newFragment.setListAdapter(new MenuArrayAdapter(this, conversations, MainActivity.mViewPager));
            }
        }

        try {
            mSectionsPagerAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            // fragment most likely outside of activity now or something
        }

        // check here whether or not we are on the same page as we started on... should be, but sometimes not
        if (currentThread != conversations.get(mViewPager.getCurrentItem()).getThreadId()) {
            for (int i = 0; i < conversations.size(); i++) {
                if (conversations.get(i).getThreadId() == currentThread) {
                    mViewPager.setCurrentItem(i);
                    break;
                }
            }
        }

        final ImageView glow = (ImageView) findViewById(R.id.newMessageGlow);
        glow.setVisibility(View.VISIBLE);

        if (MainActivity.animationReceived == 2) {
            glow.setAlpha((float) 1);
            glow.setVisibility(View.VISIBLE);

            Animation fadeIn = new AlphaAnimation(0, (float) .9);
            fadeIn.setInterpolator(new DecelerateInterpolator());
            fadeIn.setDuration(1000);

            Animation fadeOut = new AlphaAnimation((float) .9, 0);
            fadeOut.setInterpolator(new AccelerateInterpolator());
            fadeOut.setStartOffset(1000);
            fadeOut.setDuration(1000);

            AnimationSet animation = new AnimationSet(false);
            animation.addAnimation(fadeIn);
            animation.addAnimation(fadeOut);

            animation.setAnimationListener(new AnimationListener() {

                @Override
                public void onAnimationEnd(Animation arg0) {
                    glow.setAlpha((float) 0);

                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }

                @Override
                public void onAnimationStart(Animation animation) {

                }

            });

            glow.startAnimation(animation);

            MainActivity.animationReceived = 0;
        } else {
            glow.setAlpha((float) 0);
            glow.setVisibility(View.GONE);
        }

        try {
            invalidateOptionsMenu();
        } catch (Exception e) {
        }

        if (isPopup) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    NotificationManager mNotificationManager =
                            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    mNotificationManager.cancel(1);
                    mNotificationManager.cancel(2);
                    mNotificationManager.cancel(4);
                    IOUtil.writeNotifications(new ArrayList<String>(), context);

                    Intent intent = new Intent("com.klinker.android.messaging.CLEARED_NOTIFICATION");
                    context.sendBroadcast(intent);

                    Intent stopRepeating = new Intent(context, NotificationRepeaterService.class);
                    PendingIntent pStopRepeating = PendingIntent.getService(context, 0, stopRepeating, 0);
                    AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                    alarm.cancel(pStopRepeating);
                }
            }, 500);
        }
    }

    public PullToRefreshAttacher getPullToRefreshAttacher() {
        return mPullToRefreshAttacher;
    }

    public class SectionsPagerAdapter extends android.support.v13.app.FragmentStatePagerAdapter {

        public ArrayList<String> contact = null;
        private Map<Integer, ConversationFragment> mFragments = new HashMap<Integer, ConversationFragment>();

        public SectionsPagerAdapter(android.app.FragmentManager fm) {
            super(fm);

            new Thread(new Runnable() {

                @Override
                public void run() {
                    ArrayList<String> contacts = new ArrayList<String>();

                    for (int i = 0; i < conversations.size(); i++) {
                        contacts.add(ContactUtil.loadGroupContacts(ContactUtil.findContactNumber(conversations.get(i).getNumber(), getBaseContext()), getBaseContext()));
                    }

                    contact = new ArrayList<String>();
                    contact = contacts;

                }

            }).start();
        }

        @Override
        public void notifyDataSetChanged() {
            contact = null;

            new Thread(new Runnable() {

                @Override
                public void run() {
                    ArrayList<String> contacts = new ArrayList<String>();

                    for (int i = 0; i < conversations.size(); i++) {
                        contacts.add(ContactUtil.loadGroupContacts(ContactUtil.findContactNumber(conversations.get(i).getNumber(), getBaseContext()), getBaseContext()));
                    }

                    contact = new ArrayList<String>();
                    contact = contacts;

                }

            }).start();

            for (Map.Entry<Integer, ConversationFragment> entry : mFragments.entrySet()) {
                try {
                    entry.getValue().refreshFragment();
                } catch (Exception e) {
                    // fragment is no longer available
                }

                mFragments.remove(entry);
            }

            super.notifyDataSetChanged();
        }

        @Override
        public ConversationFragment getItem(int position) {
            ConversationFragment fragment = new ConversationFragment();
            Bundle args = new Bundle();

            args.putInt("position", position);
            args.putString("myId", myContactId);
            fragment.setArguments(args);

            mFragments.put(position, fragment);
            return fragment;
        }

        @Override
        public int getCount() {
            if (MainActivity.limitConversations) {
                if (conversations.size() < 10) {
                    return conversations.size();
                } else {
                    return 10;
                }
            } else {
                return conversations.size();
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            try {
                String text = "No Messages";

                if (!settings.useTitleBar && !isPopup) {
                    return "";
                }

                if (contact == null) {
                    if (conversations.size() >= 1) {
                        if (conversations.get(position).getGroup()) {
                            if (settings.titleCaps) {
                                text = "GROUP MMS";
                            } else {
                                text = "Group MMS";
                            }
                        } else {
                            if (settings.titleCaps) {
                                if (settings.alwaysShowContactInfo) {
                                    String[] names = ContactUtil.findContactName(ContactUtil.findContactNumber(conversations.get(position).getNumber(), getBaseContext()), getBaseContext()).split(" ");
                                    text = names[0].trim().toUpperCase(Locale.getDefault());
                                } else {
                                    text = ContactUtil.findContactName(ContactUtil.findContactNumber(conversations.get(position).getNumber(), getBaseContext()), getBaseContext()).toUpperCase(Locale.getDefault());
                                }
                            } else {
                                if (settings.alwaysShowContactInfo) {
                                    try {
                                        String[] names = ContactUtil.findContactName(ContactUtil.findContactNumber(conversations.get(position).getNumber(), getBaseContext()), getBaseContext()).split(" ");
                                        text = names[0].trim();
                                    } catch (Exception e) {
                                        text = ContactUtil.findContactName(ContactUtil.findContactNumber(conversations.get(position).getNumber(), getBaseContext()), getBaseContext());
                                    }
                                } else {
                                    text = ContactUtil.findContactName(ContactUtil.findContactNumber(conversations.get(position).getNumber(), getBaseContext()), getBaseContext());
                                }
                            }
                        }
                    }

                    return text;
                } else {
                    try {
                        if (contact.size() >= 1) {
                            if (conversations.get(position).getGroup()) {
                                if (settings.titleCaps) {
                                    text = "GROUP MMS";
                                } else {
                                    text = "Group MMS";
                                }
                            } else {
                                if (settings.titleCaps) {
                                    if (settings.alwaysShowContactInfo) {
                                        try {
                                            String[] names = contact.get(position).split(" ");
                                            text = names[0].trim().toUpperCase(Locale.getDefault());
                                        } catch (Exception e) {
                                            text = contact.get(position).toUpperCase(Locale.getDefault());
                                        }
                                    } else {
                                        text = contact.get(position).toUpperCase(Locale.getDefault());
                                    }
                                } else {
                                    if (settings.alwaysShowContactInfo) {
                                        try {
                                            String[] names = contact.get(position).split(" ");
                                            text = names[0].trim();
                                        } catch (Exception e) {
                                            text = contact.get(position);
                                        }
                                    } else {
                                        text = contact.get(position);
                                    }
                                }
                            }
                        }

                        return text;
                    } catch (Exception e) {
                        if (contact.size() > 0) {
                            return contact.get(position);
                        } else {
                            return "No Messages";
                        }
                    }
                }
            } catch (Exception e) {
                return "";
            }
        }
    }

    // receivers
    private BroadcastReceiver refreshReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            refreshViewPager3();
        }
    };

    private BroadcastReceiver mmsError = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, Intent intent) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(R.string.apn_error_title);
            builder.setMessage(resources.getString(R.string.apn_error_1) + " " +
                    resources.getString(R.string.apn_error_2) + " " +
                    resources.getString(R.string.apn_error_3) + " " +
                    resources.getString(R.string.apn_error_4) + " " +
                    resources.getString(R.string.apn_error_5) +
                    resources.getString(R.string.apn_error_6));
            builder.setNeutralButton(resources.getString(R.string.apn_error_button), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog,
                                    int which) {
                    PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean("show_advanced_settings", true).commit();
                    Intent intent = new Intent(context, SettingsPagerActivity.class);
                    intent.putExtra("mms", true);
                    context.startActivity(intent);
                }

            });

            builder.create().show();
        }
    };

    private BroadcastReceiver voiceError = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, Intent intent) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(R.string.google_voice_failed_title);
            builder.setMessage(resources.getString(R.string.google_voice_failed));
            builder.setNeutralButton(resources.getString(R.string.google_voice_sign_up), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog,
                                    int which) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/voice/"));
                    context.startActivity(intent);

                }

            });

            builder.create().show();
        }
    };

    private BroadcastReceiver messageReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context arg0, Intent arg1) {
            refreshViewPager4(ContactUtil.findRecipientId(arg1.getStringExtra("address"), arg0), arg1.getStringExtra("body"), arg1.getStringExtra("date"));

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    mNotificationManager.cancel(1);
                    mNotificationManager.cancel(2);
                    mNotificationManager.cancel(4);
                }
            }, 2000);

            try {
                String addressArg = arg1.getStringExtra("address").replace(" ", "").replace("(", "").replace(")", "").replace("-", "");
                String currentAddress = ContactUtil.findContactNumber(conversations.get(mViewPager.getCurrentItem()).getNumber(), arg0).replace(" ", "").replace("(", "").replace(")", "").replace("-", "");

                if (addressArg.startsWith(currentAddress) || addressArg.endsWith(currentAddress) || addressArg.equals(currentAddress)) {
                    animationReceived = 1;
                    animationThread = mViewPager.getCurrentItem();
                } else {
                    animationReceived = 2;
                }
            } catch (Exception e) {
                animationReceived = 2;
            }

            if (animationReceived == 2) {
                if (settings.inAppNotifications) {
                    boolean flag = false;
                    String addressArg = arg1.getStringExtra("address").replace(" ", "").replace("(", "").replace(")", "").replace("-", "");
                    for (int i = 0; i < appMsgConversations; i++) {
                        try {
                            String currentAddress = ContactUtil.findContactNumber(conversations.get(i).getNumber(), arg0).replace(" ", "").replace("(", "").replace(")", "").replace("-", "");
                            if (addressArg.startsWith(currentAddress) || addressArg.endsWith(currentAddress) || addressArg.equals(currentAddress)) {
                                flag = true;
                                break;
                            }
                        } catch (Exception e) {
                        }
                    }

                    if (!flag) {
                        appMsgConversations++;
                    }

                    if (appMsgConversations == 1) {
                        appMsg = AppMsg.makeText((Activity) arg0, appMsgConversations + " " + getString(R.string.new_conversation), AppMsg.STYLE_ALERT);
                    } else {
                        appMsg = AppMsg.makeText((Activity) arg0, appMsgConversations + " " + getString(R.string.new_conversations), AppMsg.STYLE_ALERT);
                    }

                    appMsg.show();
                }
            }

            dismissCrouton = false;

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    dismissCrouton = true;
                }
            }, 500);
        }

    };

    private BroadcastReceiver killReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ((Activity) context).finish();
        }
    };
    private BroadcastReceiver mmsProgressReceiver;

}