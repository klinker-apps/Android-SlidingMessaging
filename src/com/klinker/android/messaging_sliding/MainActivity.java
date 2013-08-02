package com.klinker.android.messaging_sliding;

import android.app.*;
import android.app.Fragment;
import android.content.*;
import android.graphics.*;
import android.media.*;
import android.os.*;
import android.support.v4.app.ListFragment;
import android.support.v4.app.*;
import android.support.v4.app.TaskStackBuilder;
import android.content.Loader;
import android.support.v4.view.ViewPager;
import android.view.*;
import android.widget.*;
import com.devspark.appmsg.AppMsg;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.klinker.android.messaging_card.batch_delete.BatchDeleteActivity;
import com.klinker.android.messaging_donate.*;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.android.mms.transaction.HttpUtils;
import com.android.mms.ui.ImageAttachmentView;
import com.google.android.mms.APN;
import com.google.android.mms.APNHelper;
import com.google.android.mms.MMSPart;
import com.google.android.mms.pdu_alt.EncodedStringValue;
import com.google.android.mms.pdu_alt.PduBody;
import com.google.android.mms.pdu_alt.PduComposer;
import com.google.android.mms.pdu_alt.PduPart;
import com.google.android.mms.pdu_alt.SendReq;

import android.annotation.SuppressLint;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap.Config;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.PhoneLookup;
import android.provider.ContactsContract.Profile;
import android.provider.MediaStore;
import android.provider.Telephony;
import android.support.v4.view.PagerTitleStrip;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.InputType;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.View.OnClickListener;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.RelativeLayout.LayoutParams;
import com.klinker.android.messaging_donate.receivers.DeliveredReceiver;
import com.klinker.android.messaging_donate.receivers.DisconnectWifi;
import com.klinker.android.messaging_donate.receivers.SentReceiver;
import com.klinker.android.messaging_donate.settings.SettingsPagerActivity;
import com.klinker.android.messaging_sliding.blacklist.BlacklistContact;
import com.klinker.android.messaging_sliding.custom_dialogs.CustomListView;
import com.klinker.android.messaging_sliding.emojis.EmojiAdapter;
import com.klinker.android.messaging_sliding.emojis.EmojiAdapter2;
import com.klinker.android.messaging_sliding.emojis.EmojiConverter;
import com.klinker.android.messaging_sliding.emojis.EmojiConverter2;
import com.klinker.android.messaging_sliding.notifications.IndividualSetting;
import com.klinker.android.messaging_sliding.receivers.CacheService;
import com.klinker.android.messaging_sliding.receivers.NotificationReceiver;
import com.klinker.android.messaging_sliding.receivers.NotificationRepeaterService;
import com.klinker.android.messaging_sliding.receivers.QuickTextService;
import com.klinker.android.messaging_sliding.search.SearchActivity;
import com.klinker.android.messaging_sliding.security.PasswordActivity;
import com.klinker.android.messaging_sliding.security.PinActivity;
import com.klinker.android.messaging_sliding.templates.TemplateActivity;
import com.klinker.android.messaging_sliding.templates.TemplateArrayAdapter;
import com.tonicartos.widget.stickygridheaders.StickyGridHeadersGridView;
import group.pals.android.lib.ui.lockpattern.prefs.SecurityPrefs;
import net.simonvt.messagebar.messagebar.MessageBar;

import group.pals.android.lib.ui.lockpattern.LockPatternActivity;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher;

public class MainActivity extends FragmentActivity {

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a
	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
	 * will keep every loaded fragment in memory. If this becomes too memory
	 * intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
s
	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	public static ViewPager mViewPager;
    public static SectionsPagerAdapter mSectionsPagerAdapter;

    public static String deviceType;
    public static boolean newMessage;

    public ArrayList<String> inboxNumber, inboxDate, inboxBody;
	public ArrayList<String> group;
	public ArrayList<String> msgCount;
	public ArrayList<String> msgRead;

    public static boolean waitToLoad = false;
    public static boolean threadedLoad = true;
    public static boolean notChanged = true;
	
	public static String myPhoneNumber, myContactId;
	
	public ArrayList<String> contactNames, contactNumbers, contactTypes, threadIds;
	
	public static SlidingMenu menu;
    public MessageBar messageBar;
	public boolean firstRun = true;
	public boolean firstContactSearch = true;
	public boolean refreshMyContact = true;
	
	public static boolean animationOn = false;
	public static int animationReceived = 0;
	public static int animationThread = 0;
	
	public BroadcastReceiver receiver;
	public BroadcastReceiver mmsReceiver;
	public DisconnectWifi discon;
	public WifiInfo currentWifi;
	public boolean currentWifiState;
    public boolean currentDataState;
	
	public static int contactWidth;
	public boolean jump = true;

    public ArrayList<String> drafts, draftNames;
    public ArrayList<Boolean> draftChanged;
    public ArrayList<String> draftsToDelete;
    public boolean fromDraft = false;
    public String newDraft = "";
    public boolean deleteDraft = true;
	
	public ListView menuLayout;
	public MenuArrayAdapter menuAdapter;
	public static boolean messageRecieved = false;
	public static boolean sentMessage = false;
	public static boolean loadAll = false;
    public static int numToLoad = 20;
	
	public boolean sendTo = false;
	public String sendMessageTo;
	public String whatToSend = null;
	public boolean fromNotification = false;
	public String sendToThread = null;
	public String sendToMessage;
	
	public SharedPreferences sharedPrefs;
	
	public EditText messageEntry;
	public TextView mTextView;
	public ImageButton sendButton;
	public ImageButton emojiButton;
	public View v;
	public PagerTitleStrip title;
	public View imageAttachBackground;
	public ImageAttachmentView imageAttach;
	public View imageAttachBackground2;
	public ImageAttachmentView imageAttach2;
	public Uri attachedImage;
	public Uri attachedImage2;
	public int attachedPosition;
	
	public Uri capturedPhotoUri;
	public boolean fromCamera = false;
	public boolean multipleAttachments = false;
	
	public Typeface font;
    public SoundPool soundPool;
    public int ping;

    public PullToRefreshAttacher mPullToRefreshAttacher;
    public static int pullToRefreshPosition = -1;

    public AppMsg appMsg = null;
    public int appMsgConversations = 0;
    public boolean dismissCrouton = true;
    public boolean dismissNotification = true;

    public static boolean limitConversations = true;

    public static final String GSM_CHARACTERS_REGEX = "^[A-Za-z0-9 \\r\\n@Ł$ĽčéůěňÇŘřĹĺ\u0394_\u03A6\u0393\u039B\u03A9\u03A0\u03A8\u03A3\u0398\u039EĆćßÉ!\"#$%&'()*+,\\-./:;<=>?ĄÄÖŃÜ§żäöńüŕ^{}\\\\\\[~\\]|\u20AC]*$";
    private static final int REQ_ENTER_PATTERN = 7;

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setUpWindow();

        mPullToRefreshAttacher = new PullToRefreshAttacher(this, sharedPrefs.getBoolean("ct_light_action_bar", false), true);
        appMsg = AppMsg.makeText(this, "", AppMsg.STYLE_ALERT);

        if (sharedPrefs.getBoolean("limit_conversations_start", true)) {
            limitConversations = true;
        } else {
            limitConversations = false;
        }

        MainActivity.notChanged = true;
		
		setUpIntentStuff();
		
		if (sharedPrefs.getBoolean("custom_font", false))
		{
			try
			{
				font = Typeface.createFromFile(sharedPrefs.getString("custom_font_path", null));
			} catch (Exception e)
			{
				Editor edit = sharedPrefs.edit();
				edit.putBoolean("custom_font", false);
				edit.commit();
			}
		}
		
		if (sharedPrefs.getBoolean("quick_text", false))
		{
			Intent mIntent = new Intent(this, QuickTextService.class);
			this.startService(mIntent);
		} else
		{
			NotificationManager mNotificationManager =
		            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			mNotificationManager.cancel(3);
		}
		
		if (sharedPrefs.getBoolean("override_lang", false))
		{
			String languageToLoad  = "en";
		    Locale locale = new Locale(languageToLoad); 
		    Locale.setDefault(locale);
		    Configuration config = new Configuration();
		    config.locale = locale;
		    getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
		}
        
        title = (PagerTitleStrip) findViewById(R.id.pager_title_strip);
		
		if (sharedPrefs.getString("page_or_menu2", "2").equals("1"))
		{
			title.setTextSpacing(5000);
		}
		
		if (!sharedPrefs.getBoolean("custom_theme", false))
        {
        	if (sharedPrefs.getBoolean("title_text_color", false))
        	{
        		title.setTextColor(getResources().getColor(R.color.black));
        	}
        } else
        {
        	title.setTextColor(sharedPrefs.getInt("ct_titleBarTextColor", getResources().getColor(R.color.white)));
        }
        
        if (!sharedPrefs.getBoolean("title_caps", true))
        {
        	title.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        }
		
        if (sharedPrefs.getBoolean("hide_title_bar", true))
        {
        	if (!sharedPrefs.getBoolean("custom_theme", false))
        	{
	        	String titleColor = sharedPrefs.getString("title_color", "blue");
				
				if (titleColor.equals("blue"))
				{
					title.setBackgroundColor(getResources().getColor(R.color.holo_blue));
				} else if (titleColor.equals("orange"))
				{
					title.setBackgroundColor(getResources().getColor(R.color.holo_orange));
				} else if (titleColor.equals("red"))
				{
					title.setBackgroundColor(getResources().getColor(R.color.holo_red));
				} else if (titleColor.equals("green"))
				{
					title.setBackgroundColor(getResources().getColor(R.color.holo_green));
				} else if (titleColor.equals("purple"))
				{
					title.setBackgroundColor(getResources().getColor(R.color.holo_purple));
				} else if (titleColor.equals("grey"))
				{
					title.setBackgroundColor(getResources().getColor(R.color.grey));
				} else if (titleColor.equals("black"))
				{
					title.setBackgroundColor(getResources().getColor(R.color.pitch_black));
				} else if (titleColor.equals("darkgrey"))
				{
					title.setBackgroundColor(getResources().getColor(R.color.darkgrey));
				}
        	} else
        	{
        		title.setBackgroundColor(sharedPrefs.getInt("ct_titleBarColor", getResources().getColor(R.color.holo_blue)));
        	}
        } else
        {
        	title.setVisibility(View.GONE);
        }
        
        menuLayout = new ListView(this);
		
		myPhoneNumber = getMyPhoneNumber();
		
		this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		
		receiver = new BroadcastReceiver() {
		    @Override
		    public void onReceive(final Context context, Intent intent) {
                    deleteDraft = false;
			    	Bundle extras = intent.getExtras();
			        
			        String body = "";
			        String address = "";
			        String date = "";
			         
			        if ( extras != null )
			        {
			            Object[] smsExtra = (Object[]) extras.get( "pdus" );
			            
			            for ( int i = 0; i < smsExtra.length; ++i )
			            {
			                SmsMessage sms = SmsMessage.createFromPdu((byte[])smsExtra[i]);
			                 
			                body += sms.getMessageBody().toString();
			                address = sms.getOriginatingAddress();
			                date = sms.getTimestampMillis() + "";
			            }
			        }

                    ArrayList<BlacklistContact> blacklist = readFromFile6(context);
                    int blacklistType = 0;

                    for (int i = 0; i < blacklist.size(); i++)
                    {
                        if (blacklist.get(i).name.equals(address.replace("-", "").replace("(", "").replace(")", "").replace(" ", "").replace("+1", "")))
                        {
                            blacklistType = blacklist.get(i).type;
                        }
                    }

                    if (blacklistType == 2)
                    {
                        abortBroadcast();
                    } else {
			        
                        Calendar cal = Calendar.getInstance();
                        ContentValues values = new ContentValues();
                        values.put("address", address);
                        values.put("body", body);
                        values.put("date", cal.getTimeInMillis() + "");
                        values.put("read", false);
                        values.put("date_sent", date);
                        getContentResolver().insert(Uri.parse("content://sms/inbox"), values);

                        String name = findContactName(address, context);
                        String id = "0";

                        Uri phoneUri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(address.replaceAll("[^0-9\\+]", "")));
                        Cursor phonesCursor = context.getContentResolver().query(phoneUri, new String[] {ContactsContract.Contacts._ID}, null, null, null);

                        if(phonesCursor != null && phonesCursor.moveToFirst()) {
                            id = phonesCursor.getString(0);
                        }

                        InputStream input = openDisplayPhoto(Long.parseLong(id));

                        if (input == null)
                        {
                            input = context.getResources().openRawResource(R.drawable.ic_contact_picture);
                        }

                        Bitmap contactImage = Bitmap.createScaledBitmap(BitmapFactory.decodeStream(input), 120, 120, true);

                        if (sharedPrefs.getBoolean("notifications", true))
                        {
                            NotificationCompat.Builder mBuilder =
                                    new NotificationCompat.Builder(context)
                                            .setSmallIcon(R.drawable.stat_notify_sms)
                                            .setContentTitle(name)
                                            .setContentText(body)
                                            .setTicker(name + ": " + body);

                            if (!id.equals("0"))
                                mBuilder.setLargeIcon(contactImage);

                            setIcon(mBuilder);

                            if(!individualNotification(mBuilder, name, context))
                            {
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
                            }

                            Intent resultIntent = new Intent(context, com.klinker.android.messaging_donate.MainActivity.class);
                            resultIntent.setAction(Intent.ACTION_SENDTO);
                            resultIntent.putExtra("com.klinker.android.OPEN", address);

                            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                            stackBuilder.addParentStack(com.klinker.android.messaging_donate.MainActivity.class);
                            stackBuilder.addNextIntent(resultIntent);
                            PendingIntent resultPendingIntent =
                                    stackBuilder.getPendingIntent(
                                            0,
                                            PendingIntent.FLAG_CANCEL_CURRENT
                                    );

                            mBuilder.setContentIntent(resultPendingIntent);
                            mBuilder.setAutoCancel(true);

                            NotificationManager mNotificationManager =
                                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                            Notification notification = new NotificationCompat.BigTextStyle(mBuilder).bigText(body).build();
                            Intent deleteIntent = new Intent(context, NotificationReceiver.class);
                            notification.deleteIntent = PendingIntent.getBroadcast(context, 0, deleteIntent, 0);
                            mNotificationManager.notify(1, notification);
                            dismissNotification = false;

                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    NotificationManager mNotificationManager =
                                            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                                    mNotificationManager.cancel(1);
                                    dismissNotification = true;
                                }
                            }, 1000);
                        }

                        messageRecieved = true;
                        notChanged = false;
                        jump = false;

                        try
                        {
                            if (address.replace(" ", "").replace("(", "").replace(")", "").replace("-", "").endsWith(findContactNumber(inboxNumber.get(mViewPager.getCurrentItem()), context).replace(" ", "").replace("(", "").replace(")", "").replace("-", "")))
                            {
                                animationReceived = 1;
                                animationThread = mViewPager.getCurrentItem();
                            } else
                            {
                                animationReceived = 2;
                            }
                        } catch (Exception e)
                        {
                            animationReceived = 2;
                        }

                        if (animationReceived == 2) {
                            if (sharedPrefs.getBoolean("in_app_notifications", true)) {
                                boolean flag = false;
                                for (int i = 0; i < appMsgConversations; i++) {
                                    if (address.replace(" ", "").replace("(", "").replace(")", "").replace("-", "").endsWith(findContactNumber(inboxNumber.get(i), context).replace(" ", "").replace("(", "").replace(")", "").replace("-", ""))) {
                                        flag = true;
                                        break;
                                    }
                                }

                                if (!flag) {
                                    appMsgConversations++;
                                }

                                if (appMsgConversations == 1) {
                                    appMsg = AppMsg.makeText((Activity) context, appMsgConversations + getString(R.string.new_conversation), AppMsg.STYLE_ALERT);
                                } else {
                                    appMsg = AppMsg.makeText((Activity) context, appMsgConversations + getString(R.string.new_conversations), AppMsg.STYLE_ALERT);
                                }

                                appMsg.show();
                            }
                        }

                        dismissCrouton = false;

                        refreshViewPager4(address, body, date);

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                dismissCrouton = true;
                            }
                        }, 500);

                        if (!sharedPrefs.getBoolean("hide_title_bar", true) || sharedPrefs.getBoolean("always_show_contact_info", false))
                        {
                            final ActionBar ab = getActionBar();

                            if (ab != null) {
                                if (group.get(mViewPager.getCurrentItem()).equals("yes"))
                                {
                                    ab.setTitle("Group MMS");
                                    ab.setSubtitle(null);
                                } else
                                {
                                    new Thread(new Runnable() {

                                        @Override
                                        public void run() {
                                            final String title = findContactName(findContactNumber(inboxNumber.get(mViewPager.getCurrentItem()), context), context);

                                            ((Activity) context).getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {

                                                @Override
                                                public void run() {
                                                    ab.setTitle(title);
                                                }

                                            });

                                        }

                                    }).start();

                                    Locale sCachedLocale = Locale.getDefault();
                                    int sFormatType = PhoneNumberUtils.getFormatTypeForLocale(sCachedLocale);
                                    Editable editable = new SpannableStringBuilder(findContactNumber(inboxNumber.get(mViewPager.getCurrentItem()), context));
                                    PhoneNumberUtils.formatNumber(editable, sFormatType);
                                    ab.setSubtitle(editable.toString());

                                    if (ab.getTitle().equals(ab.getSubtitle()))
                                    {
                                        ab.setSubtitle(null);
                                    }
                                }
                            }
                        }

                        if (sharedPrefs.getBoolean("title_contact_image", false))
                        {
                            final ActionBar ab = getActionBar();

                            if (ab != null) {
                                new Thread(new Runnable() {

                                    @Override
                                    public void run() {
                                        final Bitmap image = getFacebookPhoto(findContactNumber(inboxNumber.get(mViewPager.getCurrentItem()), context), context);
                                        final BitmapDrawable image2 = new BitmapDrawable(image);

                                        ((Activity) context).getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {

                                            @Override
                                            public void run() {
                                                ab.setIcon(image2);
                                            }

                                        });

                                    }

                                }).start();
                            }
                        }

                        Intent updateWidget = new Intent("com.klinker.android.messaging.UPDATE_WIDGET");
                        context.sendBroadcast(updateWidget);

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                deleteDraft = true;
                            }
                        }, 2000);

                        abortBroadcast();
                    }
		        }
		};
		
		mmsReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context arg0, Intent arg1) {
				String currentThread = threadIds.get(mViewPager.getCurrentItem());
				
				refreshViewPager(true);
				
				for (int i = 0; i < threadIds.size(); i++)
				{
					if (currentThread.equals(threadIds.get(i)))
					{
						mViewPager.setCurrentItem(i, false);
						break;
					}
				}
				
			}
			
		};
		
		final float scale = getResources().getDisplayMetrics().density;
		MainActivity.contactWidth = (int) (64 * scale + 0.5f);

        try {
            ActionBar ab = getActionBar();
            ab.setDisplayHomeAsUpEnabled(true);

            if (!sharedPrefs.getBoolean("ct_light_action_bar", false))
            {
                ab.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.black)));

                if (sharedPrefs.getInt("ct_conversationListBackground", getResources().getColor(R.color.light_silver)) == getResources().getColor(R.color.pitch_black))
                {
                    if (!sharedPrefs.getBoolean("hide_title_bar", true))
                    {
                        ab.setBackgroundDrawable(getResources().getDrawable(R.drawable.pitch_black_action_bar_blue));
                    } else
                    {
                        ab.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.pitch_black)));
                    }
                }
            } else
            {
                ab.setBackgroundDrawable(getResources().getDrawable(R.drawable.ab_hangouts));
            }
        } catch (Exception e) {
            // no action bar, dialog theme
        }
		
		View v = findViewById(R.id.newMessageGlow);
		v.setVisibility(View.GONE);

		setUpSendbar();
	}

    public void setUpWindow() {
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        if (sharedPrefs.getBoolean("ct_light_action_bar", false))
        {
            setTheme(R.style.HangoutsTheme);
        }

        String pinType = sharedPrefs.getString("pin_conversation_list", "1");
        if (!pinType.equals("1")) {
            if (pinType.equals("2")) {
                setContentView(R.layout.activity_main_phone);
            } else if (pinType.equals("3")) {
                if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
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

        getWindow().setBackgroundDrawable(null);
    }
	
	public void setUpIntentStuff() {
		Intent intent = getIntent();
		String action = intent.getAction();

        try {
            if (action != null)
            {
                if (action.equals(Intent.ACTION_SENDTO))
                {
                    sendTo = true;

                    try
                    {
                        if (intent.getDataString().startsWith("smsto:"))
                        {
                            sendMessageTo = Uri.decode(intent.getDataString()).substring("smsto:".length()).replace("(", "").replace(")", "").replace("-", "").replace(" ", "");
                            fromNotification = false;
                        } else
                        {
                            sendMessageTo = Uri.decode(intent.getDataString()).substring("sms:".length()).replace("(", "").replace(")", "").replace("-", "").replace(" ", "");
                            fromNotification = false;
                        }
                    } catch (Exception e)
                    {
                        sendMessageTo = intent.getStringExtra("com.klinker.android.OPEN").replace("(", "").replace(")", "").replace("-", "").replace(" ", "");
                        fromNotification = true;
                    }
                } else if (action.equals(Intent.ACTION_SEND))
                {
                    Bundle extras = intent.getExtras();

                    if (extras != null)
                    {
                        if (extras.containsKey(Intent.EXTRA_TEXT))
                        {
                            whatToSend = (String) extras.getCharSequence(Intent.EXTRA_TEXT);
                        }

                        if (extras.containsKey(Intent.EXTRA_STREAM))
                        {
                            sendTo = true;
                            sendMessageTo = "";
                            fromNotification = false;
                            attachedImage2 = intent.getParcelableExtra(Intent.EXTRA_STREAM);
                        }
                    }
                }
            } else
            {
                Bundle extras = intent.getExtras();

                if (extras != null)
                {
                    if (extras.containsKey("com.klinker.android.OPEN_THREAD"))
                    {
                        sendToThread = extras.getString("com.klinker.android.OPEN_THREAD");
                        sendToMessage = extras.getString("com.klinker.android.CURRENT_TEXT");
                    }
                }
            }
        } catch (Exception e) {

        }
	}

	public void refreshMessages(boolean totalRefresh)
	{
		inboxNumber = new ArrayList<String>();
		inboxDate = new ArrayList<String>();
		inboxBody = new ArrayList<String>();
		threadIds = new ArrayList<String>();
		group = new ArrayList<String>();
		msgCount = new ArrayList<String>();
		msgRead = new ArrayList<String>();
		ContentResolver contentResolver = getContentResolver();

        boolean nullPointer = false;

        if (com.klinker.android.messaging_donate.MainActivity.threadIds == null)
        {
            nullPointer = true;
        }

        if (firstRun && !nullPointer)
        {
            threadIds = com.klinker.android.messaging_donate.MainActivity.threadIds;
            msgCount = com.klinker.android.messaging_donate.MainActivity.msgCount;
            msgRead = com.klinker.android.messaging_donate.MainActivity.msgRead;
            inboxBody = com.klinker.android.messaging_donate.MainActivity.inboxBody;
            inboxDate = com.klinker.android.messaging_donate.MainActivity.inboxDate;
            inboxNumber = com.klinker.android.messaging_donate.MainActivity.inboxNumber;
            group = com.klinker.android.messaging_donate.MainActivity.group;
        } else
        {
            try
            {
                String[] projection = new String[]{"_id", "date", "message_count", "recipient_ids", "snippet", "read"};
                Uri uri = Uri.parse("content://mms-sms/conversations/?simple=true");
                Cursor query = contentResolver.query(uri, projection, null, null, "date desc");

                if (query.moveToFirst())
                {
                    do
                    {
                        threadIds.add(query.getString(query.getColumnIndex("_id")));
                        msgCount.add(query.getString(query.getColumnIndex("message_count")));
                        msgRead.add(query.getString(query.getColumnIndex("read")));

                        inboxBody.add(" ");

                        try
                        {
                            inboxBody.set(inboxBody.size() - 1, query.getString(query.getColumnIndex("snippet")).replaceAll("\\\n", " "));
                        } catch (Exception e)
                        {
                        }

                        inboxDate.add(query.getString(query.getColumnIndex("date")));
                        inboxNumber.add(query.getString(query.getColumnIndex("recipient_ids")));

                        if (query.getString(query.getColumnIndex("recipient_ids")).split(" ").length > 1)
                        {
                            group.add("yes");
                        } else
                        {
                            group.add("no");
                        }
                    } while (query.moveToNext());
                }

                query.close();
            } catch (Exception e)
            {

            }
        }
		
		if (inboxNumber.size() > 0)
		{
			messageEntry.setVisibility(View.VISIBLE);
			sendButton.setVisibility(View.VISIBLE);
			v.setVisibility(View.VISIBLE);
			
			if (sharedPrefs.getBoolean("hide_title_bar", true))
			{
				title.setVisibility(View.VISIBLE);
			}
		} else
		{
			messageEntry.setVisibility(View.GONE);
			sendButton.setVisibility(View.GONE);
			v.setVisibility(View.GONE);
			title.setVisibility(View.GONE);
			emojiButton.setVisibility(View.GONE);
			
			getWindow().getDecorView().setBackgroundColor(sharedPrefs.getInt("ct_messageListBackground", getResources().getColor(R.color.light_silver)));
		}
		
		if (refreshMyContact)
		{
			String[] mProjection = new String[]
				    {
				        Profile._ID
				    };
	
			Cursor mProfileCursor =
				        getContentResolver().query(
				                Profile.CONTENT_URI,
				                mProjection ,
				                null,
				                null,
				                null);
			
			try
			{
				if (mProfileCursor.moveToFirst())
				{
					myContactId = mProfileCursor.getString(mProfileCursor.getColumnIndex(Profile._ID));
				}
			} catch (Exception e)
			{
				myContactId = myPhoneNumber;
			} finally
			{	
				mProfileCursor.close();
			}

			
			refreshMyContact = false;
		}
	}

    public static String findContactNumber(String id, Context context) {
        try {
            String[] ids = id.split(" ");
            String numbers = "";

            for (int i = 0; i < ids.length; i++)
            {
                try
                {
                    if (ids[i] != null && (!ids[i].equals("") || !ids[i].equals(" ")))
                    {
                        Cursor number = context.getContentResolver().query(Uri.parse("content://mms-sms/canonical-addresses"), null, "_id=" + ids[i], null, null);

                        if (number.moveToFirst())
                        {
                            numbers += number.getString(number.getColumnIndex("address")).replace("-", "").replace(")", "").replace("(", "").replace(" ", "") + " ";
                        } else
                        {
                            numbers += ids[i] + " ";
                        }

                        number.close();
                    } else
                    {

                    }
                } catch (Exception e)
                {
                    numbers += "0 ";
                }
            }

            return numbers;
        } catch (Exception e) {
            return id;
        }
    }
	
	public static String findContactName(String number, Context context)
	{
		String name = "";

		String origin = number;

		if (origin.length() != 0)
		{
			Uri phoneUri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(origin));
			Cursor phonesCursor = context.getContentResolver().query(phoneUri, new String[] {ContactsContract.Contacts.DISPLAY_NAME_PRIMARY, ContactsContract.RawContacts._ID}, null, null, ContactsContract.Contacts.DISPLAY_NAME + " desc limit 1");

			if(phonesCursor != null && phonesCursor.moveToFirst()) {
				name = phonesCursor.getString(0);
			} else
			{
				if (!number.equals(""))
				{
					try
					{
						Locale sCachedLocale = Locale.getDefault();
						int sFormatType = PhoneNumberUtils.getFormatTypeForLocale(sCachedLocale);
						Editable editable = new SpannableStringBuilder(number);
						PhoneNumberUtils.formatNumber(editable, sFormatType);
						name = editable.toString();
					} catch (Exception e)
					{
						name = number;
					}
				} else
				{
					name = "No Information";
				}
			}

			phonesCursor.close();
		} else
		{
			if (!number.equals(""))
			{
				try
				{
					Long.parseLong(number.replaceAll("[^0-9]", ""));
					Locale sCachedLocale = Locale.getDefault();
					int sFormatType = PhoneNumberUtils.getFormatTypeForLocale(sCachedLocale);
					Editable editable = new SpannableStringBuilder(number);
					PhoneNumberUtils.formatNumber(editable, sFormatType);
					name = editable.toString();
				} catch (Exception e)
				{
					name = number;
				}
			} else
			{
				name = "No Information";
			}
		}

		return name;
	}
	
	public static String loadGroupContacts(String numbers, Context context)
	{
		String names = "";
		String[] number;
		
		try
		{
			number = numbers.split(" ");
		} catch (Exception e)
		{
			return "";
		}
		
		for (int i = 0; i < number.length; i++)
		{				
			try
			{
				String origin = number[i];
				
				Uri phoneUri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(origin));
				Cursor phonesCursor = context.getContentResolver().query(phoneUri, new String[] {ContactsContract.Contacts.DISPLAY_NAME_PRIMARY, ContactsContract.RawContacts._ID}, null, null, ContactsContract.Contacts.DISPLAY_NAME + " desc limit 1");
	
				if(phonesCursor != null && phonesCursor.moveToFirst()) {
					names += ", " + phonesCursor.getString(0);
				} else
				{
					try
					{
						Locale sCachedLocale = Locale.getDefault();
						int sFormatType = PhoneNumberUtils.getFormatTypeForLocale(sCachedLocale);
						Editable editable = new SpannableStringBuilder(number[i]);
						PhoneNumberUtils.formatNumber(editable, sFormatType);
						names += ", " + editable.toString();
					} catch (Exception e)
					{
						names += ", " + number;
					}
				}
				
				phonesCursor.close();
			} catch (IllegalArgumentException e)
			{
				try
				{
					Locale sCachedLocale = Locale.getDefault();
					int sFormatType = PhoneNumberUtils.getFormatTypeForLocale(sCachedLocale);
					Editable editable = new SpannableStringBuilder(number[i]);
					PhoneNumberUtils.formatNumber(editable, sFormatType);
					names += ", " + editable.toString();
				} catch (Exception f)
				{
					names += ", " + number;
				}
			}
		}
		
		try
		{
			return names.substring(2);
		} catch (Exception e)
		{
			return "";
		}
	}
	
	@SuppressWarnings("deprecation")
	public void setUpSendbar()
	{
		mTextView = (TextView) findViewById(R.id.charsRemaining2);
		messageEntry = (EditText) findViewById(R.id.messageEntry);
		sendButton = (ImageButton) findViewById(R.id.sendButton);
		emojiButton = (ImageButton) findViewById(R.id.display_emoji);
		v = findViewById(R.id.view1);
		imageAttachBackground = findViewById(R.id.image_attachment_view_background2);
		imageAttach = (ImageAttachmentView) findViewById(R.id.image_attachment_view);

        deviceType = messageEntry.getTag().toString();

        try {
            if (deviceType.equals("phablet") || deviceType.equals("tablet"))
            {
                getActionBar().setDisplayHomeAsUpEnabled(false);
            }
        } catch (Exception e) {
            // no action bar, dialog theme
        }
		
		if (!sharedPrefs.getBoolean("keyboard_type", true))
		{
			messageEntry.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
			messageEntry.setImeOptions(EditorInfo.IME_ACTION_NONE);
		}

        if (deviceType.equals("phablet") || deviceType.equals("tablet"))
        {
            if (!sharedPrefs.getBoolean("keyboard_type", true))
            {
                messageEntry.setImeOptions(EditorInfo.IME_ACTION_NONE | EditorInfo.IME_FLAG_NO_EXTRACT_UI);
            } else
            {
                messageEntry.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
            }
        }
		
		mTextView.setVisibility(View.GONE);

		messageEntry.addTextChangedListener(new TextWatcher() {
	        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
	        }

	        public void onTextChanged(CharSequence s, int start, int before, int count) {
	        	int length = Integer.parseInt(String.valueOf(s.length()));

	        	if (!sharedPrefs.getString("signature", "").equals(""))
	        	{
	        		length += ("\n" + sharedPrefs.getString("signature", "")).length();
	        	}

	        	String patternStr = "[^" + MainActivity.GSM_CHARACTERS_REGEX + "]";
				Pattern pattern = Pattern.compile(patternStr);
				Matcher matcher = pattern.matcher(s);

				int size = 160;

				if (matcher.find() && !sharedPrefs.getBoolean("strip_unicode", false))
				{
					size = 70;
				}

	        	int pages = 1;

	        	while (length > size)
	        	{
	        		length-=size;
	        		pages++;
	        	}

	            mTextView.setText(pages + "/" + (size - length));

	            if ((pages == 1 && (size - length) <= 30) || pages != 1)
	            {
	            	mTextView.setVisibility(View.VISIBLE);
	            }

	            if ((pages + "/" + (size - length)).equals("1/31"))
	            {
	            	mTextView.setVisibility(View.GONE);
	            }

	            if ((pages + "/" + (size - length)).equals("1/160"))
	            {
	            	mTextView.setVisibility(View.GONE);
	            }

	            if (imageAttach.getVisibility() == View.VISIBLE || group.get(mViewPager.getCurrentItem()).equals("yes"))
	            {
	            	mTextView.setVisibility(View.GONE);
	            }

	            if (sharedPrefs.getBoolean("send_as_mms", false) && pages >= sharedPrefs.getInt("mms_after", 4))
	            {
	            	mTextView.setVisibility(View.GONE);
	            }

	            if (sharedPrefs.getBoolean("send_with_return", false))
	            {
	            	if (messageEntry.getText().toString().endsWith("\n"))
	            	{
	            		messageEntry.setText(messageEntry.getText().toString().substring(0, messageEntry.getText().toString().length() - 1));
	            		sendButton.performClick();
	            	}
	            }

                messageEntry.setError(null);
	        }

	        public void afterTextChanged(Editable s) {
                if (sharedPrefs.getBoolean("enable_drafts", true)) {
                    if (newDraft.equals(""))
                    {
                        newDraft = threadIds.get(mViewPager.getCurrentItem());
                    }
                }
	        }
		});

		final Context context = this;
		
		sendButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				
				Intent updateWidget = new Intent("com.klinker.android.messaging.UPDATE_WIDGET");
				context.sendBroadcast(updateWidget);
				
				boolean sendMmsFromLength = false;
				String[] counter = mTextView.getText().toString().split("/");
				
				if (Integer.parseInt(counter[0]) >= sharedPrefs.getInt("mms_after", 4) && sharedPrefs.getBoolean("send_as_mms", false))
				{
					sendMmsFromLength = true;
				}
				
				if (group.get(mViewPager.getCurrentItem()).equals("no") && imageAttach.getVisibility() == View.GONE && sendMmsFromLength == false)
				{
					if (messageEntry.getText().toString().equals(""))
					{
						messageEntry.setError("Nothing to send");
					} else
					{
						MainActivity.animationOn = true;
						
						if (sharedPrefs.getBoolean("hide_keyboard", false))
						{
							new Thread(new Runnable() {

								@Override
								public void run() {
									try {
										Thread.sleep(2000);
									} catch (InterruptedException e) {
										e.printStackTrace();
									} finally
									{
										InputMethodManager keyboard = (InputMethodManager)
								                getSystemService(Context.INPUT_METHOD_SERVICE);
								        keyboard.hideSoftInputFromWindow(messageEntry.getWindowToken(), 0);
									}
									
								}
								
							}).start();
						}
						
						String body2 = messageEntry.getText().toString();
						
						if (!sharedPrefs.getString("signature", "").equals(""))
						{
							body2 += "\n" + sharedPrefs.getString("signature", "");
						}
						
						final String body = body2;
						final int position2 = mViewPager.getCurrentItem();
						
						new Thread(new Runnable() {

							@Override
							public void run() {
								
								if (sharedPrefs.getBoolean("delivery_reports", false))
								{
									if (!findContactNumber(inboxNumber.get(position2), context).replaceAll("[^0-9]", "").equals(""))
									{
										String SENT = "SMS_SENT";
								        String DELIVERED = "SMS_DELIVERED";
								 
								        PendingIntent sentPI = PendingIntent.getBroadcast(context, 0,
								            new Intent(SENT), 0);
								 
								        PendingIntent deliveredPI = PendingIntent.getBroadcast(context, 0,
								            new Intent(DELIVERED), 0);
								 
								        //---when the SMS has been sent---
								        context.registerReceiver(new BroadcastReceiver(){
								            @Override
								            public void onReceive(Context arg0, Intent arg1) {
                                                try {
                                                    switch (getResultCode())
                                                    {
                                                        case Activity.RESULT_OK:
                                                            Cursor query = context.getContentResolver().query(Uri.parse("content://sms/outbox"), null, null, null, null);

                                                            if (query.moveToFirst())
                                                            {
                                                                String id = query.getString(query.getColumnIndex("_id"));
                                                                ContentValues values = new ContentValues();
                                                                values.put("type", "2");
                                                                context.getContentResolver().update(Uri.parse("content://sms/outbox"), values, "_id=" + id, null);
                                                                ((MainActivity) context).refreshViewPager3();
                                                            }

                                                            query.close();

                                                            if (sharedPrefs.getBoolean("message_sounds", false)) {
                                                                AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
                                                                float actualVolume = (float) audioManager
                                                                        .getStreamVolume(AudioManager.STREAM_NOTIFICATION);
                                                                float maxVolume = (float) audioManager
                                                                        .getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION);
                                                                float volume = actualVolume / maxVolume;
                                                                soundPool.play(ping, volume, volume, 1, 0, 1f);
                                                            }

                                                            break;
                                                        case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                                                            try
                                                            {
                                                                wait(500);
                                                            } catch (Exception e)
                                                            {

                                                            }

                                                            query = context.getContentResolver().query(Uri.parse("content://sms/outbox"), null, null, null, null);

                                                            if (query.moveToFirst())
                                                            {
                                                                String id = query.getString(query.getColumnIndex("_id"));
                                                                ContentValues values = new ContentValues();
                                                                values.put("type", "5");
                                                                context.getContentResolver().update(Uri.parse("content://sms/outbox"), values, "_id=" + id, null);
                                                                ((MainActivity) context).refreshViewPager3();
                                                            }

                                                            NotificationCompat.Builder mBuilder =
                                                                    new NotificationCompat.Builder(context)
                                                                            .setSmallIcon(R.drawable.ic_alert)
                                                                            .setContentTitle("Error")
                                                                            .setContentText("Could not send message");

                                                            Intent resultIntent = new Intent(context, MainActivity.class);

                                                            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                                                            stackBuilder.addParentStack(MainActivity.class);
                                                            stackBuilder.addNextIntent(resultIntent);
                                                            PendingIntent resultPendingIntent =
                                                                    stackBuilder.getPendingIntent(
                                                                            0,
                                                                            PendingIntent.FLAG_UPDATE_CURRENT
                                                                    );

                                                            mBuilder.setContentIntent(resultPendingIntent);
                                                            mBuilder.setAutoCancel(true);
                                                            long[] pattern = {0L, 400L, 100L, 400L};
                                                            mBuilder.setVibrate(pattern);
                                                            mBuilder.setLights(0xFFffffff, 1000, 2000);

                                                            try
                                                            {
                                                                mBuilder.setSound(Uri.parse(sharedPrefs.getString("ringtone", "null")));
                                                            } catch(Exception e)
                                                            {
                                                                mBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
                                                            }

                                                            NotificationManager mNotificationManager =
                                                                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                                                            Notification notification = mBuilder.build();
                                                            Intent deleteIntent = new Intent(context, NotificationReceiver.class);
                                                            notification.deleteIntent = PendingIntent.getBroadcast(context, 0, deleteIntent, 0);
                                                            mNotificationManager.notify(1, notification);
                                                            break;
                                                        case SmsManager.RESULT_ERROR_NO_SERVICE:
                                                            try
                                                            {
                                                                wait(500);
                                                            } catch (Exception e)
                                                            {

                                                            }

                                                            query = context.getContentResolver().query(Uri.parse("content://sms/outbox"), null, null, null, null);

                                                            if (query.moveToFirst())
                                                            {
                                                                String id = query.getString(query.getColumnIndex("_id"));
                                                                ContentValues values = new ContentValues();
                                                                values.put("type", "5");
                                                                context.getContentResolver().update(Uri.parse("content://sms/outbox"), values, "_id=" + id, null);
                                                                ((MainActivity) context).refreshViewPager3();
                                                            }

                                                            Toast.makeText(context, "No service",
                                                                    Toast.LENGTH_SHORT).show();
                                                            break;
                                                        case SmsManager.RESULT_ERROR_NULL_PDU:
                                                            try
                                                            {
                                                                wait(500);
                                                            } catch (Exception e)
                                                            {

                                                            }

                                                            query = context.getContentResolver().query(Uri.parse("content://sms/outbox"), null, null, null, null);

                                                            if (query.moveToFirst())
                                                            {
                                                                String id = query.getString(query.getColumnIndex("_id"));
                                                                ContentValues values = new ContentValues();
                                                                values.put("type", "5");
                                                                context.getContentResolver().update(Uri.parse("content://sms/outbox"), values, "_id=" + id, null);
                                                                ((MainActivity) context).refreshViewPager3();
                                                            }

                                                            Toast.makeText(context, "Null PDU",
                                                                    Toast.LENGTH_SHORT).show();
                                                            break;
                                                        case SmsManager.RESULT_ERROR_RADIO_OFF:
                                                            try
                                                            {
                                                                wait(500);
                                                            } catch (Exception e)
                                                            {

                                                            }

                                                            query = context.getContentResolver().query(Uri.parse("content://sms/outbox"), null, null, null, null);

                                                            if (query.moveToFirst())
                                                            {
                                                                String id = query.getString(query.getColumnIndex("_id"));
                                                                ContentValues values = new ContentValues();
                                                                values.put("type", "5");
                                                                context.getContentResolver().update(Uri.parse("content://sms/outbox"), values, "_id=" + id, null);
                                                                ((MainActivity) context).refreshViewPager3();
                                                            }

                                                            Toast.makeText(context, "Radio off",
                                                                    Toast.LENGTH_SHORT).show();
                                                            break;
                                                    }

                                                    context.unregisterReceiver(this);
                                                } catch (Exception e) {

                                                }
								            }
								        }, new IntentFilter(SENT));
								 
								        //---when the SMS has been delivered---
								        context.registerReceiver(new BroadcastReceiver(){
								            @Override
								            public void onReceive(Context arg0, Intent arg1) {
                                                try {
                                                    if (sharedPrefs.getString("delivery_options", "2").equals("1"))
                                                    {
                                                        switch (getResultCode())
                                                        {
                                                            case Activity.RESULT_OK:
                                                                AlertDialog.Builder builder = new AlertDialog.Builder(context);

                                                                try
                                                                {
                                                                    builder.setTitle(loadGroupContacts(findContactNumber(inboxNumber.get(position2), context), context));
                                                                } catch (Exception e)
                                                                {

                                                                }

                                                                builder.setMessage(R.string.message_delivered)
                                                                        .setPositiveButton(R.string.dismiss, new DialogInterface.OnClickListener() {
                                                                            public void onClick(DialogInterface dialog, int id) {
                                                                                dialog.dismiss();
                                                                            }
                                                                        });

                                                                builder.create().show();

                                                                Cursor query = context.getContentResolver().query(Uri.parse("content://sms/sent"), null, null, null, "date desc");

                                                                if (query.moveToFirst())
                                                                {
                                                                    String id = query.getString(query.getColumnIndex("_id"));
                                                                    ContentValues values = new ContentValues();
                                                                    values.put("status", "0");
                                                                    context.getContentResolver().update(Uri.parse("content://sms/sent"), values, "_id=" + id, null);
                                                                    ((MainActivity) context).refreshViewPager3();
                                                                }

                                                                query.close();

                                                                break;
                                                            case Activity.RESULT_CANCELED:
                                                                AlertDialog.Builder builder2 = new AlertDialog.Builder(context);

                                                                try
                                                                {
                                                                    builder2.setTitle(loadGroupContacts(findContactNumber(inboxNumber.get(position2), context), context));
                                                                } catch (Exception e)
                                                                {

                                                                }

                                                                builder2.setMessage(R.string.message_not_delivered)
                                                                        .setPositiveButton(R.string.dismiss, new DialogInterface.OnClickListener() {
                                                                            public void onClick(DialogInterface dialog, int id) {
                                                                                dialog.dismiss();
                                                                            }
                                                                        });

                                                                builder2.create().show();

                                                                Cursor query2 = context.getContentResolver().query(Uri.parse("content://sms/sent"), null, null, null, "date desc");

                                                                if (query2.moveToFirst())
                                                                {
                                                                    String id = query2.getString(query2.getColumnIndex("_id"));
                                                                    ContentValues values = new ContentValues();
                                                                    values.put("status", "64");
                                                                    context.getContentResolver().update(Uri.parse("content://sms/sent"), values, "_id=" + id, null);
                                                                    ((MainActivity) context).refreshViewPager3();
                                                                }

                                                                query2.close();

                                                                break;
                                                        }
                                                    } else
                                                    {
                                                        switch (getResultCode())
                                                        {
                                                            case Activity.RESULT_OK:
                                                                if (sharedPrefs.getString("delivery_options", "2").equals("2"))
                                                                {
                                                                    Toast.makeText(context, R.string.message_delivered, Toast.LENGTH_LONG).show();
                                                                }

                                                                Cursor query = context.getContentResolver().query(Uri.parse("content://sms/sent"), null, null, null, "date desc");

                                                                if (query.moveToFirst())
                                                                {
                                                                    String id = query.getString(query.getColumnIndex("_id"));
                                                                    ContentValues values = new ContentValues();
                                                                    values.put("status", "0");
                                                                    context.getContentResolver().update(Uri.parse("content://sms/sent"), values, "_id=" + id, null);
                                                                    ((MainActivity) context).refreshViewPager3();
                                                                }

                                                                query.close();

                                                                break;
                                                            case Activity.RESULT_CANCELED:
                                                                if (sharedPrefs.getString("delivery_options", "2").equals("2"))
                                                                {
                                                                    Toast.makeText(context, R.string.message_not_delivered, Toast.LENGTH_LONG).show();
                                                                }

                                                                Cursor query2 = context.getContentResolver().query(Uri.parse("content://sms/sent"), null, null, null, "date desc");

                                                                if (query2.moveToFirst())
                                                                {
                                                                    String id = query2.getString(query2.getColumnIndex("_id"));
                                                                    ContentValues values = new ContentValues();
                                                                    values.put("status", "64");
                                                                    context.getContentResolver().update(Uri.parse("content://sms/sent"), values, "_id=" + id, null);
                                                                    ((MainActivity) context).refreshViewPager3();
                                                                }

                                                                query2.close();

                                                                break;
                                                        }
                                                    }

                                                    context.unregisterReceiver(this);
                                                } catch (Exception e) {

                                                }
								            }
								        }, new IntentFilter(DELIVERED));
								        
								        ArrayList<PendingIntent> sPI = new ArrayList<PendingIntent>();
								        ArrayList<PendingIntent> dPI = new ArrayList<PendingIntent>();
								        
								        String body2 = body;
								        
								        if (sharedPrefs.getBoolean("strip_unicode", false))
								        {
								        	body2 = StripAccents.stripAccents(body2);
								        }
								        
										SmsManager smsManager = SmsManager.getDefault();

										if (sharedPrefs.getBoolean("split_sms", false))
										{
											int length = 160;
											
											String patternStr = "[^\\x20-\\x7E]";
											Pattern pattern = Pattern.compile(patternStr);
											Matcher matcher = pattern.matcher(body2);
											  
											if (matcher.find())
											{
												length = 70;
											}

                                            boolean counter = false;

                                            if (sharedPrefs.getBoolean("split_counter", false)) {
                                                counter = true;
                                                length -= 7;
                                            }
											
											String[] textToSend = splitByLength(body2, length, counter);
											
											for (int i = 0; i < textToSend.length; i++)
											{
												ArrayList<String> parts = smsManager.divideMessage(textToSend[i]); 
												
												for (int j = 0; j < parts.size(); j++)
												{
													sPI.add(sentPI);
													dPI.add(deliveredPI);
												}
												
												smsManager.sendMultipartTextMessage(findContactNumber(inboxNumber.get(position2), context), null, parts, sPI, dPI);
											}
										} else
										{
											ArrayList<String> parts = smsManager.divideMessage(body2); 
											
											for (int i = 0; i < parts.size(); i++)
											{
												sPI.add(sentPI);
												dPI.add(deliveredPI);
											}

											try {
												smsManager.sendMultipartTextMessage(findContactNumber(inboxNumber.get(position2), context), null, parts, sPI, dPI);
											} catch (Exception e) {
                                                getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {

                                                    @Override
                                                    public void run() {
                                                        Toast.makeText(context, "Error, check the \"Split SMS\" option in advanced settings and retry.", Toast.LENGTH_LONG).show();
                                                    }

                                                });
											}
										}
									} else
									{
									}
								} else
								{
									if (!findContactNumber(inboxNumber.get(position2), context).replaceAll("[^0-9]", "").equals(""))
									{
										String SENT = "SMS_SENT";
										 
								        PendingIntent sentPI = PendingIntent.getBroadcast(context, 0,
								            new Intent(SENT), 0);
								 
								        //---when the SMS has been sent---
								        context.registerReceiver(new BroadcastReceiver(){
								            @Override
								            public void onReceive(Context arg0, Intent arg1) {
                                                try {
                                                    switch (getResultCode())
                                                    {
                                                        case Activity.RESULT_OK:
                                                            Cursor query = context.getContentResolver().query(Uri.parse("content://sms/outbox"), null, null, null, null);

                                                            if (query.moveToFirst())
                                                            {
                                                                String id = query.getString(query.getColumnIndex("_id"));
                                                                ContentValues values = new ContentValues();
                                                                values.put("type", "2");
                                                                context.getContentResolver().update(Uri.parse("content://sms/outbox"), values, "_id=" + id, null);
                                                                ((MainActivity) context).refreshViewPager3();
                                                            }

                                                            query.close();

                                                            break;
                                                        case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                                                            try
                                                            {
                                                                wait(500);
                                                            } catch (Exception e)
                                                            {

                                                            }

                                                            query = context.getContentResolver().query(Uri.parse("content://sms/outbox"), null, null, null, null);

                                                            if (query.moveToFirst())
                                                            {
                                                                String id = query.getString(query.getColumnIndex("_id"));
                                                                ContentValues values = new ContentValues();
                                                                values.put("type", "5");
                                                                context.getContentResolver().update(Uri.parse("content://sms/outbox"), values, "_id=" + id, null);
                                                                ((MainActivity) context).refreshViewPager3();
                                                            }

                                                            NotificationCompat.Builder mBuilder =
                                                                    new NotificationCompat.Builder(context)
                                                                            .setSmallIcon(R.drawable.ic_alert)
                                                                            .setContentTitle("Error")
                                                                            .setContentText("Could not send message");

                                                            Intent resultIntent = new Intent(context, MainActivity.class);

                                                            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                                                            stackBuilder.addParentStack(MainActivity.class);
                                                            stackBuilder.addNextIntent(resultIntent);
                                                            PendingIntent resultPendingIntent =
                                                                    stackBuilder.getPendingIntent(
                                                                            0,
                                                                            PendingIntent.FLAG_UPDATE_CURRENT
                                                                    );

                                                            mBuilder.setContentIntent(resultPendingIntent);
                                                            mBuilder.setAutoCancel(true);
                                                            long[] pattern = {0L, 400L, 100L, 400L};
                                                            mBuilder.setVibrate(pattern);
                                                            mBuilder.setLights(0xFFffffff, 1000, 2000);

                                                            try
                                                            {
                                                                mBuilder.setSound(Uri.parse(sharedPrefs.getString("ringtone", "null")));
                                                            } catch(Exception e)
                                                            {
                                                                mBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
                                                            }

                                                            NotificationManager mNotificationManager =
                                                                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                                                            Notification notification = mBuilder.build();
                                                            Intent deleteIntent = new Intent(context, NotificationReceiver.class);
                                                            notification.deleteIntent = PendingIntent.getBroadcast(context, 0, deleteIntent, 0);
                                                            mNotificationManager.notify(1, notification);
                                                            break;
                                                        case SmsManager.RESULT_ERROR_NO_SERVICE:
                                                            try
                                                            {
                                                                wait(500);
                                                            } catch (Exception e)
                                                            {

                                                            }

                                                            query = context.getContentResolver().query(Uri.parse("content://sms/outbox"), null, null, null, null);

                                                            if (query.moveToFirst())
                                                            {
                                                                String id = query.getString(query.getColumnIndex("_id"));
                                                                ContentValues values = new ContentValues();
                                                                values.put("type", "5");
                                                                context.getContentResolver().update(Uri.parse("content://sms/outbox"), values, "_id=" + id, null);
                                                                ((MainActivity) context).refreshViewPager3();
                                                            }

                                                            Toast.makeText(context, "No service",
                                                                    Toast.LENGTH_SHORT).show();
                                                            break;
                                                        case SmsManager.RESULT_ERROR_NULL_PDU:
                                                            try
                                                            {
                                                                wait(500);
                                                            } catch (Exception e)
                                                            {

                                                            }

                                                            query = context.getContentResolver().query(Uri.parse("content://sms/outbox"), null, null, null, null);

                                                            if (query.moveToFirst())
                                                            {
                                                                String id = query.getString(query.getColumnIndex("_id"));
                                                                ContentValues values = new ContentValues();
                                                                values.put("type", "5");
                                                                context.getContentResolver().update(Uri.parse("content://sms/outbox"), values, "_id=" + id, null);
                                                                ((MainActivity) context).refreshViewPager3();
                                                            }

                                                            Toast.makeText(context, "Null PDU",
                                                                    Toast.LENGTH_SHORT).show();
                                                            break;
                                                        case SmsManager.RESULT_ERROR_RADIO_OFF:
                                                            try
                                                            {
                                                                wait(500);
                                                            } catch (Exception e)
                                                            {

                                                            }

                                                            query = context.getContentResolver().query(Uri.parse("content://sms/outbox"), null, null, null, null);

                                                            if (query.moveToFirst())
                                                            {
                                                                String id = query.getString(query.getColumnIndex("_id"));
                                                                ContentValues values = new ContentValues();
                                                                values.put("type", "5");
                                                                context.getContentResolver().update(Uri.parse("content://sms/outbox"), values, "_id=" + id, null);
                                                                ((MainActivity) context).refreshViewPager3();
                                                            }

                                                            Toast.makeText(context, "Radio off",
                                                                    Toast.LENGTH_SHORT).show();
                                                            break;
                                                    }

                                                    context.unregisterReceiver(this);
                                                } catch (Exception e) {

                                                }
								            }
								        }, new IntentFilter(SENT));
								        
								        ArrayList<PendingIntent> sPI = new ArrayList<PendingIntent>();
								        
								        String body2 = body;
								        
								        if (sharedPrefs.getBoolean("strip_unicode", false))
								        {
								        	body2 = StripAccents.stripAccents(body2);
								        }
								        
										SmsManager smsManager = SmsManager.getDefault();

										if (sharedPrefs.getBoolean("split_sms", false))
										{
											int length = 160;
											
											String patternStr = "[^\\x20-\\x7E]";
											Pattern pattern = Pattern.compile(patternStr);
											Matcher matcher = pattern.matcher(body2);
											  
											if (matcher.find())
											{
												length = 70;
											}

                                            boolean counter = false;

                                            if (sharedPrefs.getBoolean("split_counter", false)) {
                                                counter = true;
                                                length -= 7;
                                            }

                                            String[] textToSend = splitByLength(body2, length, counter);
											
											for (int i = 0; i < textToSend.length; i++)
											{
												ArrayList<String> parts = smsManager.divideMessage(textToSend[i]); 
												
												for (int j = 0; j < parts.size(); j++)
												{
													sPI.add(sentPI);
												}
												
												smsManager.sendMultipartTextMessage(findContactNumber(inboxNumber.get(position2), context), null, parts, sPI, null);
											}
										} else
										{
											ArrayList<String> parts = smsManager.divideMessage(body2); 
											
											for (int i = 0; i < parts.size(); i++)
											{
												sPI.add(sentPI);
											}

											try {
												smsManager.sendMultipartTextMessage(findContactNumber(inboxNumber.get(position2), context), null, parts, sPI, null);
											} catch (Exception e) {
                                                getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {

                                                    @Override
                                                    public void run() {
                                                        Toast.makeText(context, "Error, check the \"Split SMS\" option in advanced settings and retry.", Toast.LENGTH_LONG).show();
                                                    }

                                                });
											}
										}
									} else
									{
									}
								}
								
								String address = findContactNumber(inboxNumber.get(position2), context);
								String body2 = body;
								
								if (sharedPrefs.getBoolean("strip_unicode", false))
								{
									body2 = StripAccents.stripAccents(body2);
								}
							    
								if (!address.replaceAll("[^0-9]", "").equals(""))
								{
								    final Calendar cal = Calendar.getInstance();
								    ContentValues values = new ContentValues();
								    values.put("address", address);
								    values.put("body", body2); 
								    values.put("date", cal.getTimeInMillis() + "");
								    values.put("read", true);
                                    values.put("thread_id", threadIds.get(mViewPager.getCurrentItem()));
								    context.getContentResolver().insert(Uri.parse("content://sms/outbox"), values);
									
								    final String address2 = address;
								    
								    ((Activity) context).getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {
	
										@Override
										public void run() {
                                            if (sharedPrefs.getBoolean("enable_drafts", true)) {
                                                if (fromDraft)
                                                {
                                                    try {
                                                        for (int i = 0; i < draftNames.size(); i++)
                                                        {
                                                            if (draftNames.get(i).equals(threadIds.get(mViewPager.getCurrentItem())))
                                                            {
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
								        	refreshViewPager4(address2, StripAccents.stripAccents(body), cal.getTimeInMillis() + "");
								        	mViewPager.setCurrentItem(0);
								        	mTextView.setVisibility(View.GONE);
										}
								    	
								    });
								}
							}
							
						}).start();
						
						messageEntry.setText("");
					}
				} else
				{
					Bitmap b;
					byte[] byteArray;
					
					try
					{
						if (!fromCamera)
						{
							b = decodeFile2(new File(getPath(attachedImage)));
							ByteArrayOutputStream stream = new ByteArrayOutputStream();
							b.compress(Bitmap.CompressFormat.PNG, 100, stream);
							byteArray = stream.toByteArray();
						} else
						{
							b = decodeFile2(new File(Environment.getExternalStorageDirectory() + "/SlidingMessaging/", "photoToSend.png"));
							ByteArrayOutputStream stream = new ByteArrayOutputStream();
							b.compress(Bitmap.CompressFormat.PNG, 100, stream);
							byteArray = stream.toByteArray();
						}
					} catch (Exception e)
					{
						byteArray = null;
					}
					
					String body = messageEntry.getText().toString();
					
					String[] to = ("insert-address-token " + findContactNumber(inboxNumber.get(mViewPager.getCurrentItem()), context)).split(" ");

                    if (!sharedPrefs.getBoolean("send_with_stock", false))
                    {
                        if (multipleAttachments == false)
                        {
                            insert(context, to, "", byteArray, body);

                            MMSPart[] parts = new MMSPart[2];

                            if (imageAttach.getVisibility() == View.VISIBLE)
                            {
                                parts[0] = new MMSPart();
                                parts[0].Name = "Image";
                                parts[0].MimeType = "image/png";
                                parts[0].Data = byteArray;

                                if (!body.equals(""))
                                {
                                    parts[1] = new MMSPart();
                                    parts[1].Name = "Text";
                                    parts[1].MimeType = "text/plain";
                                    parts[1].Data = body.getBytes();
                                }
                            } else
                            {
                                parts[0] = new MMSPart();
                                parts[0].Name = "Text";
                                parts[0].MimeType = "text/plain";
                                parts[0].Data = body.getBytes();
                            }

                            sendMMS(findContactNumber(inboxNumber.get(mViewPager.getCurrentItem()), context), parts);
                        } else
                        {
                            ArrayList<byte[]> bytes = new ArrayList<byte[]>();
                            ArrayList<String> mimes = new ArrayList<String>();

                            for (int i = 0; i < AttachMore.data.size(); i++)
                            {
                                bytes.add(AttachMore.data.get(i).Data);
                                mimes.add(AttachMore.data.get(i).MimeType);
                            }

                            insert(context, to, "", bytes, mimes, body);

                            MMSPart part = new MMSPart();
                            part.Name = "Text";
                            part.MimeType = "text/plain";
                            part.Data = body.getBytes();
                            AttachMore.data.add(part);

                            sendMMS(findContactNumber(inboxNumber.get(mViewPager.getCurrentItem()), context), AttachMore.data.toArray(new MMSPart[AttachMore.data.size()]));

                            AttachMore.data = new ArrayList<MMSPart>();
                        }
                    } else
                    {
                        if (multipleAttachments == false)
                        {
                            Intent sendIntent = new Intent(Intent.ACTION_SEND);
                            sendIntent.putExtra("address", findContactNumber(inboxNumber.get(mViewPager.getCurrentItem()), context).replace(" ", ";"));
                            sendIntent.putExtra("sms_body", body);
                            sendIntent.putExtra(Intent.EXTRA_STREAM, attachedImage);
                            sendIntent.setType("image/png");
                            startActivity(sendIntent);

                            com.klinker.android.messaging_sliding.MainActivity.messageRecieved = true;
                        } else
                        {
                            Toast.makeText(context, "Cannot send multiple images through stock", Toast.LENGTH_SHORT).show();
                        }
                    }
					
					messageEntry.setText("");
					imageAttach.setVisibility(false);
					imageAttachBackground.setVisibility(View.GONE);
					
					refreshViewPager4(findContactNumber(inboxNumber.get(mViewPager.getCurrentItem()), context), StripAccents.stripAccents(body), "0");
				}
			}
			
		});

        try {
            messageEntry.setTextSize(Integer.parseInt(sharedPrefs.getString("text_size", "14").substring(0,2)));
        } catch (Exception e) {
            messageEntry.setTextSize(Integer.parseInt(sharedPrefs.getString("text_size", "14").substring(0,1)));
        }

		
		if (!sharedPrefs.getBoolean("emoji", false))
		{
			emojiButton.setVisibility(View.GONE);
			LayoutParams params = (RelativeLayout.LayoutParams)messageEntry.getLayoutParams();
			params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
			messageEntry.setLayoutParams(params);
		} else
		{
			emojiButton.setOnClickListener(new OnClickListener() {

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
					
					final StickyGridHeadersGridView emojiGrid = (StickyGridHeadersGridView) frame.findViewById(R.id.emojiGrid);
					Button okButton = (Button) frame.findViewById(R.id.emoji_ok);
					
					if (sharedPrefs.getBoolean("emoji_type", true))
					{
						emojiGrid.setAdapter(new EmojiAdapter2(context));
						emojiGrid.setOnItemClickListener(new OnItemClickListener() {
						
								public void onItemClick(AdapterView<?> parent, View v, int position, long id)
								{
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
					} else
					{
						emojiGrid.setAdapter(new EmojiAdapter(context));
						emojiGrid.setOnItemClickListener(new OnItemClickListener() {
						
								public void onItemClick(AdapterView<?> parent, View v, int position, long id)
								{
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
							if (sharedPrefs.getBoolean("emoji_type", true))
							{
								messageEntry.setText(EmojiConverter2.getSmiledText(context, messageEntry.getText().toString() + editText.getText().toString()));
								messageEntry.setSelection(messageEntry.getText().length());
							} else
							{
								messageEntry.setText(EmojiConverter.getSmiledText(context, messageEntry.getText().toString() + editText.getText().toString()));
								messageEntry.setSelection(messageEntry.getText().length());
							}
							
							dialog.dismiss();
						}
						
					});
				}
				
			});
		}
		
		mTextView.setTextColor(sharedPrefs.getInt("ct_sendButtonColor", getResources().getColor(R.color.black)));
		v.setBackgroundColor(sharedPrefs.getInt("ct_sendbarBackground", getResources().getColor(R.color.white)));
		sendButton.setImageResource(R.drawable.ic_action_send_white);
		sendButton.setBackgroundResource(R.drawable.pitch_black_send_button);
		sendButton.setColorFilter(sharedPrefs.getInt("ct_sendButtonColor", getResources().getColor(R.color.black)));
		emojiButton.setBackgroundResource(R.drawable.pitch_black_send_button);
		emojiButton.setColorFilter(sharedPrefs.getInt("ct_emojiButtonColor", getResources().getColor(R.color.emoji_button)));
		messageEntry.setTextColor(sharedPrefs.getInt("ct_draftTextColor", sharedPrefs.getInt("ct_sendButtonColor", getResources().getColor(R.color.black))));
		imageAttachBackground.setBackgroundColor(sharedPrefs.getInt("ct_messageListBackground", context.getResources().getColor(R.color.light_silver)));
		Drawable attachBack = getResources().getDrawable(R.drawable.attachment_editor_bg);
		attachBack.setColorFilter(sharedPrefs.getInt("ct_sentMessageBackground", context.getResources().getColor(R.color.white)), Mode.MULTIPLY);
		imageAttach.setBackgroundDrawable(attachBack);
		imageAttachBackground.setVisibility(View.GONE);
		imageAttach.setVisibility(false);
		
		if (sharedPrefs.getBoolean("custom_font", false))
		{
			mTextView.setTypeface(font);
			messageEntry.setTypeface(font);
		}

        if (sharedPrefs.getString("run_as", "sliding").equals("hangout") || sharedPrefs.getString("run_as", "sliding").equals("card2"))
        {
            emojiButton.setImageResource(R.drawable.ic_emoji_dark);
        }
	}
	
	@SuppressWarnings("deprecation")
	public void createMenu()
	{
        if (deviceType.equals("phablet") || deviceType.equals("tablet"))
        {
            ListFragment newFragment = (ListFragment) getSupportFragmentManager().findFragmentById(R.id.menuList);
            final ListView menuLayout = newFragment.getListView();
            if (sharedPrefs.getBoolean("limit_conversations_start", true) && inboxNumber.size() > 10) {
                final Button footer = new Button(this);
                footer.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        menuLayout.removeFooterView(footer);
                        limitConversations = false;
                        refreshViewPager(true);
                    }
                });
                footer.setText(getResources().getString(R.string.load_all));
                footer.setTypeface(font);
                footer.setBackgroundResource(android.R.color.transparent);
                footer.setTextColor(sharedPrefs.getInt("ct_nameTextColor", getResources().getColor(R.color.black)));
                menuLayout.addFooterView(footer);
            }
            newFragment.setListAdapter(new MenuArrayAdapter(this, inboxBody, inboxDate, inboxNumber, MainActivity.mViewPager, threadIds, group, msgCount, msgRead));

            if (sharedPrefs.getBoolean("custom_background", false))
            {
                try
                {
                    BitmapFactory.Options options = new BitmapFactory.Options();

                    options.inSampleSize = 2;
                    Bitmap myBitmap = BitmapFactory.decodeFile(Uri.parse(sharedPrefs.getString("custom_background_location", "")).getPath(),options);
                    this.getResources();
                    Drawable d = new BitmapDrawable(Resources.getSystem(),myBitmap);
                    newFragment.getView().setBackgroundDrawable(d);
                } catch (Exception e)
                {

                }
            } else
            {
                newFragment.getView().setBackgroundColor(sharedPrefs.getInt("ct_conversationListBackground", getResources().getColor(R.color.light_silver)));
            }

            newFragment.getListView().setDivider(new ColorDrawable(sharedPrefs.getInt("ct_conversationDividerColor", getResources().getColor(R.color.white))));

            if (sharedPrefs.getBoolean("ct_messageDividerVisibility", true))
            {
                newFragment.getListView().setDividerHeight(1);
            } else
            {
                newFragment.getListView().setDividerHeight(0);
            }
        } else
        {
            if (sharedPrefs.getBoolean("limit_conversations_start", true) && inboxNumber.size() > 10) {
                final Button footer = new Button(this);
                footer.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        menuLayout.removeFooterView(footer);
                        limitConversations = false;
                        refreshViewPager(true);
                    }
                });
                footer.setText(getResources().getString(R.string.load_all));
                footer.setTypeface(font);
                footer.setBackgroundResource(android.R.color.transparent);
                footer.setTextColor(sharedPrefs.getInt("ct_nameTextColor", getResources().getColor(R.color.black)));
                menuLayout.addFooterView(footer);
            }

            if (sharedPrefs.getBoolean("custom_background", false))
            {
                try
                {
                    BitmapFactory.Options options = new BitmapFactory.Options();

                    options.inSampleSize = 2;
                    Bitmap myBitmap = BitmapFactory.decodeFile(Uri.parse(sharedPrefs.getString("custom_background_location", "")).getPath(),options);
                    this.getResources();
                    Drawable d = new BitmapDrawable(Resources.getSystem(),myBitmap);
                    menuLayout.setBackgroundDrawable(d);
                } catch (Exception e)
                {

                }
            } else
            {
                menuLayout.setBackgroundColor(sharedPrefs.getInt("ct_conversationListBackground", getResources().getColor(R.color.light_silver)));
            }

            final Activity activity = this;

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    menuAdapter = new MenuArrayAdapter(activity, inboxBody, inboxDate, inboxNumber, mViewPager, threadIds, group, msgCount, msgRead);
                    menuLayout.setAdapter(menuAdapter);
                    menuLayout.setDivider(new ColorDrawable(sharedPrefs.getInt("ct_conversationDividerColor", getResources().getColor(R.color.white))));

                    if (sharedPrefs.getBoolean("ct_messageDividerVisibility", true))
                    {
                        menuLayout.setDividerHeight(1);
                    } else
                    {
                        menuLayout.setDividerHeight(0);
                    }
                }
            }, 100);
        }
		
		LayoutInflater inflater2 = (LayoutInflater) this
		        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		final View newMessageView = inflater2.inflate(R.layout.new_message_frame, (ViewGroup) this.getWindow().getDecorView(), false);
		
		final TextView mTextView = (TextView) newMessageView.findViewById(R.id.charsRemaining2);
		final EditText mEditText = (EditText) newMessageView.findViewById(R.id.messageEntry2);
		final ImageButton sendButton = (ImageButton) newMessageView.findViewById(R.id.sendButton);
		imageAttachBackground2 = newMessageView.findViewById(R.id.image_attachment_view_background);
		imageAttach2 = (ImageAttachmentView) newMessageView.findViewById(R.id.image_attachment_view);
        ImageButton contactLister = (ImageButton) newMessageView.findViewById(R.id.contactLister);
		
		mTextView.setVisibility(View.GONE);
		mEditText.requestFocus();
		
		mEditText.addTextChangedListener(new TextWatcher() {
	        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
	        }

	        public void onTextChanged(CharSequence s, int start, int before, int count) {
	        	int length = Integer.parseInt(String.valueOf(s.length()));
	        	
	        	if (!sharedPrefs.getString("signature", "").equals(""))
	        	{
	        		length += ("\n" + sharedPrefs.getString("signature", "")).length();
	        	}
	        	
	        	String patternStr = "[^" + MainActivity.GSM_CHARACTERS_REGEX + "]";
				Pattern pattern = Pattern.compile(patternStr);
				Matcher matcher = pattern.matcher(s);
				
				int size = 160;
				
				if (matcher.find() && !sharedPrefs.getBoolean("strip_unicode", false))
				{
					size = 70;
				}
	        	
	        	int pages = 1;
	        	
	        	while (length > size)
	        	{
	        		length-=size;
	        		pages++;
	        	}
	        	
	            mTextView.setText(pages + "/" + (size - length));
	            
	            if ((pages == 1 && (size - length) <= 30) || pages != 1)
	            {
	            	mTextView.setVisibility(View.VISIBLE);
	            }
	            
	            if ((pages + "/" + (size - length)).equals("1/31"))
	            {
	            	mTextView.setVisibility(View.GONE);
	            }
	            
	            if ((pages + "/" + (size - length)).equals("1/160"))
	            {
	            	mTextView.setVisibility(View.GONE);
	            }
	            
	            if (imageAttach2.getVisibility() == View.VISIBLE)
	            {
	            	mTextView.setVisibility(View.GONE);
	            }
	            
	            if (sharedPrefs.getBoolean("send_as_mms", false) && pages >= sharedPrefs.getInt("mms_after", 4))
	            {
	            	mTextView.setVisibility(View.GONE);
	            }
	            
	            if (sharedPrefs.getBoolean("send_with_return", false))
	            {
	            	if (mEditText.getText().toString().endsWith("\n"))
	            	{
	            		mEditText.setText(mEditText.getText().toString().substring(0, mEditText.getText().toString().length() - 1));
	            		sendButton.performClick();
	            	}
	            }

                mEditText.setError(null);
	        }

	        public void afterTextChanged(Editable s) {
	        }
		});
		
		if (!sharedPrefs.getBoolean("keyboard_type", true))
		{
			mEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
			mEditText.setImeOptions(EditorInfo.IME_ACTION_NONE);
		}
		
		final Context context = this;
		final EditText contact = (EditText) newMessageView.findViewById(R.id.contactEntry);

        final ListPopupWindow lpw = new ListPopupWindow(this);
        lpw.setBackgroundDrawable(new ColorDrawable(sharedPrefs.getInt("ct_conversationListBackground", getResources().getColor(R.color.light_silver))));

        lpw.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                final ArrayList<String> currentNames = new ArrayList<String>(), currentNumbers = new ArrayList<String>(), currentTypes = new ArrayList<String>();

                String[] numbers = contact.getText().toString().split("; ");

                for (int i = 0; i < numbers.length; i++)
                {
                    currentNumbers.add(numbers[i]);
                    currentTypes.add("");
                    currentNames.add(findContactName(numbers[i], context));
                }

                getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {

                    @Override
                    public void run() {
                        ListView current = (ListView) newMessageView.findViewById(R.id.contactSearch);

                        if (!currentNames.get(0).equals("No Information"))
                        {
                            current.setAdapter(new ContactSearchArrayAdapter((Activity)context, currentNames, currentNumbers, currentTypes));
                        } else
                        {
                            current.setAdapter(new ContactSearchArrayAdapter((Activity)context, new ArrayList<String>(), new ArrayList<String>(), new ArrayList<String>()));
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
                String[] projection    = new String[] {ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                        ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.LABEL};

                Cursor people = getContentResolver().query(uri, projection, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " asc");
                int indexName = people.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                int indexNumber = people.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);

                if (people.moveToFirst())
                {
                    do {
                        int type = people.getInt(people.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
                        String customLabel = people.getString(people.getColumnIndex(ContactsContract.CommonDataKinds.Phone.LABEL));

                        if (sharedPrefs.getBoolean("mobile_only", false))
                        {
                            if (type == 2)
                            {
                                contactNames.add(people.getString(indexName));
                                contactNumbers.add(people.getString(indexNumber).replaceAll("[^0-9\\+]", ""));
                                contactTypes.add(ContactsContract.CommonDataKinds.Phone.getTypeLabel(context.getResources(), type, customLabel).toString());
                            }
                        } else
                        {
                            contactNames.add(people.getString(indexName));
                            contactNumbers.add(people.getString(indexNumber).replaceAll("[^0-9\\+]", ""));
                            contactTypes.add(ContactsContract.CommonDataKinds.Phone.getTypeLabel(context.getResources(), type, customLabel).toString());
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
                        lpw.setAdapter(new ContactSearchArrayAdapter((Activity)context, contactNames, contactNumbers, contactTypes));
                        lpw.setAnchorView(contact);
                        lpw.setWidth(ListPopupWindow.WRAP_CONTENT);
                        lpw.setHeight(height/3);
                        lpw.show();
                    }
                }, 500);
            }
        });

		contact.addTextChangedListener(new TextWatcher() {
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
	        	if (firstContactSearch)
	        	{
	        		try
	        		{
	        			contactNames = new ArrayList<String>();
	        			contactNumbers = new ArrayList<String>();
	        			contactTypes = new ArrayList<String>();
	        			
	        			Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
	        			String[] projection    = new String[] {ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
	        			                ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.LABEL};
	        	
	        			Cursor people = getContentResolver().query(uri, projection, null, null, null);
	        	
	        			int indexName = people.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
	        			int indexNumber = people.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
	        	
	        			if (people.moveToFirst()) {
							do {
								int type = people.getInt(people.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
								String customLabel = people.getString(people.getColumnIndex(ContactsContract.CommonDataKinds.Phone.LABEL));
	
								try {
									if (sharedPrefs.getBoolean("mobile_only", false))
									{
										if (type == 2)
										{
											contactNames.add(people.getString(indexName));
											contactNumbers.add(people.getString(indexNumber).replaceAll("[^0-9\\+]", ""));
											contactTypes.add(ContactsContract.CommonDataKinds.Phone.getTypeLabel(context.getResources(), type, customLabel).toString());
										}
									} else
									{
										contactNames.add(people.getString(indexName));
										contactNumbers.add(people.getString(indexNumber).replaceAll("[^0-9\\+]", ""));
										contactTypes.add(ContactsContract.CommonDataKinds.Phone.getTypeLabel(context.getResources(), type, customLabel).toString());
									}
								} catch (Exception e) {
									contactNames.add(people.getString(indexName));
									contactNumbers.add(people.getString(indexName));
									contactTypes.add(ContactsContract.CommonDataKinds.Phone.getTypeLabel(context.getResources(), type, customLabel).toString());
								}
							} while (people.moveToNext());
						}
	        			people.close();
	        		} catch (IllegalArgumentException e)
	        		{
	        			
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
	        	
	        	text = text2[text2.length-1];
	        	
	        	if (text.startsWith("+"))
	        	{
	        		text = text.substring(1);
	        	}
	        	
	        	Pattern pattern;
	        	
	        	try
	        	{
	        		pattern = Pattern.compile(text.toLowerCase());
	        	} catch (Exception e)
	        	{
	        		pattern = Pattern.compile(text.toLowerCase().replace("(", "").replace(")", "").replace("?", "").replace("[", "").replace("{", "").replace("}", "").replace("\\", ""));
	        	}

                try {
                    for (int i = 0; i < contactNames.size(); i++)
                    {
                        try
                        {
                            Long.parseLong(text);

                            if (text.length() <= contactNumbers.get(i).length())
                            {
                                Matcher matcher = pattern.matcher(contactNumbers.get(i));
                                if(matcher.find())
                                {
                                    searchedNames.add(contactNames.get(i));
                                    searchedNumbers.add(contactNumbers.get(i));
                                    searchedTypes.add(contactTypes.get(i));
                                }
                            }
                        } catch (Exception e)
                        {
                            if (contactNames == null)
                            {
                                contactNames = new ArrayList<String>();
                                contactNumbers = new ArrayList<String>();
                                contactTypes = new ArrayList<String>();
                            }
                            if (text.length() <= contactNames.get(i).length())
                            {
                                Matcher matcher = pattern.matcher(contactNames.get(i).toLowerCase());
                                if(matcher.find())
                                {
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
                            lpw.setAdapter(new ContactSearchArrayAdapter((Activity)context, searchedNames, searchedNumbers, searchedTypes));
                            lpw.setAnchorView(findViewById(R.id.contactEntry));
                            lpw.setWidth(ListPopupWindow.WRAP_CONTENT);
							lpw.setHeight(height/3);


                            if (firstContactSearch)
                            {
                                lpw.show();
                                firstContactSearch = false;
                            }

                            if (textF.length() == 0)
                            {
                                lpw.dismiss();
                                firstContactSearch = true;
                            }
                        }
                    }, 500);
                } else {
                    lpw.setAdapter(new ContactSearchArrayAdapter((Activity)context, searchedNames, searchedNumbers, searchedTypes));
                    lpw.setAnchorView(findViewById(R.id.contactEntry));
                    lpw.setWidth(ListPopupWindow.WRAP_CONTENT);
					lpw.setHeight(height/3);


                    if (firstContactSearch)
                    {
                        lpw.show();
                        firstContactSearch = false;
                    }

                    if (text.length() == 0)
                    {
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
				Intent updateWidget = new Intent("com.klinker.android.messaging.UPDATE_WIDGET");
				context.sendBroadcast(updateWidget);
				
				if (contact.getText().toString().equals(""))
				{
					contact.setError("No Recipients");
				} else if (mEditText.getText().toString().equals("") && imageAttach2.getVisibility() == View.GONE)
				{
					mEditText.setError("Nothing to Send");
				} else
				{
					MainActivity.animationOn = true;
					
					if (sharedPrefs.getBoolean("hide_keyboard", false))
					{
						new Thread(new Runnable() {

							@Override
							public void run() {
								try {
									Thread.sleep(2000);
								} catch (InterruptedException e) {
									e.printStackTrace();
								} finally
								{
									InputMethodManager keyboard = (InputMethodManager)
							                getSystemService(Context.INPUT_METHOD_SERVICE);
							        keyboard.hideSoftInputFromWindow(messageEntry.getWindowToken(), 0);
								}
								
							}
							
						}).start();
					}
					
					String[] contacts = contact.getText().toString().split("; ");
					final int contactLength = contacts.length;
					
					boolean sendMmsFromLength = false;
					String[] counter = mTextView.getText().toString().split("/");
					
					if (Integer.parseInt(counter[0]) >= sharedPrefs.getInt("mms_after", 4) && sharedPrefs.getBoolean("send_as_mms", false))
					{
						sendMmsFromLength = true;
					}
					
					if ((imageAttach2.getVisibility() == View.GONE) && (sendMmsFromLength == false) && (contactLength == 1 || (contactLength > 1 && sharedPrefs.getBoolean("group_message", false) == false)))
					{
						for (int i = 0; i < contacts.length; i++)
						{
							String body2 = mEditText.getText().toString();
							
							if (!sharedPrefs.getString("signature", "").equals(""))
							{
								body2 += "\n" + sharedPrefs.getString("signature", "");
							}
							
							final String body = body2;
							final int index = i;
							final String address = contacts[i];
							
							new Thread(new Runnable() {
		
								@Override
								public void run() {
									try
									{
										if(sharedPrefs.getBoolean("delivery_reports", false))
										{
											String SENT = "SMS_SENT";
									        String DELIVERED = "SMS_DELIVERED";
									 
									        PendingIntent sentPI = PendingIntent.getBroadcast(context, 0,
									            new Intent(SENT), 0);
									 
									        PendingIntent deliveredPI = PendingIntent.getBroadcast(context, 0,
									            new Intent(DELIVERED), 0);
									 
									        //---when the SMS has been sent---
									        registerReceiver(new BroadcastReceiver(){
									            @Override
									            public void onReceive(Context arg0, Intent arg1) {
                                                    try {
                                                        switch (getResultCode())
                                                        {
                                                            case Activity.RESULT_OK:
                                                                Cursor query = context.getContentResolver().query(Uri.parse("content://sms/outbox"), null, null, null, null);

                                                                if (query.moveToFirst())
                                                                {
                                                                    String id = query.getString(query.getColumnIndex("_id"));
                                                                    ContentValues values = new ContentValues();
                                                                    values.put("type", "2");
                                                                    context.getContentResolver().update(Uri.parse("content://sms/outbox"), values, "_id=" + id, null);
                                                                    ((MainActivity) context).refreshViewPager3();
                                                                }

                                                                query.close();

                                                                break;
                                                            case SmsManager.RESULT_ERROR_GENERIC_FAILURE:

                                                                try
                                                                {
                                                                    wait(500);
                                                                } catch (Exception e)
                                                                {

                                                                }

                                                                query = context.getContentResolver().query(Uri.parse("content://sms/outbox"), null, null, null, null);

                                                                if (query.moveToFirst())
                                                                {
                                                                    String id = query.getString(query.getColumnIndex("_id"));
                                                                    ContentValues values = new ContentValues();
                                                                    values.put("type", "5");
                                                                    context.getContentResolver().update(Uri.parse("content://sms/outbox"), values, "_id=" + id, null);
                                                                    ((MainActivity) context).refreshViewPager3();
                                                                }

                                                                NotificationCompat.Builder mBuilder =
                                                                        new NotificationCompat.Builder(context)
                                                                                .setSmallIcon(R.drawable.ic_alert)
                                                                                .setContentTitle("Error")
                                                                                .setContentText("Could not send message");

                                                                Intent resultIntent = new Intent(context, MainActivity.class);

                                                                TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                                                                stackBuilder.addParentStack(MainActivity.class);
                                                                stackBuilder.addNextIntent(resultIntent);
                                                                PendingIntent resultPendingIntent =
                                                                        stackBuilder.getPendingIntent(
                                                                                0,
                                                                                PendingIntent.FLAG_UPDATE_CURRENT
                                                                        );

                                                                mBuilder.setContentIntent(resultPendingIntent);
                                                                mBuilder.setAutoCancel(true);
                                                                long[] pattern = {0L, 400L, 100L, 400L};
                                                                mBuilder.setVibrate(pattern);
                                                                mBuilder.setLights(0xFFffffff, 1000, 2000);

                                                                try
                                                                {
                                                                    mBuilder.setSound(Uri.parse(sharedPrefs.getString("ringtone", "null")));
                                                                } catch(Exception e)
                                                                {
                                                                    mBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
                                                                }

                                                                NotificationManager mNotificationManager =
                                                                        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                                                                Notification notification = mBuilder.build();
                                                                Intent deleteIntent = new Intent(context, NotificationReceiver.class);
                                                                notification.deleteIntent = PendingIntent.getBroadcast(context, 0, deleteIntent, 0);
                                                                mNotificationManager.notify(1, notification);
                                                                break;
                                                            case SmsManager.RESULT_ERROR_NO_SERVICE:
                                                                try
                                                                {
                                                                    wait(500);
                                                                } catch (Exception e)
                                                                {

                                                                }

                                                                query = context.getContentResolver().query(Uri.parse("content://sms/outbox"), null, null, null, null);

                                                                if (query.moveToFirst())
                                                                {
                                                                    String id = query.getString(query.getColumnIndex("_id"));
                                                                    ContentValues values = new ContentValues();
                                                                    values.put("type", "5");
                                                                    context.getContentResolver().update(Uri.parse("content://sms/outbox"), values, "_id=" + id, null);
                                                                    ((MainActivity) context).refreshViewPager3();
                                                                }

                                                                Toast.makeText(context, "No service",
                                                                        Toast.LENGTH_SHORT).show();
                                                                break;
                                                            case SmsManager.RESULT_ERROR_NULL_PDU:
                                                                try
                                                                {
                                                                    wait(500);
                                                                } catch (Exception e)
                                                                {

                                                                }

                                                                query = context.getContentResolver().query(Uri.parse("content://sms/outbox"), null, null, null, null);

                                                                if (query.moveToFirst())
                                                                {
                                                                    String id = query.getString(query.getColumnIndex("_id"));
                                                                    ContentValues values = new ContentValues();
                                                                    values.put("type", "5");
                                                                    context.getContentResolver().update(Uri.parse("content://sms/outbox"), values, "_id=" + id, null);
                                                                    ((MainActivity) context).refreshViewPager3();
                                                                }

                                                                Toast.makeText(context, "Null PDU",
                                                                        Toast.LENGTH_SHORT).show();
                                                                break;
                                                            case SmsManager.RESULT_ERROR_RADIO_OFF:
                                                                try
                                                                {
                                                                    wait(500);
                                                                } catch (Exception e)
                                                                {

                                                                }

                                                                query = context.getContentResolver().query(Uri.parse("content://sms/outbox"), null, null, null, null);

                                                                if (query.moveToFirst())
                                                                {
                                                                    String id = query.getString(query.getColumnIndex("_id"));
                                                                    ContentValues values = new ContentValues();
                                                                    values.put("type", "5");
                                                                    context.getContentResolver().update(Uri.parse("content://sms/outbox"), values, "_id=" + id, null);
                                                                    ((MainActivity) context).refreshViewPager3();
                                                                }

                                                                Toast.makeText(context, "Radio off",
                                                                        Toast.LENGTH_SHORT).show();
                                                                break;
                                                        }

                                                        context.unregisterReceiver(this);
                                                    } catch (Exception e) {

                                                    }
									            }
									        }, new IntentFilter(SENT));
									 
									        //---when the SMS has been delivered---
									        registerReceiver(new BroadcastReceiver(){
									            @Override
									            public void onReceive(Context arg0, Intent arg1) {
                                                    try {
                                                        if (sharedPrefs.getString("delivery_options", "2").equals("1"))
                                                        {
                                                            switch (getResultCode())
                                                            {
                                                                case Activity.RESULT_OK:
                                                                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                                                    builder.setMessage(R.string.message_delivered)
                                                                            .setPositiveButton(R.string.dismiss, new DialogInterface.OnClickListener() {
                                                                                public void onClick(DialogInterface dialog, int id) {
                                                                                    dialog.dismiss();
                                                                                }
                                                                            });

                                                                    builder.create().show();

                                                                    Cursor query = context.getContentResolver().query(Uri.parse("content://sms/sent"), null, null, null, "date desc");

                                                                    if (query.moveToFirst())
                                                                    {
                                                                        String id = query.getString(query.getColumnIndex("_id"));
                                                                        ContentValues values = new ContentValues();
                                                                        values.put("status", "0");
                                                                        context.getContentResolver().update(Uri.parse("content://sms/sent"), values, "_id=" + id, null);
                                                                        ((MainActivity) context).refreshViewPager3();
                                                                    }

                                                                    query.close();

                                                                    break;
                                                                case Activity.RESULT_CANCELED:
                                                                    AlertDialog.Builder builder2 = new AlertDialog.Builder(context);
                                                                    builder2.setMessage(R.string.message_not_delivered)
                                                                            .setPositiveButton(R.string.dismiss, new DialogInterface.OnClickListener() {
                                                                                public void onClick(DialogInterface dialog, int id) {
                                                                                    dialog.dismiss();
                                                                                }
                                                                            });

                                                                    builder2.create().show();

                                                                    Cursor query2 = context.getContentResolver().query(Uri.parse("content://sms/sent"), null, null, null, "date desc");

                                                                    if (query2.moveToFirst())
                                                                    {
                                                                        String id = query2.getString(query2.getColumnIndex("_id"));
                                                                        ContentValues values = new ContentValues();
                                                                        values.put("status", "64");
                                                                        context.getContentResolver().update(Uri.parse("content://sms/sent"), values, "_id=" + id, null);
                                                                        ((MainActivity) context).refreshViewPager3();
                                                                    }

                                                                    query2.close();

                                                                    break;
                                                            }
                                                        } else
                                                        {
                                                            switch (getResultCode())
                                                            {
                                                                case Activity.RESULT_OK:
                                                                    if (sharedPrefs.getString("delivery_options", "2").equals("2"))
                                                                    {
                                                                        Toast.makeText(context, R.string.message_delivered, Toast.LENGTH_LONG).show();
                                                                    }

                                                                    Cursor query = context.getContentResolver().query(Uri.parse("content://sms/sent"), null, null, null, "date desc");

                                                                    if (query.moveToFirst())
                                                                    {
                                                                        String id = query.getString(query.getColumnIndex("_id"));
                                                                        ContentValues values = new ContentValues();
                                                                        values.put("status", "0");
                                                                        context.getContentResolver().update(Uri.parse("content://sms/sent"), values, "_id=" + id, null);
                                                                        ((MainActivity) context).refreshViewPager3();
                                                                    }

                                                                    query.close();

                                                                    break;
                                                                case Activity.RESULT_CANCELED:
                                                                    if (sharedPrefs.getString("delivery_options", "2").equals("2"))
                                                                    {
                                                                        Toast.makeText(context, R.string.message_not_delivered, Toast.LENGTH_LONG).show();
                                                                    }

                                                                    Cursor query2 = context.getContentResolver().query(Uri.parse("content://sms/sent"), null, null, null, "date desc");

                                                                    if (query2.moveToFirst())
                                                                    {
                                                                        String id = query2.getString(query2.getColumnIndex("_id"));
                                                                        ContentValues values = new ContentValues();
                                                                        values.put("status", "64");
                                                                        context.getContentResolver().update(Uri.parse("content://sms/sent"), values, "_id=" + id, null);
                                                                        ((MainActivity) context).refreshViewPager3();
                                                                    }

                                                                    query2.close();

                                                                    break;
                                                            }
                                                        }

                                                        context.unregisterReceiver(this);
                                                    } catch (Exception e) {

                                                    }
									            }
									        }, new IntentFilter(DELIVERED));
									        
									        ArrayList<PendingIntent> sPI = new ArrayList<PendingIntent>();
									        ArrayList<PendingIntent> dPI = new ArrayList<PendingIntent>();
									        
									        String body2 = body;
									        
									        if (sharedPrefs.getBoolean("strip_unicode", false))
									        {
									        	body2 = StripAccents.stripAccents(body2);
									        }
									        
											SmsManager smsManager = SmsManager.getDefault();

											if (sharedPrefs.getBoolean("split_sms", false))
											{
												int length = 160;
												
												String patternStr = "[^\\x20-\\x7E]";
												Pattern pattern = Pattern.compile(patternStr);
												Matcher matcher = pattern.matcher(body2);
												  
												if (matcher.find())
												{
													length = 70;
												}

                                                boolean counter = false;

                                                if (sharedPrefs.getBoolean("split_counter", false)) {
                                                    counter = true;
                                                    length -= 7;
                                                }

                                                String[] textToSend = splitByLength(body2, length, counter);
												
												for (int i = 0; i < textToSend.length; i++)
												{
													ArrayList<String> parts = smsManager.divideMessage(textToSend[i]); 
													
													for (int j = 0; j < parts.size(); j++)
													{
														sPI.add(sentPI);
														dPI.add(deliveredPI);
													}
													
													smsManager.sendMultipartTextMessage(address, null, parts, sPI, dPI);
												}
											} else
											{
												ArrayList<String> parts = smsManager.divideMessage(body2); 
												
												for (int i = 0; i < parts.size(); i++)
												{
													sPI.add(sentPI);
													dPI.add(deliveredPI);
												}

												try {
													smsManager.sendMultipartTextMessage(address, null, parts, sPI, dPI);
												} catch (Exception e) {
                                                    getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {

                                                        @Override
                                                        public void run() {
                                                            Toast.makeText(context, "Error, check the \"Split SMS\" option in advanced settings and retry.", Toast.LENGTH_LONG).show();
                                                        }

                                                    });
												}
											}
										} else
										{
											String SENT = "SMS_SENT";
											 
									        PendingIntent sentPI = PendingIntent.getBroadcast(context, 0,
									            new Intent(SENT), 0);
									 
									        //---when the SMS has been sent---
									        context.registerReceiver(new BroadcastReceiver(){
									            @Override
									            public void onReceive(Context arg0, Intent arg1) {
                                                    try {
                                                        switch (getResultCode())
                                                        {
                                                            case Activity.RESULT_OK:
                                                                Cursor query = context.getContentResolver().query(Uri.parse("content://sms/outbox"), null, null, null, null);

                                                                if (query.moveToFirst())
                                                                {
                                                                    String id = query.getString(query.getColumnIndex("_id"));
                                                                    ContentValues values = new ContentValues();
                                                                    values.put("type", "2");
                                                                    context.getContentResolver().update(Uri.parse("content://sms/outbox"), values, "_id=" + id, null);
                                                                    ((MainActivity) context).refreshViewPager3();
                                                                }

                                                                query.close();

                                                                break;
                                                            case SmsManager.RESULT_ERROR_GENERIC_FAILURE:

                                                                try
                                                                {
                                                                    wait(500);
                                                                } catch (Exception e)
                                                                {

                                                                }

                                                                query = context.getContentResolver().query(Uri.parse("content://sms/outbox"), null, null, null, null);

                                                                if (query.moveToFirst())
                                                                {
                                                                    String id = query.getString(query.getColumnIndex("_id"));
                                                                    ContentValues values = new ContentValues();
                                                                    values.put("type", "5");
                                                                    context.getContentResolver().update(Uri.parse("content://sms/outbox"), values, "_id=" + id, null);
                                                                    ((MainActivity) context).refreshViewPager3();
                                                                }

                                                                NotificationCompat.Builder mBuilder =
                                                                        new NotificationCompat.Builder(context)
                                                                                .setSmallIcon(R.drawable.ic_alert)
                                                                                .setContentTitle("Error")
                                                                                .setContentText("Could not send message");

                                                                Intent resultIntent = new Intent(context, MainActivity.class);

                                                                TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                                                                stackBuilder.addParentStack(MainActivity.class);
                                                                stackBuilder.addNextIntent(resultIntent);
                                                                PendingIntent resultPendingIntent =
                                                                        stackBuilder.getPendingIntent(
                                                                                0,
                                                                                PendingIntent.FLAG_UPDATE_CURRENT
                                                                        );

                                                                mBuilder.setContentIntent(resultPendingIntent);
                                                                mBuilder.setAutoCancel(true);
                                                                long[] pattern = {0L, 400L, 100L, 400L};
                                                                mBuilder.setVibrate(pattern);
                                                                mBuilder.setLights(0xFFffffff, 1000, 2000);

                                                                try
                                                                {
                                                                    mBuilder.setSound(Uri.parse(sharedPrefs.getString("ringtone", "null")));
                                                                } catch(Exception e)
                                                                {
                                                                    mBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
                                                                }

                                                                NotificationManager mNotificationManager =
                                                                        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                                                                Notification notification = mBuilder.build();
                                                                Intent deleteIntent = new Intent(context, NotificationReceiver.class);
                                                                notification.deleteIntent = PendingIntent.getBroadcast(context, 0, deleteIntent, 0);
                                                                mNotificationManager.notify(1, notification);
                                                                break;
                                                            case SmsManager.RESULT_ERROR_NO_SERVICE:
                                                                try
                                                                {
                                                                    wait(500);
                                                                } catch (Exception e)
                                                                {

                                                                }

                                                                query = context.getContentResolver().query(Uri.parse("content://sms/outbox"), null, null, null, null);

                                                                if (query.moveToFirst())
                                                                {
                                                                    String id = query.getString(query.getColumnIndex("_id"));
                                                                    ContentValues values = new ContentValues();
                                                                    values.put("type", "5");
                                                                    context.getContentResolver().update(Uri.parse("content://sms/outbox"), values, "_id=" + id, null);
                                                                    ((MainActivity) context).refreshViewPager3();
                                                                }

                                                                Toast.makeText(context, "No service",
                                                                        Toast.LENGTH_SHORT).show();
                                                                break;
                                                            case SmsManager.RESULT_ERROR_NULL_PDU:
                                                                try
                                                                {
                                                                    wait(500);
                                                                } catch (Exception e)
                                                                {

                                                                }

                                                                query = context.getContentResolver().query(Uri.parse("content://sms/outbox"), null, null, null, null);

                                                                if (query.moveToFirst())
                                                                {
                                                                    String id = query.getString(query.getColumnIndex("_id"));
                                                                    ContentValues values = new ContentValues();
                                                                    values.put("type", "5");
                                                                    context.getContentResolver().update(Uri.parse("content://sms/outbox"), values, "_id=" + id, null);
                                                                    ((MainActivity) context).refreshViewPager3();
                                                                }

                                                                Toast.makeText(context, "Null PDU",
                                                                        Toast.LENGTH_SHORT).show();
                                                                break;
                                                            case SmsManager.RESULT_ERROR_RADIO_OFF:
                                                                try
                                                                {
                                                                    wait(500);
                                                                } catch (Exception e)
                                                                {

                                                                }

                                                                query = context.getContentResolver().query(Uri.parse("content://sms/outbox"), null, null, null, null);

                                                                if (query.moveToFirst())
                                                                {
                                                                    String id = query.getString(query.getColumnIndex("_id"));
                                                                    ContentValues values = new ContentValues();
                                                                    values.put("type", "5");
                                                                    context.getContentResolver().update(Uri.parse("content://sms/outbox"), values, "_id=" + id, null);
                                                                    ((MainActivity) context).refreshViewPager3();
                                                                }

                                                                Toast.makeText(context, "Radio off",
                                                                        Toast.LENGTH_SHORT).show();
                                                                break;
                                                        }

                                                        context.unregisterReceiver(this);
                                                    } catch (Exception e) {

                                                    }
									            }
									        }, new IntentFilter(SENT));
									        
									        ArrayList<PendingIntent> sPI = new ArrayList<PendingIntent>();
									        
									        String body2 = body;
									        
									        if (sharedPrefs.getBoolean("strip_unicode", false))
									        {
									        	body2 = StripAccents.stripAccents(body2);
									        }
									        
											SmsManager smsManager = SmsManager.getDefault();

											if (sharedPrefs.getBoolean("split_sms", false))
											{
												int length = 160;
												
												String patternStr = "[^\\x20-\\x7E]";
												Pattern pattern = Pattern.compile(patternStr);
												Matcher matcher = pattern.matcher(body2);
												  
												if (matcher.find())
												{
													length = 70;
												}

                                                boolean counter = false;

                                                if (sharedPrefs.getBoolean("split_counter", false)) {
                                                    counter = true;
                                                    length -= 7;
                                                }

                                                String[] textToSend = splitByLength(body2, length, counter);
												
												for (int i = 0; i < textToSend.length; i++)
												{
													ArrayList<String> parts = smsManager.divideMessage(textToSend[i]); 
													
													for (int j = 0; j < parts.size(); j++)
													{
														sPI.add(sentPI);
													}
													
													smsManager.sendMultipartTextMessage(address, null, parts, sPI, null);
												}
											} else
											{
												ArrayList<String> parts = smsManager.divideMessage(body2); 
												
												for (int i = 0; i < parts.size(); i++)
												{
													sPI.add(sentPI);
												}

												try {
													smsManager.sendMultipartTextMessage(address, null, parts, sPI, null);
												} catch (Exception e) {
                                                    getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {

                                                        @Override
                                                        public void run() {
                                                            Toast.makeText(context, "Error, check the \"Split SMS\" option in advanced settings and retry.", Toast.LENGTH_LONG).show();
                                                        }

                                                    });
												}
											}
										}
									} catch (NullPointerException e)
									{
										Toast.makeText(context, "Error sending message", Toast.LENGTH_SHORT).show();
									}
									
									String address2 = address;
									String body2 = body;
									
									if (sharedPrefs.getBoolean("strip_unicode", false))
									{
										body2 = StripAccents.stripAccents(body2);
									}
								    
								    Calendar cal = Calendar.getInstance();
								    ContentValues values = new ContentValues();
								    values.put("address", address2); 
								    values.put("body", body2); 
								    values.put("date", cal.getTimeInMillis() + "");
								    values.put("read", true);
								    getContentResolver().insert(Uri.parse("content://sms/outbox"), values);
									
								    if (index == contactLength - 1)
								    {
									    getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {
			
											@Override
											public void run() {

                                                MainActivity.threadedLoad = false;
                                                MainActivity.notChanged = false;
                                                sentMessage = true;
                                                refreshViewPager(true);
                                                mTextView.setVisibility(View.GONE);
											}
									    	
									    });
								    }
								}
								
							}).start();
						}
					} else
					{
						Bitmap b;
						byte[] byteArray;
						
						try
						{
							if (!fromCamera)
							{
								b = decodeFile2(new File(getPath(attachedImage2)));
								ByteArrayOutputStream stream = new ByteArrayOutputStream();
								b.compress(Bitmap.CompressFormat.PNG, 100, stream);
								byteArray = stream.toByteArray();
							} else
							{
								b = decodeFile2(new File(Environment.getExternalStorageDirectory() + "/SlidingMessaging/", "photoToSend.png"));
								ByteArrayOutputStream stream = new ByteArrayOutputStream();
								b.compress(Bitmap.CompressFormat.PNG, 100, stream);
								byteArray = stream.toByteArray();
							}
						} catch (Exception e)
						{
							byteArray = null;
						}
						
						String body = mEditText.getText().toString();
						
						String[] to = ("insert-address-token; " + contact.getText().toString()).split("; ");

                        if (!sharedPrefs.getBoolean("send_with_stock", false))
                        {
                            if (multipleAttachments == false)
                            {
                                insert(context, to, "", byteArray, body);

                                MMSPart[] parts = new MMSPart[2];

                                if (imageAttach2.getVisibility() == View.VISIBLE)
                                {
                                    parts[0] = new MMSPart();
                                    parts[0].Name = "Image";
                                    parts[0].MimeType = "image/png";
                                    parts[0].Data = byteArray;

                                    if (!body.equals(""))
                                    {
                                        parts[1] = new MMSPart();
                                        parts[1].Name = "Text";
                                        parts[1].MimeType = "text/plain";
                                        parts[1].Data = body.getBytes();
                                    }
                                } else
                                {
                                    parts[0] = new MMSPart();
                                    parts[0].Name = "Text";
                                    parts[0].MimeType = "text/plain";
                                    parts[0].Data = body.getBytes();
                                }

                                sendMMS(contact.getText().toString(), parts);
                            } else
                            {
                                ArrayList<byte[]> bytes = new ArrayList<byte[]>();
                                ArrayList<String> mimes = new ArrayList<String>();

                                for (int i = 0; i < AttachMore.data.size(); i++)
                                {
                                    bytes.add(AttachMore.data.get(i).Data);
                                    mimes.add(AttachMore.data.get(i).MimeType);
                                }

                                insert(context, to, "", bytes, mimes, body);

                                MMSPart part = new MMSPart();
                                part.Name = "Text";
                                part.MimeType = "text/plain";
                                part.Data = body.getBytes();
                                AttachMore.data.add(part);

                                sendMMS(contact.getText().toString(), AttachMore.data.toArray(new MMSPart[AttachMore.data.size()]));

                                AttachMore.data = new ArrayList<MMSPart>();
                            }
                        } else
                        {
                            if (multipleAttachments == false)
                            {
                                Intent sendIntent = new Intent(Intent.ACTION_SEND);
                                sendIntent.putExtra("address", contact.getText().toString().replace(",", ""));
                                sendIntent.putExtra("sms_body", body);
                                sendIntent.putExtra(Intent.EXTRA_STREAM, attachedImage2);
                                sendIntent.setType("image/png");
                                startActivity(sendIntent);

                                com.klinker.android.messaging_sliding.MainActivity.messageRecieved = true;
                            } else
                            {
                                Toast.makeText(context, "Cannot send multiple images through stock", Toast.LENGTH_SHORT).show();
                            }
                        }
						
						refreshViewPager(true);
					}
					
					contact.setText("");
					mEditText.setText("");
					imageAttach2.setVisibility(false);
					imageAttachBackground2.setVisibility(View.GONE);
					menu.showContent();
					mViewPager.setCurrentItem(0);


				}
			}
			
		});
		
		ImageButton emojiButton = (ImageButton) newMessageView.findViewById(R.id.display_emoji);
		
		if (!sharedPrefs.getBoolean("emoji", false))
		{
			emojiButton.setVisibility(View.GONE);
			LayoutParams params = (RelativeLayout.LayoutParams)mEditText.getLayoutParams();
			params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
			mEditText.setLayoutParams(params);
		} else
		{
			emojiButton.setOnClickListener(new OnClickListener() {

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
					
					if (sharedPrefs.getBoolean("emoji_type", true))
					{
						emojiGrid.setAdapter(new EmojiAdapter2(context));
						emojiGrid.setOnItemClickListener(new OnItemClickListener() {
						
								public void onItemClick(AdapterView<?> parent, View v, int position, long id)
								{
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
					} else
					{
						emojiGrid.setAdapter(new EmojiAdapter(context));
						emojiGrid.setOnItemClickListener(new OnItemClickListener() {
						
								public void onItemClick(AdapterView<?> parent, View v, int position, long id)
								{
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
							if (sharedPrefs.getBoolean("emoji_type", true))
							{
								mEditText.setText(EmojiConverter2.getSmiledText(context, mEditText.getText().toString() + editText.getText().toString()));
								mEditText.setSelection(mEditText.getText().length());
							} else
							{
								mEditText.setText(EmojiConverter.getSmiledText(context, mEditText.getText().toString() + editText.getText().toString()));
								mEditText.setSelection(mEditText.getText().length());
							}
							
							dialog.dismiss();
						}
						
					});
				}
				
			});
		}
		
		ListView searchView = (ListView) newMessageView.findViewById(R.id.contactSearch);

        try {
            mEditText.setTextSize(Integer.parseInt(sharedPrefs.getString("text_size", "14").substring(0, 2)));
        } catch (Exception e) {
            mEditText.setTextSize(Integer.parseInt(sharedPrefs.getString("text_size", "14").substring(0, 1)));
        }
		
		View v1 = newMessageView.findViewById(R.id.view1);
		View v2 = newMessageView.findViewById(R.id.sentBackground);
		
		mTextView.setTextColor(sharedPrefs.getInt("ct_sentButtonColor", getResources().getColor(R.color.black)));
		v1.setBackgroundColor(sharedPrefs.getInt("ct_sendbarBackground", getResources().getColor(R.color.white)));
		v2.setBackgroundColor(sharedPrefs.getInt("ct_sendbarBackground", getResources().getColor(R.color.white)));
		sendButton.setBackgroundResource(R.drawable.pitch_black_send_button);
		sendButton.setImageResource(R.drawable.ic_action_send_white);
		sendButton.setColorFilter(sharedPrefs.getInt("ct_sendButtonColor", getResources().getColor(R.color.black)));
		searchView.setBackgroundColor(sharedPrefs.getInt("ct_conversationListBackground", getResources().getColor(R.color.light_silver)));
		emojiButton.setBackgroundResource(R.drawable.pitch_black_send_button);
		emojiButton.setColorFilter(sharedPrefs.getInt("ct_emojiButtonColor", getResources().getColor(R.color.emoji_button)));
		mEditText.setTextColor(sharedPrefs.getInt("ct_draftTextColor", sharedPrefs.getInt("ct_sendButtonColor", getResources().getColor(R.color.black))));
		contact.setTextColor(sharedPrefs.getInt("ct_draftTextColor", sharedPrefs.getInt("ct_sendButtonColor", getResources().getColor(R.color.black))));
        contactLister.setColorFilter(sharedPrefs.getInt("ct_sendButtonColor", getResources().getColor(R.color.black)));
		
		imageAttachBackground2.setBackgroundColor(sharedPrefs.getInt("ct_messageListBackground", context.getResources().getColor(R.color.light_silver)));
		Drawable attachBack = getResources().getDrawable(R.drawable.attachment_editor_bg);
		attachBack.setColorFilter(sharedPrefs.getInt("ct_sentMessageBackground", context.getResources().getColor(R.color.white)), Mode.MULTIPLY);
		imageAttach2.setBackgroundDrawable(attachBack);
		imageAttachBackground2.setVisibility(View.GONE);
		imageAttach2.setVisibility(false);
		
		if (sharedPrefs.getBoolean("custom_font", false))
		{
			mTextView.setTypeface(font);
			mEditText.setTypeface(font);
			contact.setTypeface(font);
		}

        if (sharedPrefs.getString("run_as", "sliding").equals("hangout") || sharedPrefs.getString("run_as", "sliding").equals("card2"))
        {
            emojiButton.setImageResource(R.drawable.ic_emoji_dark);
        }
		
		if (sharedPrefs.getBoolean("custom_background", false))
		{
			try
			{
				BitmapFactory.Options options = new BitmapFactory.Options();

				options.inSampleSize = 2;
				Bitmap myBitmap = BitmapFactory.decodeFile(Uri.parse(sharedPrefs.getString("custom_background_location", "")).getPath(),options);
				this.getResources();
				Drawable d = new BitmapDrawable(Resources.getSystem(),myBitmap);
				searchView.setBackgroundDrawable(d);
			} catch (Exception e)
			{
				
			}
		}
		
		menu = new SlidingMenu(this);

        if (deviceType.equals("phone") || deviceType.equals("phablet2"))
        {
            menu.setMode(SlidingMenu.LEFT_RIGHT);
            menu.setShadowDrawable(R.drawable.shadow);
            menu.setSecondaryShadowDrawable(R.drawable.shadowright);
        } else if (deviceType.equals("phablet") || deviceType.equals("tablet"))
        {
            menu.setMode(SlidingMenu.RIGHT);
            menu.setShadowDrawable(R.drawable.shadowright);
        }

        menu.setShadowWidthRes(R.dimen.shadow_width);
        
        if (!sharedPrefs.getBoolean("slide_messages", false))
        {
            if (deviceType.equals("phone") || deviceType.equals("phablet2"))
            {
        	    menu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
            } else if (deviceType.equals("phablet") || deviceType.equals("tablet"))
            {
                menu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
            }
        } else
        {
        	menu.setBehindOffset(0);
        }
        
        menu.setFadeDegree(0.35f);
        menu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);

        if (deviceType.equals("phone") || deviceType.equals("phablet2"))
        {
            menu.setMenu(menuLayout);
            menu.setSecondaryMenu(newMessageView);
        } else if (deviceType.equals("phablet") || deviceType.equals("tablet"))
        {
            menu.setMenu(newMessageView);
        }
        
        menu.setOnOpenedListener(new SlidingMenu.OnOpenedListener() {

            @Override
            public void onOpened() {
                invalidateOptionsMenu();

                if (menu.isSecondaryMenuShowing()) {
                    contact.requestFocus();
                }

                try {
                    ActionBar ab = getActionBar();
                    ab.setTitle(R.string.app_name_in_app);
                    ab.setSubtitle(null);
                    ab.setIcon(R.drawable.ic_launcher);

                    ab.setDisplayHomeAsUpEnabled(false);
                } catch (Exception e) {
                    // no action bar, dialog theme
                }

                if (menu.isMenuShowing() && !menu.isSecondaryMenuShowing()) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(
                            Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
                }

                if (menu.isMenuShowing() && menu.isSecondaryMenuShowing()) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(
                            Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(contact, 0);
                }
            }

        });
        
        menu.setOnClosedListener(new SlidingMenu.OnClosedListener() {

            @Override
            public void onClosed() {

                invalidateOptionsMenu();

                try {
                    if (deviceType.equals("phone") || deviceType.equals("phablet2")) {
                        getActionBar().setDisplayHomeAsUpEnabled(true);
                    }

                    if (!sharedPrefs.getBoolean("hide_title_bar", true) || sharedPrefs.getBoolean("always_show_contact_info", false)) {
                        final ActionBar ab = getActionBar();

                        if (ab != null) {
                            try {
                                new Thread(new Runnable() {

                                    @Override
                                    public void run() {
                                        if (inboxNumber.size() != 0) {
                                            final String title = findContactName(findContactNumber(inboxNumber.get(mViewPager.getCurrentItem()), context), context);

                                            ((MainActivity) context).getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {

                                                @Override
                                                public void run() {
                                                    ab.setTitle(title);

                                                    Locale sCachedLocale = Locale.getDefault();
                                                    int sFormatType = PhoneNumberUtils.getFormatTypeForLocale(sCachedLocale);
                                                    Editable editable = new SpannableStringBuilder(findContactNumber(inboxNumber.get(mViewPager.getCurrentItem()), context));
                                                    PhoneNumberUtils.formatNumber(editable, sFormatType);
                                                    ab.setSubtitle(editable.toString());

                                                    if (ab.getTitle().equals(ab.getSubtitle())) {
                                                        ab.setSubtitle(null);
                                                    }

                                                    if (group.get(mViewPager.getCurrentItem()).equals("yes")) {
                                                        ab.setTitle("Group MMS");
                                                        ab.setSubtitle(null);
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
                    }
                } catch (Exception e) {
                    // no action bar, dialog theme
                }

                if (sharedPrefs.getBoolean("title_contact_image", false)) {
                    final ActionBar ab = getActionBar();

                    if (ab != null) {
                        new Thread(new Runnable() {

                            @Override
                            public void run() {
                                if (inboxNumber.size() != 0) {
                                    Bitmap image = getFacebookPhoto(findContactNumber(inboxNumber.get(mViewPager.getCurrentItem()), context), context);
                                    final BitmapDrawable image2 = new BitmapDrawable(image);

                                    ((MainActivity) context).getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {

                                        @Override
                                        public void run() {
                                            ab.setIcon(image2);
                                        }

                                    });
                                }

                            }

                        }).start();
                    }

                    if (threadIds.size() == 0 && ab != null) {
                        ab.setIcon(R.drawable.ic_launcher);
                    }
                }

                EditText textEntry = (EditText) findViewById(R.id.messageEntry);
                textEntry.requestFocus();
            }

        });

        messageBar = new MessageBar (this);

        drafts = new ArrayList<String>();
        draftNames = new ArrayList<String>();
        draftChanged = new ArrayList<Boolean>();
        draftsToDelete = new ArrayList<String>();

        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener()
		{
			@Override
			public void onPageSelected(int arg0) {
				if (!menu.isMenuShowing())
		    	{
		    		menu.showContent();
		    	}

                if (mViewPager.getCurrentItem() < appMsgConversations && appMsg.isShowing() && dismissCrouton) {
                    appMsg.cancel();
                    appMsgConversations = 0;
                }

                new Thread(new Runnable() {

					@Override
					public void run() {
						ArrayList<String> newMessages = readFromFile(context);

				        for (int j = 0; j < newMessages.size(); j++)
				        {
				        	if (newMessages.get(j).replaceAll("-", "").endsWith(findContactName(findContactNumber(inboxNumber.get(mViewPager.getCurrentItem()), context), context).replace("-", "")))
				        	{
				        		newMessages.remove(j);
				        	}
				        }

				        writeToFile(newMessages, context);

                        final ActionBar ab = getActionBar();
                        String title = "";
                        String subtitle = "";

                        if (!sharedPrefs.getBoolean("hide_title_bar", true) || sharedPrefs.getBoolean("always_show_contact_info", false))
                        {
                            if (group.get(mViewPager.getCurrentItem()).equals("yes"))
                            {
                                title = "Group MMS";
                                subtitle = null;
                            } else
                            {
                                title = findContactName(findContactNumber(inboxNumber.get(mViewPager.getCurrentItem()), context), context);

                                Locale sCachedLocale = Locale.getDefault();
                                int sFormatType = PhoneNumberUtils.getFormatTypeForLocale(sCachedLocale);
                                Editable editable = new SpannableStringBuilder(findContactNumber(inboxNumber.get(mViewPager.getCurrentItem()), context));
                                PhoneNumberUtils.formatNumber(editable, sFormatType);
                                subtitle = editable.toString();

                                if (title.equals(subtitle))
                                {
                                    subtitle = null;
                                }
                            }
                        }

                        final String titleF = title, subtitleF = subtitle;

                        BitmapDrawable image2 = null;

                        if (sharedPrefs.getBoolean("title_contact_image", false))
                        {
                            Bitmap image = getFacebookPhoto(findContactNumber(inboxNumber.get(mViewPager.getCurrentItem()), context), context);
                            image2 = new BitmapDrawable(image);
                        }

                        final BitmapDrawable icon = image2;

                        boolean contains = false;
                        int where = -1;
                        int index = -1;

                        if (sharedPrefs.getBoolean("enable_drafts", true)) {
                            try {
                                for (int i = 0; i < draftNames.size(); i++)
                                {
                                    if (draftNames.get(i).equals(newDraft))
                                    {
                                        contains = true;
                                        where = i;
                                        break;
                                    }
                                }

                                if (!contains && messageEntry.getText().toString().trim().length() > 0)
                                {
                                    draftNames.add(newDraft);
                                    drafts.add(messageEntry.getText().toString());
                                    draftChanged.add(true);
                                } else if (contains && messageEntry.getText().toString().trim().length() > 0)
                                {
                                    drafts.set(where, messageEntry.getText().toString());
                                    draftChanged.set(where, true);
                                } else if (contains && messageEntry.getText().toString().trim().length() == 0 && fromDraft)
                                {
                                    draftsToDelete.add(draftNames.get(where));
                                    draftNames.remove(where);
                                    drafts.remove(where);
                                    draftChanged.remove(where);
                                }
                            } catch (Exception e) {

                            }

                            newDraft = "";

                            try {
                                for (int i = 0; i < draftNames.size(); i++)
                                {
                                    if (draftNames.get(i).equals(threadIds.get(mViewPager.getCurrentItem())))
                                    {
                                        index = i;
                                        break;
                                    }
                                }
                            } catch (Exception e) {

                            }
                        }

                        final int indexF = index;
				        
				        ((Activity) context).getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {

                            @Override
                            public void run() {
                                try {
                                    View row = menuLayout.getChildAt(mViewPager.getCurrentItem());
                                    if (!sharedPrefs.getBoolean("custom_background", false)) {
                                        row.setBackgroundColor(sharedPrefs.getInt("ct_conversationListBackground", context.getResources().getColor(R.color.light_silver)));
                                    }
                                } catch (Exception e) {

                                }

                                if ((!sharedPrefs.getBoolean("hide_title_bar", true) || sharedPrefs.getBoolean("always_show_contact_info", false)) && ab != null) {
                                    ab.setTitle(titleF);
                                    ab.setSubtitle(subtitleF);
                                }

                                if (sharedPrefs.getBoolean("title_contact_image", false) && ab != null) {
                                    ab.setIcon(icon);
                                }

                                if (sharedPrefs.getBoolean("enable_drafts", true)) {
                                    if (deleteDraft) {
                                        if (!messageEntry.getText().equals("")) {
                                            messageEntry.setText("");
                                        }

                                        fromDraft = false;

                                        if (indexF != -1) {
                                            if (sharedPrefs.getBoolean("auto_insert_draft", false)) {
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

                                                messageBar.show(getString(R.string.draft_found), getString(R.string.apply_draft));
                                            }
                                        }
                                    } else {
                                        fromDraft = true;
                                    }
                                }
                            }

                        });
					}
					
				}).start();
			}
		});
        
        mViewPager.setOffscreenPageLimit(1);

        if (sharedPrefs.getString("run_as", "sliding").equals("card2")) {
            mViewPager.setOffscreenPageLimit(2);
            int scale = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -26, context.getResources().getDisplayMetrics());
            mViewPager.setPageMargin(scale);
        }
	}
	
	public Bitmap drawableToBitmap (Drawable drawable) {
	    if (drawable instanceof BitmapDrawable) {
	        return ((BitmapDrawable)drawable).getBitmap();
	    }

	    try
	    {
		    int width = drawable.getIntrinsicWidth();
		    width = width > 0 ? width : 1;
		    int height = drawable.getIntrinsicHeight();
		    height = height > 0 ? height : 1;
	
		    Bitmap bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		    Canvas canvas = new Canvas(bitmap); 
		    drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
		    drawable.draw(canvas);
		    return bitmap;
	    } catch (Exception e)
	    {
	    	if (sharedPrefs.getBoolean("ct_darkContactImage", false))
	    	{
	    		return BitmapFactory.decodeResource(this.getResources(), R.drawable.ic_contact_dark);
	    	} else
	    	{
	    		return BitmapFactory.decodeResource(this.getResources(), R.drawable.ic_contact_picture);
	    	}
	    }
	}
	 
	 public InputStream openDisplayPhoto(long contactId) {
		 Uri contactUri = ContentUris.withAppendedId(Contacts.CONTENT_URI, contactId);
	     Uri photoUri = Uri.withAppendedPath(contactUri, Contacts.Photo.CONTENT_DIRECTORY);
	     Cursor cursor = getContentResolver().query(photoUri,
	          new String[] {Contacts.Photo.PHOTO}, null, null, null);
	     if (cursor == null) {
	         return null;
	     }
	     try {
	         if (cursor.moveToFirst()) {
	             byte[] data = cursor.getBlob(0);
	             if (data != null) {
	                 return new ByteArrayInputStream(data);
	             }
	         }
	     } finally {
	         cursor.close();
	     }
	     return null;
	 }
	 
	 public String getMyPhoneNumber(){
		    TelephonyManager mTelephonyMgr;
		    mTelephonyMgr = (TelephonyManager)
		        getSystemService(Context.TELEPHONY_SERVICE); 
		    return mTelephonyMgr.getLine1Number();
		}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);

        //SearchView searchView = (SearchView)menu.findItem(R.id.menu_search).getActionView();
        //searchView.setQueryHint("Search");

		return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu)
	{
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

        if (deviceType.equals("phone") || deviceType.equals("phablet2"))
        {
            if (inboxNumber.size() == 0 || MainActivity.menu.isMenuShowing()) // on conversation list
            {
                menu.getItem(0).setVisible(false);
                menu.getItem(1).setVisible(false);
                menu.getItem(2).setVisible(true);
                menu.getItem(3).setVisible(true);
                menu.getItem(4).setVisible(true);
                menu.getItem(5).setVisible(false);
                menu.getItem(6).setVisible(false);
                menu.getItem(7).setVisible(true);
                menu.getItem(8).setVisible(false);

                if (MainActivity.menu.isSecondaryMenuShowing()) // on new message
                {
                    menu.getItem(0).setVisible(false);
                    menu.getItem(1).setVisible(true);
                    menu.getItem(2).setVisible(false);
                    menu.getItem(3).setVisible(false);
                    menu.getItem(4).setVisible(false);
                    menu.getItem(5).setVisible(true);
                    menu.getItem(6).setVisible(false);
                    menu.getItem(7).setVisible(false);
                    menu.getItem(8).setVisible(false);
                }
            } else // in ViewPager
            {
                menu.getItem(0).setVisible(true);
                menu.getItem(1).setVisible(true);
                menu.getItem(2).setVisible(false);
                menu.getItem(3).setVisible(true);
                menu.getItem(4).setVisible(false);
                menu.getItem(5).setVisible(true);
                menu.getItem(6).setVisible(true);
                menu.getItem(7).setVisible(true);
                menu.getItem(8).setVisible(true);

                if (group.get(mViewPager.getCurrentItem()).equals("yes")) // if there is a group message
                {
                    menu.getItem(8).setVisible(false);
                }
            }
        } else
        {
            if (inboxNumber.size() == 0 || MainActivity.menu.isMenuShowing())
            {
                menu.getItem(0).setVisible(false);
                menu.getItem(3).setVisible(false);
                menu.getItem(4).setVisible(false);
                menu.getItem(6).setVisible(false);
                menu.getItem(7).setVisible(false);
                menu.getItem(8).setVisible(false);
            } else
            {
                menu.getItem(0).setVisible(true);
                menu.getItem(3).setVisible(true);
                menu.getItem(4).setVisible(true);
                menu.getItem(6).setVisible(true);
                menu.getItem(7).setVisible(true);
                menu.getItem(8).setVisible(true);

                if (group.get(mViewPager.getCurrentItem()).equals("yes"))
                {
                    menu.getItem(8).setVisible(false);
                }
            }
        }

        if (sharedPrefs.getBoolean("ct_light_action_bar", false))
        {
            Drawable callButton = getResources().getDrawable(R.drawable.ic_menu_call);
            callButton.setColorFilter(getResources().getColor(R.color.hangouts_ab_icon), Mode.MULTIPLY);
            menu.getItem(0).setIcon(callButton);

            Drawable attachButton = getResources().getDrawable(R.drawable.ic_attach);
            attachButton.setColorFilter(getResources().getColor(R.color.hangouts_ab_icon), Mode.MULTIPLY);
            menu.getItem(1).setIcon(attachButton);

            Drawable searchButton = getResources().getDrawable(R.drawable.ic_search);
            searchButton.setColorFilter(getResources().getColor(R.color.hangouts_ab_icon), Mode.MULTIPLY);
            menu.getItem(2).setIcon(searchButton);

            Drawable replyButton = getResources().getDrawable(R.drawable.ic_reply);
            replyButton.setColorFilter(getResources().getColor(R.color.hangouts_ab_icon), Mode.MULTIPLY);
            menu.getItem(3).setIcon(replyButton);
        } else
        {
            Drawable callButton = getResources().getDrawable(R.drawable.ic_menu_call);
            callButton.setColorFilter(getResources().getColor(R.color.white), Mode.MULTIPLY);
            menu.getItem(0).setIcon(callButton);

            Drawable attachButton = getResources().getDrawable(R.drawable.ic_attach);
            attachButton.setColorFilter(getResources().getColor(R.color.white), Mode.MULTIPLY);
            menu.getItem(1).setIcon(attachButton);

            Drawable searchButton = getResources().getDrawable(R.drawable.ic_search);
            searchButton.setColorFilter(getResources().getColor(R.color.white), Mode.MULTIPLY);
            menu.getItem(2).setIcon(searchButton);

            Drawable replyButton = getResources().getDrawable(R.drawable.ic_reply);
            replyButton.setColorFilter(getResources().getColor(R.color.white), Mode.MULTIPLY);
            menu.getItem(3).setIcon(replyButton);
        }
		
		return true;
	}

    public String version;
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    case R.id.menu_new_message:
            if (deviceType.equals("phone") || deviceType.equals("phablet2"))
            {
	            menu.showSecondaryMenu();
            } else
            {
                menu.showMenu();
            }

	        return true;
	    case R.id.menu_settings:
	    	startActivity(new Intent(this, SettingsPagerActivity.class));
            finish();
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

            builder.setMessage(this.getResources().getString(R.string.version) + ": " + version +
                    "\n\n" + this.getResources().getString(R.string.about_expanded) + "\n\n© 2013 Jacob Klinker and Luke Klinker")
                    .setPositiveButton(this.getResources().getString(R.string.changelog), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Intent wizardintent = new Intent(getApplicationContext(), wizardpager.MainActivity.class);
                            wizardintent.putExtra("version", version);
                            startActivity(wizardintent);
                        }
                    })
                    .setNegativeButton(this.getResources().getString(R.string.tweet_us), new DialogInterface.OnClickListener() {
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
            if (deviceType.equals("phone") || deviceType.equals("phablet2"))
            {
	    	    menu.showMenu();
            } else
            {

            }

	    	return true;
	    case R.id.menu_attach:
	    	multipleAttachments = false;
	    	AttachMore.data = new ArrayList<MMSPart>();

            //boolean newMessage;

            if (deviceType.equals("phone") || deviceType.equals("phablet2"))
            {
                newMessage = menu.isSecondaryMenuShowing();
            } else
            {
                newMessage = menu.isMenuShowing();
            }
	    	
	    	if (newMessage)
	    	{
	    		final Context context = this;
	    		
	    		AlertDialog.Builder attachBuilder = new AlertDialog.Builder(this);
	    		attachBuilder.setItems(R.array.selectImage, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						switch (arg1)
						{
						case 0:
							Intent intent = new Intent();
			                intent.setType("image/*");
			                intent.setAction(Intent.ACTION_GET_CONTENT);
			                startActivityForResult(Intent.createChooser(intent, getResources().getString(R.string.select_picture)), 2);
			                
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
	    	} else
	    	{
	    		final Context context = this;
	    		
	    		attachedPosition = mViewPager.getCurrentItem();
	    		
	    		AlertDialog.Builder attachBuilder = new AlertDialog.Builder(this);
	    		attachBuilder.setItems(R.array.selectImage, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						switch (arg1)
						{
						case 0:
							Intent intent = new Intent();
			                intent.setType("image/*");
			                intent.setAction(Intent.ACTION_GET_CONTENT);
			                startActivityForResult(Intent.createChooser(intent, getResources().getString(R.string.select_picture)), 1);
			                
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
	    	
	    	return true;
	    case R.id.menu_call:
	    	try
	    	{
		    	Intent callIntent = new Intent(Intent.ACTION_CALL);
		        callIntent.setData(Uri.parse("tel:"+findContactNumber(inboxNumber.get(mViewPager.getCurrentItem()), this)));
		        callIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		        startActivity(callIntent);
	    	} catch (Exception e)
	    	{
	    		Toast.makeText(this, "No contact to call", Toast.LENGTH_SHORT).show();
	    	}
	    	return true;
	    case R.id.menu_delete:
	    	Intent intent = new Intent(this, BatchDeleteActivity.class);
			intent.putExtra("threadIds", threadIds);
			intent.putExtra("inboxNumber", inboxNumber);
			startActivity(intent);
            overridePendingTransition(R.anim.activity_slide_in_right, R.anim.activity_slide_out_left);
            
	    	return true;
	    case R.id.menu_template:
			AlertDialog.Builder template = new AlertDialog.Builder(this);
			template.setTitle(getResources().getString(R.string.insert_template));
			
			ListView templates = new ListView(this);
			
			TextView footer = new TextView(this);
			footer.setText(getResources().getString(R.string.add_templates));
			int scale = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics());
			footer.setPadding(scale, scale, scale, scale);

			templates.addFooterView(footer);
			
			final ArrayList<String> text = readFromFile4(this);
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
					try
					{
                        boolean newMessage;

                        if (deviceType.equals("phone") || deviceType.equals("phablet2"))
                        {
                            newMessage = menu.isSecondaryMenuShowing();
                        } else
                        {
                            newMessage = menu.isMenuShowing();
                        }

						if (newMessage)
						{
                            if (deviceType.equals("phone") || deviceType.equals("phablet2"))
                            {
							    ((TextView) menu.getSecondaryMenu().findViewById(R.id.messageEntry2)).setText(text.get(arg2));
                            } else
                            {
                                ((TextView) menu.getMenu().findViewById(R.id.messageEntry2)).setText(text.get(arg2));
                            }

							templateDialog.cancel();
						} else
						{
							messageEntry.setText(text.get(arg2));
							messageEntry.setSelection(text.get(arg2).length());
							templateDialog.cancel();
						}
					} catch (Exception e)
					{
						
					}
					
				}
				
			});		
			
			return true;
        case R.id.copy_sender:
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Copied Address", findContactNumber(inboxNumber.get(mViewPager.getCurrentItem()), this));
            clipboard.setPrimaryClip(clip);

            Toast.makeText(this, R.string.text_saved, Toast.LENGTH_SHORT).show();
            return true;
        case R.id.delete_conversation:
            final Context context = this;

            AlertDialog.Builder deleteDialog = new AlertDialog.Builder(this);
            deleteDialog.setTitle(getResources().getString(R.string.delete_conversation));
            deleteDialog.setMessage(getResources().getString(R.string.delete_conversation_message));
            deleteDialog.setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    final ProgressDialog progDialog = new ProgressDialog(context);
                    progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    progDialog.setMessage(getResources().getString(R.string.deleting));
                    progDialog.show();

                    new Thread(new Runnable(){

                        @Override
                        public void run() {
                            deleteSMS(context, threadIds.get(mViewPager.getCurrentItem()));

                            ((Activity) context).getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {

                                @Override
                                public void run() {
                                    ((MainActivity)context).refreshViewPager(true);
                                    progDialog.dismiss();

                                    Intent updateWidget = new Intent("com.klinker.android.messaging.UPDATE_WIDGET");
                                    context.sendBroadcast(updateWidget);
                                }

                            });
                        }

                    }).start();
                }
            });
            deleteDialog.setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });

            deleteDialog.create().show();

            return true;
            case R.id.menu_mark_all_read:
                final Context context1 = this;

                new Thread(new Runnable(){

                    @Override
                    public void run() {

                        String[] projection = new String[]{"_id"};
                        Uri uri = Uri.parse("content://mms-sms/conversations/?simple=true");
                        Cursor cursor = context1.getContentResolver().query(uri, projection, null, null, null);

                        try{

                             cursor.moveToFirst();

                             do {

                                String SmsMessageId = cursor.getString(cursor.getColumnIndex("_id"));
                                ContentValues values = new ContentValues();
                                values.put("read", true);
                                context1.getContentResolver().update(Uri.parse("content://sms/inbox"), values, "_id=" + SmsMessageId, null);

                            } while (cursor.moveToNext());

                            cursor.close();

                            ((Activity) context1).getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {

                                @Override
                                public void run() {
                                    ((MainActivity)context1).refreshViewPager(true);

                                    Intent updateWidget = new Intent("com.klinker.android.messaging.UPDATE_WIDGET");
                                    context1.sendBroadcast(updateWidget);
                                }

                            });
                        }catch(Exception e)
                        {
                            e.printStackTrace();
                        } finally
                        {
                            cursor.close();
                        }
                    }

                }).start();

                return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}
	
	public void deleteSMS(Context context, String threadId) {
	    try {
	        	context.getContentResolver().delete(Uri.parse("content://mms-sms/conversations/" + threadId + "/"), null, null);
	    } catch (Exception e) {
	    	Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show();
	    }
	}
	
	@SuppressWarnings("deprecation")
	protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) { 
        
        if (requestCode == 1)
        {
            if(resultCode == RESULT_OK){  
                final Uri selectedImage = imageReturnedIntent.getData();
                attachedImage = selectedImage;
                fromCamera = false;
                
                imageAttachBackground.setBackgroundColor(sharedPrefs.getInt("ct_messageListBackground", getResources().getColor(R.color.light_silver)));
	    		Drawable attachBack = getResources().getDrawable(R.drawable.attachment_editor_bg);
	    		attachBack.setColorFilter(sharedPrefs.getInt("ct_sentMessageBackground", getResources().getColor(R.color.white)), Mode.MULTIPLY);
	    		imageAttach.setBackgroundDrawable(attachBack);
	    		imageAttachBackground.setVisibility(View.VISIBLE);
	    		imageAttach.setVisibility(true);
	    		
	    		try
	    		{
	    			imageAttach.setImage("send_image", decodeFile(new File(getPath(selectedImage))));
	    		} catch (Exception e)
	    		{
	    			Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
	    			imageAttach.setVisibility(false);
	    			imageAttachBackground.setVisibility(View.GONE);
	    		}
	    		
	    		final Context context = this;
	    		
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
		                startActivityForResult(Intent.createChooser(intent, getResources().getString(R.string.select_picture)), 1);
						
					}
	    			
	    		});
	    		
	    		removeImage.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						imageAttach.setVisibility(false);
						imageAttachBackground.setVisibility(View.GONE);
						
					}
	    			
	    		});
	    		
	    		MainActivity.menu.showContent();
	    		mViewPager.setCurrentItem(attachedPosition);

            }
        } else if (requestCode == 2)
        {
        	if(resultCode == RESULT_OK){ 
        		final Uri selectedImage = imageReturnedIntent.getData();
        		attachedImage2 = selectedImage;
        		fromCamera = false;
                
                imageAttachBackground2.setBackgroundColor(sharedPrefs.getInt("ct_conversationListBackground", getResources().getColor(R.color.light_silver)));
	    		Drawable attachBack = getResources().getDrawable(R.drawable.attachment_editor_bg);
	    		attachBack.setColorFilter(sharedPrefs.getInt("ct_sentMessageBackground", getResources().getColor(R.color.white)), Mode.MULTIPLY);
	    		imageAttach2.setBackgroundDrawable(attachBack);
	    		imageAttachBackground2.setVisibility(View.VISIBLE);
	    		imageAttach2.setVisibility(true);
	    		
	    		try
	    		{
	    			imageAttach2.setImage("send_image", decodeFile(new File(getPath(selectedImage))));
	    		} catch (Exception e)
	    		{
	    			Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
	    			imageAttach2.setVisibility(false);
	    			imageAttachBackground2.setVisibility(View.GONE);
	    		}
	    		
	    		final Context context = this;
	    		
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
		                startActivityForResult(Intent.createChooser(intent, getResources().getString(R.string.select_picture)), 2);
						
					}
	    			
	    		});
	    		
	    		removeImage.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						imageAttach2.setVisibility(false);
						imageAttachBackground2.setVisibility(View.GONE);
						
					}
	    			
	    		});
	    		if (deviceType.equals("phone") || deviceType.equals("phablet2"))
                {
	    		    MainActivity.menu.showSecondaryMenu();
                } else
                {
                    MainActivity.menu.showMenu();
                }

            }
        } else if (requestCode == 3)
        {
        	if (resultCode == Activity.RESULT_OK)
        	{
        		getContentResolver().notifyChange(capturedPhotoUri, null);
        		attachedImage = Uri.fromFile(new File(Environment.getExternalStorageDirectory() + "/SlidingMessaging/", "photoToSend.png"));
        		fromCamera = true;
        		
        		imageAttachBackground.setBackgroundColor(sharedPrefs.getInt("ct_conversationListBackground", getResources().getColor(R.color.light_silver)));
 	    		Drawable attachBack = getResources().getDrawable(R.drawable.attachment_editor_bg);
 	    		attachBack.setColorFilter(sharedPrefs.getInt("ct_sentMessageBackground", getResources().getColor(R.color.white)), Mode.MULTIPLY);
 	    		imageAttach.setBackgroundDrawable(attachBack);
 	    		imageAttachBackground.setVisibility(View.VISIBLE);
 	    		imageAttach.setVisibility(true);
 	    		
 	    		try
 	    		{
 	    			Bitmap image = android.provider.MediaStore.Images.Media.getBitmap(getContentResolver(), capturedPhotoUri);
 	    			File f = new File(capturedPhotoUri.getPath());
 	    			image = decodeFile(f);
 	    			imageAttach.setImage("send_image", image);
 	    		} catch (Exception e)
 	    		{
 	    			e.printStackTrace();
 	    			Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
 	    			imageAttach.setVisibility(false);
 	    			imageAttachBackground.setVisibility(View.GONE);
 	    		}
 	    		
 	    		final Context context = this;
 	    		
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
 		                startActivityForResult(Intent.createChooser(intent, getResources().getString(R.string.select_picture)), 1);
 						
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
        } else if (requestCode == 4)
        {
        	if (resultCode == Activity.RESULT_OK)
        	{
        		getContentResolver().notifyChange(capturedPhotoUri, null);
        		attachedImage2 = Uri.fromFile(new File(Environment.getExternalStorageDirectory() + "/SlidingMessaging/", "photoToSend.png"));
        		fromCamera = true;
        		
        		imageAttachBackground2.setBackgroundColor(sharedPrefs.getInt("ct_conversationListBackground", getResources().getColor(R.color.light_silver)));
 	    		Drawable attachBack = getResources().getDrawable(R.drawable.attachment_editor_bg);
 	    		attachBack.setColorFilter(sharedPrefs.getInt("ct_sentMessageBackground", getResources().getColor(R.color.white)), Mode.MULTIPLY);
 	    		imageAttach2.setBackgroundDrawable(attachBack);
 	    		imageAttachBackground2.setVisibility(View.VISIBLE);
 	    		imageAttach2.setVisibility(true);
 	    		
 	    		try
 	    		{
 	    			Bitmap image = android.provider.MediaStore.Images.Media.getBitmap(getContentResolver(), capturedPhotoUri);
 	    			File f = new File(capturedPhotoUri.getPath());
 	    			image = decodeFile(f);
 	    			imageAttach2.setImage("send_image", image);
 	    		} catch (Exception e)
 	    		{
 	    			e.printStackTrace();
 	    			Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
 	    			imageAttach2.setVisibility(false);
 	    			imageAttachBackground2.setVisibility(View.GONE);
 	    		}
 	    		
 	    		final Context context = this;
 	    		
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
 		                startActivityForResult(Intent.createChooser(intent, getResources().getString(R.string.select_picture)), 1);
 						
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
        } else if (requestCode == 5)
        {
        	if (resultCode == Activity.RESULT_OK)
            {
                multipleAttachments = true;
                
                imageAttachBackground.setBackgroundColor(sharedPrefs.getInt("ct_conversationListBackground", getResources().getColor(R.color.light_silver)));
 	    		Drawable attachBack = getResources().getDrawable(R.drawable.attachment_editor_bg);
 	    		attachBack.setColorFilter(sharedPrefs.getInt("ct_sentMessageBackground", getResources().getColor(R.color.white)), Mode.MULTIPLY);
 	    		imageAttach.setBackgroundDrawable(attachBack);
 	    		imageAttachBackground.setVisibility(View.VISIBLE);
 	    		imageAttach.setVisibility(true);
 	    		
 	    		try
 	    		{
 	    			Bitmap bmp = BitmapFactory.decodeByteArray(AttachMore.data.get(0).Data, 0, AttachMore.data.get(0).Data.length);
 	    			Bitmap mutableBitmap = bmp.copy(Bitmap.Config.ARGB_8888, true);
 	    			imageAttach.setImage("send_image", mutableBitmap);
 	    		} catch (Exception e)
 	    		{
 	    			e.printStackTrace();
 	    			Toast.makeText(this, "Error loading MMS", Toast.LENGTH_SHORT).show();
 	    			imageAttach.setVisibility(false);
 	    			imageAttachBackground.setVisibility(View.GONE);
 	    		}
                
                final Context context = this;
 	    		
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
        } else if (requestCode == 6)
        {
        	if (resultCode == Activity.RESULT_OK)
            {
                multipleAttachments = true;
                
                imageAttachBackground2.setBackgroundColor(sharedPrefs.getInt("ct_conversationListBackground", getResources().getColor(R.color.light_silver)));
 	    		Drawable attachBack = getResources().getDrawable(R.drawable.attachment_editor_bg);
 	    		attachBack.setColorFilter(sharedPrefs.getInt("ct_sentMessageBackground", getResources().getColor(R.color.white)), Mode.MULTIPLY);
 	    		imageAttach2.setBackgroundDrawable(attachBack);
 	    		imageAttachBackground2.setVisibility(View.VISIBLE);
 	    		imageAttach2.setVisibility(true);
 	    		
 	    		try
 	    		{
 	    			Bitmap bmp = BitmapFactory.decodeByteArray(AttachMore.data.get(0).Data, 0, AttachMore.data.get(0).Data.length);
 	    			Bitmap mutableBitmap = bmp.copy(Bitmap.Config.ARGB_8888, true);
 	    			imageAttach2.setImage("send_image", mutableBitmap);
 	    		} catch (Exception e)
 	    		{
 	    			e.printStackTrace();
 	    			Toast.makeText(this, "Error loading MMS", Toast.LENGTH_SHORT).show();
 	    			imageAttach2.setVisibility(false);
 	    			imageAttachBackground2.setVisibility(View.GONE);
 	    		}
                
                final Context context = this;
 	    		
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
            }
        } else if (requestCode == REQ_ENTER_PATTERN) // Code for pattern unlock
        {
            /*
            * NOTE that there are 3 possible result codes!!!
            */
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
        } else
        {
        	
        }
        
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent); 
	}
	
	public String getPath(Uri uri) {
		String[] projection = { MediaStore.Images.Media.DATA };
		Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
		int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();
		String path = cursor.getString(column_index);
		cursor.close();
		return path;
		}
	
	private Bitmap decodeFile(File f){
	    try {
	        //Decode image size
	        BitmapFactory.Options o = new BitmapFactory.Options();
	        o.inJustDecodeBounds = true;
	        BitmapFactory.decodeStream(new FileInputStream(f),null,o);

	        //The new size we want to scale to
	        final int REQUIRED_SIZE=150;

	        //Find the correct scale value. It should be the power of 2.
	        int scale=1;
	        while(o.outWidth/scale/2>=REQUIRED_SIZE && o.outHeight/scale/2>=REQUIRED_SIZE)
	            scale*=2;

	        //Decode with inSampleSize
	        BitmapFactory.Options o2 = new BitmapFactory.Options();
	        o2.inSampleSize=scale;
	        Bitmap image = BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
	        
	        try
	        {
	        	ExifInterface exif = new ExifInterface(f.getAbsolutePath());
	        	int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
	        	
	        	if (orientation == 6)
	        	{
		        	Matrix matrix = new Matrix();
		        	matrix.postRotate(90);
		        	image = Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getHeight(), matrix, true);
	        	} else if (orientation == 3)
	        	{
	        		Matrix matrix = new Matrix();
		        	matrix.postRotate(180);
		        	image = Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getHeight(), matrix, true);
	        	} else if (orientation == 8)
	        	{
	        		Matrix matrix = new Matrix();
		        	matrix.postRotate(2700);
		        	image = Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getHeight(), matrix, true);
	        	}
	        } catch (Exception e)
	        {
	        	e.printStackTrace();
	        }
	        
	        return image;
	    } catch (FileNotFoundException e) {}
	    return null;
	}
	
	private Bitmap decodeFile2(File f){
	    try {
	        //Decode image size
	        BitmapFactory.Options o = new BitmapFactory.Options();
	        o.inJustDecodeBounds = true;
	        BitmapFactory.decodeStream(new FileInputStream(f),null,o);

	        //The new size we want to scale to
            int REQUIRED_SIZE=300;

            if (!sharedPrefs.getBoolean("limit_attachment_size", true))
            {
                REQUIRED_SIZE = 500;
            }

	        //Find the correct scale value. It should be the power of 2.
	        int scale=1;
	        while(o.outWidth/scale/2>=REQUIRED_SIZE && o.outHeight/scale/2>=REQUIRED_SIZE)
	            scale*=2;

	        //Decode with inSampleSize
	        BitmapFactory.Options o2 = new BitmapFactory.Options();
	        o2.inSampleSize=scale;
	        Bitmap image = BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
	        
	        try
	        {
	        	ExifInterface exif = new ExifInterface(f.getPath());
	        	int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
	        	
	        	if (orientation == 6)
	        	{
		        	Matrix matrix = new Matrix();
		        	matrix.postRotate(90);
		        	image = Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getHeight(), matrix, true);
	        	} else if (orientation == 3)
	        	{
	        		Matrix matrix = new Matrix();
		        	matrix.postRotate(180);
		        	image = Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getHeight(), matrix, true);
	        	} else if (orientation == 8)
	        	{
	        		Matrix matrix = new Matrix();
		        	matrix.postRotate(2700);
		        	image = Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getHeight(), matrix, true);
	        	}
	        } catch (Exception e)
	        {
	        	e.printStackTrace();
	        }
	        
	        return image;
	    } catch (FileNotFoundException e) {}
	    return null;
	}
	
	@Override
	public void onBackPressed() {
        if (deviceType.equals("phone") || deviceType.equals("phablet2"))
        {
            if (menu.isSecondaryMenuShowing())
            {
                if (!sharedPrefs.getBoolean("open_contact_menu", false))
                {
                    menu.showContent();
                } else
                {
                    menu.showMenu();
                }
            } else
            {
                if (menu.isMenuShowing() && !sharedPrefs.getBoolean("open_contact_menu", false))
                {
                    menu.showContent();
                } else if (!menu.isMenuShowing() && sharedPrefs.getBoolean("open_contact_menu", false))
                {
                    menu.showMenu();
                } else
                {
                    super.onBackPressed();
                }
            }
        } else
        {
            if (menu.isMenuShowing())
            {
                menu.showContent();
            } else
            {
                super.onBackPressed();
            }
        }

	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
	    
	}
	
	@Override
	public void onResume()
	{
		super.onResume();
		
		IntentFilter filter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        filter.setPriority(3);
        registerReceiver(receiver, filter);
        
        filter = new IntentFilter("com.klinker.android.messaging.NEW_MMS");
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        filter.setPriority(3);
        registerReceiver(mmsReceiver, filter);
        
        String menuOption = sharedPrefs.getString("page_or_menu2", "2");
        
        if (menuOption.equals("2"))
        {
        	menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
        } else if (menuOption.equals("1"))
        {
        	menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
        } else
        {
        	menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
        }
        
        if (imageAttach.getVisibility() == View.VISIBLE)
        {
        	menu.showContent();
        } else if (imageAttach2.getVisibility() == View.VISIBLE)
        {
            if (deviceType.equals("phone") || deviceType.equals("phablet2"))
            {
        	    menu.showSecondaryMenu();
            } else
            {
                menu.showMenu();
            }
        }
        
        if (whatToSend != null)
        {
        	messageEntry.setText(whatToSend);
        	whatToSend = null;
        }

        Intent updateWidget = new Intent("com.klinker.android.messaging.UPDATE_WIDGET");
        sendBroadcast(updateWidget);

        if(!sharedPrefs.getString("security_option", "none").equals("none"))
        {
            long currentTime = Calendar.getInstance().getTimeInMillis();
            long lastTime = sharedPrefs.getLong("last_time", 0);

            if (currentTime - lastTime > Long.parseLong(sharedPrefs.getString("timeout_settings", "300000")))
            {
                if (sharedPrefs.getString("security_option", "none").equals("pin"))
                {
                    Intent passwordIntent = new Intent(getApplicationContext(), PinActivity.class);
                    startActivity(passwordIntent);
                    finish();
                } else if (sharedPrefs.getString("security_option", "none").equals("password"))
                {
                    Intent passwordIntent = new Intent(getApplicationContext(), PasswordActivity.class);
                    startActivity(passwordIntent);
                    finish();
                } else if (sharedPrefs.getString("security_option", "none").equals("pattern"))
                {
                    SecurityPrefs.setAutoSavePattern(this, true);
                    Intent intent = new Intent(LockPatternActivity.ACTION_COMPARE_PATTERN, null,
                            this, LockPatternActivity.class);
                    startActivityForResult(intent, REQ_ENTER_PATTERN);
                }

            }
        }

        if (sharedPrefs.getBoolean("enable_drafts", true)) {
            try {
                drafts = new ArrayList<String>();
                draftNames = new ArrayList<String>();
                draftChanged = new ArrayList<Boolean>();
                draftsToDelete = new ArrayList<String>();

                Cursor query = getContentResolver().query(Uri.parse("content://sms/draft/"), new String[] {"thread_id", "body"}, null, null, null);

                if (query.moveToFirst())
                {
                    do {
                        drafts.add(query.getString(query.getColumnIndex("body")));
                        draftNames.add(query.getString(query.getColumnIndex("thread_id")));
                        draftChanged.add(false);
                    } while (query.moveToNext());
                }

                query.close();
            } catch (Exception e) {

            }

            int index = -1;

            try {
                for (int i = 0; i < draftNames.size(); i++)
                {
                    if (draftNames.get(i).equals(threadIds.get(mViewPager.getCurrentItem())))
                    {
                        index = i;
                        break;
                    }
                }
            } catch (Exception e) {

            }

            fromDraft = false;

            if (index != -1)
            {
                if (sharedPrefs.getBoolean("auto_insert_draft", false))
                {
                    fromDraft = true;
                    messageEntry.setText(drafts.get(index));
                    messageEntry.setSelection(drafts.get(index).length());
                } else
                {
                    final int indexF = index;

                    messageBar.setOnClickListener(new MessageBar.OnMessageClickListener() {
                        @Override
                        public void onMessageClick(Parcelable token) {
                            fromDraft = true;
                            messageEntry.setText(drafts.get(indexF));
                            messageEntry.setSelection(drafts.get(indexF).length());
                        }
                    });

                    messageBar.show(getString(R.string.draft_found), getString(R.string.apply_draft));
                }
            }
        }
	}
	
	@Override
	public void onPause()
	{
		super.onPause();
		
		try
		{
			unregisterReceiver(receiver);
			unregisterReceiver(mmsReceiver);
		} catch (Exception e)
		{
			
		}
		
		ComponentName receiver = new ComponentName(this, SentReceiver.class);
		ComponentName receiver2 = new ComponentName(this, DeliveredReceiver.class);
	    PackageManager pm = this.getPackageManager();

	    pm.setComponentEnabledSetting(receiver,
	            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
	            PackageManager.DONT_KILL_APP);
	    
	    pm.setComponentEnabledSetting(receiver2,
	    		PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
	            PackageManager.DONT_KILL_APP);
	}
	
	@Override
	public void onStop()
	{
		super.onStop();

        Intent updateWidget = new Intent("com.klinker.android.messaging.UPDATE_WIDGET");
        sendBroadcast(updateWidget);

        final Context context = this;

        if (sharedPrefs.getBoolean("enable_drafts", true)) {
            if (messageEntry.getText().toString().length() != 0) {
                draftChanged.add(true);
                draftNames.add(threadIds.get(mViewPager.getCurrentItem()));
                drafts.add(messageEntry.getText().toString());
                messageEntry.setText("");
            }

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        for (int i = 0; i < draftChanged.size(); i++) {
                            if (draftChanged.get(i) == false) {
                                draftChanged.remove(i);
                                draftNames.remove(i);
                                drafts.remove(i);
                                i--;
                            }
                        }

                        ArrayList<String> ids = new ArrayList<String>();

                        Cursor query = context.getContentResolver().query(Uri.parse("content://sms/draft/"), new String[] {"_id", "thread_id"}, null, null, null);

                        if (query != null) {
                            if (query.moveToFirst()) {
                                do {
                                    for (int i = 0; i < draftsToDelete.size(); i++) {
                                        if (query.getString(query.getColumnIndex("thread_id")).equals(draftsToDelete.get(i))) {
                                            ids.add(query.getString(query.getColumnIndex("_id")));
                                            break;
                                        }
                                    }

                                    for (int i = 0; i < draftNames.size(); i++) {
                                        if (draftNames.get(i).equals(query.getString(query.getColumnIndex("thread_id")))) {
                                            context.getContentResolver().delete(Uri.parse("content://sms/" + query.getString(query.getColumnIndex("_id"))), null, null);
                                            break;
                                        }
                                    }
                                } while (query.moveToNext());

                                for (int i = 0; i < ids.size(); i++) {
                                    context.getContentResolver().delete(Uri.parse("content://sms/" + ids.get(i)), null, null);
                                }
                            }

                            query.close();
                        }

                        for (int i = 0; i < draftNames.size(); i++) {
                            String address = "";

                            for (int j = 0; j < inboxNumber.size(); j++) {
                                if (threadIds.get(j).equals(draftNames.get(i))) {
                                    address = findContactNumber(inboxNumber.get(j), context);
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

        if (sharedPrefs.getBoolean("cache_conversations", false)) {
            Intent cacheService = new Intent(context, CacheService.class);
            context.startService(cacheService);
        }
	}

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (deviceType.startsWith("phablet"))
        {
            recreate();
        }
    }
	
	@SuppressWarnings("deprecation")
	@Override
	public void onStart()
	{
		super.onStart();

        try {
            ComponentName receiver = new ComponentName(this, SentReceiver.class);
            ComponentName receiver2 = new ComponentName(this, DeliveredReceiver.class);
            PackageManager pm = this.getPackageManager();

            pm.setComponentEnabledSetting(receiver,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP);

            pm.setComponentEnabledSetting(receiver2,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP);
        } catch (Exception e) {

        }
		
		if (firstRun)
		{
			refreshViewPager(false);
			createMenu();
			firstRun = false;
			
			if (sharedPrefs.getBoolean("open_contact_menu", false) && (deviceType.equals("phone") || deviceType.equals("phablet2")))
			{
				menu.showMenu();
			}
			
			if (sendTo && !fromNotification)
			{
                try {
                    boolean flag = false;

                    for (int i = 0; i < inboxNumber.size(); i++)
                    {
                        if (findContactNumber(inboxNumber.get(i), this).replace("-","").replace("+", "").equals(sendMessageTo.replace("-", "").replace("+1", "")))
                        {
                            mViewPager.setCurrentItem(i);
                            menu.showContent();
                            flag = true;
                            break;
                        }
                    }

                    if (flag == false)
                    {
                        String name = findContactName(sendMessageTo, this);

                        for (int i = 0; i < inboxNumber.size(); i++)
                        {
                            if (findContactName(findContactNumber(inboxNumber.get(i), this), this).equals(name))
                            {
                                mViewPager.setCurrentItem(i);
                                menu.showContent();
                                flag = true;
                                break;
                            }
                        }
                    }

                    if (flag == false)
                    {
                        View newMessage;

                        if (deviceType.equals("phone") || deviceType.equals("phablet2"))
                        {
                            menu.showSecondaryMenu();
                            newMessage = menu.getSecondaryMenu();
                        } else
                        {
                            menu.showMenu();
                            newMessage = menu.getMenu();
                        }

                        EditText contact = (EditText) newMessage.findViewById(R.id.contactEntry);
                        contact.setText(sendMessageTo);

                        if (attachedImage2 != null)
                        {
                            imageAttachBackground2.setBackgroundColor(sharedPrefs.getInt("ct_conversationListBackground", getResources().getColor(R.color.light_silver)));
                            Drawable attachBack = getResources().getDrawable(R.drawable.attachment_editor_bg);
                            attachBack.setColorFilter(sharedPrefs.getInt("ct_sentMessageBackground", getResources().getColor(R.color.white)), Mode.MULTIPLY);
                            imageAttach2.setBackgroundDrawable(attachBack);
                            imageAttachBackground2.setVisibility(View.VISIBLE);
                            imageAttach2.setVisibility(true);

                            try
                            {
                                imageAttach2.setImage("send_image", decodeFile(new File(getPath(attachedImage2))));
                            } catch (Exception e)
                            {
                                Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
                                imageAttach2.setVisibility(false);
                                imageAttachBackground2.setVisibility(View.GONE);
                            }

                            final Context context = this;

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
                                    startActivityForResult(Intent.createChooser(intent, getResources().getString(R.string.select_picture)), 2);

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
			
			if (sendToThread != null)
			{
				for (int i = 0; i < threadIds.size(); i++)
				{
					if (threadIds.get(i).equals(sendToThread))
					{
						mViewPager.setCurrentItem(i);
						sendToThread = null;
						break;
					}
				}
				
				messageEntry.setText(sendToMessage);
				
				try
				{
					messageEntry.setSelection(sendToMessage.length());
				} catch (Exception e)
				{
					
				}
			}
		} else
		{
			if (messageRecieved == true)
			{
				refreshViewPager(false);
				messageRecieved = false;
			}
			
			if (sendTo == true)
			{
				menu.showContent();
			} else
			{
				if (sharedPrefs.getBoolean("open_contact_menu", false) && (imageAttach.getVisibility() != View.VISIBLE && imageAttach2.getVisibility() != View.VISIBLE) && (deviceType.equals("phone") || deviceType.equals("phablet2")))
				{
					menu.showMenu();
				} else if (imageAttach.getVisibility() == View.VISIBLE)
				{
					menu.showContent();
				} else if (imageAttach2.getVisibility() == View.VISIBLE)
				{
                    if (deviceType.equals("phone") || deviceType.equals("phablet2"))
                    {
					    menu.showSecondaryMenu();
                    } else
                    {
                        menu.showMenu();
                    }
				}
			}
		}

		if (fromNotification)
		{
			menu.showContent();
			fromNotification = false;
		}
	}
	
	@SuppressWarnings("deprecation")
	public void refreshViewPager(boolean totalRefresh)
	{
        pullToRefreshPosition = -1;
		String threadTitle = "0";
		
		if (!firstRun && inboxNumber.size() != 0)
		{
            MainActivity.notChanged = false;
			threadTitle = findContactName(findContactNumber(inboxNumber.get(mViewPager.getCurrentItem()), this), this);
		}
		
		refreshMessages(totalRefresh);

        mSectionsPagerAdapter = new SectionsPagerAdapter(
                getFragmentManager());

        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setBackgroundColor(sharedPrefs.getInt("ct_messageListBackground", getResources().getColor(R.color.light_silver)));

        if (sharedPrefs.getBoolean("custom_background2", false))
        {
            try
            {
                BitmapFactory.Options options = new BitmapFactory.Options();

                options.inSampleSize = 2;
                Bitmap myBitmap = BitmapFactory.decodeFile(Uri.parse(sharedPrefs.getString("custom_background2_location", "")).getPath(),options);
                Drawable d = new BitmapDrawable(Resources.getSystem(),myBitmap);
                mViewPager.setBackgroundDrawable(d);
            } catch (Exception e)
            {

            }
        }
		
		if ((messageRecieved && jump) || sentMessage)
		{
			threadTitle = "0";
			sentMessage = false;
		}

        if (dismissNotification) {
            NotificationManager mNotificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.cancel(1);
            mNotificationManager.cancel(2);
            writeToFile2(new ArrayList<String>(), this);

            Intent intent = new Intent("com.klinker.android.messaging.CLEARED_NOTIFICATION");
            this.sendBroadcast(intent);

            Intent stopRepeating = new Intent(this, NotificationRepeaterService.class);
            PendingIntent pStopRepeating = PendingIntent.getService(this, 0, stopRepeating, 0);
            AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            alarm.cancel(pStopRepeating);
        }
		
		if (!firstRun)
		{
            if (deviceType.equals("phone") || deviceType.equals("phablet2"))
            {
                menuAdapter = new MenuArrayAdapter(this, inboxBody, inboxDate, inboxNumber, mViewPager, threadIds, group, msgCount, msgRead);
                menuLayout.setAdapter(menuAdapter);
            } else
            {
                ListFragment newFragment = (ListFragment) getSupportFragmentManager().findFragmentById(R.id.menuList);
                newFragment.setListAdapter(new MenuArrayAdapter(this, inboxBody, inboxDate, inboxNumber, MainActivity.mViewPager, threadIds, group, msgCount, msgRead));
            }
		} else
		{
			final Context context = this;
			
			new Thread(new Runnable() {

				@Override
				public void run() {
					ArrayList<String> newMessages = readFromFile(context);
			        
					if (inboxNumber.size() > 0)
					{
                        if (newMessages.size() != 0)
                            newMessages.remove(newMessages.size() - 1);
					} else
					{
						newMessages = new ArrayList<String>();
					}
			        
			        writeToFile(newMessages, context);
					
				}
				
			}).start();
		}
		
		if (threadTitle.equals("0"))
		{
			mViewPager.setCurrentItem(0);
		} else
		{
			final String threadT = threadTitle;
			final Context context = this;
			
			new Thread(new Runnable() {

				@Override
				public void run() {
					boolean flag = false;
					
					for (int i = 0; i < inboxNumber.size(); i++)
					{
						if (threadT.equals(findContactName(findContactNumber(inboxNumber.get(i), context), context)))
						{
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
					
					if (!flag)
					{
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
		
		if (!sharedPrefs.getBoolean("hide_title_bar", true) || sharedPrefs.getBoolean("always_show_contact_info", false))
		{
			final ActionBar ab = getActionBar();
			final Context context = this;

            if (ab != null) {
                try
                {
                    ab.setTitle(findContactName(findContactNumber(inboxNumber.get(mViewPager.getCurrentItem()), context), context));

                    Locale sCachedLocale = Locale.getDefault();
                    int sFormatType = PhoneNumberUtils.getFormatTypeForLocale(sCachedLocale);
                    Editable editable = new SpannableStringBuilder(findContactNumber(inboxNumber.get(mViewPager.getCurrentItem()), context));
                    PhoneNumberUtils.formatNumber(editable, sFormatType);
                    ab.setSubtitle(editable.toString());

                    if (ab.getTitle().equals(ab.getSubtitle()))
                    {
                        ab.setSubtitle(null);
                    }

                    if (group.get(mViewPager.getCurrentItem()).equals("yes"))
                    {
                        ab.setTitle("Group MMS");
                        ab.setSubtitle(null);
                    }
                } catch (Exception e)
                {
                    ab.setTitle(R.string.app_name_in_app);
                    ab.setSubtitle(null);
                    ab.setIcon(R.drawable.ic_launcher);
                }
            }
		}
        
        if (sharedPrefs.getBoolean("title_contact_image", false))
        {
        	final ActionBar ab = getActionBar();
        	final Context context = this;
        	
        	try
        	{
        		ab.setIcon(new BitmapDrawable(getFacebookPhoto(findContactNumber(inboxNumber.get(mViewPager.getCurrentItem()), context), context)));
        	} catch (Exception e)
        	{
        		
        	}
        }
		
		MainActivity.loadAll = false;
		
		if (MainActivity.animationReceived == 2)
		{
			final ImageView glow = (ImageView) findViewById(R.id.newMessageGlow);
			
			glow.setVisibility(View.VISIBLE);
			glow.setAlpha((float)1);
			
			Animation fadeIn = new AlphaAnimation(0, (float).9);
			fadeIn.setInterpolator(new DecelerateInterpolator());
			fadeIn.setDuration(1000);
	
			Animation fadeOut = new AlphaAnimation((float).9, 0);
			fadeOut.setInterpolator(new AccelerateInterpolator());
			fadeOut.setStartOffset(1000);
			fadeOut.setDuration(1000);
	
			AnimationSet animation = new AnimationSet(false);
			animation.addAnimation(fadeIn);
			animation.addAnimation(fadeOut);
			
			animation.setAnimationListener(new AnimationListener() {

				@Override
				public void onAnimationEnd(Animation arg0) {
					glow.setAlpha((float)0);
					
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
		} else
		{
			final ImageView glow = (ImageView) findViewById(R.id.newMessageGlow);
			glow.setAlpha((float)0);
			glow.setVisibility(View.GONE);
		}
		
		try
		{
			invalidateOptionsMenu();
		} catch (Exception e)
		{
			
		}
	}
	
	public void refreshViewPager3()
	{
        pullToRefreshPosition = -1;
        MainActivity.notChanged = false;
        MainActivity.threadedLoad = false;
        int position = mViewPager.getCurrentItem();
		
		mSectionsPagerAdapter = new SectionsPagerAdapter(
            getFragmentManager());

        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setCurrentItem(position);
		
		try
		{
			invalidateOptionsMenu();
		} catch (Exception e)
		{
			
		}
	}
	
	public void refreshViewPager4(String number, String body, String date)
	{
        pullToRefreshPosition = -1;
        number = number.replace("(", "").replace(")", "").replace("-", "").replace(" ", "").replace("+1", "");
        MainActivity.notChanged = false;
        MainActivity.threadedLoad = false;
		int position = mViewPager.getCurrentItem();
		String currentNumber = inboxNumber.get(position);
		
		boolean flag = false;
		
		for (int i = 0; i < inboxNumber.size(); i++)
		{
			if (number.endsWith(findContactNumber(inboxNumber.get(i), this)))
			{
				inboxBody.add(0, body);
				inboxDate.add(0, date);
				inboxNumber.add(0, inboxNumber.get(i));
				threadIds.add(0, threadIds.get(i));
				group.add(0, group.get(i));
				msgCount.add(0, Integer.parseInt(msgCount.get(i)) + 1 + "");
				msgRead.add(0, "0");
				
				inboxBody.remove(i+1);
				inboxDate.remove(i+1);
				inboxNumber.remove(i+1);
				threadIds.remove(i+1);
				group.remove(i+1);
				msgCount.remove(i+1);
				msgRead.remove(i+1);
				
				flag = true;
				break;
			}
		}
		
		if (flag == true)
		{
            if (deviceType.equals("phone") || deviceType.equals("phablet2"))
            {
                menuAdapter = new MenuArrayAdapter(this, inboxBody, inboxDate, inboxNumber, mViewPager, threadIds, group, msgCount, msgRead);
                menuLayout.setAdapter(menuAdapter);
            } else
            {
                ListFragment newFragment = (ListFragment) getSupportFragmentManager().findFragmentById(R.id.menuList);
                newFragment.setListAdapter(new MenuArrayAdapter(this, inboxBody, inboxDate, inboxNumber, MainActivity.mViewPager, threadIds, group, msgCount, msgRead));
            }

            mSectionsPagerAdapter.notifyDataSetChanged();

			mViewPager = (ViewPager) findViewById(R.id.pager);
			mViewPager.setAdapter(mSectionsPagerAdapter);
			
			for (int i = 0; i < inboxNumber.size(); i++)
			{
				if (currentNumber.equals(inboxNumber.get(i)))
				{
					position = i;
					break;
				}
			}
			
			mViewPager.setCurrentItem(position, false);
			
			final ImageView glow = (ImageView) findViewById(R.id.newMessageGlow);
			glow.setVisibility(View.VISIBLE);
			
			if (MainActivity.animationReceived == 2)
			{
				glow.setAlpha((float)1);
				glow.setVisibility(View.VISIBLE);
				
				Animation fadeIn = new AlphaAnimation(0, (float).9);
				fadeIn.setInterpolator(new DecelerateInterpolator());
				fadeIn.setDuration(1000);
		
				Animation fadeOut = new AlphaAnimation((float).9, 0);
				fadeOut.setInterpolator(new AccelerateInterpolator());
				fadeOut.setStartOffset(1000);
				fadeOut.setDuration(1000);
		
				AnimationSet animation = new AnimationSet(false);
				animation.addAnimation(fadeIn);
				animation.addAnimation(fadeOut);
				
				animation.setAnimationListener(new AnimationListener() {

					@Override
					public void onAnimationEnd(Animation arg0) {
						glow.setAlpha((float)0);
						
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
			} else
			{
				glow.setAlpha((float)0);
				glow.setVisibility(View.GONE);
			}
		} else
		{
			refreshViewPager(true);
		}
		
		try
		{
			invalidateOptionsMenu();
		} catch (Exception e)
		{
			
		}
	}
	
	public static Uri insert(Context context, String[] to, String subject, byte[] imageBytes, String text)
	{
	    try
	    {           
	        Uri destUri = Uri.parse("content://mms");

	        // Get thread id
	        Set<String> recipients = new HashSet<String>();
	        recipients.addAll(Arrays.asList(to));
	        long thread_id = Telephony.Threads.getOrCreateThreadId(context, recipients);

	        // Create a dummy sms
	        ContentValues dummyValues = new ContentValues();
	        dummyValues.put("thread_id", thread_id);
	        dummyValues.put("body", "Dummy SMS body.");
	        Uri dummySms = context.getContentResolver().insert(Uri.parse("content://sms/sent"), dummyValues);

	        // Create a new message entry
	        long now = System.currentTimeMillis();
	        ContentValues mmsValues = new ContentValues();
	        mmsValues.put("thread_id", thread_id);
	        mmsValues.put("date", now/1000L);
	        mmsValues.put("msg_box", 4);
	        //mmsValues.put("m_id", System.currentTimeMillis());
	        mmsValues.put("read", true);
	        mmsValues.put("sub", subject);
	        mmsValues.put("sub_cs", 106);
	        mmsValues.put("ct_t", "application/vnd.wap.multipart.related");
	        
	        if (imageBytes != null)
	        {
	        	mmsValues.put("exp", imageBytes.length);
	        } else
	        {
	        	mmsValues.put("exp", 0);
	        }
	        
	        mmsValues.put("m_cls", "personal");
	        mmsValues.put("m_type", 128); // 132 (RETRIEVE CONF) 130 (NOTIF IND) 128 (SEND REQ)
	        mmsValues.put("v", 19);
	        mmsValues.put("pri", 129);
	        mmsValues.put("tr_id", "T"+ Long.toHexString(now));
	        mmsValues.put("resp_st", 128);

	        // Insert message
	        Uri res = context.getContentResolver().insert(destUri, mmsValues);
	        String messageId = res.getLastPathSegment().trim();

	        // Create part
	        if (imageBytes != null)
	        {
	        	createPartImage(context, messageId, imageBytes, "image/png");
	        }
	        
	        createPartText(context, messageId, text);

	        // Create addresses
	        for (String addr : to)
	        {
	            createAddr(context, messageId, addr);
	        }

	        //res = Uri.parse(destUri + "/" + messageId);

	        // Delete dummy sms
	        context.getContentResolver().delete(dummySms, null, null);

	        return res;
	    }
	    catch (Exception e)
	    {
	        e.printStackTrace();
	    }

	    return null;
	}

	public static Uri insert(Context context, String[] to, String subject, ArrayList<byte[]> imageBytes, ArrayList<String> mimeTypes, String text)
	{
	    try
	    {           
	        Uri destUri = Uri.parse("content://mms");

	        // Get thread id
	        Set<String> recipients = new HashSet<String>();
	        recipients.addAll(Arrays.asList(to));
	        long thread_id = Telephony.Threads.getOrCreateThreadId(context, recipients);

	        // Create a dummy sms
	        ContentValues dummyValues = new ContentValues();
	        dummyValues.put("thread_id", thread_id);
	        dummyValues.put("body", "Dummy SMS body.");
	        Uri dummySms = context.getContentResolver().insert(Uri.parse("content://sms/sent"), dummyValues);

	        // Create a new message entry
	        long now = System.currentTimeMillis();
	        ContentValues mmsValues = new ContentValues();
	        mmsValues.put("thread_id", thread_id);
	        mmsValues.put("date", now/1000L);
	        mmsValues.put("msg_box", 4);
	        //mmsValues.put("m_id", System.currentTimeMillis());
	        mmsValues.put("read", true);
	        mmsValues.put("sub", subject);
	        mmsValues.put("sub_cs", 106);
	        mmsValues.put("ct_t", "application/vnd.wap.multipart.related");
	        
	        if (imageBytes != null)
	        {
	        	mmsValues.put("exp", imageBytes.get(0).length);
	        } else
	        {
	        	mmsValues.put("exp", 0);
	        }
	        
	        mmsValues.put("m_cls", "personal");
	        mmsValues.put("m_type", 128); // 132 (RETRIEVE CONF) 130 (NOTIF IND) 128 (SEND REQ)
	        mmsValues.put("v", 19);
	        mmsValues.put("pri", 129);
	        mmsValues.put("tr_id", "T"+ Long.toHexString(now));
	        mmsValues.put("resp_st", 128);

	        // Insert message
	        Uri res = context.getContentResolver().insert(destUri, mmsValues);
	        String messageId = res.getLastPathSegment().trim();

	        // Create part
	        for (int i = 0; i < imageBytes.size(); i++)
	        {
	        	createPartImage(context, messageId, imageBytes.get(i), mimeTypes.get(i));
	        }
	        
	        createPartText(context, messageId, text);

	        // Create addresses
	        for (String addr : to)
	        {
	            createAddr(context, messageId, addr);
	        }

	        //res = Uri.parse(destUri + "/" + messageId);

	        // Delete dummy sms
	        context.getContentResolver().delete(dummySms, null, null);

	        return res;
	    }
	    catch (Exception e)
	    {
	        e.printStackTrace();
	    }

	    return null;
	}

	private static Uri createPartImage(Context context, String id, byte[] imageBytes, String mimeType) throws Exception
	{
	    ContentValues mmsPartValue = new ContentValues();
	    mmsPartValue.put("mid", id);
	    mmsPartValue.put("ct", mimeType);
	    mmsPartValue.put("cid", "<" + System.currentTimeMillis() + ">");
	    Uri partUri = Uri.parse("content://mms/" + id + "/part");
	    Uri res = context.getContentResolver().insert(partUri, mmsPartValue);

	    // Add data to part
	    OutputStream os = context.getContentResolver().openOutputStream(res);
	    ByteArrayInputStream is = new ByteArrayInputStream(imageBytes);
	    byte[] buffer = new byte[256];
	    for (int len=0; (len=is.read(buffer)) != -1;)
	    {
	        os.write(buffer, 0, len);
	    }
	    os.close();
	    is.close();

	    return res;
	}
	
	private static Uri createPartText(Context context, String id, String text) throws Exception
	{
	    ContentValues mmsPartValue = new ContentValues();
	    mmsPartValue.put("mid", id);
	    mmsPartValue.put("ct", "text/plain");
	    mmsPartValue.put("cid", "<" + System.currentTimeMillis() + ">");
	    mmsPartValue.put("text", text);
	    Uri partUri = Uri.parse("content://mms/" + id + "/part");
	    Uri res = context.getContentResolver().insert(partUri, mmsPartValue);

	    return res;
	}

	private static Uri createAddr(Context context, String id, String addr) throws Exception
	{
	    ContentValues addrValues = new ContentValues();
	    addrValues.put("address", addr);
	    addrValues.put("charset", "106");
	    addrValues.put("type", 151); // TO
	    Uri addrUri = Uri.parse("content://mms/"+ id +"/addr");
	    Uri res = context.getContentResolver().insert(addrUri, addrValues);

	    return res;
	}

    public static void setMobileDataEnabled(Context context, boolean enabled) {
        try {
            final ConnectivityManager conman = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            final Class conmanClass = Class.forName(conman.getClass().getName());
            final Field iConnectivityManagerField = conmanClass.getDeclaredField("mService");
            iConnectivityManagerField.setAccessible(true);
            final Object iConnectivityManager = iConnectivityManagerField.get(conman);
            final Class iConnectivityManagerClass = Class.forName(iConnectivityManager.getClass().getName());
            final Method setMobileDataEnabledMethod = iConnectivityManagerClass.getDeclaredMethod("setMobileDataEnabled", Boolean.TYPE);
            setMobileDataEnabledMethod.setAccessible(true);

            setMobileDataEnabledMethod.invoke(iConnectivityManager, enabled);
        } catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    public static Boolean isMobileDataEnabled(Context context){
        Object connectivityService = context.getSystemService(CONNECTIVITY_SERVICE);
        ConnectivityManager cm = (ConnectivityManager) connectivityService;

        try {
            Class<?> c = Class.forName(cm.getClass().getName());
            Method m = c.getDeclaredMethod("getMobileDataEnabled");
            m.setAccessible(true);
            return (Boolean)m.invoke(cm);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void ensureRouteToHost(String url, String proxy) throws IOException {
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        int inetAddr;
        if (!proxy.equals("")) {
            String proxyAddr = proxy;
            inetAddr = lookupHost(proxyAddr);
            if (inetAddr == -1) {
                throw new IOException("Cannot establish route for " + url + ": Unknown host");
            } else {
                if (!connMgr.requestRouteToHost(
                        ConnectivityManager.TYPE_MOBILE_MMS, inetAddr)) {
                    throw new IOException("Cannot establish route to proxy " + inetAddr);
                }
            }
        } else {
            Uri uri = Uri.parse(url);
            inetAddr = lookupHost(uri.getHost());
            if (inetAddr == -1) {
                throw new IOException("Cannot establish route for " + url + ": Unknown host");
            } else {
                if (!connMgr.requestRouteToHost(
                        ConnectivityManager.TYPE_MOBILE_MMS, inetAddr)) {
                    throw new IOException("Cannot establish route to " + inetAddr + " for " + url);
                }
            }
        }
    }

    public static int lookupHost(String hostname) {
        InetAddress inetAddress;
        try {
            inetAddress = InetAddress.getByName(hostname);
        } catch (UnknownHostException e) {
            return -1;
        }
        byte[] addrBytes;
        int addr;
        addrBytes = inetAddress.getAddress();
        addr = ((addrBytes[3] & 0xff) << 24)
                | ((addrBytes[2] & 0xff) << 16)
                | ((addrBytes[1] & 0xff) << 8)
                |  (addrBytes[0] & 0xff);
        return addr;
    }
	
	public void sendMMS(final String recipient, final MMSPart[] parts)
	{
		if (sharedPrefs.getBoolean("wifi_mms_fix", true))
		{
			WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
			currentWifi = wifi.getConnectionInfo();
			currentWifiState = wifi.isWifiEnabled();
			wifi.disconnect();
			discon = new DisconnectWifi();
			registerReceiver(discon, new IntentFilter(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION));
            currentDataState = isMobileDataEnabled(this);
            setMobileDataEnabled(this, true);
		}
		
		ConnectivityManager mConnMgr =  (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        final int result = mConnMgr.startUsingNetworkFeature(ConnectivityManager.TYPE_MOBILE, "enableMMS");
		
		if (result != 0)
		{
			IntentFilter filter = new IntentFilter();
			filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
			BroadcastReceiver receiver = new BroadcastReceiver() {
	
				@Override
				public void onReceive(Context context, Intent intent) {
					String action = intent.getAction();
					
					if (!action.equals(ConnectivityManager.CONNECTIVITY_ACTION))
					{
						return;
					}
					
					@SuppressWarnings("deprecation")
					NetworkInfo mNetworkInfo = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
					
					if ((mNetworkInfo == null) || (mNetworkInfo.getType() != ConnectivityManager.TYPE_MOBILE))
					{
						return;
					}
					
					if (!mNetworkInfo.isConnected())
					{
						return;
					} else
					{
						sendData(recipient, parts);
						
						unregisterReceiver(this);
					}
					
				}
				
			};
			
			registerReceiver(receiver, filter);
		} else
		{
			sendData(recipient, parts);
		}
	}
	
	public void sendData(final String recipient, final MMSPart[] parts)
	{
		final Context context = this;
		
		new Thread(new Runnable() {

			@Override
			public void run() {
				
				final SendReq sendRequest = new SendReq();
				
				String[] recipients = recipient.replace(";", "").split(" ");
				
				for (int i = 0; i < recipients.length; i++)
				{
					final EncodedStringValue[] phoneNumbers = EncodedStringValue.extract(recipients[i]);
					
					if (phoneNumbers != null && phoneNumbers.length > 0)
					{
						sendRequest.addTo(phoneNumbers[0]);
					}
				}
				
				final PduBody pduBody = new PduBody();
				
				if (parts != null)
				{
					for (MMSPart part : parts)
					{
						if (part != null)
						{
							try
							{
								final PduPart partPdu = new PduPart();
								partPdu.setName(part.Name.getBytes());
								partPdu.setContentType(part.MimeType.getBytes());
								partPdu.setData(part.Data);
								pduBody.addPart(partPdu);
							} catch (Exception e)
							{
								
							}
						}
					}
				}
				
				sendRequest.setBody(pduBody);
				
				final PduComposer composer = new PduComposer(context, sendRequest);
				final byte[] bytesToSend = composer.make();
				
				List<APN> apns = new ArrayList<APN>();
				
				try
				{
					APNHelper helper = new APNHelper(context);
					apns = helper.getMMSApns();
					
					final APN apn = apns.get(0);
					
					((Activity) context).getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {
						
						@Override
						public void run() {
							AlertDialog.Builder builder = new AlertDialog.Builder(context);
							builder.setTitle("System APNs");
							builder.setMessage("MMSC Url: " + apn.MMSCenterUrl + "\n" +
							                   "MMS Proxy: " + apn.MMSProxy + "\n" +
									           "MMS Port: " + apn.MMSPort + "\n");
							builder.create().show();
						}
					});
					
				} catch (Exception e)
				{
					APN apn = new APN(sharedPrefs.getString("mmsc_url", ""), sharedPrefs.getString("mms_port", ""), sharedPrefs.getString("mms_proxy", ""));
					apns.add(apn);
					
					String mmscUrl = apns.get(0).MMSCenterUrl != null ? apns.get(0).MMSCenterUrl.trim() : null;
					apns.get(0).MMSCenterUrl = mmscUrl;
					
					try
					{
						if (sharedPrefs.getBoolean("apn_username_password", false))
						{
							if (!sharedPrefs.getString("apn_username", "").equals("") && !sharedPrefs.getString("apn_username", "").equals(""))
							{
								String mmsc = apns.get(0).MMSCenterUrl;
								String[] parts = mmsc.split("://");
								String newMmsc = parts[0] + "://";
								
								newMmsc += sharedPrefs.getString("apn_username", "") + ":" + sharedPrefs.getString("apn_password", "") + "@";
								
								for (int i = 1; i < parts.length; i++)
								{
									newMmsc += parts[i];
								}
								
								apns.set(0, new APN(newMmsc, apns.get(0).MMSPort, apns.get(0).MMSProxy));
							}
						}
					} catch (Exception f)
					{
						((Activity) context).getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {
							
							@Override
							public void run() {
								Toast.makeText(context, "There may be an error in your username and password settings.", Toast.LENGTH_LONG).show();
							}
						});
					}
				}
				
				try {
                    ensureRouteToHost(apns.get(0).MMSCenterUrl, apns.get(0).MMSProxy);
					HttpUtils.httpConnection(context, 4444L, apns.get(0).MMSCenterUrl, bytesToSend, HttpUtils.HTTP_POST_METHOD, !TextUtils.isEmpty(apns.get(0).MMSProxy), apns.get(0).MMSProxy, Integer.parseInt(apns.get(0).MMSPort));
					
					IntentFilter filter = new IntentFilter();
					filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
					BroadcastReceiver receiver = new BroadcastReceiver() {
			
						@Override
						public void onReceive(Context context, Intent intent) {
							Cursor query = context.getContentResolver().query(Uri.parse("content://mms"), new String[] {"_id"}, null, null, "date desc");
							query.moveToFirst();
							String id = query.getString(query.getColumnIndex("_id"));
							query.close();
							
							ContentValues values = new ContentValues();
						    values.put("msg_box", 2);
						    String where = "_id" + " = '" + id + "'";
						    context.getContentResolver().update(Uri.parse("content://mms"), values, where, null);
						    
						    ((MainActivity) context).refreshViewPager3();
						    context.unregisterReceiver(this);
						    
						    if (sharedPrefs.getBoolean("wifi_mms_fix", true))
							{
                                try {
                                    context.unregisterReceiver(discon);
                                } catch (Exception e) {

                                }

							    WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
							    wifi.setWifiEnabled(false);
							    wifi.setWifiEnabled(currentWifiState);
							    Log.v("Reconnect", "" + wifi.reconnect());
                                setMobileDataEnabled(context, currentDataState);
							}
						}
						
					};
					
					registerReceiver(receiver, filter);
				} catch (Exception e) {
					
					if (sharedPrefs.getBoolean("wifi_mms_fix", true))
					{
                        try {
                            context.unregisterReceiver(discon);
                        } catch (Exception f) {

                        }

					    WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
					    wifi.setWifiEnabled(false);
					    wifi.setWifiEnabled(currentWifiState);
					    Log.v("Reconnect", "" + wifi.reconnect());
                        setMobileDataEnabled(context, currentDataState);
					}
					
					Cursor query = context.getContentResolver().query(Uri.parse("content://mms"), new String[] {"_id"}, null, null, "date desc");
					query.moveToFirst();
					String id = query.getString(query.getColumnIndex("_id"));
					query.close();
					
					ContentValues values = new ContentValues();
				    values.put("msg_box", 5);
				    String where = "_id" + " = '" + id + "'";
				    context.getContentResolver().update(Uri.parse("content://mms"), values, where, null);
				    
					((Activity) context).getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {
						
						@Override
						public void run() {
							((MainActivity) context).refreshViewPager3();
							
							AlertDialog.Builder builder = new AlertDialog.Builder(context);
							builder.setTitle(R.string.apn_error_title);
							builder.setMessage(context.getResources().getString(R.string.apn_error_1) + " " +
									           context.getResources().getString(R.string.apn_error_2) + " " +
									           context.getResources().getString(R.string.apn_error_3) + " " +
									           context.getResources().getString(R.string.apn_error_4) + " " +
									           context.getResources().getString(R.string.apn_error_5) +
									           context.getResources().getString(R.string.apn_error_6));
							builder.setNeutralButton(context.getResources().getString(R.string.apn_error_button), new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
                                    Intent intent = new Intent(context, SettingsPagerActivity.class);
                                    intent.putExtra("mms", true);
									context.startActivity(intent);
									
								}
								
							});
							
							builder.create().show();
						}
				    	
				    });
				}
				
			}
			
		}).start();
			
	}
	
	private void writeToFile2(ArrayList<String> data, Context context) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("notifications.txt", Context.MODE_PRIVATE));
            
            for (int i = 0; i < data.size(); i++)
            {
            	outputStreamWriter.write(data.get(i) + "\n");
            }
            	
            outputStreamWriter.close();
        }
        catch (IOException e) {
            
        } 
		
	}
	
	private ArrayList<String> readFromFile(Context context) {
		
	      ArrayList<String> ret = new ArrayList<String>();
	      
	      try {
	          InputStream inputStream = context.openFileInput("newMessages.txt");
	          
	          if ( inputStream != null ) {
	          	InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
	          	BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
	          	String receiveString = "";
	          	
	          	while ( (receiveString = bufferedReader.readLine()) != null ) {
	          		ret.add(receiveString);
	          	}
	          	
	          	inputStream.close();
	          }
	      }
	      catch (FileNotFoundException e) {
	      	
			} catch (IOException e) {
				
			}

	      return ret;
		}
	  	
	  	private void writeToFile(ArrayList<String> data, Context context) {
	        try {
	            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("newMessages.txt", Context.MODE_PRIVATE));
	            
	            for (int i = 0; i < data.size(); i++)
	            {
	            	outputStreamWriter.write(data.get(i) + "\n");
	            }
	            	
	            outputStreamWriter.close();
	        }
	        catch (IOException e) {
	            
	        } 
			
		}
	  	
	  	@SuppressWarnings("resource")
		private ArrayList<String> readFromFile4(Context context) {
			
		      ArrayList<String> ret = new ArrayList<String>();
		      
		      try {
		    	  InputStream inputStream;
		          
		          if (sharedPrefs.getBoolean("save_to_external", true))
		          {
		         	 inputStream = new FileInputStream(Environment.getExternalStorageDirectory() + "/SlidingMessaging/templates.txt");
		          } else
		          {
		        	  inputStream = context.openFileInput("templates.txt");
		          }
		          
		          if ( inputStream != null ) {
		          	InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
		          	BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
		          	String receiveString = "";
		          	
		          	while ( (receiveString = bufferedReader.readLine()) != null ) {
		          		ret.add(receiveString);
		          	}
		          	
		          	inputStream.close();
		          }
		      }
		      catch (FileNotFoundException e) {
		      	
				} catch (IOException e) {
					
				}

		      return ret;
			}

    PullToRefreshAttacher getPullToRefreshAttacher() {
        return mPullToRefreshAttacher;
    }

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends android.support.v13.app.FragmentStatePagerAdapter {
		
		public ArrayList<String> contact = null;
        private ArrayList<Fragment> mFragments = new ArrayList<Fragment>();
		
		public SectionsPagerAdapter(android.app.FragmentManager fm) {
			super(fm);
			
			new Thread(new Runnable() {

				@Override
				public void run() {
					ArrayList<String> contacts = new ArrayList<String>();
					
					for (int i = 0; i < inboxNumber.size(); i++)
					{
						contacts.add(loadGroupContacts(findContactNumber(inboxNumber.get(i), getBaseContext()), getBaseContext()));
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

                    for (int i = 0; i < inboxNumber.size(); i++)
                    {
                        contacts.add(loadGroupContacts(findContactNumber(inboxNumber.get(i), getBaseContext()), getBaseContext()));
                    }

                    contact = new ArrayList<String>();
                    contact = contacts;

                }

            }).start();

            super.notifyDataSetChanged();
        }

		@Override
		public DummySectionFragment getItem(int position) {
			DummySectionFragment fragment = new DummySectionFragment();
			Bundle args = new Bundle();
			
			args.putInt(DummySectionFragment.ARG_SECTION_NUMBER, position + 1);
			args.putInt("position", position);
			args.putStringArrayList("numbers", inboxNumber);
			args.putString("myId", myContactId);
			args.putString("myPhone", myPhoneNumber);
			args.putStringArrayList("threadIds", threadIds);
			args.putStringArrayList("group", group);
			fragment.setArguments(args);
			return fragment;
		}

		@Override
		public int getCount() {
            if (MainActivity.limitConversations) {
                if (inboxNumber.size() < 10) {
                    return inboxNumber.size();
                } else {
                    return 10;
                }
            } else {
                return inboxNumber.size();
            }
		}

		@Override
		public CharSequence getPageTitle(int position) {	
			try {
                String text = "No Messages";

                if (!sharedPrefs.getBoolean("hide_title_bar", true))
                {
                    return "";
                }

                if (contact == null)
                {
                    if (inboxNumber.size() >= 1)
                    {
                        if (group.get(position).equals("yes"))
                        {
                            if (sharedPrefs.getBoolean("title_caps", true))
                            {
                                text = "GROUP MMS";
                            } else
                            {
                                text = "Group MMS";
                            }
                        } else
                        {
                            if (sharedPrefs.getBoolean("title_caps", true))
                            {
                                if (sharedPrefs.getBoolean("always_show_contact_info", false))
                                {
                                    String[] names = findContactName(findContactNumber(inboxNumber.get(position), getBaseContext()), getBaseContext()).split(" ");
                                    text = names[0].trim().toUpperCase(Locale.getDefault());
                                } else
                                {
                                    text = findContactName(findContactNumber(inboxNumber.get(position), getBaseContext()), getBaseContext()).toUpperCase(Locale.getDefault());
                                }
                            } else
                            {
                                if (sharedPrefs.getBoolean("always_show_contact_info", false))
                                {
                                    try
                                    {
                                        String[] names = findContactName(findContactNumber(inboxNumber.get(position), getBaseContext()), getBaseContext()).split(" ");
                                        text = names[0].trim();
                                    } catch (Exception e)
                                    {
                                        text = findContactName(findContactNumber(inboxNumber.get(position), getBaseContext()), getBaseContext());
                                    }
                                } else
                                {
                                    text = findContactName(findContactNumber(inboxNumber.get(position), getBaseContext()), getBaseContext());
                                }
                            }
                        }
                    }

                    return text;
                } else
                {
                    try
                    {
                        if (contact.size() >= 1)
                        {
                            if (group.get(position).equals("yes"))
                            {
                                if (sharedPrefs.getBoolean("title_caps", true))
                                {
                                    text = "GROUP MMS";
                                } else
                                {
                                    text = "Group MMS";
                                }
                            } else
                            {
                                if (sharedPrefs.getBoolean("title_caps", true))
                                {
                                    if (sharedPrefs.getBoolean("always_show_contact_info", false))
                                    {
                                        try
                                        {
                                            String[] names = contact.get(position).split(" ");
                                            text = names[0].trim().toUpperCase(Locale.getDefault());
                                        } catch (Exception e)
                                        {
                                            text = contact.get(position).toUpperCase(Locale.getDefault());
                                        }
                                    } else
                                    {
                                        text = contact.get(position).toUpperCase(Locale.getDefault());
                                    }
                                } else
                                {
                                    if (sharedPrefs.getBoolean("always_show_contact_info", false))
                                    {
                                        try
                                        {
                                            String[] names = contact.get(position).split(" ");
                                            text = names[0].trim();
                                        } catch (Exception e)
                                        {
                                            text = contact.get(position);
                                        }
                                    } else
                                    {
                                        text = contact.get(position);
                                    }
                                }
                            }
                        }

                        return text;
                    } catch (Exception e)
                    {
                        if (contact.size() > 0)
                        {
                            return contact.get(position);
                        } else
                        {
                            return "No Messages";
                        }
                    }
                }
            } catch (Exception e) {
                return "";
            }
		}
	}

	/**
	 * A dummy fragment representing a section of the app, but that simply
	 * displays dummy text.
	 */
	public class DummySectionFragment extends android.app.Fragment implements android.app.LoaderManager.LoaderCallbacks<Cursor>, PullToRefreshAttacher.OnRefreshListener {
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		public static final String ARG_SECTION_NUMBER = "section_number";
		public ArrayList<String> threadIds;
		public int position;
		public ArrayList<String> numbers;
		public ArrayList<String> group;
		public String myId, myPhoneNumber;
		public View view;
		private SharedPreferences sharedPrefs;
		public Context context;
        public Cursor messageQuery;
        public CustomListView listView;

        public ProgressBar spinner;
        private PullToRefreshAttacher mPullToRefreshAttacher;
		
		public DummySectionFragment() {
			
		}
		
		@Override
		public void onSaveInstanceState(Bundle outState) {
		    
		}
		
		@Override
		public void onConfigurationChanged(Configuration newConfig) {
		  super.onConfigurationChanged(newConfig);
		}
		
		@Override
		public void onCreate(Bundle savedInstanceState)
		{
			super.onCreate(savedInstanceState);
			setRetainInstance(true);
			
			Bundle args = getArguments();
			
			this.position = args.getInt("position");
			this.numbers = args.getStringArrayList("numbers");
			this.myId = args.getString("myId");
			this.myPhoneNumber = args.getString("myPhone");
			this.threadIds = args.getStringArrayList("threadIds");
			this.group = args.getStringArrayList("group");
			
			this.sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		}
		
		@Override
		public void onAttach(Activity activity)
		{
			super.onAttach(activity);
			context = activity;
		}

        @Override
        public void onDetach()
        {
            super.onDetach();

            try
            {
                if (!sharedPrefs.getBoolean("cache_conversations", false) || !CacheService.cached || !MainActivity.notChanged || !(position < sharedPrefs.getInt("num_cache_conversations", 5))) {
                    messageQuery.close();
                }
            } catch (Exception e)
            {

            }
        }

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			
			view = inflater.inflate(R.layout.message_frame, container, false);

            mPullToRefreshAttacher = ((MainActivity) getActivity()).getPullToRefreshAttacher();

			return refreshMessages();
		}		
		
		@SuppressWarnings("deprecation")
		public View refreshMessages()
		{
			final ContentResolver contentResolver = context.getContentResolver();
			
			final TextView groupList = (TextView) view.findViewById(R.id.groupList);
			
			if (group.get(position).equals("yes"))
			{
				new Thread(new Runnable() {

					@Override
					public void run() {
						final String name = loadGroupContacts(findContactNumber(numbers.get(position), context), context);
						
						((Activity) context).getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {
							
							@Override
							public void run() {
								groupList.setText(name);
							}
						});
						
					}
					
				}).start();
				
				groupList.setTextColor(sharedPrefs.getInt("ct_titleBarTextColor", getResources().getColor(R.color.white)));
				
				if (!sharedPrefs.getBoolean("custom_theme", false))
	        	{
		        	String titleColor = sharedPrefs.getString("title_color", "blue");
					
					if (titleColor.equals("blue"))
					{
						groupList.setBackgroundColor(getResources().getColor(R.color.holo_blue));
					} else if (titleColor.equals("orange"))
					{
						groupList.setBackgroundColor(getResources().getColor(R.color.holo_orange));
					} else if (titleColor.equals("red"))
					{
						groupList.setBackgroundColor(getResources().getColor(R.color.holo_red));
					} else if (titleColor.equals("green"))
					{
						groupList.setBackgroundColor(getResources().getColor(R.color.holo_green));
					} else if (titleColor.equals("purple"))
					{
						groupList.setBackgroundColor(getResources().getColor(R.color.holo_purple));
					} else if (titleColor.equals("grey"))
					{
						groupList.setBackgroundColor(getResources().getColor(R.color.grey));
					} else if (titleColor.equals("black"))
					{
						groupList.setBackgroundColor(getResources().getColor(R.color.pitch_black));
					} else if (titleColor.equals("darkgrey"))
					{
						groupList.setBackgroundColor(getResources().getColor(R.color.darkgrey));
					}
	        	} else
	        	{
	        		groupList.setBackgroundColor(sharedPrefs.getInt("ct_titleBarColor", context.getResources().getColor(R.color.holo_blue)));
	        	}
			} else
			{
				groupList.setHeight(0);
			}
			
			listView = (CustomListView) view.findViewById(R.id.fontListView);

            spinner = (ProgressBar) view.findViewById(R.id.emptyView);

            if (MainActivity.waitToLoad)
            {
                spinner.setVisibility(View.VISIBLE);
            } else
            {
                spinner.setVisibility(View.GONE);
            }

            if (sharedPrefs.getBoolean("cache_conversations", false) && CacheService.cached && MainActivity.notChanged && position < sharedPrefs.getInt("num_cache_conversations", 5)) {
                MainActivity.threadedLoad = false;
            }

            MainActivity.numToLoad = 20;

            if (MainActivity.threadedLoad)
            {
                if (MainActivity.waitToLoad)
                {
                    new Thread(new Runnable() {

                        @Override
                        public void run() {
                            try
                            {
                                Thread.sleep(500);
                            } catch (Exception e)
                            {

                            }

                            MainActivity.waitToLoad = false;

                            Uri uri3 = Uri.parse("content://mms-sms/conversations/" + threadIds.get(position) + "/");
                            String[] projection2;
                            String proj = "_id body date type read msg_box";

                            if (sharedPrefs.getBoolean("show_original_timestamp", false))
                            {
                                proj += " date_sent";
                            }

                            if (sharedPrefs.getBoolean("delivery_reports", false)) {
                                proj += " status";
                            }

                            projection2 = proj.split(" ");

                            String sortOrder = "normalized_date desc";

                            if (sharedPrefs.getBoolean("limit_messages", true))
                            {
                                sortOrder += " limit " + MainActivity.numToLoad;
                            }

                            messageQuery = contentResolver.query(uri3, projection2, null, null, sortOrder);

                            ((Activity) context).getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {

                                @Override
                                public void run() {

                                    com.klinker.android.messaging_hangout.MessageCursorAdapter adapter = new com.klinker.android.messaging_hangout.MessageCursorAdapter((Activity) context, myId, findContactNumber(numbers.get(position), context), threadIds.get(position), messageQuery, position);

                                    listView.setAdapter(adapter);
                                    listView.setStackFromBottom(true);
                                    spinner.setVisibility(View.GONE);

                                    listView.setOnSizeChangedListener(new CustomListView.OnSizeChangedListener() {
                                        public void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
                                            smoothScrollToEnd(false, height - oldHeight);
                                        }
                                    });
                                }

                            });

                            try
                            {
                                Thread.sleep(500);
                            } catch (Exception e)
                            {

                            }

                            ((Activity) context).getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {

                                @Override
                                public void run() {
                                    try
                                    {
                                        listView.setSelection(messageQuery.getCount() - 1);
                                    } catch (Exception e)
                                    {

                                    }
                                }

                            });

                        }

                    }).start();
                } else
                {
                    getLoaderManager().restartLoader(position, null, this);
                }
            } else
            {
                getLoaderManager().destroyLoader(position);

                Uri uri3 = Uri.parse("content://mms-sms/conversations/" + threadIds.get(position) + "/");
                String[] projection2;
                String proj = "_id body date type read msg_box";

                if (sharedPrefs.getBoolean("show_original_timestamp", false))
                {
                    proj += " date_sent";
                }

                if (sharedPrefs.getBoolean("delivery_reports", false)) {
                    proj += " status";
                }

                projection2 = proj.split(" ");

                String sortOrder = "normalized_date desc";

                if (sharedPrefs.getBoolean("limit_messages", true))
                {
                    sortOrder += " limit " + MainActivity.numToLoad;
                }

                if (!sharedPrefs.getBoolean("cache_conversations", false) || !CacheService.cached || !MainActivity.notChanged || !(position < sharedPrefs.getInt("num_cache_conversations", 5))) {
                    messageQuery = contentResolver.query(uri3, projection2, null, null, sortOrder);
                } else {
                    messageQuery = CacheService.conversations.get(position);
                }

                com.klinker.android.messaging_hangout.MessageCursorAdapter adapter = new com.klinker.android.messaging_hangout.MessageCursorAdapter((Activity) context, myId, findContactNumber(numbers.get(position), context), threadIds.get(position), messageQuery, position);

                listView.setAdapter(adapter);
                listView.setStackFromBottom(true);
                spinner.setVisibility(View.GONE);

                listView.setOnSizeChangedListener(new CustomListView.OnSizeChangedListener() {
                    public void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
                        smoothScrollToEnd(false, height - oldHeight);
                    }
                });

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        MainActivity.threadedLoad = true;
                    }
                }, 100L);
            }

			listView.setDivider(new ColorDrawable(sharedPrefs.getInt("ct_messageDividerColor", context.getResources().getColor(R.color.light_silver))));
			
			if (sharedPrefs.getBoolean("ct_messageDividerVisibility", true) && sharedPrefs.getString("run_as", "sliding").equals("sliding"))
			{
				listView.setDividerHeight(1);
			} else
			{
				listView.setDividerHeight(0);
			}

            final PullToRefreshAttacher.OnRefreshListener refreshListener = this;

            listView.setOnScrollListener(new AbsListView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView absListView, int i) {
                    if (position != MainActivity.pullToRefreshPosition) {
                        mPullToRefreshAttacher.setRefreshableView(listView, refreshListener);
                        MainActivity.pullToRefreshPosition = position;
                    }
                }

                @Override
                public void onScroll(AbsListView absListView, int i, int i2, int i3) {

                }
            });
			
			return view;
		}
		
		public InputStream openDisplayPhoto(long contactId) {
			Uri contactUri = ContentUris.withAppendedId(Contacts.CONTENT_URI, contactId);
		     Uri photoUri = Uri.withAppendedPath(contactUri, Contacts.Photo.CONTENT_DIRECTORY);
		     Cursor cursor = getActivity().getContentResolver().query(photoUri,
		          new String[] {Contacts.Photo.PHOTO}, null, null, null);
		     if (cursor == null) {
		         return null;
		     }
		     try {
		         if (cursor.moveToFirst()) {
		             byte[] data = cursor.getBlob(0);
		             if (data != null) {
		                 return new ByteArrayInputStream(data);
		             }
		         }
		     } finally {
		         cursor.close();
		     }
		     return null;
		 }

        @Override
        public android.content.Loader<Cursor> onCreateLoader(int loaderID, Bundle bundle)
        {
            Uri uri3 = Uri.parse("content://mms-sms/conversations/" + threadIds.get(position) + "/");
            String[] projection2;
            String proj = "_id body date type read msg_box";

            if (sharedPrefs.getBoolean("show_original_timestamp", false))
            {
                proj += " date_sent";
            }

            if (sharedPrefs.getBoolean("delivery_reports", false)) {
                proj += " status";
            }

            projection2 = proj.split(" ");

            String sortOrder = "normalized_date desc";

            if (sharedPrefs.getBoolean("limit_messages", true))
            {
                sortOrder += " limit " + MainActivity.numToLoad;
            }

            return new android.content.CursorLoader(
                    context,
                    uri3,
                    projection2,
                    null,
                    null,
                    sortOrder
            );
        }

        @Override
        public void onLoadFinished(android.content.Loader<Cursor> loader, final Cursor query)
        {
            com.klinker.android.messaging_hangout.MessageCursorAdapter adapter = new com.klinker.android.messaging_hangout.MessageCursorAdapter((Activity) context, myId, findContactNumber(numbers.get(position), context), threadIds.get(position), query, position);

            listView.setAdapter(adapter);
            listView.setStackFromBottom(true);
            spinner.setVisibility(View.GONE);

            listView.setOnSizeChangedListener(new CustomListView.OnSizeChangedListener() {
                public void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
                    smoothScrollToEnd(false, height - oldHeight);
                }
            });

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        listView.setSelection(query.getCount() - 1);
                    } catch (Exception e) {

                    }
                }
            }, 500);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader)
        {

        }

        @Override
        public void onRefreshStarted(View view) {

            MainActivity.numToLoad += 20;

            new AsyncTask<Void, Void, Void>() {

                private Cursor query;

                @Override
                protected Void doInBackground(Void... params) {
                    long startTime = Calendar.getInstance().getTimeInMillis();

                    Uri uri3 = Uri.parse("content://mms-sms/conversations/" + threadIds.get(position) + "/");
                    String[] projection2;
                    String proj = "_id body date type read msg_box";

                    if (sharedPrefs.getBoolean("show_original_timestamp", false))
                    {
                        proj += " date_sent";
                    }

                    if (sharedPrefs.getBoolean("delivery_reports", false)) {
                        proj += " status";
                    }

                    projection2 = proj.split(" ");

                    String sortOrder = "normalized_date desc";

                    if (sharedPrefs.getBoolean("limit_messages", true))
                    {
                        sortOrder += " limit " + MainActivity.numToLoad;
                    }

                    query = context.getContentResolver().query(uri3, projection2, null, null, sortOrder);

                    // time for a cool animation ;)
                    if (Calendar.getInstance().getTimeInMillis() - startTime < 500) {
                        try {
                            Thread.sleep(500);
                        } catch (Exception e) {

                        }
                    }

                    return null;
                }

                @Override
                protected void onPostExecute(Void result) {
                    super.onPostExecute(result);

                    com.klinker.android.messaging_hangout.MessageCursorAdapter adapter = new com.klinker.android.messaging_hangout.MessageCursorAdapter((Activity) context, myId, findContactNumber(numbers.get(position), context), threadIds.get(position), query, position);

                    listView.setAdapter(adapter);
                    listView.setStackFromBottom(true);
                    spinner.setVisibility(View.GONE);

                    listView.setOnSizeChangedListener(new CustomListView.OnSizeChangedListener() {
                        public void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
                            smoothScrollToEnd(false, height - oldHeight);
                        }
                    });

                    listView.setSelection(adapter.getCount() - MainActivity.numToLoad + 20);

                    // Notify PullToRefreshAttacher that the refresh has finished
                    mPullToRefreshAttacher.setRefreshComplete();
                }
            }.execute();
        }

        private int mLastSmoothScrollPosition;

        private void smoothScrollToEnd(boolean force, int listSizeChange) {
            int lastItemVisible = listView.getLastVisiblePosition();
            int lastItemInList = listView.getAdapter().getCount() - 1;
            if (lastItemVisible < 0 || lastItemInList < 0) {
                return;
            }

            View lastChildVisible =
                    listView.getChildAt(lastItemVisible - listView.getFirstVisiblePosition());
            int lastVisibleItemBottom = 0;
            int lastVisibleItemHeight = 0;
            if (lastChildVisible != null) {
                lastVisibleItemBottom = lastChildVisible.getBottom();
                lastVisibleItemHeight = lastChildVisible.getHeight();
            }

            int listHeight = listView.getHeight();
            boolean lastItemTooTall = lastVisibleItemHeight > listHeight;
            boolean willScroll = force ||
                    ((listSizeChange != 0 || lastItemInList != mLastSmoothScrollPosition) &&
                            lastVisibleItemBottom + listSizeChange <=
                                    listHeight - listView.getPaddingBottom());
            if (willScroll || (lastItemTooTall && lastItemInList == lastItemVisible)) {
                if (Math.abs(listSizeChange) > 200) {
                    if (lastItemTooTall) {
                        listView.setSelectionFromTop(lastItemInList,
                                listHeight - lastVisibleItemHeight);
                    } else {
                        listView.setSelection(lastItemInList);
                    }
                } else if (lastItemInList - lastItemVisible > 20) {
                    listView.setSelection(lastItemInList);
                } else {
                    if (lastItemTooTall) {
                        listView.setSelectionFromTop(lastItemInList,
                                listHeight - lastVisibleItemHeight);
                    } else {
                        listView.smoothScrollToPosition(lastItemInList);
                    }
                    mLastSmoothScrollPosition = lastItemInList;
                }
            }
        }
	}
	
	public static Bitmap getFacebookPhoto(String phoneNumber, Context context) {
		  try
		  {
		    Uri phoneUri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
		    Uri photoUri;
		    ContentResolver cr = context.getContentResolver();
		    Cursor contact = cr.query(phoneUri,
		            new String[] { ContactsContract.Contacts._ID }, null, null, null);

		    try
		    {
			    if (contact.moveToFirst()) {
			        long userId = contact.getLong(contact.getColumnIndex(ContactsContract.Contacts._ID));
			        photoUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, userId);
			        contact.close();
			    }
			    else {
			        Bitmap defaultPhoto = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_contact_picture);
			        
			        contact.close();
			        return defaultPhoto;
			    }
			    if (photoUri != null) {
			        InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(
			                cr, photoUri);
			        if (input != null) {
			        	contact.close();
			            return BitmapFactory.decodeStream(input);
			        }
			    } else {
			        Bitmap defaultPhoto = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_contact_picture);
			        
			        contact.close();
			        return defaultPhoto;
			    }
			    Bitmap defaultPhoto = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_contact_picture);
		        
		        contact.close();
			    return defaultPhoto;
		    } catch (Exception e)
		    {
		        	contact.close();
		        	return BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_contact_picture);
		    }
		  } catch (Exception e)
		  {
		        	return BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_contact_picture);
		  }
		}
	
	public static String[] splitByLength(String s, int chunkSize, boolean counter)
  	{
  		int arraySize = (int) Math.ceil((double) s.length() / chunkSize);

  	    String[] returnArray = new String[arraySize];

  	    int index = 0;
  	    for(int i = 0; i < s.length(); i = i+chunkSize)
  	    {
  	        if(s.length() - i < chunkSize)
  	        {
  	            returnArray[index++] = s.substring(i);
  	        } 
  	        else
  	        {
  	            returnArray[index++] = s.substring(i, i+chunkSize);
  	        }
  	    }

        if (counter) {
            for (int i = 0; i < returnArray.length; i++) {
                returnArray[i] = "(" + (i+1) + "/" + returnArray.length + ") " + returnArray[i];
            }
        }

  	    return returnArray;
  	}

    public void setIcon(NotificationCompat.Builder mBuilder)
    {
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

    public boolean individualNotification(NotificationCompat.Builder mBuilder, String name, Context context)
    {
        ArrayList<IndividualSetting> individuals = readFromFile5(context);

        for (int i = 0; i < individuals.size(); i++)
        {
            if (individuals.get(i).name.equals(name))
            {
                mBuilder.setSound(Uri.parse(individuals.get(i).ringtone));

                try
                {
                    String[] vibPat = individuals.get(i).vibratePattern.replace("L", "").split(", ");
                    long[] pattern = new long[vibPat.length];

                    for (int j = 0; j < vibPat.length; j++)
                    {
                        pattern[j] = Long.parseLong(vibPat[j]);
                    }

                    mBuilder.setVibrate(pattern);
                } catch (Exception e)
                {
                    e.printStackTrace();
                }

                mBuilder.setLights(individuals.get(i).color, sharedPrefs.getInt("led_on_time", 1000), sharedPrefs.getInt("led_off_time", 2000));

                return true;
            }
        }

        return false;
    }

    private ArrayList<IndividualSetting> readFromFile5(Context context) {

        ArrayList<IndividualSetting> ret = new ArrayList<IndividualSetting>();

        try {
            InputStream inputStream;

            if (sharedPrefs.getBoolean("save_to_external", true))
            {
                inputStream = new FileInputStream(Environment.getExternalStorageDirectory() + "/SlidingMessaging/individualNotifications.txt");
            } else
            {
                inputStream = context.openFileInput("individualNotifications.txt");
            }

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";

                while ( (receiveString = bufferedReader.readLine()) != null) {
                    ret.add(new IndividualSetting(receiveString, Integer.parseInt(bufferedReader.readLine()), bufferedReader.readLine(), bufferedReader.readLine()));
                }

                inputStream.close();
            }
        }
        catch (FileNotFoundException e) {

        } catch (IOException e) {

        }

        return ret;
    }

    public ArrayList<BlacklistContact> readFromFile6(Context context) {

        ArrayList<BlacklistContact> ret = new ArrayList<BlacklistContact>();

        try {
            InputStream inputStream;

            if (sharedPrefs.getBoolean("save_to_external", true))
            {
                inputStream = new FileInputStream(Environment.getExternalStorageDirectory() + "/SlidingMessaging/blacklist.txt");
            } else
            {
                inputStream = context.openFileInput("blacklist.txt");
            }

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";

                while ( (receiveString = bufferedReader.readLine()) != null) {
                    ret.add(new BlacklistContact(receiveString, Integer.parseInt(bufferedReader.readLine())));
                }

                inputStream.close();
            }
        }
        catch (FileNotFoundException e) {

        } catch (IOException e) {

        }

        return ret;
    }


}
