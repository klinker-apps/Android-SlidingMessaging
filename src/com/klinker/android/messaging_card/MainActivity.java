package com.klinker.android.messaging_card;

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
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.graphics.*;
import android.view.*;
import android.widget.*;
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
import com.klinker.android.messaging_sliding.batch_delete.BatchDeleteAllActivity;
import com.klinker.android.messaging_card.group.GroupActivity;
import com.klinker.android.messaging_donate.*;
import com.klinker.android.messaging_donate.R;
import com.klinker.android.messaging_donate.receivers.DeliveredReceiver;
import com.klinker.android.messaging_donate.receivers.DisconnectWifi;
import com.klinker.android.messaging_donate.receivers.SentReceiver;
import com.klinker.android.messaging_donate.settings.SettingsPagerActivity;
import com.klinker.android.messaging_sliding.*;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.ExifInterface;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.media.ThumbnailUtils;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.provider.Telephony;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.PhoneLookup;
import android.provider.ContactsContract.Profile;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.text.Editable;
import android.text.InputType;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.View.OnClickListener;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Animation.AnimationListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout.LayoutParams;
import com.klinker.android.messaging_sliding.custom_dialogs.CustomListView;
import com.klinker.android.messaging_sliding.emojis.EmojiAdapter;
import com.klinker.android.messaging_sliding.emojis.EmojiAdapter2;
import com.klinker.android.messaging_sliding.emojis.EmojiConverter;
import com.klinker.android.messaging_sliding.emojis.EmojiConverter2;
import com.klinker.android.messaging_sliding.receivers.CacheService;
import com.klinker.android.messaging_sliding.receivers.NotificationReceiver;
import com.klinker.android.messaging_sliding.receivers.NotificationRepeaterService;
import com.klinker.android.messaging_sliding.receivers.QuickTextService;
import com.klinker.android.messaging_sliding.security.PasswordActivity;
import com.klinker.android.messaging_sliding.security.PinActivity;
import com.klinker.android.messaging_sliding.templates.TemplateArrayAdapter;

import group.pals.android.lib.ui.lockpattern.LockPatternActivity;
import group.pals.android.lib.ui.lockpattern.prefs.SecurityPrefs;
import wizardpager.ChangeLogMain;

public class MainActivity extends FragmentActivity implements PopupMenu.OnMenuItemClickListener {

	public static SharedPreferences sharedPrefs;
	
	// Custom Action Bar Buttons
	public ImageButton menuButton;
	public ImageButton attachButton;
	public ImageButton newMessageButton;
	public ImageButton backButton;
	public ImageButton callButton;
	public TextView contactName;
	
	public View sendBarBackground;
	public ImageButton emojiButton;
	public static EditText messageEntry;
	public ImageButton sendButton;
	public TextView charsRemaining;
	public static ImageAttachmentView attachImageView;
	
	public Uri capturedPhotoUri;
	public boolean fromCamera = false;
	public static Uri attachedImage;
	public int attachedPosition;
	public boolean multipleAttachments = false;
	
	public BroadcastReceiver receiver;
	public BroadcastReceiver mmsReceiver;
    public BroadcastReceiver killReceiver;
	
	public DisconnectWifi discon;
	public WifiInfo currentWifi;
	public boolean currentWifiState;
    public boolean currentDataState;
	
	public ContactPagerAdapter contactPagerAdapter;
	public static MessagePagerAdapter messagePagerAdapter;
	public ContactViewPager contactPager;
	public static ViewPager messagePager;
	
	public boolean firstRun = true;
	public boolean fromDashclock = false;
	public boolean sendTo = false;
	public String sendMessageTo;
	public String whatToSend = null;
	public String sendToThread = null;
	public String sendToMessage;
	
	public ArrayList<String> inboxNumber;
	public ArrayList<String> inboxDate;
	public ArrayList<String> inboxBody;
	public ArrayList<String> inboxId;
	public static ArrayList<String> threadIds;
	public ArrayList<Boolean> inboxSent, mms;
	public ArrayList<String> images;
	public ArrayList<String> group;
	public ArrayList<String> msgCount;
	public ArrayList<String> msgRead;
	
	public static String myPhoneNumber, myContactId;
	public static Bitmap myPicture;

    public static boolean notChanged = true;

    public ArrayList<String> drafts, draftNames;
    public ArrayList<Boolean> draftChanged;
    public ArrayList<String> draftsToDelete;
    public boolean fromDraft = false;
    public String newDraft = "";
	
	public boolean refreshMyContact = true;
	public static ArrayList<String> contactNames;
	public static ArrayList<String> contactNumbers;
	public static ArrayList<String> contactTypes;
	public static boolean firstContactSearch = true;
	
	public static boolean animationOn = false;
	public static int animationReceived = 0;
	public static int animationThread = -1;
	public static boolean sentMessage = false;
	
	public static boolean isFastScrolling = false;
	public static int scrollTo = 0;
	public static boolean waitMessagePager = true;
    public static int loadAllMessagesPosition = -1;
    public static boolean loadAllMessages = false;
	
	public static boolean fromNewMessageButton = false;
    public static boolean limitConversations = true;
	
	public static int currentMessageTag = 1;
	
	public static EditText contactEntry;
	
	public static Typeface font;

    private static final int REQ_ENTER_PATTERN = 7;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED, WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);

        MainActivity.notChanged = true;

		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		if (sharedPrefs.getString("card_theme", "Light").equals("Light"))
		{
			if (sharedPrefs.getBoolean("top_actionbar", false))
			{
				setContentView(R.layout.card_main_abtop);
			} else
			{
				setContentView(R.layout.card_main);
			}
			
			setTheme(R.style.cardThemeLight);
		} else if (sharedPrefs.getString("card_theme", "Light").equals("Dark"))
		{
			if (sharedPrefs.getBoolean("top_actionbar", false))
			{
				setContentView(R.layout.card_main_abtop_dark);
			} else
			{
				setContentView(R.layout.card_main_dark);
			}
			
			setTheme(R.style.cardTheme);
		} else if (sharedPrefs.getString("card_theme", "Light").equals("Pitch Black"))
		{
			if (sharedPrefs.getBoolean("top_actionbar", false))
			{
				setContentView(R.layout.card_main_abtop_black);
			} else
			{
				setContentView(R.layout.card_main_black);
			}
			
			setTheme(R.style.cardTheme);
		}
		
		getWindow().setBackgroundDrawable(null);

        killReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                ((Activity) context).finish();
            }
        };
		
		receiver = new BroadcastReceiver() {
		    @Override
		    public void onReceive(Context context, Intent intent) {
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
			        
			        Calendar cal = Calendar.getInstance();
			        ContentValues values = new ContentValues();
			        values.put("address", address);
			        values.put("body", body);
			        values.put("date", cal.getTimeInMillis() + "");
			        values.put("read", false);
			        values.put("date_sent", date);
			        getContentResolver().insert(Uri.parse("content://sms/inbox"), values);
			        
			        if (sharedPrefs.getBoolean("notifications", true))
			        {
				        AudioManager am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
	
				        switch (am.getRingerMode()) {
				            case AudioManager.RINGER_MODE_SILENT:
				                break;
				            case AudioManager.RINGER_MODE_VIBRATE:
				            	if (sharedPrefs.getBoolean("vibrate", true))
						        {
						        	Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
						        	
						        	if (!sharedPrefs.getBoolean("custom_vibrate_pattern", false))
						        	{
							        	String vibPat = sharedPrefs.getString("vibrate_pattern", "2short");
							        	
							        	if (vibPat.equals("short"))
							        	{
							        		long[] pattern = {0L, 400L};
							        		vibrator.vibrate(pattern, -1);
							        	} else if (vibPat.equals("long"))
							        	{
							        		long[] pattern = {0L, 800L};
							        		vibrator.vibrate(pattern, -1);
							        	} else if (vibPat.equals("2short"))
							        	{
							        		long[] pattern = {0L, 400L, 100L, 400L};
							        		vibrator.vibrate(pattern, -1);
							        	} else if (vibPat.equals("2long"))
							        	{
							        		long[] pattern = {0L, 800L, 200L, 800L};
							        		vibrator.vibrate(pattern, -1);
							        	} else if (vibPat.equals("3short"))
							        	{
							        		long[] pattern = {0L, 400L, 100L, 400L, 100L, 400L};
							        		vibrator.vibrate(pattern, -1);
							        	} else if (vibPat.equals("3long"))
							        	{
							        		long[] pattern = {0L, 800L, 200L, 800L, 200L, 800L};
							        		vibrator.vibrate(pattern, -1);
							        	}
						        	} else
						        	{
						        		try
						        		{
							        		String[] vibPat = sharedPrefs.getString("set_custom_vibrate_pattern", "0, 100, 100, 100").split(", ");
							        		long[] pattern = new long[vibPat.length];
							        		
							        		for (int i = 0; i < vibPat.length; i++)
							        		{
							        			pattern[i] = Long.parseLong(vibPat[i]);
							        		}
							        		
							        		vibrator.vibrate(pattern, -1);
						        		} catch (Exception e)
						        		{
						        			
						        		}
						        	}
						        }
				            	
				                break;
				            case AudioManager.RINGER_MODE_NORMAL:
				            	try
				            	{
					            	Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
					            	
					            	try
							        {
							        	notification = (Uri.parse(sharedPrefs.getString("ringtone", "null")));
							        } catch(Exception e)
							        {
							        	notification = (RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
							        }
					            	
					            	Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
					            	r.play();
					            	
					            	if (sharedPrefs.getBoolean("vibrate", true))
							        {
							        	Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
							        	
							        	if (!sharedPrefs.getBoolean("custom_vibrate_pattern", false))
							        	{
								        	String vibPat = sharedPrefs.getString("vibrate_pattern", "2short");
								        	
								        	if (vibPat.equals("short"))
								        	{
								        		long[] pattern = {0L, 400L};
								        		vibrator.vibrate(pattern, -1);
								        	} else if (vibPat.equals("long"))
								        	{
								        		long[] pattern = {0L, 800L};
								        		vibrator.vibrate(pattern, -1);
								        	} else if (vibPat.equals("2short"))
								        	{
								        		long[] pattern = {0L, 400L, 100L, 400L};
								        		vibrator.vibrate(pattern, -1);
								        	} else if (vibPat.equals("2long"))
								        	{
								        		long[] pattern = {0L, 800L, 200L, 800L};
								        		vibrator.vibrate(pattern, -1);
								        	} else if (vibPat.equals("3short"))
								        	{
								        		long[] pattern = {0L, 400L, 100L, 400L, 100L, 400L};
								        		vibrator.vibrate(pattern, -1);
								        	} else if (vibPat.equals("3long"))
								        	{
								        		long[] pattern = {0L, 800L, 200L, 800L, 200L, 800L};
								        		vibrator.vibrate(pattern, -1);
								        	}
							        	} else
							        	{
							        		try
							        		{
								        		String[] vibPat = sharedPrefs.getString("set_custom_vibrate_pattern", "0, 100, 100, 100").split(", ");
								        		long[] pattern = new long[vibPat.length];
								        		
								        		for (int i = 0; i < vibPat.length; i++)
								        		{
								        			pattern[i] = Long.parseLong(vibPat[i]);
								        		}
								        		
								        		vibrator.vibrate(pattern, -1);
							        		} catch (Exception e)
							        		{
							        			
							        		}
							        	}
							        }
				            	} catch (Exception e)
				            	{
				            		
				            	}
				            	
				                break;
				        }
			        }
			        
			        try
			        {
				        if (address.replace(" ", "").replace("(","").replace("(","").replace("-","").endsWith(findContactNumber(inboxNumber.get(messagePager.getCurrentItem() - 1), context).replace(" ", "").replace("(","").replace("(","").replace("-","")))
				        {
				        	animationReceived = 1;
				        	animationThread = messagePager.getCurrentItem() - 1;
				        } else
				        {
				        	animationReceived = 2;
				        }
			        } catch (Exception e)
			        {
			        	animationReceived = 0;
			        }

                    notChanged = false;
			        
		        	refreshViewPager4(address, body, date);
		        	
		        	Intent updateWidget = new Intent("com.klinker.android.messaging.UPDATE_WIDGET");
					context.sendBroadcast(updateWidget);
					
					abortBroadcast();
		        }
		};
		
		mmsReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context arg0, Intent arg1) {
				String currentThread = threadIds.get(messagePager.getCurrentItem());
				
				refreshViewPager(true);
				
				for (int i = 0; i < threadIds.size(); i++)
				{
					if (currentThread.equals(threadIds.get(i)))
					{
						messagePager.setCurrentItem(i, false);
						break;
					}
				}
				
			}
			
		};
		
		Intent intent = getIntent();
		Bundle b = intent.getExtras();
		
		if (b != null)
		{
			if (b.getBoolean("dashclock"))
			{
				fromDashclock = true;
			}
		}
		
		String action = intent.getAction();
		
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
						fromDashclock = false;
					} else
					{
						sendMessageTo = Uri.decode(intent.getDataString()).substring("sms:".length()).replace("(", "").replace(")", "").replace("-", "").replace(" ", "");
						fromDashclock = false;
					}
				} catch (Exception e)
				{
                    try {
                        sendMessageTo = intent.getStringExtra("com.klinker.android.OPEN").replace("(", "").replace(")", "").replace("-", "").replace(" ", "");
                        fromDashclock = true;
                    } catch (Exception f) {

                    }
				}
				
				attachedImage = null;
			} else if (action.equals(Intent.ACTION_SEND))
			{
				Bundle extras = intent.getExtras();
				
				if (extras.containsKey(Intent.EXTRA_TEXT))
				{
					whatToSend = (String) extras.getCharSequence(Intent.EXTRA_TEXT);
				}
				
				if (extras.containsKey(Intent.EXTRA_STREAM))
				{
					sendTo = true;
					sendMessageTo = "";
					fromDashclock = false;
					attachedImage = intent.getParcelableExtra(Intent.EXTRA_STREAM);
				}
			}
		} else
		{
			attachedImage = null;
			
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

        if (sharedPrefs.getBoolean("limit_conversations_start", true)) {
            limitConversations = true;
        } else {
            limitConversations = false;
        }
		
		menuButton = (ImageButton) findViewById(R.id.menuButton);
		attachButton = (ImageButton) findViewById(R.id.attachButton);
		newMessageButton = (ImageButton) findViewById(R.id.newMessageButton);
		backButton = (ImageButton) findViewById(R.id.backButton);
		callButton = (ImageButton) findViewById(R.id.callButton);
		contactName = (TextView) findViewById(R.id.contactText);
		
		if (sharedPrefs.getString("card_theme", "Light").equals("Light"))
		{
			menuButton.setColorFilter(getResources().getColor(R.color.card_action_button));
			attachButton.setColorFilter(getResources().getColor(R.color.card_action_button));
			newMessageButton.setColorFilter(getResources().getColor(R.color.card_action_button));
			backButton.setColorFilter(getResources().getColor(R.color.card_action_button));
			callButton.setColorFilter(getResources().getColor(R.color.card_action_button));
			
			menuButton.setAlpha((float).75);
			attachButton.setAlpha((float).75);
			newMessageButton.setAlpha((float).75);
			backButton.setAlpha((float).75);
			callButton.setAlpha((float).75);
			contactName.setAlpha((float).75);
		} else if (sharedPrefs.getString("card_theme", "Light").equals("Dark"))
		{
			menuButton.setColorFilter(getResources().getColor(R.color.card_dark_action_button));
			attachButton.setColorFilter(getResources().getColor(R.color.card_dark_action_button));
			newMessageButton.setColorFilter(getResources().getColor(R.color.card_dark_action_button));
			backButton.setColorFilter(getResources().getColor(R.color.card_dark_action_button));
			callButton.setColorFilter(getResources().getColor(R.color.card_dark_action_button));
		}  else if (sharedPrefs.getString("card_theme", "Light").equals("Pitch Black"))
		{
			menuButton.setColorFilter(getResources().getColor(R.color.card_black_action_button));
			attachButton.setColorFilter(getResources().getColor(R.color.card_black_action_button));
			newMessageButton.setColorFilter(getResources().getColor(R.color.card_black_action_button));
			backButton.setColorFilter(getResources().getColor(R.color.card_black_action_button));
			callButton.setColorFilter(getResources().getColor(R.color.card_black_action_button));
		}
		
		final Context context = this;
		
		attachButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				attachedPosition = messagePager.getCurrentItem();
				multipleAttachments = false;
				AttachMore.data = new ArrayList<MMSPart>();
	    		
	    		AlertDialog.Builder attachBuilder = new AlertDialog.Builder(context);
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
							startActivityForResult(captureIntent, 2);
							break;
						case 2:
							Intent attachMore = new Intent(context, AttachMore.class);
							startActivityForResult(attachMore, 3);
							break;
						}
						
					}
	    			
	    		});
	    		
	    		attachBuilder.create().show();
				
			}
			
		});
		
		backButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				MainActivity.isFastScrolling = true;
				MainActivity.scrollTo = 0;
				messagePager.setCurrentItem(0);
				
			}
			
		});
		
		if (sharedPrefs.getBoolean("display_contact_names", false))
		{
			contactName.setOnClickListener(new OnClickListener() {
	
				@Override
				public void onClick(View arg0) {
					MainActivity.isFastScrolling = true;
					MainActivity.scrollTo = 0;
					messagePager.setCurrentItem(0);
					
				}
				
			});
		}
		
		newMessageButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				MainActivity.isFastScrolling = true;
				MainActivity.scrollTo = messagePagerAdapter.getCount() - 1;
				messagePager.setCurrentItem(messagePagerAdapter.getCount() - 1);
				
				MainActivity.fromNewMessageButton = true;
				
				new Thread(new Runnable() {

					@Override
					public void run() {
						try
						{
							contactEntry.postDelayed(new Runnable() {

								@Override
								public void run() {
									InputMethodManager keyboard = (InputMethodManager)
							        getSystemService(Context.INPUT_METHOD_SERVICE);
							        keyboard.showSoftInput(contactEntry, 0); 
									
								}
								
							}, 750);
						} catch (Exception e)
						{
							
						}
						
					}
					
				}).start();
			}
			
		});
		
		callButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (messagePager.getCurrentItem() != 0 && messagePager.getCurrentItem() != messagePagerAdapter.getCount() - 1)
				{
					if (group.get(messagePager.getCurrentItem() - 1).equals("no"))
					{
						Intent callIntent = new Intent(Intent.ACTION_CALL);
				        callIntent.setData(Uri.parse("tel:"+findContactNumber(inboxNumber.get(messagePager.getCurrentItem() - 1), context)));
				        callIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				        startActivity(callIntent);
					} else
					{
						Toast.makeText(getBaseContext(), "Can't call a group", Toast.LENGTH_SHORT).show();
					}
				} else
				{
					Toast.makeText(getBaseContext(), "No one to call", Toast.LENGTH_SHORT).show();
				}
				
			}
			
		});
		
		if (sharedPrefs.getBoolean("custom_font", false))
		{
			font = Typeface.createFromFile(sharedPrefs.getString("custom_font_path", ""));
		}
		
		if (sharedPrefs.getBoolean("quick_text", false))
		{
			Intent mIntent = new Intent(this, QuickTextService.class);
			this.startService(mIntent);
		} else
		{
			NotificationManager mNotificationManager =
		            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			mNotificationManager.cancel(2);
		}
		
		View v = findViewById(R.id.newMessageGlow);
		v.setVisibility(View.GONE);
		
		setUpSendbar();
	}


	
	public void refreshMessages(boolean totalRefresh)
	{		
		inboxSent = new ArrayList<Boolean>();
		inboxNumber = new ArrayList<String>();
		inboxDate = new ArrayList<String>();
		inboxBody = new ArrayList<String>();
		inboxId = new ArrayList<String>();
		threadIds = new ArrayList<String>();
		mms = new ArrayList<Boolean>();
		images = new ArrayList<String>();
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
			
			int scale = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60, getResources().getDisplayMetrics());
		    
			InputStream input2;
			
			try
		    {
				input2 = openDisplayPhoto(Long.parseLong(MainActivity.myContactId));
		    } catch (NumberFormatException e)
		    {
		    	input2 = null;
		    }
			
			  if (input2 == null)
			  {
				  input2 = getResources().openRawResource(R.drawable.ic_contact_picture);
			  }
			  
			  Bitmap im;
			  
			  try
			  {
				  im = Bitmap.createScaledBitmap(BitmapFactory.decodeStream(input2), scale, scale, true);
			  } catch (Exception e)
			  {
				  im = Bitmap.createScaledBitmap(drawableToBitmap(getResources().getDrawable(R.drawable.ic_contact_picture)), scale, scale, true);
			  }
			  
			  myPicture = im;

			
			refreshMyContact = false;
		}
	}
	
	public void setUpSendbar()
	{
		charsRemaining = (TextView) findViewById(R.id.charsRemaining);
		messageEntry = (EditText) findViewById(R.id.messageEntry);
		sendButton = (ImageButton) findViewById(R.id.sendButton);
		emojiButton = (ImageButton) findViewById(R.id.emojiButton);
		sendBarBackground = findViewById(R.id.sendBarBackground);
		attachImageView = (ImageAttachmentView) findViewById(R.id.image_attachment_view);
		
		if (!sharedPrefs.getBoolean("keyboard_type", true))
		{
			messageEntry.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
			messageEntry.setImeOptions(EditorInfo.IME_ACTION_NONE);
		}
		
		if (sharedPrefs.getBoolean("simple_cards", true))
		{
			if (sharedPrefs.getString("card_theme", "Light").equals("Light"))
			{
				sendBarBackground.setBackgroundColor(getResources().getColor(R.color.white));
				attachImageView.setBackgroundColor(getResources().getColor(R.color.white));
			} else if (sharedPrefs.getString("card_theme", "Light").equals("Dark"))
			{
				sendBarBackground.setBackgroundColor(getResources().getColor(R.color.card_dark_card_background));
				attachImageView.setBackgroundColor(getResources().getColor(R.color.card_dark_card_background));
			} else if (sharedPrefs.getString("card_theme", "Light").equals("Pitch Black"))
			{
				sendBarBackground.setBackgroundColor(getResources().getColor(R.color.card_black_card_background));
				attachImageView.setBackgroundColor(getResources().getColor(R.color.card_black_card_background));
			}
		}
		
		charsRemaining.setVisibility(View.GONE);
		
		messageEntry.addTextChangedListener(new TextWatcher() {
	        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
	        }

	        public void onTextChanged(CharSequence s, int start, int before, int count) {
	        	int length = Integer.parseInt(String.valueOf(s.length()));
	        	
	        	if (!sharedPrefs.getString("signature", "").equals(""))
	        	{
	        		length += ("\n" + sharedPrefs.getString("signature", "")).length();
	        	}
	        	
	        	String patternStr = "[^" + com.klinker.android.messaging_sliding.MainActivity.GSM_CHARACTERS_REGEX + "]";
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
	        	
	            charsRemaining.setText(pages + "/" + (size - length));
	            
	            if ((pages == 1 && (size - length) <= 30) || pages != 1)
	            {
	            	charsRemaining.setVisibility(View.VISIBLE);
	            }
	            
	            if ((pages + "/" + (size - length)).equals("1/31"))
	            {
	            	charsRemaining.setVisibility(View.GONE);
	            }
	            
	            if ((pages + "/" + (size - length)).equals("1/160"))
	            {
	            	charsRemaining.setVisibility(View.GONE);
	            }
	            
	            try
	            {
		            if (attachImageView.getVisibility() == View.VISIBLE || group.get(messagePager.getCurrentItem() - 1).equals("yes"))
		            {
		            	charsRemaining.setVisibility(View.GONE);
		            }
	            } catch(Exception e)
	            {
	            	
	            }
	            
	            if (sharedPrefs.getBoolean("send_as_mms", false) && pages >= sharedPrefs.getInt("mms_after", 4))
	            {
	            	charsRemaining.setVisibility(View.GONE);
	            }
	            
	            if (sharedPrefs.getBoolean("send_with_return", false))
	            {
	            	if (messageEntry.getText().toString().endsWith("\n"))
	            	{
	            		messageEntry.setText(messageEntry.getText().toString().substring(0, messageEntry.getText().toString().length() - 1));
	            		sendButton.performClick();
	            	}
	            }
	        }

	        public void afterTextChanged(Editable s) {
                if (sharedPrefs.getBoolean("enable_drafts", true)) {
                    if (newDraft.equals("") && messagePager.getCurrentItem() != 0)
                    {
                        try {
                            newDraft = threadIds.get(messagePager.getCurrentItem() - 1);
                        } catch (Exception e) {

                        }

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
				
				boolean flag = false;
				boolean sendMmsFromLength = false;
				String[] counter = charsRemaining.getText().toString().split("/");
				
				if (Integer.parseInt(counter[0]) >= sharedPrefs.getInt("mms_after", 4) && sharedPrefs.getBoolean("send_as_mms", false))
				{
					sendMmsFromLength = true;
				}
				
				if (messagePager.getCurrentItem() == 0)
				{
					Toast.makeText(context, "No contact to send to", Toast.LENGTH_SHORT).show();
					return;
				}
				
				int position = messagePager.getCurrentItem() - 1;
				
				if (messagePager.getCurrentItem() == messagePagerAdapter.getCount() - 1)
				{
					flag = true;
					position --;
				}
				
				if (flag == false)
				{
					if (group.get(position).equals("no") && attachImageView.getVisibility() == View.GONE && sendMmsFromLength == false)
					{
						if (messageEntry.getText().toString().equals(""))
						{
							Toast.makeText(context, "ERROR: Nothing to send", Toast.LENGTH_SHORT).show();
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
							final int position2 = position;
							
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
                                                                new Thread(new Runnable() {

                                                                    @Override
                                                                    public void run() {
                                                                        try
                                                                        {
                                                                            Thread.sleep(800);
                                                                        } catch (Exception e)
                                                                        {

                                                                        }

                                                                        Cursor query = context.getContentResolver().query(Uri.parse("content://sms/outbox"), null, null, null, null);

                                                                        if (query.moveToFirst())
                                                                        {
                                                                            String id = query.getString(query.getColumnIndex("_id"));
                                                                            ContentValues values = new ContentValues();
                                                                            values.put("type", "2");
                                                                            context.getContentResolver().update(Uri.parse("content://sms/outbox"), values, "_id=" + id, null);

                                                                            ((Activity) context).getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {

                                                                                @Override
                                                                                public void run() {
                                                                                    ((MainActivity) context).refreshViewPager3();
                                                                                }

                                                                            });
                                                                        }

                                                                        query.close();

                                                                    }

                                                                }).start();

                                                                break;
                                                            case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                                                                try
                                                                {
                                                                    Thread.sleep(500);
                                                                } catch (Exception e)
                                                                {

                                                                }

                                                                Cursor query = context.getContentResolver().query(Uri.parse("content://sms/outbox"), null, null, null, null);

                                                                if (query.moveToFirst())
                                                                {
                                                                    String id = query.getString(query.getColumnIndex("_id"));
                                                                    ContentValues values = new ContentValues();
                                                                    values.put("type", "5");
                                                                    context.getContentResolver().update(Uri.parse("content://sms/outbox"), values, "_id=" + id, null);
                                                                    ((MainActivity) context).refreshViewPager3();
                                                                }

                                                                query.close();

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
                                                                    Thread.sleep(500);
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
                                                                    Thread.sleep(500);
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
                                                                    Thread.sleep(500);
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
                                                                        builder.setTitle(findContactName(findContactNumber(inboxNumber.get(position2), context), context));
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
                                                                        builder2.setTitle(findContactName(findContactNumber(inboxNumber.get(position2), context), context));
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
                                                                new Thread(new Runnable() {

                                                                    @Override
                                                                    public void run() {
                                                                        try
                                                                        {
                                                                            Thread.sleep(800);
                                                                        } catch (Exception e)
                                                                        {

                                                                        }

                                                                        Cursor query = context.getContentResolver().query(Uri.parse("content://sms/outbox"), null, null, null, null);

                                                                        if (query.moveToFirst())
                                                                        {
                                                                            String id = query.getString(query.getColumnIndex("_id"));
                                                                            ContentValues values = new ContentValues();
                                                                            values.put("type", "2");
                                                                            context.getContentResolver().update(Uri.parse("content://sms/outbox"), values, "_id=" + id, null);

                                                                            ((Activity) context).getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {

                                                                                @Override
                                                                                public void run() {
                                                                                    ((MainActivity) context).refreshViewPager3();
                                                                                }

                                                                            });
                                                                        }

                                                                        query.close();

                                                                    }

                                                                }).start();

                                                                break;
                                                            case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                                                                try
                                                                {
                                                                    Thread.sleep(500);
                                                                } catch (Exception e)
                                                                {

                                                                }

                                                                Cursor query = context.getContentResolver().query(Uri.parse("content://sms/outbox"), null, null, null, null);

                                                                if (query.moveToFirst())
                                                                {
                                                                    String id = query.getString(query.getColumnIndex("_id"));
                                                                    ContentValues values = new ContentValues();
                                                                    values.put("type", "5");
                                                                    context.getContentResolver().update(Uri.parse("content://sms/outbox"), values, "_id=" + id, null);
                                                                    ((MainActivity) context).refreshViewPager3();
                                                                }

                                                                query.close();

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
                                                                    Thread.sleep(500);
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
                                                                    Thread.sleep(500);
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
                                                                    Thread.sleep(500);
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
                                        values.put("thread_id", threadIds.get(messagePager.getCurrentItem() - 1));
									    context.getContentResolver().insert(Uri.parse("content://sms/outbox"), values);
										
									    final String address2 = address;
									    
									    ((Activity) context).getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {
		
											@Override
											public void run() {
                                                if (sharedPrefs.getBoolean("enable_drafts", true)) {
                                                    if (fromDraft)
                                                    {
                                                        for (int i = 0; i < draftNames.size(); i++)
                                                        {
                                                            if (draftNames.get(i).equals(threadIds.get(messagePager.getCurrentItem() - 1)))
                                                            {
                                                                draftsToDelete.add(draftNames.get(i));
                                                                draftNames.remove(i);
                                                                drafts.remove(i);
                                                                draftChanged.remove(i);
                                                                break;
                                                            }
                                                        }
                                                    }
                                                }

												MainActivity.sentMessage = true;
                                                notChanged = false;
									        	refreshViewPager4(address2, StripAccents.stripAccents(body), cal.getTimeInMillis() + "");
									        	messagePager.setCurrentItem(1);
									        	charsRemaining.setVisibility(View.GONE);
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
						
						String[] to = ("insert-address-token " + findContactNumber(inboxNumber.get(messagePager.getCurrentItem() - 1), context)).split(" ");

                        if (!sharedPrefs.getBoolean("send_with_stock", false))
                        {
                            if (multipleAttachments == false)
                            {
                                insert(context, to, "", byteArray, body);

                                MMSPart[] parts = new MMSPart[2];

                                if (attachImageView.getVisibility() == View.VISIBLE)
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

                                sendMMS(findContactNumber(inboxNumber.get(messagePager.getCurrentItem() - 1), context), parts);
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

                                sendMMS(findContactNumber(inboxNumber.get(messagePager.getCurrentItem() - 1), context), AttachMore.data.toArray(new MMSPart[AttachMore.data.size()]));

                                AttachMore.data = new ArrayList<MMSPart>();
                            }
                        } else
                        {
                            if (multipleAttachments == false)
                            {
                                Intent sendIntent = new Intent(Intent.ACTION_SEND);
                                sendIntent.putExtra("address", findContactNumber(inboxNumber.get(messagePager.getCurrentItem() - 1), context).replace(" ", ";"));
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
						attachImageView.setVisibility(false);
						
						refreshViewPager(true);
						messagePager.setCurrentItem(1);
					}
				} else
				{
					if (contactEntry.getText().toString().equals(""))
					{
						Toast.makeText(context, "ERROR: No valid recipients", Toast.LENGTH_SHORT).show();
					} else if (messageEntry.getText().toString().equals("") && attachImageView.getVisibility() == View.GONE)
					{
						Toast.makeText(context, "ERROR: Nothing to send", Toast.LENGTH_SHORT).show();
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
						
						String[] contacts = contactEntry.getText().toString().split("; ");
						final int contactLength = contacts.length;
						
						boolean sendMmsFromLength2 = false;
						String[] counter2 = charsRemaining.getText().toString().split("/");
						
						if (Integer.parseInt(counter2[0]) >= sharedPrefs.getInt("mms_after", 4) && sharedPrefs.getBoolean("send_as_mms", false))
						{
							sendMmsFromLength = true;
						}
						
						if ((attachImageView.getVisibility() == View.GONE) && (sendMmsFromLength2 == false) && (contactLength == 1 || (contactLength > 1 && sharedPrefs.getBoolean("group_message", false) == false)))
						{
							for (int i = 0; i < contacts.length; i++)
							{
								String body2 = messageEntry.getText().toString();
								
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
                                                                        Thread.sleep(500);
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
                                                                        Thread.sleep(500);
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
                                                                        Thread.sleep(500);
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
                                                                        Thread.sleep(500);
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
                                                                        Thread.sleep(500);
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
                                                                        Thread.sleep(500);
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
                                                                        Thread.sleep(500);
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
                                                                        Thread.sleep(500);
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

                                                    if (sharedPrefs.getBoolean("enable_drafts", true)) {
                                                        if (fromDraft)
                                                        {
                                                            for (int i = 0; i < draftNames.size(); i++)
                                                            {
                                                                if (draftNames.get(i).equals(threadIds.get(messagePager.getCurrentItem() - 1)))
                                                                {
                                                                    draftsToDelete.add(draftNames.get(i));
                                                                    draftNames.remove(i);
                                                                    drafts.remove(i);
                                                                    draftChanged.remove(i);
                                                                    break;
                                                                }
                                                            }
                                                        }
                                                    }

													sentMessage = true;
													waitMessagePager = false;
                                                    notChanged = false;
													refreshViewPager(true);
													charsRemaining.setVisibility(View.GONE);
													messagePager.setCurrentItem(1);
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
							
							String[] to = ("insert-address-token; " + contactEntry.getText().toString()).split("; ");

                            if (!sharedPrefs.getBoolean("send_with_stock", false))
                            {
                                if (multipleAttachments == false)
                                {
                                    insert(context, to, "", byteArray, body);

                                    MMSPart[] parts = new MMSPart[2];

                                    if (attachImageView.getVisibility() == View.VISIBLE)
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

                                    sendMMS(contactEntry.getText().toString(), parts);
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

                                    sendMMS(contactEntry.getText().toString(), AttachMore.data.toArray(new MMSPart[AttachMore.data.size()]));

                                    AttachMore.data = new ArrayList<MMSPart>();
                                }
                            } else
                            {
                                if (multipleAttachments == false)
                                {
                                    Intent sendIntent = new Intent(Intent.ACTION_SEND);
                                    sendIntent.putExtra("address", contactEntry.getText().toString().replace(",", ""));
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
							
							refreshViewPager(true);
						}
						
						contactEntry.setText("");
						messageEntry.setText("");
						attachImageView.setVisibility(false);
						messagePager.setCurrentItem(1);
					}
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
			params.addRule(RelativeLayout.ALIGN_LEFT, R.id.sendBarBackground);
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
		
		attachImageView.setVisibility(false);
		
		if (sharedPrefs.getString("card_theme", "Light").equals("Light"))
		{
			sendButton.setColorFilter(getResources().getColor(R.color.card_action_button));
			sendButton.setAlpha((float) .75);
		} else if (sharedPrefs.getString("card_theme", "Light").equals("Dark"))
		{
			sendButton.setColorFilter(getResources().getColor(R.color.card_dark_action_button_2));
		} else if (sharedPrefs.getString("card_theme", "Light").equals("Pitch Black"))
		{
			sendButton.setColorFilter(getResources().getColor(R.color.card_black_action_button_2));
		}
		
		emojiButton.setAlpha((float) .75);
		
		if (sharedPrefs.getBoolean("custom_font", false))
		{
			charsRemaining.setTypeface(font);
			messageEntry.setTypeface(font);
		}
	}
	
	public void refreshViewPager(boolean totalRefresh)
	{
        if (!firstRun) {
            notChanged = true;
        }

		refreshMessages(totalRefresh);
		
		contactPagerAdapter = new ContactPagerAdapter(getFragmentManager());
		contactPager = (ContactViewPager) findViewById(R.id.contactPager);
		contactPager.setAdapter(contactPagerAdapter);
		contactPager.setPageMargin(getResources().getDisplayMetrics().widthPixels / -18);
		contactPager.setOffscreenPageLimit(2);
		
		messagePagerAdapter = new MessagePagerAdapter(getFragmentManager());
		messagePager = (ViewPager) findViewById(R.id.messagePager);
		messagePager.setAdapter(messagePagerAdapter);
		messagePager.setPageMargin(getResources().getDisplayMetrics().widthPixels / -18);
		messagePager.setOffscreenPageLimit(2);
		
		if (!sharedPrefs.getBoolean("display_contact_cards", false))
		{
			contactPager.setVisibility(View.GONE);
			callButton.setVisibility(View.VISIBLE);
		}
		
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
		
		final Context context = this;

        drafts = new ArrayList<String>();
        draftNames = new ArrayList<String>();
        draftChanged = new ArrayList<Boolean>();
        draftsToDelete = new ArrayList<String>();
		
		messagePager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageScrollStateChanged(int arg0) {
				
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				
			}

			@Override
			public void onPageSelected(int arg0) {
				if (sharedPrefs.getBoolean("display_contact_cards", false))
				{
					contactPager.setCurrentItem(arg0, true);
				}
				
				if (sharedPrefs.getBoolean("display_contact_names", false))
				{
					if (arg0 == 0 || arg0 == messagePagerAdapter.getCount() - 1)
					{
						contactName.setText("");
					} else
					{
						if (group.get(messagePager.getCurrentItem() - 1).equals("no"))
						{
							contactName.setText(findContactName(findContactNumber(inboxNumber.get(messagePager.getCurrentItem() - 1), context), context));
						} else
						{
							contactName.setText("Group MMS");
						}
					}
				}
				
				if (arg0 == 0)
				{
					messageEntry.postDelayed(new Runnable() {
							@Override
							public void run() {
								InputMethodManager imm = (InputMethodManager)getSystemService(
									      Context.INPUT_METHOD_SERVICE);
								imm.hideSoftInputFromWindow(messageEntry.getWindowToken(), 0);
								
							}
					}, 300);
					
					backButton.setVisibility(View.GONE);
					callButton.setVisibility(View.GONE);
				} else
				{
					backButton.setVisibility(View.VISIBLE);
					
					if (!sharedPrefs.getBoolean("display_contact_cards", false))
					{
						callButton.setVisibility(View.VISIBLE);
					} else
					{
						callButton.setVisibility(View.GONE);
					}
				}
				
				if (messagePager.getCurrentItem() < 4)
				{
					try
					{
						View conversation = findViewById(10 + messagePager.getCurrentItem() - 1);
		        		View background = conversation.findViewById(R.id.view1);
		        		
		        		if (sharedPrefs.getString("card_theme", "Light").equals("Light"))
		        		{
		        			background.setBackgroundResource(R.drawable.card_background);
		        		} else if (sharedPrefs.getString("card_theme", "Light").equals("Dark"))
		        		{
		        			background.setBackgroundResource(R.drawable.card_background_dark);
		        		} else if (sharedPrefs.getString("card_theme", "Light").equals("Pitch Black"))
		        		{
		        			background.setBackgroundResource(R.drawable.card_background_black);
		        		}
					} catch (Exception e)
					{
						
					}
				}
				
				new Thread(new Runnable() {

					@Override
					public void run() {
						ArrayList<String> newMessages = readFromFile(context);
				        
				        for (int j = 0; j < newMessages.size(); j++)
				        {
				        	try
				        	{
					        	if (newMessages.get(j).replaceAll("-", "").endsWith(findContactName(findContactNumber(inboxNumber.get(messagePager.getCurrentItem() - 1), context).replace("-", ""), context)))
					        	{
					        		newMessages.remove(j);
					        	}
				        	} catch (Exception e)
				        	{
				        		e.printStackTrace();
				        	}
				        }
				        
				        writeToFile(newMessages, context);

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
                            } catch (Exception e) {

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

                            newDraft = "";

                            try {
                                for (int i = 0; i < draftNames.size(); i++)
                                {
                                    if (draftNames.get(i).equals(threadIds.get(messagePager.getCurrentItem() - 1)))
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
                                if (sharedPrefs.getBoolean("enable_drafts", true)) {
                                    if (!messageEntry.getText().equals(""))
                                    {
                                        messageEntry.setText("");
                                    }

                                    fromDraft = false;

                                    if (indexF != -1)
                                    {
                                        fromDraft = true;
                                        messageEntry.setText(drafts.get(indexF));
                                        messageEntry.setSelection(drafts.get(indexF).length());
                                    }
                                }
                            }
                        });
					}
					
				}).start();
			}
			
		});
		
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
	}
	
	public void refreshViewPager4(String number, String body, String date)
	{
        notChanged = false;

		int position = messagePager.getCurrentItem();
		
		String currentNumber; 
		
		if (position != 0 && position != messagePagerAdapter.getCount() - 1)
		{
			currentNumber = findContactNumber(inboxNumber.get(position - 1), this);
		} else
		{
			currentNumber = findContactNumber(inboxNumber.get(0), this);
		}
		
		boolean flag = false;
		boolean flag2 = false;
		
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
		
		for (int i = 0; i < inboxNumber.size(); i++)
		{
			if (currentNumber.equals(findContactNumber(inboxNumber.get(i), this)))
			{
				position = i + 1;
				break;
			}
		}
		
		if (flag == true)
		{
			if (messagePager.getCurrentItem() > 3)
			{
				flag2 = true;
			}
		}
		
		if (flag == true && flag2 == false)
		{
			waitMessagePager = false;
			contactPagerAdapter.notifyDataSetChanged();
			messagePagerAdapter.notifyDataSetChanged();
			
			for (int i = 0; i < inboxNumber.size(); i++)
			{
				if (currentNumber.equals(findContactNumber(inboxNumber.get(i), this)))
				{
					position = i + 1;
					break;
				}
			}

			messagePager.setCurrentItem(position, false);
			
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
		} else if (flag == false && flag2 == false)
		{
			refreshViewPager(true);
		} else if (flag == true && flag2 == true)
		{
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
		}
	}
	
	public void refreshViewPager3()
	{
		try
		{
			View message = findViewById(MainActivity.currentMessageTag);
			TextView text = (TextView) message.findViewById(R.id.textDate);
			
			Calendar cal = Calendar.getInstance();
			long date2 = cal.getTimeInMillis();
			
			if (sharedPrefs.getBoolean("hour_format", false))
			{
				  text.setText(DateFormat.getTimeInstance(DateFormat.SHORT, Locale.GERMAN).format(date2));
			} else
			{
				  text.setText(DateFormat.getTimeInstance(DateFormat.SHORT, Locale.US).format(date2));
			}
		} catch (Exception e)
		{
			messagePagerAdapter.notifyDataSetChanged();
		}
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) { 
        
        if (requestCode == 1)
        {
            if(resultCode == RESULT_OK){  
                final Uri selectedImage = imageReturnedIntent.getData();
                attachedImage = selectedImage;
                fromCamera = false;
                
	    		attachImageView.setVisibility(true);
	    		
	    		try
	    		{
	    			attachImageView.setImage("send_image", decodeFile(new File(getPath(selectedImage))));
	    		} catch (Exception e)
	    		{
	    			Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
	    			attachImageView.setVisibility(false);
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
						attachImageView.setVisibility(false);
						
					}
	    			
	    		});
	    		
	    		messagePager.setCurrentItem(attachedPosition);

            }
        } else if (requestCode == 2)
        {
        	if (resultCode == Activity.RESULT_OK)
        	{
        		getContentResolver().notifyChange(capturedPhotoUri, null);
        		attachedImage = Uri.fromFile(new File(Environment.getExternalStorageDirectory() + "/SlidingMessaging/", "photoToSend.png"));
        		fromCamera = true;
        		
 	    		attachImageView.setVisibility(true);
 	    		
 	    		try
 	    		{
 	    			Bitmap image = android.provider.MediaStore.Images.Media.getBitmap(getContentResolver(), capturedPhotoUri);
 	    			File f = new File(capturedPhotoUri.getPath());
 	    			image = decodeFile(f);
 	    			attachImageView.setImage("send_image", image);
 	    		} catch (Exception e)
 	    		{
 	    			e.printStackTrace();
 	    			Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
 	    			attachImageView.setVisibility(false);
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
 						attachImageView.setVisibility(false);
 						
 					}
 	    			
 	    		});
        	}
        } else if (requestCode == 3)
        {
        	if (resultCode == Activity.RESULT_OK)
            {
                multipleAttachments = true;
                
                attachImageView.setVisibility(true);
 	    		
 	    		try
 	    		{
 	    			Bitmap bmp = BitmapFactory.decodeByteArray(AttachMore.data.get(0).Data, 0, AttachMore.data.get(0).Data.length);
 	    			Bitmap mutableBitmap = bmp.copy(Bitmap.Config.ARGB_8888, true);
 	    			attachImageView.setImage("send_image", mutableBitmap);
 	    		} catch (Exception e)
 	    		{
 	    			e.printStackTrace();
 	    			
 	    			try
 	    			{
 	    				String path = getPath(AttachMore.data.get(0).Path);
 	    		    	Bitmap thumb = ThumbnailUtils.createVideoThumbnail(path,
 	    	    			    MediaStore.Images.Thumbnails.MINI_KIND);
 	    		    	attachImageView.setImage("send_image", thumb);
 	    			} catch (Exception f)
 	    			{
 	    				e.printStackTrace();
 	    				Toast.makeText(this, "Error loading MMS", Toast.LENGTH_SHORT).show();
 	 	    			attachImageView.setVisibility(false);
 	    			}
 	    		}
                
                final Context context = this;
 	    		
 	    		Button viewImage = (Button) findViewById(R.id.view_image_button);
 	    		Button replaceImage = (Button) findViewById(R.id.replace_image_button);
 	    		Button removeImage = (Button) findViewById(R.id.remove_image_button);
 	    		
 	    		viewImage.setOnClickListener(new OnClickListener() {

 					@Override
 					public void onClick(View arg0) {
 						Intent attachMore = new Intent(context, AttachMore.class);
						startActivityForResult(attachMore, 3);
 						
 					}
 	    			
 	    		});
 	    		
 	    		replaceImage.setOnClickListener(new OnClickListener() {

 					@Override
 					public void onClick(View v) {
 						Intent attachMore = new Intent(context, AttachMore.class);
						startActivityForResult(attachMore, 3);
 						
 					}
 	    			
 	    		});
 	    		
 	    		removeImage.setOnClickListener(new OnClickListener() {

 					@Override
 					public void onClick(View v) {
 						attachImageView.setVisibility(false);
 						multipleAttachments = false;
 						AttachMore.data = new ArrayList<MMSPart>();
 						
 					}
 	    			
 	    		});
            }
        } else if (requestCode == REQ_ENTER_PATTERN) // code for pattern unlock
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
	
	@Override
	public void onStart()
	{
		super.onStart();
		
		ComponentName receiver = new ComponentName(this, SentReceiver.class);
		ComponentName receiver2 = new ComponentName(this, DeliveredReceiver.class);
	    PackageManager pm = this.getPackageManager();

	    pm.setComponentEnabledSetting(receiver,
	            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
	            PackageManager.DONT_KILL_APP);
	    
	    pm.setComponentEnabledSetting(receiver2,
	    		PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
	            PackageManager.DONT_KILL_APP);
	    
		if (firstRun)
		{
			refreshViewPager(false);
			firstRun = false;
			
			if (sharedPrefs.getBoolean("open_to_first", false))
			{
				MainActivity.isFastScrolling = true;
				MainActivity.scrollTo = 1;
				messagePager.setCurrentItem(1);
			} else
			{
				callButton.setVisibility(View.GONE);
				backButton.setVisibility(View.GONE);
			}
			
			if (sendTo && !fromDashclock)
			{
				boolean flag = false;

                try {
                    if (attachedImage == null)
                    {
                        for (int i = 0; i < inboxNumber.size(); i++)
                        {
                            if (findContactNumber(inboxNumber.get(i), this).replace("-","").replace("+", "").startsWith(sendMessageTo.replace("-", "").replace("+1", "")) || findContactNumber(inboxNumber.get(i), this).replace("-","").replace("+", "").endsWith(sendMessageTo.replace("-", "").replace("+1", "")))
                            {
                                MainActivity.isFastScrolling = true;
                                MainActivity.scrollTo = i + 1;
                                messagePager.setCurrentItem(i + 1);
                                flag = true;
                                break;
                            }
                        }

                        if (flag == false)
                        {
                            String name = findContactName(sendMessageTo.replace("-","").replace("+1", "").replace("+", ""), this);

                            for (int i = 0; i < inboxNumber.size(); i++)
                            {
                                if (findContactName(findContactNumber(inboxNumber.get(i), this), this).equals(name))
                                {
                                    MainActivity.isFastScrolling = true;
                                    MainActivity.scrollTo = i + 1;
                                    messagePager.setCurrentItem(i + 1);
                                    flag = true;
                                    break;
                                }
                            }
                        }
                    }
                } catch (Exception e) {

                }
				
				if (flag == false)
				{
					messagePager.setCurrentItem(messagePagerAdapter.getCount() - 1);
					
					final Context context = this;
					
					new Thread(new Runnable() {

						@Override
						public void run() {
							boolean keepGoing = true;
							
							while (keepGoing)
							{
								try
								{
									try {
										Thread.sleep(1000);
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
									
									((Activity) context).getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {
										
										@Override
										public void run() {
											contactEntry.setText(sendMessageTo);
										}
									});
									
									break;
								} catch (Exception e)
								{
									keepGoing = true;
								}
							}
							
						}
						
					}).start();
					
					if (attachedImage != null)
					{
			    		attachImageView.setVisibility(true);
			    		
			    		try
			    		{
			    			attachImageView.setImage("send_image", decodeFile(new File(getPath(attachedImage))));
			    		} catch (Exception e)
			    		{
			    			Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
			    			attachImageView.setVisibility(false);
			    		}
			    		
			    		Button viewImage = (Button) findViewById(R.id.view_image_button);
			    		Button replaceImage = (Button) findViewById(R.id.replace_image_button);
			    		Button removeImage = (Button) findViewById(R.id.remove_image_button);
			    		
			    		viewImage.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View arg0) {
								context.startActivity(new Intent(Intent.ACTION_VIEW, attachedImage));
								
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
								attachImageView.setVisibility(false);
								
							}
			    			
			    		});
					}
				}
				
				sendTo = false;
			}
			
			if (whatToSend != null)
	        {
	        	messageEntry.setText(whatToSend);
	        	whatToSend = null;
	        }
			
			if (sendToThread != null)
			{
				for (int i = 0; i < threadIds.size(); i++)
				{
					if (threadIds.get(i).equals(sendToThread))
					{
						messagePager.setCurrentItem(i + 1);
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
		} else if (com.klinker.android.messaging_sliding.MainActivity.messageRecieved == true)
		{
			int position = messagePager.getCurrentItem();
			String threadId = "0";
			
			try
			{
				threadId = threadIds.get(position - 1);
			} catch (Exception e)
			{
				
			}
			
			refreshViewPager(false);
			com.klinker.android.messaging_sliding.MainActivity.messageRecieved = false;
			
			for (int i = 0; i < threadIds.size(); i++)
			{
				if (threadId.equals(threadIds.get(i)))
				{
					MainActivity.isFastScrolling = true;
					MainActivity.scrollTo = i + 1;
					messagePager.setCurrentItem(i + 1);
				}
			}
		}
		
		if (fromDashclock)
		{
			MainActivity.isFastScrolling = true;
			MainActivity.scrollTo = 1;
			messagePager.setCurrentItem(1);
			fromDashclock = false;
		}
		
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
	
	@Override
	public void onResume()
	{
		super.onResume();

        Intent clearMessages = new Intent("com.klinker.android.messaging.CLEAR_MESSAGES");
        getApplicationContext().sendBroadcast(clearMessages);

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
                    //finish();
                }

            }
        }
		
		IntentFilter filter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        filter.setPriority(3);
        registerReceiver(receiver, filter);
        
        filter = new IntentFilter("com.klinker.android.messaging.NEW_MMS");
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        filter.setPriority(3);
        registerReceiver(mmsReceiver, filter);

        filter = new IntentFilter("com.klinker.android.messaging_donate.KILL_FOR_HALO");
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        registerReceiver(killReceiver, filter);

        drafts = new ArrayList<String>();
        draftNames = new ArrayList<String>();
        draftChanged = new ArrayList<Boolean>();
        draftsToDelete = new ArrayList<String>();

        if (sharedPrefs.getBoolean("enable_drafts", true)) {
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

            int index = -1;

            try {
                for (int i = 0; i < draftNames.size(); i++)
                {
                    if (draftNames.get(i).equals(threadIds.get(messagePager.getCurrentItem() - 1)))
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
                fromDraft = true;
                messageEntry.setText(drafts.get(index));
                messageEntry.setSelection(drafts.get(index).length());
            }
        }
	}
	
	@Override
	public void onPause()
	{		
		super.onPause();
		unregisterReceiver(receiver);
		unregisterReceiver(mmsReceiver);
        unregisterReceiver(killReceiver);
		
		ComponentName receiver = new ComponentName(this, SentReceiver.class);
		ComponentName receiver2 = new ComponentName(this, DeliveredReceiver.class);
	    PackageManager pm = this.getPackageManager();

	    pm.setComponentEnabledSetting(receiver,
	            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
	            PackageManager.DONT_KILL_APP);
	    
	    pm.setComponentEnabledSetting(receiver2,
	    		PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
	            PackageManager.DONT_KILL_APP);

        Intent updateWidget = new Intent("com.klinker.android.messaging.UPDATE_WIDGET");
        sendBroadcast(updateWidget);
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

                try {
                    draftNames.add(threadIds.get(messagePager.getCurrentItem() - 1));
                } catch (Exception e) {
                    draftNames.add(threadIds.get(messagePager.getCurrentItem()));
                }

                drafts.add(messageEntry.getText().toString());
                messageEntry.setText("");
            }

            new Thread(new Runnable() {
                @Override
                public void run() {
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

					try {
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
					} catch (Exception e) {
						// error with drafts, oh well
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
                }
            }).start();
        }

        if (sharedPrefs.getBoolean("cache_conversations", false)) {
            Intent cacheService = new Intent(context, CacheService.class);
            context.startService(cacheService);
        }
	}
	
	@Override
	public void onBackPressed() {
		if (messagePager.getCurrentItem() > 0)
		{
			messagePager.setCurrentItem(0, true);
			return;
		}
		
		super.onBackPressed();
        overridePendingTransition(R.anim.activity_slide_in_left, R.anim.activity_slide_out_right);
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
	    
	}
	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
	    if (keyCode == KeyEvent.KEYCODE_MENU) {
	    	PopupMenu popup = new PopupMenu(this, menuButton);
			MenuInflater inflater = popup.getMenuInflater();
			inflater.inflate(R.menu.card_options, popup.getMenu());
			popup.setOnMenuItemClickListener(this);
			popup.show();
			
	    	return true;
	    }
	    
	    return super.onKeyUp(keyCode, event);
	}
	
	public void showMenuPopup(View v)
	{
		PopupMenu popup = new PopupMenu(this, v);
		MenuInflater inflater = popup.getMenuInflater();
		inflater.inflate(R.menu.card_options, popup.getMenu());
		popup.setOnMenuItemClickListener(this);
		popup.show();
	}

    public String version;
	
	@Override
	public boolean onMenuItemClick(MenuItem item)
	{
		switch (item.getItemId())
		{
		case R.id.menu_settings:
			startActivity(new Intent(this, SettingsPagerActivity.class));
            finish();
            overridePendingTransition(R.anim.activity_slide_in_right, R.anim.activity_slide_out_left);
			return true;
		case R.id.menu_delete:
			Intent intent = new Intent(this, BatchDeleteAllActivity.class);
			intent.putExtra("threadIds", threadIds);
			intent.putExtra("inboxNumber", inboxNumber);
			startActivity(intent);
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
					           "\n\n" + this.getResources().getString(R.string.about_expanded) + "\n\n� 2013 Jacob Klinker and Luke Klinker")
                    .setPositiveButton(this.getResources().getString(R.string.changelog), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Intent wizardintent = new Intent(getApplicationContext(), ChangeLogMain.class);
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
			
			templates.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					try
					{
						messageEntry.setText(text.get(arg2));
						messageEntry.setSelection(text.get(arg2).length());
						templateDialog.cancel();
					} catch (Exception e)
					{
						
					}
					
				}
				
			});		
			
			return true;
		}
		
		return false;
	}
	
	public class ContactPagerAdapter extends android.support.v13.app.FragmentStatePagerAdapter {
		
		public ContactPagerAdapter(android.app.FragmentManager fm) {
			super(fm);
		}

		@Override
		public ContactSectionFragment getItem(int position) {
			ContactSectionFragment fragment = new ContactSectionFragment();
			Bundle args = new Bundle();
			
			args.putInt("position", position);
			args.putStringArrayList("group", group);
			//args.putStringArrayList("contacts", inboxContacts);
			args.putStringArrayList("numbers", inboxNumber);
			args.putStringArrayList("count", msgCount);
			args.putStringArrayList("read", msgRead);
			fragment.setArguments(args);
			return fragment;
		}

		@Override
		public int getCount() {
            try {
                if (MainActivity.limitConversations) {
                    if (threadIds.size() < 10) {
                        return threadIds.size() + 2;
                    } else {
                        return 12;
                    }
                } else {
                    return threadIds.size() + 2;
                }
            } catch (Exception e)
            {
                return 0;
            }

		}

		@Override
		public CharSequence getPageTitle(int position) {	
			return "";
		}
		
		@Override
		public int getItemPosition(Object object)
		{
			return POSITION_NONE;
		}
	}

	public static class ContactSectionFragment extends android.app.Fragment {
		
		public int position;
		public View view;
		public Context context;
		public ArrayList<String> group, inboxNumber, msgCount, msgRead;
		
		public ContactSectionFragment() {
			
		}
		
		@Override
		public void onConfigurationChanged(Configuration newConfig) {
		  super.onConfigurationChanged(newConfig);
		}
		
		@Override
		public void onSaveInstanceState(Bundle outState) {
		    
		}
		
		@Override
		public void onCreate(Bundle savedInstanceState)
		{
			super.onCreate(savedInstanceState);
			
			Bundle args = getArguments();
			
			this.position = args.getInt("position");
			this.group = args.getStringArrayList("group");
			this.inboxNumber = args.getStringArrayList("numbers");
			this.msgCount = args.getStringArrayList("count");
			this.msgRead = args.getStringArrayList("read");
		}
		
		@Override
		public void onAttach(Activity activity)
		{
			super.onAttach(activity);
			context = activity;
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			
			if (position != messagePagerAdapter.getCount() - 1)
			{
				view = inflater.inflate(R.layout.contact_card, container, false);
			} else
			{
				view = inflater.inflate(R.layout.new_message_card, container, false);
			}
			
			return refreshMessages();
		}		
		
		public View refreshMessages()
		{
			final TextView name = (TextView) view.findViewById(R.id.contactName);
			final TextView number = (TextView) view.findViewById(R.id.contactNumber);
			final TextView numberType = (TextView) view.findViewById(R.id.contactNumberType);
			ImageButton callButton = (ImageButton) view.findViewById(R.id.callButton);
			final QuickContactBadge contactImage = (QuickContactBadge) view.findViewById(R.id.contactPicture);
			TextView deleteText = (TextView) view.findViewById(R.id.deleteText);
			TextView allText = (TextView) view.findViewById(R.id.allText);
			View background = view.findViewById(R.id.view1);
			
			try
			{
				if (sharedPrefs.getString("card_theme", "Light").equals("Light"))
				{
					background.setBackgroundResource(R.drawable.card_background);
					name.setTextColor(context.getResources().getColor(R.color.card_conversation_name));
					number.setTextColor(context.getResources().getColor(R.color.card_conversation_summary));
					numberType.setTextColor(context.getResources().getColor(R.color.card_conversation_summary));
					callButton.setColorFilter(context.getResources().getColor(R.color.card_action_button));
					deleteText.setTextColor(context.getResources().getColor(R.color.card_conversation_summary));
					allText.setTextColor(context.getResources().getColor(R.color.card_conversation_summary));
				} else if (sharedPrefs.getString("card_theme", "Light").equals("Dark"))
				{
					background.setBackgroundResource(R.drawable.card_background_dark);
					name.setTextColor(context.getResources().getColor(R.color.card_dark_conversation_name));
					number.setTextColor(context.getResources().getColor(R.color.card_dark_conversation_summary));
					numberType.setTextColor(context.getResources().getColor(R.color.card_dark_conversation_summary));
					callButton.setColorFilter(context.getResources().getColor(R.color.card_dark_action_button_2));
					deleteText.setTextColor(context.getResources().getColor(R.color.card_dark_conversation_summary));
					allText.setTextColor(context.getResources().getColor(R.color.card_dark_conversation_summary));
				} else if (sharedPrefs.getString("card_theme", "Light").equals("Pitch Black"))
				{
					background.setBackgroundResource(R.drawable.card_background_black);
					name.setTextColor(context.getResources().getColor(R.color.card_black_conversation_name));
					number.setTextColor(context.getResources().getColor(R.color.card_black_conversation_summary));
					numberType.setTextColor(context.getResources().getColor(R.color.card_black_conversation_summary));
					callButton.setColorFilter(context.getResources().getColor(R.color.card_black_action_button_2));
					deleteText.setTextColor(context.getResources().getColor(R.color.card_black_conversation_summary));
					allText.setTextColor(context.getResources().getColor(R.color.card_black_conversation_summary));
				}
			} catch (Exception e)
			{
				
			}
			
			if (sharedPrefs.getBoolean("simple_cards", true))
			{
				if (sharedPrefs.getString("card_theme", "Light").equals("Light"))
				{
					background.setBackgroundColor(context.getResources().getColor(R.color.white));
				} else if (sharedPrefs.getString("card_theme", "Light").equals("Dark"))
				{
					background.setBackgroundColor(context.getResources().getColor(R.color.card_dark_card_background));
				} else if (sharedPrefs.getString("card_theme", "Light").equals("Dark"))
				{
					background.setBackgroundColor(context.getResources().getColor(R.color.card_black_card_background));
				}
			}
			
			if (position > 0 && position < messagePagerAdapter.getCount() - 1)
			{
				if (sharedPrefs.getBoolean("custom_font", false))
				{
					name.setTypeface(MainActivity.font);
					number.setTypeface(MainActivity.font);
					numberType.setTypeface(MainActivity.font);
				}
				
				position = position - 1;
				deleteText.setVisibility(View.GONE);
				allText.setVisibility(View.GONE);
				
				if (group.get(position).equals("no"))
				{
					Locale sCachedLocale = Locale.getDefault();
					int sFormatType = PhoneNumberUtils.getFormatTypeForLocale(sCachedLocale);
					Editable editable = new SpannableStringBuilder(findContactNumber(inboxNumber.get(position), context));
					PhoneNumberUtils.formatNumber(editable, sFormatType);
					final String number2 = editable.toString();
					
					number.setText(number2);
					
					if (Integer.parseInt(msgCount.get(position)) > 1)
					{
						numberType.setText(msgCount.get(position) + " " + context.getResources().getString(R.string.messages));
					} else
					{
						numberType.setText(msgCount.get(position) + " " + context.getResources().getString(R.string.message));
					}
					
					callButton.setOnClickListener(new OnClickListener() {
	
						@Override
						public void onClick(View v) {
							Intent callIntent = new Intent(Intent.ACTION_CALL);
					        callIntent.setData(Uri.parse("tel:"+findContactNumber(inboxNumber.get(position), context)));
					        callIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					        startActivity(callIntent);
						}
						
					});
					
					new Thread(new Runnable() {
	
						@Override
						public void run() {
							final Bitmap contactImageSet = getFacebookPhoto(findContactNumber(inboxNumber.get(position), context), context);
							final String name2 = MainActivity.findContactName(findContactNumber(inboxNumber.get(position), context), context);
							
							((Activity) context).getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {
								
								@Override
								public void run() {
									contactImage.setImageBitmap(contactImageSet);
									contactImage.assignContactFromPhone(findContactNumber(inboxNumber.get(position), context), true);
									
									name.setText(name2);
									
									if (name2.equals(number2))
									{
										number.setText("");
									}
								}
							});
							
						}
						
					}).start();
				} else
				{
					name.setText("Group MMS");
					
					new Thread(new Runnable() {

						@Override
						public void run() {
							final String name2 = MainActivity.loadGroupContacts(findContactNumber(inboxNumber.get(position), context), context);
							
							((Activity) context).getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {

								@Override
								public void run() {
									number.setText(name2);
									
									int numPeople = number.getText().toString().split(", ").length;
									numberType.setText(numPeople + " " + context.getResources().getString(R.string.people));
								}
						    	
						    });
							
							
							
						}
						  
					  }).start();
					
					if (sharedPrefs.getString("card_theme", "Light").equals("Light"))
					{
						contactImage.setImageResource(R.drawable.card_group);
						callButton.setImageResource(R.drawable.list_group);
					} else
					{
						contactImage.setImageResource(R.drawable.card_group_dark);
						callButton.setImageResource(R.drawable.list_group_dark);
					}
					
					callButton.setOnClickListener(new OnClickListener() {
	
						@Override
						public void onClick(View v) {
							Intent intent = new Intent(context, GroupActivity.class);
							intent.putExtra("names", loadGroupContacts(findContactNumber(inboxNumber.get(position), context), context));
							intent.putExtra("numbers", findContactNumber(inboxNumber.get(position), context));
							context.startActivity(intent);
						}
						
					});
				}
			} else if (position == 0)
			{
				if (sharedPrefs.getBoolean("custom_font", false))
				{
					name.setTypeface(MainActivity.font);
					number.setTypeface(MainActivity.font);
					numberType.setTypeface(MainActivity.font);
					deleteText.setTypeface(MainActivity.font);
					allText.setTypeface(MainActivity.font);
				}
				
				name.setText(context.getResources().getString(R.string.conversation_list));
				
				if (inboxNumber.size() == 1)
				{
					number.setText(inboxNumber.size() + " " + context.getResources().getString(R.string.conversation));
				} else
				{
					number.setText(inboxNumber.size() + " " + context.getResources().getString(R.string.conversations));
				}
				
				numberType.setText("");
				
				if (sharedPrefs.getString("card_theme", "Light").equals("Light"))
				{
					contactImage.setImageResource(R.drawable.card_group);
					callButton.setImageResource(R.drawable.ic_menu_delete_light);
				} else
				{
					contactImage.setImageResource(R.drawable.card_group_dark);
					callButton.setImageResource(R.drawable.ic_menu_delete);
				}
				
				callButton.setScaleType(ScaleType.CENTER);
				callButton.setPadding(15, 10, 15, 22);
				
				callButton.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						Intent intent = new Intent(context, BatchDeleteAllActivity.class);
						intent.putExtra("threadIds", threadIds);
						intent.putExtra("inboxNumber", inboxNumber);
						startActivity(intent);
						
					}
					
				});
				
				allText.setPadding(0,0,0,5);
				
				new Thread(new Runnable() {

					@Override
					public void run() {
						int numMessages = 0;
						int numUnread = 0;
						
						for (int i = 0; i < msgCount.size(); i++)
						{
							numMessages += Integer.parseInt(msgCount.get(i));
						}
						
						for (int i = 0; i < msgRead.size(); i++)
						{
							if (msgRead.get(i).equals("0"))
							{
								numUnread += 1;
							}
						}
						
						final int numMes = numMessages;
						final int numUn = numUnread;
						
						((Activity) context).getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {
							
							@Override
							public void run() {
								if (numMes == 1)
								{
									numberType.setText(numMes + " " + context.getResources().getString(R.string.message));
								} else
								{
									numberType.setText(numMes + " " + context.getResources().getString(R.string.messages));
								}
								
								if (numUn == 1)
								{
									name.setText(numUn + " " + context.getResources().getString(R.string.new_conversation));
								} else if (numUn > 1)
								{
									name.setText(numUn + " " + context.getResources().getString(R.string.new_conversations));
								}
							}
						});
						
					}
					
				}).start();
			} else if (position == messagePagerAdapter.getCount() - 1)
			{
				TextView newMessageText = (TextView) view.findViewById(R.id.textView1);
				
				if (sharedPrefs.getBoolean("custom_font", false))
				{
					newMessageText.setTypeface(MainActivity.font);
				}
				
				if (sharedPrefs.getString("card_theme", "Light").equals("Dark"))
				{
					newMessageText.setTextColor(context.getResources().getColor(R.color.card_dark_conversation_name));
				} else if (sharedPrefs.getString("card_theme", "Light").equals("Pitch Black"))
				{
					newMessageText.setTextColor(context.getResources().getColor(R.color.card_black_conversation_name));
				}
				
				if (MainActivity.fromNewMessageButton == true)
				{
					new Thread(new Runnable() {
						
						@Override
						public void run() {
							try {
								Thread.sleep(500);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
					}).start();
				}
				
			}
			
			return view;
		}
	}
	
	public class MessagePagerAdapter extends android.support.v13.app.FragmentStatePagerAdapter {
		
		public MessagePagerAdapter(android.app.FragmentManager fm) {
			super(fm);
		}

		@Override
		public MessageSectionFragment getItem(int position) {
			MessageSectionFragment fragment = new MessageSectionFragment();
			Bundle args = new Bundle();
			
			args.putInt("position", position);
			args.putStringArrayList("threadIds", threadIds);
			args.putStringArrayList("number", inboxNumber);
			args.putStringArrayList("body", inboxBody);
			args.putStringArrayList("group", group);
			args.putStringArrayList("count", msgCount);
			args.putStringArrayList("read", msgRead);
			
			fragment.setArguments(args);
			return fragment;
		}

		@Override
		public int getCount() {
            if (MainActivity.limitConversations) {
                if (threadIds.size() < 10) {
                    return threadIds.size() + 2;
                } else {
                    return 12;
                }
            } else {
                return threadIds.size() + 2;
            }
		}

		@Override
		public CharSequence getPageTitle(int position) {	
			return "";
		}
		
		@Override
		public int getItemPosition(Object object)
		{
			return POSITION_NONE;
		}
	}

	public static class MessageSectionFragment extends android.app.Fragment {
		
		public int position;
		public View view;
		public Context context;
		public ArrayList<String> threadIds, inboxNumber, inboxBody, group, count, read;
		public Cursor query2;
        public CustomListView messageList;
		
		public MessageSectionFragment() {
			
		}
		
		@Override
		public void onConfigurationChanged(Configuration newConfig) {
		  super.onConfigurationChanged(newConfig);
		}
		
		@Override
		public void onSaveInstanceState(Bundle outState) {
		    
		}
		
		@Override
		public void onCreate(Bundle savedInstanceState)
		{
			super.onCreate(savedInstanceState);
			
			Bundle args = getArguments();
			
			this.position = args.getInt("position");
			this.threadIds = args.getStringArrayList("threadIds");
			this.inboxNumber = args.getStringArrayList("number");
			this.inboxBody = args.getStringArrayList("body");
			this.group = args.getStringArrayList("group");
			this.read = args.getStringArrayList("read");
			this.count = args.getStringArrayList("count");
		}
		
		@Override
		public void onAttach(Activity activity)
		{
			super.onAttach(activity);
			context = activity;
		}
		
		@Override
		public void onDestroyView()
		{
			super.onDestroyView();
			
			try
			{
                if (!sharedPrefs.getBoolean("cache_conversations", false) || !CacheService.cached || !MainActivity.notChanged || !(position < sharedPrefs.getInt("num_cache_conversations", 5))) {
				    query2.close();
                }
			} catch (Exception e)
			{
				
			}
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			
			if (position > 0 && position < messagePagerAdapter.getCount() - 1)
			{
				view = inflater.inflate(R.layout.message_list_card, container, false);
			} else if (position == 0)
			{
				view = inflater.inflate(R.layout.custom_fonts, container, false);
			} else
			{
				if (sharedPrefs.getString("card_theme", "Light").equals("Light"))
				{
					view = inflater.inflate(R.layout.new_message_list_card, container, false);
				} else if (sharedPrefs.getString("card_theme", "Light").equals("Dark"))
				{
					view = inflater.inflate(R.layout.new_message_list_card_dark, container, false);
				} else if (sharedPrefs.getString("card_theme", "Light").equals("Pitch Black"))
				{
					view = inflater.inflate(R.layout.new_message_list_card_black, container, false);
				}
			}
				
			return refreshMessages();
		}		
		
		public View refreshMessages()
		{
			if (position > 0 && position < messagePagerAdapter.getCount() - 1)
			{
				position = position - 1;
				if (!MainActivity.isFastScrolling || (MainActivity.isFastScrolling && position >= MainActivity.scrollTo - 3 && position <= MainActivity.scrollTo + 2))
				{
                    new Thread(new Runnable() {

                        @Override
                        public void run() {
                            if (waitMessagePager)
                            {
                                try
                                {
                                    Thread.sleep(500);
                                } catch (Exception e)
                                {

                                }
                            }

                            if (position == 1)
                            {
                                waitMessagePager = true;
                            }

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

                            if (sharedPrefs.getBoolean("limit_messages", true) && !(MainActivity.loadAllMessages && position == MainActivity.loadAllMessagesPosition))
                            {
                                sortOrder += " limit 20";
                            }

                            if (MainActivity.loadAllMessages && position == MainActivity.loadAllMessagesPosition)
                            {
                                MainActivity.loadAllMessages = false;
                                MainActivity.loadAllMessagesPosition = -1;
                            }

                            if (!sharedPrefs.getBoolean("cache_conversations", false) || !CacheService.cached || !MainActivity.notChanged || !(position < sharedPrefs.getInt("num_cache_conversations", 5))) {
                                query2 = context.getContentResolver().query(uri3, projection2, null, null, sortOrder);
                            } else {
                                query2 = CacheService.conversations.get(position);
                            }

                            messageList = (CustomListView) view.findViewById(R.id.messageListView);
                            final MessageArrayAdapter adapter = new MessageArrayAdapter((Activity) context, findContactNumber(inboxNumber.get(position), context), threadIds.get(position), query2, position);
                            MainActivity.isFastScrolling = false;
                            MainActivity.scrollTo = 0;

                            ((Activity) context).getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {

                                @Override
                                public void run() {

                                    if (adapter.getCount() >= 20 && messageList.getHeaderViewsCount() == 0)
                                    {
                                        Button footer = new Button (context);
                                        int scale = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 14, context.getResources().getDisplayMetrics());
                                        footer.setPadding(0, scale, 0, scale);
                                        footer.setGravity(Gravity.CENTER);
                                        footer.setText(context.getResources().getString(R.string.load_all));
                                        footer.setTextColor(sharedPrefs.getInt("ct_draftTextColor", sharedPrefs.getInt("ct_sendButtonColor", context.getResources().getColor(R.color.black))));

                                        if (sharedPrefs.getString("card_theme", "Light").equals("Light"))
                                        {
                                            footer.setBackgroundResource(R.drawable.card_background);
                                        } else if (sharedPrefs.getString("card_theme", "Light").equals("Dark"))
                                        {
                                            footer.setBackgroundResource(R.drawable.card_background_dark);
                                        } else if (sharedPrefs.getString("card_theme", "Light").equals("Pitch Black"))
                                        {
                                            footer.setBackgroundResource(R.drawable.card_background_black);
                                        }

                                        footer.setOnClickListener(new OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                MainActivity.loadAllMessages = true;
                                                MainActivity.loadAllMessagesPosition = position;
                                                ((MainActivity)context).refreshViewPager(false);
                                                messagePager.setCurrentItem(position + 1, false);
                                            }
                                        });

                                        messageList.addHeaderView(footer);
                                    }

                                    messageList.setAdapter(adapter);
                                    messageList.setStackFromBottom(true);
                                    messageList.setDividerHeight(7);
                                    messageList.setPadding(0, 0, 0, 0);

                                    messageList.setOnSizeChangedListener(new CustomListView.OnSizeChangedListener() {
                                        public void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
                                            smoothScrollToEnd(false, height - oldHeight);
                                        }
                                    });

                                    if (!sharedPrefs.getBoolean("display_contact_cards", false))
                                    {
                                        int scale = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 17, context.getResources().getDisplayMetrics());
                                        int scale2 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, context.getResources().getDisplayMetrics());
                                        view.setPadding(scale, 0, scale, scale2);
                                    }
                                }
                            });

                        }

                    }).start();
				}
			} else if (position == 0)
			{
				MainActivity.isFastScrolling = false;
				MainActivity.scrollTo = 0;
				
				new Thread(new Runnable() {

					@Override
					public void run() {
						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						
						final ListView conversationList = (ListView) view.findViewById(R.id.fontListView);
						final MenuArrayAdapter adapter = new MenuArrayAdapter((Activity) context, inboxBody, inboxNumber, threadIds, group, count, read);
						
						((Activity) context).getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {
							
							@Override
							public void run() {
                                try {
                                    if (sharedPrefs.getBoolean("limit_conversations_start", true) && inboxNumber.size() > 10) {
                                        final Button footer = new Button(context);
                                        footer.setOnClickListener(new OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                conversationList.removeFooterView(footer);
                                                limitConversations = false;
                                                ((MainActivity)context).refreshViewPager(true);
                                            }
                                        });
                                        try {
                                            footer.setText(context.getResources().getString(R.string.load_all_messages));
                                        } catch (Exception e) {
                                            footer.setText("Load All Messages");
                                        }
                                        footer.setTypeface(font);
                                        footer.setBackgroundResource(android.R.color.transparent);
                                        if (sharedPrefs.getString("card_theme", "Light").equals("Light"))
                                        {
                                            footer.setTextColor(getResources().getColor(R.color.card_conversation_name));
                                        } else if (sharedPrefs.getString("card_theme", "Light").equals("Dark"))
                                        {
                                            footer.setTextColor(getResources().getColor(R.color.card_dark_conversation_name));
                                        } else if (sharedPrefs.getString("card_theme", "Light").equals("Pitch Black"))
                                        {
                                            footer.setTextColor(getResources().getColor(R.color.card_black_conversation_name));
                                        }
                                        conversationList.addFooterView(footer);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

								conversationList.setAdapter(adapter);
								conversationList.setDividerHeight(0);
								conversationList.setPadding(0, 10, 0, 10);
								conversationList.setVerticalScrollBarEnabled(false);
							}
						});
					}
					
				}).start();
			} else
			{
				MainActivity.isFastScrolling = false;
				MainActivity.scrollTo = 0;
				
				contactEntry = (EditText) view.findViewById(R.id.contactEntry);
				
				if (sharedPrefs.getBoolean("custom_font", false))
				{
					contactEntry.setTypeface(MainActivity.font);
				}
				
				if (sharedPrefs.getBoolean("simple_cards", true))
				{
					if (sharedPrefs.getString("card_theme", "Light").equals("Light"))
					{
						contactEntry.setBackgroundColor(context.getResources().getColor(R.color.white));
					} else if (sharedPrefs.getString("card_theme", "Light").equals("Dark"))
					{
						contactEntry.setBackgroundColor(context.getResources().getColor(R.color.card_dark_card_background));
					} else if (sharedPrefs.getString("card_theme", "Light").equals("Pitch Black"))
					{
						contactEntry.setBackgroundColor(context.getResources().getColor(R.color.card_black_card_background));
					}
				}
				
				contactEntry.addTextChangedListener(new TextWatcher() {
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
			        	
			        			Cursor people = context.getContentResolver().query(uri, projection, null, null, null);
			        	
			        			int indexName = people.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
			        			int indexNumber = people.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
			        	
			        			people.moveToFirst();
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
			        			people.close();
			        		} catch (IllegalArgumentException e)
			        		{
			        			
			        		}
			        		
			        		firstContactSearch = false;
			        	}
			        }

			        @SuppressLint("DefaultLocale")
					public void onTextChanged(CharSequence s, int start, int before, int count) {
			        	ArrayList<String> searchedNames = new ArrayList<String>();
			        	ArrayList<String> searchedNumbers = new ArrayList<String>();
			        	ArrayList<String> searchedTypes = new ArrayList<String>();
			        	
			        	String text = contactEntry.getText().toString();
			        	
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
                                try {
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
                                } catch (Exception f) {

                                }
					    	}
					    }
			        	
				        ListView searchView = (ListView) view.findViewById(R.id.contactSearchList);
				        ContactSearchArrayAdapter adapter;
				        
				        if (text.length() != 0)
				        {
			        		adapter = new ContactSearchArrayAdapter((Activity)context, searchedNames, searchedNumbers, searchedTypes);
				        } else
				        {
			        		adapter = new ContactSearchArrayAdapter((Activity)context, new ArrayList<String>(), new ArrayList<String>(), new ArrayList<String>());
				        }
			        	
			        	searchView.setAdapter(adapter);
			        	searchView.bringToFront();
			        	
			        	searchView.setOnItemClickListener(new OnItemClickListener() {

							@Override
							public void onItemClick(AdapterView<?> arg0, View arg1,
									int position, long arg3) {
								TextView view2 = (TextView) arg1.findViewById(R.id.receivedMessage);
								
								String[] t1 = contactEntry.getText().toString().split("; ");
								String string = "";
								
								for (int i = 0; i < t1.length - 1; i++)
								{
									string += t1[i] + "; ";
								}
								
								contactEntry.setText(string + view2.getText() + "; ");
								contactEntry.setSelection(contactEntry.getText().toString().length());
								
								if (contactEntry.getText().toString().length() <= 12)
								{
									messageEntry.requestFocus();
								}
								
							}
			        		
			        	});
			        }

			        public void afterTextChanged(Editable s) {
			        }
				});
			}
			
			return view;
		}

        private int mLastSmoothScrollPosition;

        private void smoothScrollToEnd(boolean force, int listSizeChange) {
            int lastItemVisible = messageList.getLastVisiblePosition();
            int lastItemInList = messageList.getAdapter().getCount() - 1;
            if (lastItemVisible < 0 || lastItemInList < 0) {
                return;
            }

            View lastChildVisible =
                    messageList.getChildAt(lastItemVisible - messageList.getFirstVisiblePosition());
            int lastVisibleItemBottom = 0;
            int lastVisibleItemHeight = 0;
            if (lastChildVisible != null) {
                lastVisibleItemBottom = lastChildVisible.getBottom();
                lastVisibleItemHeight = lastChildVisible.getHeight();
            }

            int listHeight = messageList.getHeight();
            boolean lastItemTooTall = lastVisibleItemHeight > listHeight;
            boolean willScroll = force ||
                    ((listSizeChange != 0 || lastItemInList != mLastSmoothScrollPosition) &&
                            lastVisibleItemBottom + listSizeChange <=
                                    listHeight - messageList.getPaddingBottom());
            if (willScroll || (lastItemTooTall && lastItemInList == lastItemVisible)) {
                if (Math.abs(listSizeChange) > 200) {
                    if (lastItemTooTall) {
                        messageList.setSelectionFromTop(lastItemInList,
                                listHeight - lastVisibleItemHeight);
                    } else {
                        messageList.setSelection(lastItemInList);
                    }
                } else if (lastItemInList - lastItemVisible > 20) {
                    messageList.setSelection(lastItemInList);
                } else {
                    if (lastItemTooTall) {
                        messageList.setSelectionFromTop(lastItemInList,
                                listHeight - lastVisibleItemHeight);
                    } else {
                        messageList.smoothScrollToPosition(lastItemInList);
                    }
                    mLastSmoothScrollPosition = lastItemInList;
                }
            }
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
			Cursor phonesCursor = context.getContentResolver().query(phoneUri, new String[] {ContactsContract.Contacts.DISPLAY_NAME_PRIMARY}, null, null, ContactsContract.Contacts.DISPLAY_NAME + " desc limit 1");

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

	private void readFromFile3(Context context) {
	      
	      try {
	          InputStream inputStream = context.openFileInput("conversationList.txt");
	          
	          if ( inputStream != null ) {
	          	InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
	          	BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
	          	String receiveString = "";
	          	
	          	while ( (receiveString = bufferedReader.readLine()) != null ) {
	          		threadIds.add(receiveString);
	          		
	          		receiveString = bufferedReader.readLine();
	          		try
	          		{
	          			Integer.parseInt(receiveString);
		          		msgCount.add(receiveString);
	          		} catch (Exception e)
	          		{
	          			msgCount.add("1");
	          		}
	          		
	          		receiveString = bufferedReader.readLine();
	          		try
	          		{
	          			Integer.parseInt(receiveString);
		          		msgRead.add(receiveString);
	          		} catch (Exception e)
	          		{
	          			msgRead.add("1");
	          		}
					
	          		if ( (receiveString = bufferedReader.readLine()) != null )
	          		{
	          			inboxBody.add(receiveString);
	          		} else
	          		{
	          			inboxBody.add("error");
	          		}
	          		
	          		receiveString = bufferedReader.readLine();
	          		try
	          		{
	          			Long.parseLong(receiveString);
		          		inboxDate.add(receiveString);
	          		} catch (Exception e)
	          		{
	          			Calendar cal = Calendar.getInstance();
	          			inboxDate.add(cal.getTimeInMillis() + "");
	          		}
	          		
	          		if ( (receiveString = bufferedReader.readLine()) != null )
	          		{
	          			inboxNumber.add(receiveString);
	          		} else
	          		{
	          			inboxNumber.add("1");
	          		}
	          		
	          		receiveString = bufferedReader.readLine();
	          		if (receiveString != null)
	          		{
		          		if (receiveString.equals("yes") || receiveString.equals("no"))
		          		{
		          			group.add(receiveString);
		          		} else
		          		{
		          			group.add("no");
		          		}
	          		} else
	          		{
	          			group.add("no");
	          		}
	          	}
	          	
	          	inputStream.close();
	          }
	      }
	      catch (FileNotFoundException e) {
	      	
			} catch (IOException e) {
				
			}
		}
	
	public static Bitmap getFacebookPhoto(String phoneNumber, Context context) {
		  try
		  {
		    Uri phoneUri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
		    Uri photoUri = null;
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
	
	public void deleteSMS(Context context, String threadId) {
	    try {
	        	context.getContentResolver().delete(Uri.parse("content://mms-sms/conversations/" + threadId + "/"), null, null);
	    } catch (Exception e) {
	    	Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show();
	    }
	}
	
	public static void deleteSMS(Context context) {
		ArrayList<String> threadIds = new ArrayList<String>();
		String[] projection = new String[]{"_id"};
		Uri uri = Uri.parse("content://mms-sms/conversations/?simple=true");
		Cursor query = context.getContentResolver().query(uri, projection, null, null, null);
		
		if (query.moveToFirst())
		{
			do
			{
				threadIds.add(query.getString(query.getColumnIndex("_id")));
			} while (query.moveToNext());
		}
		
		query.close();
		
	    try {
	    	for (int i = 0; i < threadIds.size(); i++)
	    	{
	        	context.getContentResolver().delete(Uri.parse("content://mms-sms/conversations/" + threadIds.get(i) + "/"), null, null);
	    	}
	    } catch (Exception e) {
	    }
	}
	
	private static void writeToFile3(ArrayList<String> data, Context context) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("conversationList.txt", Context.MODE_PRIVATE));
            
            for (int i = 0; i < data.size(); i++)
            {
            	outputStreamWriter.write(data.get(i) + "\n");
            }
            	
            outputStreamWriter.close();
        }
        catch (IOException e) {
            
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
	    		return BitmapFactory.decodeResource(getResources(), R.drawable.ic_contact_dark);
	    	} else
	    	{
	    		return BitmapFactory.decodeResource(getResources(), R.drawable.ic_contact_picture);
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
	
	public String getPath(Uri uri) {
		String[] projection = { MediaStore.Images.Media.DATA };
		Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
		int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();
		String path = cursor.getString(column_index);
		cursor.close();
		return path;
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

    private void setMobileDataEnabled(Context context, boolean enabled) {
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
			currentWifiState = wifi.isWifiEnabled();
			currentWifi = wifi.getConnectionInfo();
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
					NetworkInfo mNetworkInfo = (NetworkInfo) intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
					
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
				
//					ConnectivityManager mConnMgr =  (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
//					mConnMgr.startUsingNetworkFeature(ConnectivityManager.TYPE_MOBILE, "enableMMS");
					
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
							    wifi.reconnect();
                                setMobileDataEnabled(context, currentDataState);
							}
						}
						
					};
					
					registerReceiver(receiver, filter);
				} catch (Exception e) {
					e.printStackTrace();
					
					if (sharedPrefs.getBoolean("wifi_mms_fix", true))
					{
                        try {
                            context.unregisterReceiver(discon);
                        } catch (Exception f) {

                        }

						WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
						wifi.setWifiEnabled(false);
					    wifi.setWifiEnabled(currentWifiState);
						wifi.reconnect();
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

            if (counter && returnArray.length > 1) {
                for (int i = 0; i < returnArray.length; i++) {
                    returnArray[i] = "(" + (i+1) + "/" + returnArray.length + ") " + returnArray[i];
                }
            }

	  	    return returnArray;
	  	}

}
