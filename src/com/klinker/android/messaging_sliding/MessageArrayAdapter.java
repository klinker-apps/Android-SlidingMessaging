package com.klinker.android.messaging_sliding;

import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import com.android.mms.transaction.HttpUtils;
import com.android.mms.util.SendingProgressTokenManager;
import com.google.android.mms.APN;
import com.google.android.mms.APNHelper;
import com.google.android.mms.pdu.PduParser;
import com.google.android.mms.pdu.PduPersister;
import com.google.android.mms.pdu.RetrieveConf;
import com.klinker.android.messaging_donate.receivers.DisconnectWifi;
import com.klinker.android.messaging_donate.R;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.regex.*;

import android.view.WindowManager;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SqliteWrapper;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.PorterDuff.Mode;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.PhoneLookup;
import android.provider.Telephony.Mms;
import android.provider.Telephony.Mms.Inbox;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.telephony.SmsManager;
import android.text.Html;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.Html.ImageGetter;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.QuickContactBadge;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.klinker.android.messaging_donate.StripAccents;
import com.klinker.android.messaging_sliding.emojis.*;
import com.klinker.android.messaging_sliding.receivers.NotificationReceiver;

public class MessageArrayAdapter extends ArrayAdapter<String> {
  private final Activity context;
  private final String myId;
  private final String inboxNumbers;
  private final int threadPosition;
  private final String threadIds;
  private final Bitmap contactImage;
  private final Bitmap myImage;
  private SharedPreferences sharedPrefs;
  private ContentResolver contentResolver;
  private final Cursor query;
  private Paint paint;
  private int width;
  private Typeface font;

  public DisconnectWifi discon;
  public WifiInfo currentWifi;
  public boolean currentWifiState;
  public boolean currentDataState;
  
  static class ViewHolder {
	    public TextView text;
	    public TextView text2;
	    public QuickContactBadge image;
	    public QuickContactBadge image2;
	    public View view1;
	    public View view2;
	    public View view3;
	    public ImageView view4;
	    public Button downloadButton;
	    public ImageView bubble1;
	    public ImageView bubble2;
	  }

  @SuppressWarnings("deprecation")
public MessageArrayAdapter(Activity context, String myId, String inboxNumbers, String ids, Cursor query, String myPhoneNumber, int threadPosition) {
    super(context, R.layout.message_body, new ArrayList<String>());
    this.context = context;
    this.myId = myId;
    this.inboxNumbers = inboxNumbers;
    this.threadPosition = threadPosition;
    this.threadIds = ids;
    this.sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
    this.query = query;
    this.contentResolver = context.getContentResolver();
    
    Bitmap input;
    
    try
    {
    	input = getFacebookPhoto(inboxNumbers);
    } catch (NumberFormatException e)
    {
    	input = null;
    }
	
	if (input == null)
	{
		if (sharedPrefs.getBoolean("ct_darkContactImage", false))
		{
			input = drawableToBitmap(context.getResources().getDrawable(R.drawable.ic_contact_dark));
		} else
		{
			input = drawableToBitmap(context.getResources().getDrawable(R.drawable.ic_contact_picture));
		}
	}
	
	contactImage = Bitmap.createScaledBitmap(input, MainActivity.contactWidth, MainActivity.contactWidth, true);
	
	InputStream input2;
	
	try
    {
		input2 = openDisplayPhoto(Long.parseLong(this.myId));
    } catch (NumberFormatException e)
    {
    	input2 = null;
    }
	
	  if (input2 == null)
	  {
		  if (sharedPrefs.getBoolean("ct_darkContactImage", false))
		  {
		  	input2 = context.getResources().openRawResource(R.drawable.ic_contact_dark);
		  } else
		  {
			  input2 = context.getResources().openRawResource(R.drawable.ic_contact_picture);
		  }
	  }
	  
	  Bitmap im;
	  
	  try
	  {
		  im = Bitmap.createScaledBitmap(BitmapFactory.decodeStream(input2), MainActivity.contactWidth, MainActivity.contactWidth, true);
	  } catch (Exception e)
	  {
		  if (sharedPrefs.getBoolean("ct_darkContactImage", false))
		  {
			  im = Bitmap.createScaledBitmap(drawableToBitmap(context.getResources().getDrawable(R.drawable.ic_contact_dark)), MainActivity.contactWidth, MainActivity.contactWidth, true);
		  } else
		  {
			  im = Bitmap.createScaledBitmap(drawableToBitmap(context.getResources().getDrawable(R.drawable.ic_contact_picture)), MainActivity.contactWidth, MainActivity.contactWidth, true);
		  }
	  }
	  
	  myImage = im;
		
	  Display d = ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
	  this.width = d.getWidth() - MainActivity.contactWidth - 15;
	  paint = new Paint();
	  float densityMultiplier = getContext().getResources().getDisplayMetrics().density;
	  float scaledPx = Integer.parseInt(sharedPrefs.getString("text_size", "14")) * densityMultiplier;
	  paint.setTextSize(scaledPx);
	  font = null;
	  
	  if (sharedPrefs.getBoolean("custom_font", false))
	  {
		  font = Typeface.createFromFile(sharedPrefs.getString("custom_font_path", ""));
	  	  paint.setTypeface(font);
	  }
  }
  
  @Override
  public int getCount()
  {
	  try
	  {
		  return query.getCount();
	  } catch (Exception e)
	  {
		  return 0;
	  }
  }

  @SuppressLint("SimpleDateFormat")
  @Override
  public View getView(final int position, View convertView, ViewGroup parent) {
	  View rowView = convertView;
	  
	  if (rowView == null)
	  {
		  LayoutInflater inflater = (LayoutInflater) context
			        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		  rowView = inflater.inflate(R.layout.message_body, parent, false);
		  final ViewHolder viewHolder = new ViewHolder();
		  viewHolder.text = (TextView) rowView.findViewById(R.id.messageText1);
		  viewHolder.text2 = (TextView) rowView.findViewById(R.id.date1);
		  viewHolder.image = (QuickContactBadge) rowView.findViewById(R.id.quickContactBadge1);
		  viewHolder.image2 = (QuickContactBadge) rowView.findViewById(R.id.quickContactBadge2);
		  viewHolder.view1 = (View) rowView.findViewById(R.id.view1);
		  viewHolder.view2 = (View) rowView.findViewById(R.id.sentBackground);
		  viewHolder.view3 = (View) rowView.findViewById(R.id.view3);
		  viewHolder.view4 = (ImageView) rowView.findViewById(R.id.messageImage);
		  viewHolder.downloadButton = (Button) rowView.findViewById(R.id.downloadButton);
		  viewHolder.bubble1 = (ImageView) rowView.findViewById(R.id.contactBubble1);
		  viewHolder.bubble2 = (ImageView) rowView.findViewById(R.id.contactBubble2);
		  
		  viewHolder.image.assignContactFromPhone(inboxNumbers, true);
		  viewHolder.image.setMode(ContactsContract.QuickContact.MODE_LARGE);
		  viewHolder.image2.assignContactUri(ContactsContract.Profile.CONTENT_URI);
		  viewHolder.image2.setMode(ContactsContract.QuickContact.MODE_LARGE);
		  
		  if (sharedPrefs.getBoolean("custom_font", false))
	      {
	    	  viewHolder.text.setTypeface(font);
	    	  viewHolder.text2.setTypeface(font);
	      }
		  
		  viewHolder.image.setOnClickListener(new OnClickListener()
		  {

			@Override
			public void onClick(View arg0) {
				try
				{
					viewHolder.image.onClick(viewHolder.image);
				} catch (Exception e)
				{
					
				}
				
			}
			  
		  });
		  
		  viewHolder.image2.setOnClickListener(new OnClickListener()
		  {

			@Override
			public void onClick(View arg0) {
				try
				{
					viewHolder.image2.onClick(viewHolder.image2);
				} catch (Exception e)
				{
					
				}
				
			}
			  
		  });
		  
		  if (sharedPrefs.getBoolean("contact_pictures", true))
		  {
			  viewHolder.image.setImageBitmap(contactImage);
			  viewHolder.image2.setImageBitmap(myImage);
		  }
		  
		  viewHolder.text.setTextSize(Integer.parseInt(sharedPrefs.getString("text_size", "14").substring(0,2)));
		  viewHolder.text2.setTextSize(Integer.parseInt(sharedPrefs.getString("text_size", "14").substring(0,2)) - 4);
		  
		  if (sharedPrefs.getBoolean("tiny_date", false))
		  {
			  viewHolder.text2.setTextSize(10);
		  }
		  
		  viewHolder.text.setText("");
		  viewHolder.text2.setText("");
		  
		  viewHolder.text2.setAlpha((float) .5);
		  
		  rowView.setTag(viewHolder);
	  }
	  
	  final ViewHolder holder = (ViewHolder) rowView.getTag();
	  
	  boolean sent = false;
	  boolean mms;
	  String image;
	  String video = "";
	  String body;
	  String date;
	  String id;
	  boolean sending = false;
	  boolean error = false;
	  boolean group = false;
	  String sender = "";
	  String status = "-1";
	  String location;
	  
	  String dateType = "date";
	  
	  if (sharedPrefs.getBoolean("show_original_timestamp", false))
	  {
		  dateType = "date_sent";
	  }
	  
	  try
	  {
		  query.moveToPosition(getCount() - 1 - position);

          String s = query.getString(query.getColumnIndex("msg_box"));

          if (s != null) {
				id = query.getString(query.getColumnIndex("_id"));
				mms = true;
				body = "";
				image = null;
				video = null;
				
				date = Long.parseLong(query.getString(query.getColumnIndex("date"))) * 1000 + "";
				
				String number = getAddressNumber(Integer.parseInt(query.getString(query.getColumnIndex("_id")))).trim();
				
				String[] numbers = number.split(" ");
				
				if (query.getInt(query.getColumnIndex("msg_box")) == 4)
				{
					sending = true;
					sent = true;
				} else if (query.getInt(query.getColumnIndex("msg_box")) == 5)
				{
					error = true;
					sent = true;
				} else if (query.getInt(query.getColumnIndex("msg_box")) == 1)
				{
					sent = false;

                    if (query.getInt(query.getColumnIndex("read")) == 0)
                    {
                        String SmsMessageId = query.getString(query.getColumnIndex("_id"));
                        ContentValues values = new ContentValues();
                        values.put("read", true);
                        contentResolver.update(Uri.parse("content://mms/inbox"), values, "_id=" + SmsMessageId, null);
                    }
				} else if (query.getInt(query.getColumnIndex("msg_box")) == 2)
				{
					sent = true;
				}
				
				if (numbers.length > 2)
				{
					group = true;
					sender = numbers[0];
				}
				
	        	String selectionPart = "mid=" + query.getString(query.getColumnIndex("_id"));
	        	Uri uri = Uri.parse("content://mms/part");
	        	Cursor cursor = contentResolver.query(uri, null, selectionPart, null, null);
	        	
	        	if (cursor.moveToFirst()) {
	        	    do {
	        	        String partId = cursor.getString(cursor.getColumnIndex("_id"));
	        	        String type = cursor.getString(cursor.getColumnIndex("ct"));
	        	        String body2 = "";
	        	        if ("text/plain".equals(type)) {
	        	            String data = cursor.getString(cursor.getColumnIndex("_data"));
	        	            if (data != null) {
	        	                body2 = getMmsText(partId, context);
	        	                body += body2;
	        	            } else {
	        	                body2 = cursor.getString(cursor.getColumnIndex("text"));
	        	                body += body2;
	        	                
	        	            }
	        	        }
	        	        
	        	        if (sharedPrefs.getBoolean("enable_mms", false))
	    		    	{
		        	        if ("image/jpeg".equals(type) || "image/bmp".equals(type) ||
		        	                "image/gif".equals(type) || "image/jpg".equals(type) ||
		        	                "image/png".equals(type)) {
		        	        	if (image == null)
		        	        	{
		        	        		image = "content://mms/part/" + partId;
		        	        	} else
		        	        	{
		        	        		image += " content://mms/part/" + partId;
		        	        	}
		        	        }
		        	        
		        	        if ("video/mpeg".equals(type) || "video/3gpp".equals(type))
		        	        {
		        	        	video = "content://mms/part/" + partId;
		        	        }
	    		    	}
	        	    } while (cursor.moveToNext());
	        	}
	        	
	        	cursor.close();
		    } else {
		    	String type = query.getString(query.getColumnIndex("type"));
		
				if (type.equals("1"))
				{
					sent = false;

                    try
                    {
					    body = query.getString(query.getColumnIndex("body"));
                    } catch (Exception e)
                    {
                        body = "";
                    }

					date = query.getString(query.getColumnIndex(dateType));
					id = query.getString(query.getColumnIndex("_id"));
					mms = false;
					image = null;

                    if (query.getInt(query.getColumnIndex("read")) == 0)
                    {
                        String SmsMessageId = query.getString(query.getColumnIndex("_id"));
                        ContentValues values = new ContentValues();
                        values.put("read", true);
                        contentResolver.update(Uri.parse("content://sms/inbox"), values, "_id=" + SmsMessageId, null);
                    }
				} else if (type.equals("2"))
				{
					sent = true;
					body = query.getString(query.getColumnIndex("body"));
					date = query.getString(query.getColumnIndex("date"));
					id = query.getString(query.getColumnIndex("_id"));
					mms = false;
					image = null;

                    if (sharedPrefs.getBoolean("delivery_reports", false)) {
                        status = query.getString(query.getColumnIndex("status"));

                        if (status.equals("64") || status.equals("128"))
                        {
                            error = true;
                        }
                    }
				} else if (type.equals("5"))
				{
					sent = true;
					body = query.getString(query.getColumnIndex("body"));
					date = query.getString(query.getColumnIndex("date"));
					id = query.getString(query.getColumnIndex("_id"));
					mms = false;
					image = null;
					error = true;
				} else if (type.equals("4") || type.equals("6"))
				{
					sent = true;
					body = query.getString(query.getColumnIndex("body"));
					date = query.getString(query.getColumnIndex("date"));
					id = query.getString(query.getColumnIndex("_id"));
					mms = false;
					image = null;
					sending = true;
				} else
				{
					sent = false;
					body = query.getString(query.getColumnIndex("body"));
					date = query.getString(query.getColumnIndex(dateType));
					id = query.getString(query.getColumnIndex("_id"));
					mms = false;
					image = null;
				}
		    }
	  } catch (Exception e)
	  {
		  e.printStackTrace();
		  
		  id = query.getString(query.getColumnIndex("_id"));
		  Cursor locationQuery = context.getContentResolver().query(Uri.parse("content://mms/"), new String[] {"m_size", "exp", "ct_l", "_id"}, "_id=?", new String[]{id}, null);

          if (locationQuery.moveToFirst()) {
              String exp = "1";
              String size = "1";

              try
              {
                  size = locationQuery.getString(locationQuery.getColumnIndex("m_size"));
                  exp = locationQuery.getString(locationQuery.getColumnIndex("exp"));
              } catch (Exception f)
              {

              }

              location = locationQuery.getString(locationQuery.getColumnIndex("ct_l"));

              holder.image.setVisibility(View.VISIBLE);
              holder.bubble1.setVisibility(View.VISIBLE);
              holder.image2.setVisibility(View.GONE);
              holder.bubble2.setVisibility(View.GONE);
              holder.view4.setVisibility(View.GONE);
              holder.text.setText("");
              holder.text.setGravity(Gravity.CENTER);
              holder.view1.setVisibility(View.VISIBLE);

              holder.text.setTextColor(sharedPrefs.getInt("ct_receivedTextColor", context.getResources().getColor(R.color.black)));
              holder.text2.setTextColor(sharedPrefs.getInt("ct_receivedTextColor", context.getResources().getColor(R.color.black)));
              holder.view1.setBackgroundColor(sharedPrefs.getInt("ct_receivedMessageBackground", context.getResources().getColor(R.color.white)));
              holder.view2.setBackgroundColor(sharedPrefs.getInt("ct_receivedMessageBackground", context.getResources().getColor(R.color.white)));
              holder.view3.setBackgroundColor(sharedPrefs.getInt("ct_receivedMessageBackground", context.getResources().getColor(R.color.white)));
              holder.view4.setBackgroundColor(sharedPrefs.getInt("ct_receivedMessageBackground", context.getResources().getColor(R.color.white)));
              holder.bubble1.setColorFilter(sharedPrefs.getInt("ct_receivedMessageBackground", context.getResources().getColor(R.color.white)));
              holder.bubble2.setColorFilter(sharedPrefs.getInt("ct_receivedMessageBackground", context.getResources().getColor(R.color.white)));
              holder.text2.setText("");

              try
              {
                  holder.text2.setText("Message size: " + (int)(Double.parseDouble(size)/1000) + " KB Expires: " +  DateFormat.getDateInstance(DateFormat.MEDIUM).format(new Date(Long.parseLong(exp) * 1000)));
                  holder.downloadButton.setVisibility(View.VISIBLE);
              } catch (Exception f)
              {
                  holder.text2.setText("Error loading message.");
                  holder.downloadButton.setVisibility(View.GONE);
              }

              holder.text2.setGravity(Gravity.LEFT);

              final String downloadLocation = location;
              final String msgId = id;

              holder.downloadButton.setOnClickListener(new OnClickListener() {

                  @Override
                  public void onClick(View v) {
                      if (sharedPrefs.getBoolean("enable_mms", false))
                      {
                          holder.downloadButton.setVisibility(View.INVISIBLE);

                          if (sharedPrefs.getBoolean("wifi_mms_fix", true))
                          {
                              WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                              currentWifi = wifi.getConnectionInfo();
                              currentWifiState = wifi.isWifiEnabled();
                              wifi.disconnect();
                              discon = new DisconnectWifi();
                              context.registerReceiver(discon, new IntentFilter(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION));
                              currentDataState = MainActivity.isMobileDataEnabled(context);
                              MainActivity.setMobileDataEnabled(context, true);
                          }

                          ConnectivityManager mConnMgr =  (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
                          final int result = mConnMgr.startUsingNetworkFeature(ConnectivityManager.TYPE_MOBILE, "enableMMS");

                          if (result != 0)
                          {
                              IntentFilter filter = new IntentFilter();
                              filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
                              BroadcastReceiver receiver = new BroadcastReceiver() {

                                  @Override
                                  public void onReceive(final Context context, Intent intent) {
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
                                          new Thread(new Runnable() {

                                              @Override
                                              public void run() {
                                                  List<APN> apns = new ArrayList<APN>();

                                                  try
                                                  {
                                                      APNHelper helper = new APNHelper(context);
                                                      apns = helper.getMMSApns();

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
                                                      byte[] resp = HttpUtils.httpConnection(
                                                              context, SendingProgressTokenManager.NO_TOKEN,
                                                              downloadLocation, null, HttpUtils.HTTP_GET_METHOD,
                                                              !TextUtils.isEmpty(apns.get(0).MMSProxy),
                                                              apns.get(0).MMSProxy,
                                                              Integer.parseInt(apns.get(0).MMSPort));

                                                      boolean groupMMS = false;

                                                      if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1 && sharedPrefs.getBoolean("group_message", false))
                                                      {
                                                          groupMMS = true;
                                                      }

                                                      RetrieveConf retrieveConf = (RetrieveConf) new PduParser(resp).parse();
                                                      PduPersister persister = PduPersister.getPduPersister(context);
                                                      Uri msgUri = persister.persist(retrieveConf, Inbox.CONTENT_URI, true,
                                                              groupMMS, null);

                                                      ContentValues values = new ContentValues(1);
                                                      values.put(Mms.DATE, System.currentTimeMillis() / 1000L);
                                                      SqliteWrapper.update(context, context.getContentResolver(),
                                                              msgUri, values, null, null);
                                                      SqliteWrapper.delete(context, context.getContentResolver(),
                                                              Uri.parse("content://mms/"), "thread_id=? and _id=?", new String[] {threadIds, msgId});

                                                      ((Activity) context).getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {

                                                          @Override
                                                          public void run() {
                                                              ((MainActivity) context).refreshViewPager3();
                                                          }
                                                      });
                                                  } catch (Exception e) {
                                                      e.printStackTrace();

                                                      ((Activity) context).getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {

                                                          @Override
                                                          public void run() {
                                                              holder.downloadButton.setVisibility(View.VISIBLE);
                                                          }
                                                      });
                                                  }

                                                  if (sharedPrefs.getBoolean("wifi_mms_fix", true))
                                                  {
                                                      context.unregisterReceiver(discon);
                                                      WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                                                      wifi.setWifiEnabled(false);
                                                      wifi.setWifiEnabled(currentWifiState);
                                                      Log.v("Reconnect", "" + wifi.reconnect());
                                                      MainActivity.setMobileDataEnabled(context, currentDataState);
                                                  }

                                              }

                                          }).start();

                                          context.unregisterReceiver(this);
                                      }

                                  }

                              };

                              context.registerReceiver(receiver, filter);
                          } else
                          {
                              new Thread(new Runnable() {

                                  @Override
                                  public void run() {
                                      List<APN> apns = new ArrayList<APN>();

                                      try
                                      {
                                          APNHelper helper = new APNHelper(context);
                                          apns = helper.getMMSApns();

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
                                          byte[] resp = HttpUtils.httpConnection(
                                                  context, SendingProgressTokenManager.NO_TOKEN,
                                                  downloadLocation, null, HttpUtils.HTTP_GET_METHOD,
                                                  !TextUtils.isEmpty(apns.get(0).MMSProxy),
                                                  apns.get(0).MMSProxy,
                                                  Integer.parseInt(apns.get(0).MMSPort));

                                          boolean groupMMS = false;

                                          if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1 && sharedPrefs.getBoolean("group_message", false))
                                          {
                                              groupMMS = true;
                                          }

                                          RetrieveConf retrieveConf = (RetrieveConf) new PduParser(resp).parse();
                                          PduPersister persister = PduPersister.getPduPersister(context);
                                          Uri msgUri = persister.persist(retrieveConf, Inbox.CONTENT_URI, true,
                                                  groupMMS, null);

                                          ContentValues values = new ContentValues(1);
                                          values.put(Mms.DATE, System.currentTimeMillis() / 1000L);
                                          SqliteWrapper.update(context, context.getContentResolver(),
                                                  msgUri, values, null, null);
                                          SqliteWrapper.delete(context, context.getContentResolver(),
                                                  Uri.parse("content://mms/"), "thread_id=? and _id=?", new String[] {threadIds, msgId});

                                          ((Activity) context).getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {

                                              @Override
                                              public void run() {
                                                  ((MainActivity) context).refreshViewPager3();
                                              }
                                          });
                                      } catch (Exception e) {
                                          e.printStackTrace();

                                          ((Activity) context).getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {

                                              @Override
                                              public void run() {
                                                  holder.downloadButton.setVisibility(View.VISIBLE);
                                              }
                                          });
                                      }

                                      if (sharedPrefs.getBoolean("wifi_mms_fix", true))
                                      {
                                          context.unregisterReceiver(discon);
                                          WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                                          wifi.setWifiEnabled(false);
                                          wifi.setWifiEnabled(currentWifiState);
                                          Log.v("Reconnect", "" + wifi.reconnect());
                                          MainActivity.setMobileDataEnabled(context, currentDataState);
                                      }

                                  }

                              }).start();
                          }
                      } else
                      {
                          Toast.makeText(context, "Enable MMS first in settings.", Toast.LENGTH_LONG).show();
                      }

                  }

              });

              rowView.setPadding(0,0,0,0);

              if (position == getCount() - 1)
              {
                  rowView.setPadding(0,0,0,5);
              }
          }
		  
		  return rowView;
	  }
	  
	  holder.downloadButton.setVisibility(View.GONE);
	  
	  if (group)
	  {
		  holder.image.assignContactFromPhone(sender, false);
		  holder.image.setMode(ContactsContract.QuickContact.MODE_LARGE);
		  holder.image.setImageBitmap(Bitmap.createScaledBitmap(getFacebookPhoto(sender), MainActivity.contactWidth, MainActivity.contactWidth, true));
	  }
	  
	  Date date2;
	  
	  try
	  {
		  date2 = new Date(Long.parseLong(date));
	  } catch (Exception e)
	  {
		  date2 = new Date(0);
	  }
	  
	  Calendar cal = Calendar.getInstance();
	  Date currentDate = new Date(cal.getTimeInMillis());
	  
	  if (getZeroTimeDate(date2).equals(getZeroTimeDate(currentDate)))
	  {
		  if (sharedPrefs.getBoolean("hour_format", false))
		  {
			  holder.text2.setText(DateFormat.getTimeInstance(DateFormat.SHORT, Locale.GERMAN).format(date2));
		  } else
		  {
			  holder.text2.setText(DateFormat.getTimeInstance(DateFormat.SHORT, Locale.US).format(date2));
		  }
	  } else
	  {
		  if (sharedPrefs.getBoolean("hour_format", false))
		  {
			  holder.text2.setText(DateFormat.getTimeInstance(DateFormat.SHORT, Locale.GERMAN).format(date2) + ", " + DateFormat.getDateInstance(DateFormat.MEDIUM).format(date2));
		  } else
		  {
			  holder.text2.setText(DateFormat.getTimeInstance(DateFormat.SHORT, Locale.US).format(date2) + ", " + DateFormat.getDateInstance(DateFormat.MEDIUM).format(date2));
		  }
	  }
	  
	  if (sending == true)
	  {
		  holder.text2.setText(context.getResources().getString(R.string.sending));
	  } else
	  {
		  if (sent == true && sharedPrefs.getBoolean("delivery_reports", false) && error == false && status.equals("0"))
		  {
			  String text = "<html><body><img src=\"ic_sent.png\"/> " + holder.text2.getText().toString() + "</body></html>";
			  holder.text2.setText(Html.fromHtml(text, imgGetterSent, null));
		  }
	  }
	  
	  if (group == true && sent == false)
	  {
		  Uri phoneUri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(sender.replaceAll("-", "")));
		  Cursor phonesCursor = context.getContentResolver().query(phoneUri, new String[] {ContactsContract.Contacts.DISPLAY_NAME_PRIMARY, ContactsContract.RawContacts._ID}, null, null, ContactsContract.Contacts.DISPLAY_NAME + " desc limit 1");
	  
		  if(phonesCursor != null && phonesCursor.moveToFirst()) {
				holder.text2.setText(holder.text2.getText() + " - " + phonesCursor.getString(0));
			} else
			{
				holder.text2.setText(holder.text2.getText() + " - " + sender);
			}
		  
		  phonesCursor.close();
	  }

	  try
	  {
		  if (sent == false)
		  {
				  holder.image.setVisibility(View.VISIBLE);
				  holder.image2.setVisibility(View.GONE);
				  holder.bubble2.setVisibility(View.GONE);
				  holder.bubble1.setVisibility(View.VISIBLE);
				  
		  } else
		  {
				  holder.image2.setVisibility(View.VISIBLE);
				  holder.image.setVisibility(View.GONE);
				  holder.bubble1.setVisibility(View.GONE);
				  holder.bubble2.setVisibility(View.VISIBLE);
		  }
		  
		  if (mms)
		  {
			  holder.view4.setVisibility(View.VISIBLE);
			  
			  if (image != null)
			  {
				  String images[] = image.trim().split(" ");
				  
				  if (images.length == 1)
				  {
					  try
					  {
						  holder.view4.setImageURI(Uri.parse(image.trim()));
					  } catch (OutOfMemoryError e)
					  {
						  holder.view4.setImageBitmap(decodeFile(new File(getRealPathFromURI(Uri.parse(image.trim())))));
					  }
					  
					  final String image2 = image;
					  
					  holder.view4.setOnClickListener(new OnClickListener() {
		
						@Override
						public void onClick(View v) {
							context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(image2)));
							
						}
						  
					  });
				  } else if (images.length > 1)
				  {
					  try
					  {
						  holder.view4.setImageURI(Uri.parse(images[0].trim()));
					  } catch (Exception e)
					  {
						  holder.view4.setImageBitmap(decodeFile(new File(getRealPathFromURI(Uri.parse(images[0].trim())))));
					  }
					  
					  final String image2 = image;
					  
					  holder.view4.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							Intent intent = new Intent();
							intent.setClass(context, ImageViewer.class);
							Bundle b = new Bundle();
							b.putString("image", image2);
							intent.putExtra("bundle", b);
							context.startActivity(intent);
							
						}
						  
					  });
					  
					  if (sent == false)
					  {
						  holder.text2.setText(holder.text2.getText() + " - " + context.getResources().getString(R.string.multiple_attachments));
					  } else
					  {
						  holder.text2.setText(context.getResources().getString(R.string.multiple_attachments) + " - " + holder.text2.getText());
					  }
				  }
			  } else
			  {
				  holder.view4.setVisibility(View.GONE);
			  }
			  
			  if (video != null)
			  {
				  holder.view4.setVisibility(View.VISIBLE);
				  
				  holder.view4.setImageResource(R.drawable.ic_video_play);
				  final String video2 = video;
				  
				  holder.view4.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(video2)));
						
					}
					  
				  });
			  } else
			  {
				  if (image == null)
				  {
					  holder.view4.setVisibility(View.GONE);
				  }
			  }
		  } else
		  {
			  holder.view4.setVisibility(View.GONE);
		  }
		  
		  if (sharedPrefs.getString("smilies", "with").equals("with"))
		  {
			  String patternStr = "[^\\x20-\\x7E]";
			  Pattern pattern = Pattern.compile(patternStr);
			  Matcher matcher = pattern.matcher(body);
			  
			  if (matcher.find())
			  {
				  final String bodyF = body;
				  
				  new Thread(new Runnable() {

					@Override
					public void run() {
						final Spannable text;
						
						if (sharedPrefs.getBoolean("emoji_type", true))
						{
							text = EmojiConverter2.getSmiledText(context, EmoticonConverter2.getSmiledText(context, bodyF));
						} else
						{
							text = EmojiConverter.getSmiledText(context, EmoticonConverter2.getSmiledText(context, bodyF));
						}
						
						context.getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {
							
							@Override
							public void run() {
									holder.text.setText(text);
							}
					    	
					    });
					}
					  
				  }).start();
			  } else
			  {
				  holder.text.setText(EmoticonConverter2.getSmiledText(context, body));
			  }
		  } else if (sharedPrefs.getString("smilies", "with").equals("without"))
		  {
			  String patternStr = "[^\\x20-\\x7E]";
			  Pattern pattern = Pattern.compile(patternStr);
			  Matcher matcher = pattern.matcher(body);
			  
			  if (matcher.find())
			  {
				  final String bodyF = body;
				  
				  new Thread(new Runnable() {

					@Override
					public void run() {
						final Spannable text;
						
						if (sharedPrefs.getBoolean("emoji_type", true))
						{
							text = EmojiConverter2.getSmiledText(context, EmoticonConverter.getSmiledText(context, bodyF));
						} else
						{
							text = EmojiConverter.getSmiledText(context, EmoticonConverter.getSmiledText(context, bodyF));
						}
						
						context.getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {
							
							@Override
							public void run() {
									holder.text.setText(text);
							}
					    	
					    });
					}
					  
				  }).start();
			  } else
			  {
				  holder.text.setText(EmoticonConverter.getSmiledText(context, body));
			  }
		  } else if (sharedPrefs.getString("smilies", "with").equals("none"))
		  {
			  String patternStr = "[^\\x20-\\x7E]";
			  Pattern pattern = Pattern.compile(patternStr);
			  Matcher matcher = pattern.matcher(body);
			  
			  if (matcher.find())
			  {
				  final String bodyF = body;
				  
				  new Thread(new Runnable() {

					@Override
					public void run() {
						final Spannable text;
						
						if (sharedPrefs.getBoolean("emoji_type", true))
						{
							text = EmojiConverter2.getSmiledText(context, bodyF);
						} else
						{
							text = EmojiConverter.getSmiledText(context, bodyF);
						}
						
						context.getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {
							
							@Override
							public void run() {
									holder.text.setText(text);
							}
					    	
					    });
					}
					  
				  }).start();
			  } else
			  {
				  holder.text.setText(body);
			  }
		  } else if (sharedPrefs.getString("smilies", "with").equals("both"))
		  {
			  String patternStr = "[^\\x20-\\x7E]";
			  Pattern pattern = Pattern.compile(patternStr);
			  Matcher matcher = pattern.matcher(body);
			  
			  if (matcher.find())
			  {
				  final String bodyF = body;
				  
				  new Thread(new Runnable() {

					@Override
					public void run() {
						final Spannable text;
						
						if (sharedPrefs.getBoolean("emoji_type", true))
						{
							text = EmojiConverter2.getSmiledText(context, EmoticonConverter3.getSmiledText(context, bodyF));
						} else
						{
							text = EmojiConverter.getSmiledText(context, EmoticonConverter3.getSmiledText(context, bodyF));
						}
						
						context.getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {
							
							@Override
							public void run() {
									holder.text.setText(text);
							}
					    	
					    });
					}
					  
				  }).start();
		      } else
			  {
				  holder.text.setText(EmoticonConverter3.getSmiledText(context, body));
			  }
		  }
		  
		  if (error == true)
		  {
			  String text = "<html><body><img src=\"ic_failed.png\"/> ERROR</body></html>";
			  holder.text2.setText(Html.fromHtml(text, imgGetterFail, null));
		  }
		  
		  if (sent == true)
		  {
			  holder.text.setTextColor(sharedPrefs.getInt("ct_sentTextColor", context.getResources().getColor(R.color.black)));
			  holder.text2.setTextColor(sharedPrefs.getInt("ct_sentTextColor", context.getResources().getColor(R.color.black)));
			  holder.view1.setBackgroundColor(sharedPrefs.getInt("ct_sentMessageBackground", context.getResources().getColor(R.color.white)));
			  holder.view2.setBackgroundColor(sharedPrefs.getInt("ct_sentMessageBackground", context.getResources().getColor(R.color.white)));
			  holder.view3.setBackgroundColor(sharedPrefs.getInt("ct_sentMessageBackground", context.getResources().getColor(R.color.white)));
			  holder.view4.setBackgroundColor(sharedPrefs.getInt("ct_sentMessageBackground", context.getResources().getColor(R.color.white)));
			  holder.bubble1.setColorFilter(sharedPrefs.getInt("ct_sentMessageBackground", context.getResources().getColor(R.color.white)));
			  holder.bubble2.setColorFilter(sharedPrefs.getInt("ct_sentMessageBackground", context.getResources().getColor(R.color.white)));
			  
			  holder.text.setGravity(Gravity.RIGHT);
			  holder.text2.setGravity(Gravity.RIGHT);
			  
			  if (!sharedPrefs.getBoolean("custom_theme", false))
			  {
				  String color = sharedPrefs.getString("sent_text_color", "default");
				  
				  if (color.equals("blue"))
				  {
					  holder.text.setTextColor(context.getResources().getColor(R.color.holo_blue));
					  holder.text2.setTextColor(context.getResources().getColor(R.color.holo_blue));
				  } else if (color.equals("white"))
				  {
					  holder.text.setTextColor(context.getResources().getColor(R.color.white));
					  holder.text2.setTextColor(context.getResources().getColor(R.color.white));
				  } else if (color.equals("green"))
				  {
					  holder.text.setTextColor(context.getResources().getColor(R.color.holo_green));
					  holder.text2.setTextColor(context.getResources().getColor(R.color.holo_green));
				  } else if (color.equals("orange"))
				  {
					  holder.text.setTextColor(context.getResources().getColor(R.color.holo_orange));
					  holder.text2.setTextColor(context.getResources().getColor(R.color.holo_orange));
				  } else if (color.equals("red"))
				  {
					  holder.text.setTextColor(context.getResources().getColor(R.color.holo_red));
					  holder.text2.setTextColor(context.getResources().getColor(R.color.holo_red));
				  } else if (color.equals("purple"))
				  {
					  holder.text.setTextColor(context.getResources().getColor(R.color.holo_purple));
					  holder.text2.setTextColor(context.getResources().getColor(R.color.holo_purple));
				  } else if (color.equals("black"))
				  {
					  holder.text.setTextColor(context.getResources().getColor(R.color.pitch_black));
					  holder.text2.setTextColor(context.getResources().getColor(R.color.pitch_black));
				  } else if (color.equals("grey"))
				  {
					  holder.text.setTextColor(context.getResources().getColor(R.color.grey));
					  holder.text2.setTextColor(context.getResources().getColor(R.color.grey));
				  }
			  }
		  } else
		  {
			  holder.text.setTextColor(sharedPrefs.getInt("ct_receivedTextColor", context.getResources().getColor(R.color.black)));
			  holder.text2.setTextColor(sharedPrefs.getInt("ct_receivedTextColor", context.getResources().getColor(R.color.black)));
			  holder.view1.setBackgroundColor(sharedPrefs.getInt("ct_receivedMessageBackground", context.getResources().getColor(R.color.white)));
			  holder.view2.setBackgroundColor(sharedPrefs.getInt("ct_receivedMessageBackground", context.getResources().getColor(R.color.white)));
			  holder.view3.setBackgroundColor(sharedPrefs.getInt("ct_receivedMessageBackground", context.getResources().getColor(R.color.white)));
			  holder.view4.setBackgroundColor(sharedPrefs.getInt("ct_receivedMessageBackground", context.getResources().getColor(R.color.white)));
			  holder.bubble1.setColorFilter(sharedPrefs.getInt("ct_receivedMessageBackground", context.getResources().getColor(R.color.white)));
			  holder.bubble2.setColorFilter(sharedPrefs.getInt("ct_receivedMessageBackground", context.getResources().getColor(R.color.white)));
			  
			  holder.text.setGravity(Gravity.LEFT);
			  holder.text2.setGravity(Gravity.LEFT);
			  
			  if (!sharedPrefs.getBoolean("custom_theme", false))
			  {
				  String color = sharedPrefs.getString("received_text_color", "default");
				  
				  if (color.equals("blue"))
				  {
					  holder.text.setTextColor(context.getResources().getColor(R.color.holo_blue));
					  holder.text2.setTextColor(context.getResources().getColor(R.color.holo_blue));
				  } else if (color.equals("white"))
				  {
					  holder.text.setTextColor(context.getResources().getColor(R.color.white));
					  holder.text2.setTextColor(context.getResources().getColor(R.color.white));
				  } else if (color.equals("green"))
				  {
					  holder.text.setTextColor(context.getResources().getColor(R.color.holo_green));
					  holder.text2.setTextColor(context.getResources().getColor(R.color.holo_green));
				  } else if (color.equals("orange"))
				  {
					  holder.text.setTextColor(context.getResources().getColor(R.color.holo_orange));
					  holder.text2.setTextColor(context.getResources().getColor(R.color.holo_orange));
				  } else if (color.equals("red"))
				  {
					  holder.text.setTextColor(context.getResources().getColor(R.color.holo_red));
					  holder.text2.setTextColor(context.getResources().getColor(R.color.holo_red));
				  } else if (color.equals("purple"))
				  {
					  holder.text.setTextColor(context.getResources().getColor(R.color.holo_purple));
					  holder.text2.setTextColor(context.getResources().getColor(R.color.holo_purple));
				  } else if (color.equals("black"))
				  {
					  holder.text.setTextColor(context.getResources().getColor(R.color.pitch_black));
					  holder.text2.setTextColor(context.getResources().getColor(R.color.pitch_black));
				  } else if (color.equals("grey"))
				  {
					  holder.text.setTextColor(context.getResources().getColor(R.color.grey));
					  holder.text2.setTextColor(context.getResources().getColor(R.color.grey));
				  }
			  }
		  }
		  
		  if (!sharedPrefs.getString("text_alignment", "split").equals("split"))
		  {
			  if (sharedPrefs.getString("text_alignment", "split").equals("right"))
			  {
				  if (!sent)
				  {
					  RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)holder.text.getLayoutParams();
					  params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
				  } else
				  {
					  if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1)
					  {
						  RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)holder.text.getLayoutParams();
						  params.removeRule(RelativeLayout.ALIGN_PARENT_RIGHT);
					  }
				  }
				  
				  holder.text.setGravity(Gravity.RIGHT);
				  holder.text2.setGravity(Gravity.RIGHT);
			  } else
			  {
				  if (sent)
				  {
					  RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)holder.text.getLayoutParams();
					  params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
					  RelativeLayout.LayoutParams params2 = (RelativeLayout.LayoutParams)holder.text2.getLayoutParams();
					  params2.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
				  } else
				  {
					  if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1)
					  {
						  RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)holder.text.getLayoutParams();
						  params.removeRule(RelativeLayout.ALIGN_PARENT_LEFT);
						  RelativeLayout.LayoutParams params2 = (RelativeLayout.LayoutParams)holder.text2.getLayoutParams();
						  params2.removeRule(RelativeLayout.ALIGN_PARENT_LEFT);
					  }
				  }
				  
				  holder.text.setGravity(Gravity.LEFT);
				  holder.text2.setGravity(Gravity.LEFT);
			  }
		  }
		  
		  if (!sharedPrefs.getBoolean("contact_pictures", true))
		  {
			  if (sent)
			  {
				  rowView.setBackgroundColor(sharedPrefs.getInt("ct_sentMessageBackground", context.getResources().getColor(R.color.white)));
			  } else
			  {
				  rowView.setBackgroundColor(sharedPrefs.getInt("ct_receivedMessageBackground", context.getResources().getColor(R.color.white)));
			  }
		  }
		  
		  rowView.setPadding(0,0,0,0);
		  
		  if (position == getCount() - 1)
		  {
			  rowView.setPadding(0,0,0,5);
		  }
		  
		  int numberLines = 0;
		  int textLength = (int) paint.measureText(body);
		  
		  while (textLength > 0)
		  {
			  numberLines++;
			  textLength -= width;
		  }
		  
		  String patternStr = "\n";
		  Pattern pattern = Pattern.compile(patternStr);
		  Matcher matcher = pattern.matcher(body);
		  
		  while (matcher.find())
		  {
			  numberLines++;
		  }
		  
		  holder.view1.setVisibility(View.VISIBLE);
		  holder.view2.setVisibility(View.VISIBLE);
		  holder.view3.setVisibility(View.VISIBLE);
		  
		  if (sent)
		  {
			  holder.view1.setVisibility(View.INVISIBLE);
		  } else
		  {
			  holder.view3.setVisibility(View.INVISIBLE);
		  }
		  
		  int requiredLines = 2;
		  
		  if (Integer.parseInt(sharedPrefs.getString("text_size", "14")) < 16)
		  {
			  requiredLines = 3;
		  }
		  
		  if (numberLines < requiredLines)
		  {
			  holder.view2.setVisibility(View.INVISIBLE);
		  } else
		  {
			  holder.view1.setVisibility(View.INVISIBLE);
			  holder.view3.setVisibility(View.INVISIBLE);
		  }
		  
		  double opacity = sharedPrefs.getInt("text_opacity", 100);
		  
		  if (opacity != 100)
		  {
			  holder.view1.setAlpha((float) opacity/100);
			  holder.view2.setAlpha((float) opacity/100);
			  holder.view3.setAlpha((float) opacity/100);
			  holder.view4.setAlpha((float) opacity/100);
		  }
	  } catch (Exception e)
	  {
		  holder.image.setVisibility(View.GONE);
		  holder.image2.setVisibility(View.GONE);
		  holder.view4.setVisibility(View.GONE);
		  holder.text.setText("Error loading this message.");
		  holder.text.setGravity(Gravity.CENTER);
		  holder.text2.setText("");
		  
		  rowView.setPadding(0,0,0,0);
		  
		  if (position == getCount() - 1)
		  {
			  rowView.setPadding(0,0,0,5);
		  }
	  }
	  
	  final boolean mmsT = mms;
	  final String imageT = image;
	  final String dateT = date;
	  final String idT = id;
	  int size2 = 0;
	  
	  try
	  {
		  size2 = image.split(" ").length;
	  } catch (Exception e)
	  {
		  
	  }
	  
	  final int sizeT = size2;
	  final boolean errorT = error;

      final View rowViewF = rowView;

      holder.view4.setOnLongClickListener(new OnLongClickListener() {
          @Override
          public boolean onLongClick(View view) {
              rowViewF.performLongClick();
              return true;
          }
      });

      final String idF = id;
      final boolean mmsF = mms;
      final boolean sentF = sent;

      rowView.setOnClickListener(new OnClickListener() {
          @Override
          public void onClick(View view) {
              if(!mmsF)
              {
                  Cursor query;
                  String dialogText = "";

                  try
                  {
                      if (!sentF)
                      {
                          query = contentResolver.query(Uri.parse("content://sms/" + idF + "/"), new String[] {"date", "date_sent", "type", "address"}, null, null, "date desc limit 1");

                          if (query.moveToFirst())
                          {
                              String dateSent = query.getString(query.getColumnIndex("date_sent")), dateReceived = query.getString(query.getColumnIndex("date"));
                              Date date1 = new Date(Long.parseLong(dateSent)), date2 = new Date(Long.parseLong(dateReceived));

                              if (sharedPrefs.getBoolean("hour_format", false))
                              {
                                  dateSent = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.GERMAN).format(date1) + ", " + DateFormat.getDateInstance(DateFormat.MEDIUM).format(date1);
                                  dateReceived = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.GERMAN).format(date2) + ", " + DateFormat.getDateInstance(DateFormat.MEDIUM).format(date2);
                              } else
                              {
                                  dateSent = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.US).format(date1) + ", " + DateFormat.getDateInstance(DateFormat.MEDIUM).format(date1);
                                  dateReceived = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.US).format(date2) + ", " + DateFormat.getDateInstance(DateFormat.MEDIUM).format(date2);
                              }

                              dialogText = context.getResources().getString(R.string.type) + " Text Message\n" +
                                      context.getResources().getString(R.string.from) + " " + query.getString(query.getColumnIndex("address")) + "\n" +
                                      context.getResources().getString(R.string.sent) + " " + dateSent + "\n" +
                                      context.getResources().getString(R.string.received) + " " + dateReceived;
                          }
                      } else
                      {
                          query = contentResolver.query(Uri.parse("content://sms/" + idF + "/"), new String[] {"date", "status", "type", "address"}, null, null, "date desc limit 1");

                          if (query.moveToFirst())
                          {
                              String dateReceived = query.getString(query.getColumnIndex("date"));
                              Date date2 = new Date(Long.parseLong(dateReceived));

                              if (sharedPrefs.getBoolean("hour_format", false))
                              {
                                  dateReceived = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.GERMAN).format(date2) + ", " + DateFormat.getDateInstance(DateFormat.MEDIUM).format(date2);
                              } else
                              {
                                  dateReceived = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.US).format(date2) + ", " + DateFormat.getDateInstance(DateFormat.MEDIUM).format(date2);
                              }

                              dialogText = context.getResources().getString(R.string.type) + " Text Message\n" +
                                      context.getResources().getString(R.string.to) + " " + query.getString(query.getColumnIndex("address")) + "\n" +
                                      context.getResources().getString(R.string.sent) + " " + dateReceived;

                              String status = query.getString(query.getColumnIndex("status"));

                              if (!status.equals("-1"))
                              {
                                  if (status.equals("64") || status.equals("128"))
                                  {
                                      dialogText += "\n" + context.getResources().getString(R.string.status) + " Error";
                                  } else
                                  {
                                      dialogText += "\n" + context.getResources().getString(R.string.status) + " Delivered";
                                  }
                              }
                          }
                      }
                  } catch (Exception e)
                  {
                      query = contentResolver.query(Uri.parse("content://sms/" + idF + "/"), new String[] {"date", "status", "type", "address"}, null, null, "date desc limit 1");

                      if (query.moveToFirst())
                      {
                          if (sentF)
                          {
                              String dateReceived = query.getString(query.getColumnIndex("date"));
                              Date date2 = new Date(Long.parseLong(dateReceived));

                              if (sharedPrefs.getBoolean("hour_format", false))
                              {
                                  dateReceived = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.GERMAN).format(date2) + ", " + DateFormat.getDateInstance(DateFormat.MEDIUM).format(date2);
                              } else
                              {
                                  dateReceived = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.US).format(date2) + ", " + DateFormat.getDateInstance(DateFormat.MEDIUM).format(date2);
                              }

                              dialogText = context.getResources().getString(R.string.type) + " Text Message\n" +
                                      context.getResources().getString(R.string.to) + " " + query.getString(query.getColumnIndex("address")) + "\n" +
                                      context.getResources().getString(R.string.sent) + " " + dateReceived;

                              String status = query.getString(query.getColumnIndex("status"));

                              if (!status.equals("-1"))
                              {
                                  if (status.equals("64") || status.equals("128"))
                                  {
                                      dialogText += "\n" + context.getResources().getString(R.string.status) + " Error";
                                  } else
                                  {
                                      dialogText += "\n" + context.getResources().getString(R.string.status) + " Delivered";
                                  }
                              }
                          } else
                          {
                              String dateReceived = query.getString(query.getColumnIndex("date"));
                              Date date2 = new Date(Long.parseLong(dateReceived));

                              if (sharedPrefs.getBoolean("hour_format", false))
                              {
                                  dateReceived = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.GERMAN).format(date2) + ", " + DateFormat.getDateInstance(DateFormat.MEDIUM).format(date2);
                              } else
                              {
                                  dateReceived = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.US).format(date2) + ", " + DateFormat.getDateInstance(DateFormat.MEDIUM).format(date2);
                              }

                              dialogText = context.getResources().getString(R.string.type) + " Text Message\n" +
                                      context.getResources().getString(R.string.from) + " " + query.getString(query.getColumnIndex("address")) + "\n" +
                                      context.getResources().getString(R.string.received) + " " + dateReceived;
                          }
                      }
                  }

                  AlertDialog.Builder builder = new AlertDialog.Builder(context);
                  builder.setTitle(context.getResources().getString(R.string.message_details));
                  builder.setMessage(dialogText);
                  builder.create().show();
              }
          }
      });
	  
	  rowView.setOnLongClickListener(new OnLongClickListener() {
			
			@Override
			public boolean onLongClick(final View arg0) {
				Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
		        vibrator.vibrate(25);
		        
				AlertDialog.Builder builder2 = new AlertDialog.Builder(context);
				
				if (!errorT)
				{
					if (!mmsT || sizeT > 1)
					{
						builder2.setItems(R.array.messageOptions, new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								switch (which)
								{
								case 0:
									TextView tv = (TextView) arg0.findViewById(R.id.messageText1);
									ClipboardManager clipboard = (ClipboardManager)
							        	context.getSystemService(Context.CLIPBOARD_SERVICE);
									ClipData clip = ClipData.newPlainText("Copied Message", tv.getText().toString());
									clipboard.setPrimaryClip(clip);
							
									Toast.makeText(context, R.string.text_saved, Toast.LENGTH_SHORT).show();
									break;
								case 1:
									MainActivity.menu.showSecondaryMenu();
									
									View newMessageView = MainActivity.menu.getSecondaryMenu();
									
									EditText body = (EditText) newMessageView.findViewById(R.id.messageEntry2);
									TextView tv2 = (TextView) arg0.findViewById(R.id.messageText1);
									
									body.setText(tv2.getText().toString());
									
									break;
								case 2:
									AlertDialog.Builder builder = new AlertDialog.Builder(context);
									builder.setMessage(context.getResources().getString(R.string.delete_message));
									builder.setPositiveButton(context.getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
										@SuppressLint("SimpleDateFormat")
										public void onClick(DialogInterface dialog, int id) {
											   String threadId = threadIds;
								               
								               deleteSMS(context, threadId, idT);
								               ((MainActivity) context).refreshViewPager(true);
								           }
									
									public void deleteSMS(Context context, String threadId, String messageId) {
									    try {
									            context.getContentResolver().delete(Uri.parse("content://mms-sms/conversations/" + threadId + "/"), "_id=" + messageId, null);
									    } catch (Exception e) {
									    }
									}});
									builder.setNegativeButton(context.getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
								           public void onClick(DialogInterface dialog, int id) {
								               dialog.dismiss();
								           }
								       });
									AlertDialog dialog2 = builder.create();
									
									dialog2.show();
									break;
								default:
									break;
								}
								
							}
						
						});
						
						AlertDialog dialog = builder2.create();
						dialog.show();
					} else
					{
						builder2.setItems(R.array.messageOptions2, new DialogInterface.OnClickListener() {
							
							@SuppressWarnings("deprecation")
							@Override
							public void onClick(DialogInterface dialog, int which) {
								switch (which)
								{
								case 0:
									TextView tv = (TextView) arg0.findViewById(R.id.messageText1);
									ClipboardManager clipboard = (ClipboardManager)
							        	context.getSystemService(Context.CLIPBOARD_SERVICE);
									ClipData clip = ClipData.newPlainText("Copied Message", tv.getText().toString());
									clipboard.setPrimaryClip(clip);
							
									Toast.makeText(context, R.string.text_saved, Toast.LENGTH_SHORT).show();
									break;
								case 1:
									try {
										saveImage(MediaStore.Images.Media.getBitmap(context.getContentResolver(), Uri.parse(imageT)), dateT);
									} catch (FileNotFoundException e1) {
										Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show();
										e1.printStackTrace();
									} catch (IOException e1) {
										Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show();
										e1.printStackTrace();
									} catch (Exception e)
									{
										Toast.makeText(context, "Nothing to Save", Toast.LENGTH_SHORT).show();
									}
									break;
								case 2:
									MainActivity.menu.showSecondaryMenu();
									
									View newMessageView = MainActivity.menu.getSecondaryMenu();
									
									EditText body = (EditText) newMessageView.findViewById(R.id.messageEntry2);
									TextView tv2 = (TextView) arg0.findViewById(R.id.messageText1);
									
									try
									{
										((MainActivity)context).attachedImage2 = Uri.parse(imageT);
						                
										((MainActivity)context).imageAttachBackground2.setBackgroundColor(sharedPrefs.getInt("ct_conversationListBackground", context.getResources().getColor(R.color.light_silver)));
							    		Drawable attachBack = context.getResources().getDrawable(R.drawable.attachment_editor_bg);
							    		attachBack.setColorFilter(sharedPrefs.getInt("ct_sentMessageBackground", context.getResources().getColor(R.color.white)), Mode.MULTIPLY);
							    		((MainActivity)context).imageAttach2.setBackgroundDrawable(attachBack);
							    		((MainActivity)context).imageAttachBackground2.setVisibility(View.VISIBLE);
							    		((MainActivity)context).imageAttach2.setVisibility(true);
									} catch (Exception e)
									{
										
									}
						    		
						    		try
						    		{
						    			((MainActivity)context).imageAttach2.setImage("send_image", decodeFile(new File(getPath(Uri.parse(imageT)))));
						    		} catch (Exception e)
						    		{
						    			((MainActivity)context).imageAttach2.setVisibility(false);
						    			((MainActivity)context).imageAttachBackground2.setVisibility(View.GONE);
						    		}
						    		
						    		Button viewImage = (Button) newMessageView.findViewById(R.id.view_image_button2);
						    		Button replaceImage = (Button) newMessageView.findViewById(R.id.replace_image_button2);
						    		Button removeImage = (Button) newMessageView.findViewById(R.id.remove_image_button2);
						    		
						    		viewImage.setOnClickListener(new OnClickListener() {

										@Override
										public void onClick(View arg0) {
											context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(imageT)));
											
										}
						    			
						    		});
						    		
						    		replaceImage.setOnClickListener(new OnClickListener() {

										@Override
										public void onClick(View v) {
											Intent intent = new Intent();
							                intent.setType("image/*");
							                intent.setAction(Intent.ACTION_GET_CONTENT);
							                context.startActivityForResult(Intent.createChooser(intent, context.getResources().getString(R.string.select_picture)), 2);
											
										}
						    			
						    		});
						    		
						    		removeImage.setOnClickListener(new OnClickListener() {

										@Override
										public void onClick(View v) {
											((MainActivity)context).imageAttach2.setVisibility(false);
											((MainActivity)context).imageAttachBackground2.setVisibility(View.GONE);
											
										}
						    			
						    		});
									
									body.setText(tv2.getText().toString());
									
									try
									{
										
									} catch (Exception e)
									{
										
									}
									
									break;
								case 3:
									AlertDialog.Builder builder = new AlertDialog.Builder(context);
									builder.setMessage(context.getResources().getString(R.string.delete_message));
									builder.setPositiveButton(context.getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
										@SuppressLint("SimpleDateFormat")
										public void onClick(DialogInterface dialog, int id) {
											   String threadId = threadIds;
								               
								               deleteSMS(context, threadId, idT);
								               ((MainActivity) context).refreshViewPager(true);
								           }
									
									public void deleteSMS(Context context, String threadId, String messageId) {
									    try {
									            context.getContentResolver().delete(Uri.parse("content://mms-sms/conversations/" + threadId + "/"), "_id=" + messageId, null);
									    } catch (Exception e) {
									    }
									}});
									builder.setNegativeButton(context.getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
								           public void onClick(DialogInterface dialog, int id) {
								               dialog.dismiss();
								           }
								       });
									AlertDialog dialog2 = builder.create();
									
									dialog2.show();
									break;
								default:
									break;
								}
								
							}
						
						});
					
						AlertDialog dialog = builder2.create();
						dialog.show();
					}
				} else
				{
					builder2.setItems(R.array.messageOptions3, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							switch (which)
							{
							case 0:
								if (!mmsT)
								{
									MainActivity.animationOn = true;
									
									String body2 = ((TextView) arg0.findViewById(R.id.messageText1)).getText().toString();
									
									if (!sharedPrefs.getString("signature", "").equals(""))
									{
										body2 += "\n" + sharedPrefs.getString("signature", "");
									}
									
									final String body = body2;
									
									new Thread(new Runnable() {
			
										@Override
										public void run() {
											
											if (sharedPrefs.getBoolean("delivery_reports", false))
											{
												if (inboxNumbers.replaceAll("[^0-9]", "").equals(""))
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

                                                                        break;
                                                                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
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
													ArrayList<String> parts = smsManager.divideMessage(body2); 
													
													for (int i = 0; i < parts.size(); i++)
													{
														sPI.add(sentPI);
														dPI.add(deliveredPI);
													}
													
													smsManager.sendMultipartTextMessage(inboxNumbers, null, parts, sPI, dPI);
												} else
												{
												}
											} else
											{
												if (!inboxNumbers.replaceAll("[^0-9]", "").equals(""))
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
													ArrayList<String> parts = smsManager.divideMessage(body2); 
													
													for (int i = 0; i < parts.size(); i++)
													{
														sPI.add(sentPI);
													}
													
													smsManager.sendMultipartTextMessage(inboxNumbers, null, parts, sPI, null);
												} else
												{
												}
											}
											
											String address = inboxNumbers;
										    
											if (!address.replaceAll("[^0-9]", "").equals(""))
											{
											    final Calendar cal = Calendar.getInstance();
											    ContentValues values = new ContentValues();
											    values.put("address", address);
											    values.put("body", StripAccents.stripAccents(body)); 
											    values.put("date", cal.getTimeInMillis() + "");
                                                values.put("thread_id", threadIds);
											    context.getContentResolver().insert(Uri.parse("content://sms/outbox"), values);
											    
											    Cursor deleter = context.getContentResolver().query(Uri.parse("content://sms/failed"), null, null, null, null);
											    
											    if (deleter.moveToFirst())
											    {
											    	String id = deleter.getString(deleter.getColumnIndex("_id"));
											    	
											    	context.getContentResolver().delete(Uri.parse("content://mms-sms/conversations/" + threadIds + "/"), "_id=" + id, null);
											    }

                                                deleter.close();
												
											    final String address2 = address;
											    
											    context.getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {
				
													@Override
													public void run() {
														MainActivity.sentMessage = true;
											        	((MainActivity) context).refreshViewPager4(address2, StripAccents.stripAccents(body), cal.getTimeInMillis() + "");
													}
											    	
											    });
											}
										}
										
									}).start();
								} else
								{
									Toast.makeText(context, "Cannot resend MMS, try making a new message", Toast.LENGTH_LONG).show();
								}	
								
								break;
							case 1:
								TextView tv = (TextView) arg0.findViewById(R.id.messageText1);
								ClipboardManager clipboard = (ClipboardManager)
						        	context.getSystemService(Context.CLIPBOARD_SERVICE);
								ClipData clip = ClipData.newPlainText("Copied Message", tv.getText().toString());
								clipboard.setPrimaryClip(clip);
						
								Toast.makeText(context, R.string.text_saved, Toast.LENGTH_SHORT).show();
								break;
							case 2:
								MainActivity.menu.showSecondaryMenu();
								
								View newMessageView = MainActivity.menu.getSecondaryMenu();
								
								EditText body3 = (EditText) newMessageView.findViewById(R.id.messageEntry2);
								TextView tv2 = (TextView) arg0.findViewById(R.id.messageText1);
								
								body3.setText(tv2.getText().toString());
								
								break;
							case 3:
								AlertDialog.Builder builder = new AlertDialog.Builder(context);
								builder.setMessage(context.getResources().getString(R.string.delete_message));
								builder.setPositiveButton(context.getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
									@SuppressLint("SimpleDateFormat")
									public void onClick(DialogInterface dialog, int id) {
										   String threadId = threadIds;
							               
							               deleteSMS(context, threadId, idT);
							               ((MainActivity) context).refreshViewPager(true);
							           }
								
								public void deleteSMS(Context context, String threadId, String messageId) {
								    try {
								            context.getContentResolver().delete(Uri.parse("content://mms-sms/conversations/" + threadId + "/"), "_id=" + messageId, null);
								    } catch (Exception e) {
								    }
								}});
								builder.setNegativeButton(context.getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
							           public void onClick(DialogInterface dialog, int id) {
							               dialog.dismiss();
							           }
							       });
								AlertDialog dialog2 = builder.create();
								
								dialog2.show();
								break;
							default:
								break;
							}
							
						}
					
					});
				
					AlertDialog dialog = builder2.create();
					dialog.show();
				}
				
				return false;
			}
			
		});
	  
	  if (MainActivity.animationOn == true && position == getCount() - 1 && threadPosition == 0)
	  {
		  String animation = sharedPrefs.getString("send_animation", "left");
		  
		  if (animation.equals("left"))
		  {
			  Animation anim = AnimationUtils.loadAnimation(context, R.anim.slide_in_right);
			  anim.setDuration(sharedPrefs.getInt("animation_speed", 300));
			  rowView.startAnimation(anim);
		  } else if (animation.equals("right"))
		  {
			  Animation anim = AnimationUtils.loadAnimation(context, R.anim.slide_in_left);
			  anim.setDuration(sharedPrefs.getInt("animation_speed", 300));
			  rowView.startAnimation(anim);
		  } else if (animation.equals("up"))
		  {
			  Animation anim = AnimationUtils.loadAnimation(context, R.anim.slide_up);
			  anim.setDuration(sharedPrefs.getInt("animation_speed", 300));
			  rowView.startAnimation(anim);
		  }
		  
		  MainActivity.animationOn = false;
	  }
	  
	  if (MainActivity.animationReceived == 1 && position == getCount() - 1 && MainActivity.animationThread == threadPosition)
	  {
		  String animation = sharedPrefs.getString("receive_animation", "right");
		  
		  if (animation.equals("left"))
		  {
			  Animation anim = AnimationUtils.loadAnimation(context, R.anim.slide_in_right);
			  anim.setDuration(sharedPrefs.getInt("animation_speed", 300));
			  rowView.startAnimation(anim);
		  } else if (animation.equals("right"))
		  {
			  Animation anim = AnimationUtils.loadAnimation(context, R.anim.slide_in_left);
			  anim.setDuration(sharedPrefs.getInt("animation_speed", 300));
			  rowView.startAnimation(anim);
		  } else if (animation.equals("up"))
		  {
			  Animation anim = AnimationUtils.loadAnimation(context, R.anim.slide_up);
			  anim.setDuration(sharedPrefs.getInt("animation_speed", 300));
			  rowView.startAnimation(anim);
		  }
		  
		  MainActivity.animationReceived = 0;
	  }

	  return rowView;
  }
  
  public InputStream openDisplayPhoto(long contactId) {
	  Uri contactUri = ContentUris.withAppendedId(Contacts.CONTENT_URI, contactId);
	     Uri photoUri = Uri.withAppendedPath(contactUri, Contacts.Photo.CONTENT_DIRECTORY);
	     Cursor cursor = context.getContentResolver().query(photoUri,
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
  
  public Bitmap getFacebookPhoto(String phoneNumber) {
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
		        
		        if (sharedPrefs.getBoolean("ct_darkContactImage", false))
		        {
		        	defaultPhoto = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_contact_dark);
		        }
		        
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

		        if (sharedPrefs.getBoolean("ct_darkContactImage", false))
		        {
		        	defaultPhoto = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_contact_dark);
		        }
		        
		        contact.close();
		        return defaultPhoto;
		    }
		    Bitmap defaultPhoto = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_contact_picture);

	        if (sharedPrefs.getBoolean("ct_darkContactImage", false))
	        {
	        	defaultPhoto = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_contact_dark);
	        }
	        
	        contact.close();
		    return defaultPhoto;
	    } catch (Exception e)
	    {
	    	if (sharedPrefs.getBoolean("ct_darkContactImage", false))
	        {
	    		contact.close();
	        	return BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_contact_dark);
	        } else
	        {
	        	contact.close();
	        	return BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_contact_picture);
	        }
	    }
	  } catch (Exception e)
	  {
		  if (sharedPrefs.getBoolean("ct_darkContactImage", false))
	        {
	        	return BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_contact_dark);
	        } else
	        {
	        	return BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_contact_picture);
	        }
	  }
	}
  
  @SuppressWarnings("deprecation")
	public String getPath(Uri uri) {
		String[] projection = { MediaStore.Images.Media.DATA };
		Cursor cursor = context.managedQuery(uri, projection, null, null, null);
		int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();
        String str = cursor.getString(column_index);
        cursor.close();
		return str;
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
	    		return BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_contact_dark);
	    	} else
	    	{
	    		return BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_contact_picture);
	    	}
	    }
	}
  
  private static String getMmsText(String id, Activity context) {
	    Uri partURI = Uri.parse("content://mms/part/" + id);
	    InputStream is = null;
	    StringBuilder sb = new StringBuilder();
	    try {
	        is = context.getContentResolver().openInputStream(partURI);
	        if (is != null) {
	            InputStreamReader isr = new InputStreamReader(is, "UTF-8");
	            BufferedReader reader = new BufferedReader(isr);
	            String temp = reader.readLine();
	            while (temp != null) {
	                sb.append(temp);
	                temp = reader.readLine();
	            }
	        }
	    } catch (IOException e) {}
	    finally {
	        if (is != null) {
	            try {
	                is.close();
	            } catch (IOException e) {}
	        }
	    }
	    return sb.toString();
	}
	
  private String getAddressNumber(int id) {
	    String selectionAdd = new String("msg_id=" + id);
	    String uriStr = "content://mms/" + id + "/addr";
	    Uri uriAddress = Uri.parse(uriStr);
	    Cursor cAdd = context.getContentResolver().query(uriAddress, null,
	        selectionAdd, null, null);
	    String name = "";
	    if (cAdd != null)
	    {
		    if (cAdd.moveToFirst()) {
		        do {
		            String number = cAdd.getString(cAdd.getColumnIndex("address"));
		            if (number != null) {
		                try {
		                    Long.parseLong(number.replace("-", ""));
		                    name += " " + number;
		                } catch (NumberFormatException nfe) {
		                     name += " " + number;
		                }
		            }
		        } while (cAdd.moveToNext());
		    }
	    }

        cAdd.close();
	    
	    return name.trim();
	}
	
	private void saveImage(Bitmap finalBitmap, String d) {

	    String root = Environment.getExternalStorageDirectory().toString();
	    File myDir = new File(root + "/Download");
	    myDir.mkdirs();
	    String fname = d + ".jpg";
	    File file = new File (myDir, fname);
	    if (file.exists ()) file.delete ();
	    try {
	           FileOutputStream out = new FileOutputStream(file);
	           finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
	           out.flush();
	           out.close();

	    } catch (Exception e) {
	    	Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show();
	           e.printStackTrace();
	    }
	    
	    context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + Environment.getExternalStorageDirectory())));
	    Toast.makeText(context, context.getResources().getString(R.string.save_image), Toast.LENGTH_SHORT).show();
	}
	
	private Bitmap decodeFile(File f){
	    try {
	        //Decode image size
	        BitmapFactory.Options o = new BitmapFactory.Options();
	        o.inJustDecodeBounds = true;
	        BitmapFactory.decodeStream(new FileInputStream(f),null,o);

	        //The new size we want to scale to
	        final int REQUIRED_SIZE=200;

	        //Find the correct scale value. It should be the power of 2.
	        int scale=1;
	        while(o.outWidth/scale/2>=REQUIRED_SIZE && o.outHeight/scale/2>=REQUIRED_SIZE)
	            scale*=2;

	        //Decode with inSampleSize
	        BitmapFactory.Options o2 = new BitmapFactory.Options();
	        o2.inSampleSize=scale;
	        return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
	    } catch (FileNotFoundException e) {}
	    return null;
	}
	
	public String getRealPathFromURI(Uri contentUri) {
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String path = cursor.getString(column_index);
        cursor.close();
        return path;
    }
	
	public static Date getZeroTimeDate(Date fecha) {
	    Date res = fecha;
	    Calendar cal = Calendar.getInstance();

	    cal.setTime( fecha );
	    cal.set(Calendar.HOUR_OF_DAY, 0);
	    cal.set(Calendar.MINUTE, 0);
	    cal.set(Calendar.SECOND, 0);
	    cal.set(Calendar.MILLISECOND, 0);

	    res = (Date) cal.getTime();

	    return res;
	}
	
	
	ImageGetter imgGetterSent = new Html.ImageGetter() {
        @Override
        public Drawable getDrawable(String source) {
              Drawable drawable = null;
              
              drawable = context.getResources().getDrawable(R.drawable.ic_sent);
              
              drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable
                            .getIntrinsicHeight());
              
              return drawable;
        }
	};
	
	ImageGetter imgGetterFail = new Html.ImageGetter() {
        @Override
        public Drawable getDrawable(String source) {
              Drawable drawable = null;
              
              drawable = context.getResources().getDrawable(R.drawable.ic_failed);
              
              drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable
                            .getIntrinsicHeight());
              
              return drawable;
        }
	};
} 